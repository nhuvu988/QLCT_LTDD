package com.example.qlct.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.qlct.model.entity.TransactionEntity
import com.example.qlct.model.entity.TransactionRow
import com.example.qlct.model.repository.ExpenseRepository
import com.example.qlct.model.summary.BudgetStatus
import com.example.qlct.model.summary.searchTransactions
import com.example.qlct.model.summary.summarize
import com.example.qlct.viewmodel.state.ExpenseUiState
import java.time.YearMonth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ExpenseViewModel(private val repository: ExpenseRepository, private val savedState: SavedStateHandle) : ViewModel() {
    // SavedStateHandle giữ tháng/bộ lọc/từ khóa qua tái tạo Activity và phục hồi state của tiến trình.
    // Dữ liệu giao dịch vẫn do Room lưu, không lưu trong SavedStateHandle.
    private val month = savedState.getStateFlow("month", YearMonth.now().toString())
    private val monthOnly = savedState.getStateFlow("monthOnly", true)
    private val type = savedState.getStateFlow("type", "ALL")
    private val query = savedState.getStateFlow("query", "")
    private val retry = MutableStateFlow(0)
    // Trạng thái riêng của thao tác ghi: đang bận, lỗi gần nhất và số thao tác đã thành công.
    private data class ActionState(
        val busy: Boolean = false,
        val error: String? = null,
        val completed: Long = 0
    )

    private val actionState = MutableStateFlow(ActionState())

    // Mỗi lần retry đổi, flatMapLatest hủy đăng ký cũ và tạo lại luồng đọc dữ liệu.
    private val dataState = retry.flatMapLatest {
        // combine tính lại bản tổng hợp khi danh mục, giao dịch, tháng hoặc bộ lọc thay đổi.
        val base = combine(repository.categories, repository.transactions, month, monthOnly, type) { categories, rows, selectedMonth, only, filter ->
            val selected = YearMonth.parse(selectedMonth)
            ExpenseUiState(false, categories, selected, only, filter, summarize(rows, selected, only, filter))
        }
        // Tìm kiếm chỉ lọc summary.visible; ngân sách lấy khoản chi của tháng chọn từ điểm cuối biểu đồ.
        // Do đó từ khóa hoặc bộ lọc Thu/Chi không làm sai tổng chi dùng để xét hạn mức.
        combine(base, repository.budgets, query) { current, budgets, text ->
            current.copy(query = text,
                searchResults = searchTransactions(current.summary.visible, text),
                budget = BudgetStatus(budgets.find { it.month == current.month.toString() }?.amount,
                    current.summary.chart.lastOrNull()?.amount ?: 0))
        }.catch { error ->
            if (error is CancellationException) throw error
            emit(ExpenseUiState(loading = false, loadError = "Không tải được dữ liệu. Vui lòng thử lại."))
        }
    }

    // Màn hình quan sát một state bất biến gồm dữ liệu và phản hồi của thao tác.
    val uiState: StateFlow<ExpenseUiState> = combine(dataState, actionState) { data, action ->
        data.copy(
            busy = action.busy,
            actionError = action.error,
            completedActions = action.completed
        )
    // Chuyển Flow thành StateFlow; giữ đăng ký thêm 5 giây sau khi hết người quan sát, tránh tải lại khi đổi cấu hình.
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpenseUiState())

    /**
     * Dịch tháng đang chọn theo delta (âm: trước, dương: sau) và lưu vào SavedStateHandle.
     */
    fun changeMonth(delta: Long) { savedState["month"] = YearMonth.parse(month.value).plusMonths(delta).toString() }
    /**
     * Chọn xem tháng hiện tại hoặc toàn bộ thời gian; Flow tự tính lại dữ liệu hiển thị.
     */
    fun setMonthOnly(value: Boolean) { savedState["monthOnly"] = value }
    /**
     * Lưu bộ lọc ALL, INCOME hoặc EXPENSE; bộ lọc này áp dụng cho danh sách giao dịch.
     */
    fun setType(value: String) { savedState["type"] = value }
    /**
     * Lưu từ khóa tìm kiếm; việc lọc danh sách diễn ra trong luồng dataState.
     */
    fun setQuery(value: String) { savedState["query"] = value }
    /**
     * Gửi yêu cầu lưu ngân sách qua mutate để dùng chung trạng thái bận, lỗi và thành công.
     */
    fun saveBudget(month: String, amount: Long) = mutate { repository.saveBudget(month, amount) }
    /**
     * Gửi yêu cầu xóa hạn mức tháng; dữ liệu giao dịch không bị tác động.
     */
    fun deleteBudget(month: String) = mutate { repository.deleteBudget(month) }
    /**
     * Tăng tín hiệu retry để flatMapLatest đăng ký lại các luồng dữ liệu sau lỗi tải.
     */
    fun retryLoad() { retry.value++ }
    /**
     * Xóa lỗi thao tác trước đó, giữ nguyên dữ liệu và trạng thái bộ lọc.
     */
    fun clearError() { actionState.update { it.copy(error = null) } }

    /**
     * Chuyển giao dịch cần thêm/sửa xuống Repository trong luồng xử lý thao tác chung.
     */
    fun save(transaction: TransactionEntity) = mutate { repository.save(transaction) }
    /**
     * Lấy ID dòng đang chọn và yêu cầu Repository xóa theo tài khoản hiện tại.
     */
    fun delete(transaction: TransactionRow) = mutate { repository.delete(transaction.id) }

    /**
     * Chạy một thao tác ghi bất đồng bộ và chặn thao tác mới khi đang bận.
     * Thành công tăng completed để UI đóng hộp thoại; thất bại giữ biểu mẫu và đưa lỗi vào state.
     */
    private fun mutate(action: suspend () -> Unit) {
        // Khóa thao tác trước khi launch để lần nhấn liên tiếp không tạo thêm lần ghi.
        if (actionState.value.busy) return
        actionState.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                action()
                // UI quan sát bộ đếm này để chỉ đóng biểu mẫu sau khi ghi thành công.
                actionState.update { it.copy(completed = it.completed + 1) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                actionState.update { it.copy(error = "Không thực hiện được. Dữ liệu nhập được giữ để thử lại.") }
            // Luôn bỏ trạng thái bận sau thao tác, kể cả khi lỗi hoặc coroutine bị hủy.
            } finally {
                actionState.update { it.copy(busy = false) }
            }
        }
    }

    companion object {
        /**
         * Tạo ViewModel với Repository và SavedStateHandle để phục hồi các bộ lọc đã lưu.
         */
        fun factory(repository: ExpenseRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { ExpenseViewModel(repository, createSavedStateHandle()) }
        }
    }
}
