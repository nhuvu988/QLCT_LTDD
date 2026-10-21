package com.example.qlct.view.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qlct.view.theme.IncomeGreen
import com.example.qlct.view.theme.Muted
import com.example.qlct.view.theme.Purple
import com.example.qlct.viewmodel.state.ExpenseUiState

@Composable
internal fun DashboardHeader(state: ExpenseUiState, previous: () -> Unit, next: () -> Unit, monthOnly: (Boolean) -> Unit) {
    Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF57429F), Color(0xFF9684DC))))) {
        Column(Modifier.padding(horizontal = 20.dp).padding(top = 12.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).background(Color.White.copy(alpha = .2f), CircleShape), contentAlignment = Alignment.Center) {
                    Text("Q", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("SỔ THU CHI CÁ NHÂN", color = Color.White.copy(alpha = .7f), fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 1.4.sp)
                    Text("Quản lý chi tiêu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp)
                }
                Glyph("wallet", Color.White, Modifier.size(24.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color.White.copy(alpha = .15f), shape = RoundedCornerShape(50)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = previous, modifier = Modifier.size(40.dp)) { Text("‹", fontSize = 24.sp, color = Color.White, modifier = Modifier.semantics { contentDescription = "Tháng trước" }) }
                        Text("Tháng ${state.month.monthValue}, ${state.month.year}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        IconButton(onClick = next, modifier = Modifier.size(40.dp)) { Text("›", fontSize = 24.sp, color = Color.White, modifier = Modifier.semantics { contentDescription = "Tháng sau" }) }
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { monthOnly(!state.monthOnly) }, contentPadding = PaddingValues(8.dp)) {
                    Text(if (state.monthOnly) "Theo tháng" else "Toàn bộ", color = Color.White, fontSize = 12.sp)
                }
            }
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Chênh lệch thu − chi", color = Muted, fontSize = 12.sp, lineHeight = 16.sp)
                            Text(formatMoney(state.summary.income - state.summary.expense), fontWeight = FontWeight.Bold, fontSize = 25.sp, lineHeight = 32.sp, maxLines = 1)
                        }
                        Canvas(Modifier.size(42.dp, 32.dp)) {
                            repeat(4) { i ->
                                val h = size.height * (.25f + i * .25f)
                                drawRoundRect(Purple.copy(alpha = .4f + i * .2f), Offset(i * size.width / 4, size.height - h), Size(size.width / 7, h), androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()))
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Row(Modifier.fillMaxWidth()) {
                        BalanceAmount("Tổng thu", state.summary.income, IncomeGreen, Modifier.weight(1f))
                        BalanceAmount("Tổng chi", state.summary.expense, Color(0xFFDB8296), Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceAmount(label: String, amount: Long, color: Color, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(6.dp).background(color, CircleShape))
            Text(label, color = Muted, fontSize = 11.sp, lineHeight = 14.sp)
        }
        Text(formatMoney(amount), fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}
