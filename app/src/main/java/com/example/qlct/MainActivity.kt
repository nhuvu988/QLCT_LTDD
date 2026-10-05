package com.example.qlct

import android.app.DatePickerDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var db: ExpenseDatabase
    private val worker = Executors.newSingleThreadExecutor()
    private lateinit var summary: TextView
    private lateinit var list: LinearLayout
    private lateinit var add: Button
    private var categories = emptyList<Category>()
    private var rows = emptyList<Transaction>()
    private var typeFilter = 0
    private var monthOnly = false
    private val money = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
    private fun vnd(value: Long) = "${money.format(value)} đ"
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun label(value: String, size: Float = 16f) = TextView(this).apply {
        text = value; textSize = size; setPadding(dp(8),dp(8),dp(8),dp(8))
    }
    private fun column() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        db = ExpenseDatabase(applicationContext)
        typeFilter = savedInstanceState?.getInt("filter") ?: 0
        monthOnly = savedInstanceState?.getBoolean("month") ?: false
        val root = column()
        setContentView(root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left + dp(16),bars.top,bars.right + dp(16),bars.bottom)
            insets
        }
        root.addView(label("QLCT · Sổ thu chi",26f).apply { setTypeface(null,Typeface.BOLD) })
        summary = label("Đang tải dữ liệu…",18f)
        root.addView(summary)
        val filter = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,
                listOf("Tất cả giao dịch", "Khoản thu", "Khoản chi"))
            setSelection(typeFilter)
        }
        root.addView(filter)
        root.addView(CheckBox(this).apply {
            text = "Chỉ xem tháng hiện tại"; isChecked = monthOnly
            setOnCheckedChangeListener { _,checked -> monthOnly = checked; render() }
        })
        add = Button(this).apply {
            text = "+ Thêm giao dịch"; isEnabled = false
            setOnClickListener { edit(null) }
        }
        root.addView(add)
        root.addView(label("Chạm một giao dịch để sửa hoặc xóa",13f))
        list = column()
        root.addView(ScrollView(this).apply { addView(list) },LinearLayout.LayoutParams(-1,0,1f))
        filter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(parent: AdapterView<*>?,view: View?,position: Int,id: Long) {
                typeFilter = position; render()
            }
        }
        refresh()
    }

    private fun refresh() {
        worker.execute {
            try {
                val loadedCategories = db.categories()
                val loadedRows = db.transactions()
                runOnUiThread {
                    if (!isDestroyed) {
                        categories = loadedCategories; rows = loadedRows
                        add.isEnabled = true; render()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread { if (!isDestroyed) {
                    summary.text = "Không tải được dữ liệu. Hãy thử lại."
                    list.removeAllViews()
                    list.addView(Button(this).apply { text = "Thử lại"; setOnClickListener { refresh() } })
                } }
            }
        }
    }

    private fun render() {
        if (!::list.isInitialized) return
        val month = SimpleDateFormat("yyyy-MM",Locale.US).format(Calendar.getInstance().time)
        val period = rows.filter { !monthOnly || it.date.startsWith(month) }
        val income = period.filter { it.type == "INCOME" }.sumOf { it.amount }
        val expense = period.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        summary.text = "${if(monthOnly) "Tháng $month" else "Toàn bộ thời gian"}\n" +
            "Tổng thu: ${vnd(income)}\nTổng chi: ${vnd(expense)}\nChênh lệch: ${vnd(income-expense)}"
        val visible = period.filter { typeFilter == 0 || it.type == if(typeFilter == 1) "INCOME" else "EXPENSE" }
        list.removeAllViews()
        if (visible.isEmpty()) list.addView(label("Chưa có giao dịch phù hợp. Nhấn Thêm giao dịch để bắt đầu."))
        visible.forEach { t ->
            val isIncome = t.type == "INCOME"
            list.addView(label("${if(isIncome) "THU +" else "CHI −"}${vnd(t.amount)} · ${t.categoryName}\n" +
                "${t.date}${if(t.note.isBlank()) "" else " · ${t.note}"}").apply {
                setTextColor(if(isIncome) Color.rgb(0,107,84) else Color.rgb(164,45,38))
                setOnClickListener {
                    MaterialAlertDialogBuilder(this@MainActivity).setTitle(t.categoryName)
                        .setItems(arrayOf("Sửa giao dịch", "Xóa giao dịch")) { _,which ->
                            if(which == 0) edit(t) else confirmDelete(t)
                        }.show()
                }
            })
            list.addView(View(this).apply { setBackgroundColor(Color.LTGRAY) },LinearLayout.LayoutParams(-1,dp(1)))
        }
    }

    private fun edit(existing: Transaction?) {
        val form = column().apply { setPadding(dp(20),dp(8),dp(20),dp(8)) }
        val type = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,listOf("Chi tiền", "Thu tiền"))
            setSelection(if(existing?.type == "INCOME") 1 else 0)
        }
        val amount = EditText(this).apply {
            hint = "Số tiền (VND)"; inputType = InputType.TYPE_CLASS_NUMBER
            existing?.let { setText(it.amount.toString()) }
        }
        val category = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,categories)
            setSelection(categories.indexOfFirst { it.id == existing?.categoryId }.coerceAtLeast(0))
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd",Locale.US).apply { isLenient = false }
        val calendar = Calendar.getInstance()
        existing?.let { calendar.time = dateFormat.parse(it.date)!! }
        val date = Button(this).apply { text = dateFormat.format(calendar.time) }
        date.setOnClickListener {
            DatePickerDialog(this,{ _,y,m,d ->
                calendar.set(y,m,d); date.text = dateFormat.format(calendar.time)
            },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        val note = EditText(this).apply {
            hint = "Ghi chú (không bắt buộc)"; setText(existing?.note ?: "")
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }
        form.addView(label("Loại giao dịch")); form.addView(type); form.addView(amount)
        form.addView(label("Danh mục")); form.addView(category)
        form.addView(label("Ngày giao dịch")); form.addView(date); form.addView(note)
        val dialog = MaterialAlertDialogBuilder(this).setTitle(if(existing == null) "Thêm giao dịch" else "Sửa giao dịch")
            .setView(ScrollView(this).apply { addView(form) })
            .setNegativeButton("Hủy",null).setPositiveButton("Lưu",null).create()
        dialog.setOnShowListener {
            dialog.getButton(-1).setOnClickListener {
                val value = amount.text.toString().trim().toLongOrNull()
                if(value == null || value !in 1..999999999999L) {
                    amount.error = "Nhập số tiền từ 1 đến 999.999.999.999 đ"; return@setOnClickListener
                }
                val selected = category.selectedItem as? Category ?: return@setOnClickListener
                val transaction = Transaction(existing?.id ?: 0,if(type.selectedItemPosition == 1) "INCOME" else "EXPENSE",
                    value,selected.id,date.text.toString(),note.text.toString())
                dialog.getButton(-1).isEnabled = false
                worker.execute {
                    val result = runCatching { db.save(transaction) }
                    runOnUiThread { if(!isDestroyed) {
                        if(result.isSuccess) { dialog.dismiss(); refresh() }
                        else { dialog.getButton(-1).isEnabled = true; toast("Không lưu được. Dữ liệu nhập vẫn được giữ để thử lại.") }
                    } }
                }
            }
        }
        dialog.show()
    }

    private fun confirmDelete(t: Transaction) {
        val dialog = MaterialAlertDialogBuilder(this).setTitle("Xóa giao dịch?")
            .setMessage("${t.categoryName}: ${vnd(t.amount)}\nThao tác này không thể hoàn tác.")
            .setNegativeButton("Hủy",null).setPositiveButton("Xóa",null).create()
        dialog.setOnShowListener { dialog.getButton(-1).setOnClickListener {
            dialog.getButton(-1).isEnabled = false
            worker.execute {
                val result = runCatching { db.delete(t.id) }
                runOnUiThread { if(!isDestroyed) {
                    if(result.isSuccess) { dialog.dismiss(); refresh() }
                    else { dialog.getButton(-1).isEnabled = true; toast("Không xóa được. Vui lòng thử lại.") }
                } }
            }
        } }
        dialog.show()
    }
    private fun toast(message: String) = Toast.makeText(this,message,Toast.LENGTH_LONG).show()
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("filter",typeFilter); outState.putBoolean("month",monthOnly)
        super.onSaveInstanceState(outState)
    }
    override fun onDestroy() {
        worker.execute { db.close() }; worker.shutdown()
        super.onDestroy()
    }
}
