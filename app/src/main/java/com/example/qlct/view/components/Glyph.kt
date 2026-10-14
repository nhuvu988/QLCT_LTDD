package com.example.qlct.view.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
internal fun Glyph(kind: String, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height; val stroke = 1.7.dp.toPx()
        fun line(x: Float, y: Float, xx: Float, yy: Float) = drawLine(color, Offset(w*x, h*y), Offset(w*xx, h*yy), stroke, StrokeCap.Round)
        when (kind) {
            "edit" -> { line(.18f,.72f,.7f,.15f); line(.7f,.15f,.86f,.3f); line(.86f,.3f,.34f,.87f); line(.34f,.87f,.14f,.9f); line(.14f,.9f,.18f,.72f) }
            "calendar" -> { drawRoundRect(color, Offset(w*.1f,h*.2f), Size(w*.8f,h*.7f), androidx.compose.ui.geometry.CornerRadius(w*.06f), style = Stroke(stroke)); line(.1f,.4f,.9f,.4f); line(.3f,.1f,.3f,.28f); line(.7f,.1f,.7f,.28f); repeat(3) { x -> repeat(2) { y -> drawCircle(color,w*.025f,Offset(w*(.3f+x*.2f),h*(.56f+y*.18f))) } } }

            "chart" -> { line(.2f,.85f,.2f,.55f); line(.5f,.85f,.5f,.32f); line(.8f,.85f,.8f,.1f) }
            "home" -> { line(.1f,.45f,.5f,.1f); line(.5f,.1f,.9f,.45f); line(.2f,.4f,.2f,.88f); line(.2f,.88f,.8f,.88f); line(.8f,.88f,.8f,.4f); line(.5f,.88f,.5f,.6f) }
            "food" -> { line(.25f,.1f,.25f,.9f); line(.1f,.1f,.1f,.35f); line(.4f,.1f,.4f,.35f); line(.1f,.35f,.4f,.35f); drawOval(color, Offset(w*.65f,h*.1f), Size(w*.25f,h*.38f), style = Stroke(stroke)); line(.78f,.48f,.78f,.9f) }
            "trash" -> { line(.15f,.25f,.85f,.25f); line(.35f,.12f,.65f,.12f); line(.25f,.25f,.3f,.9f); line(.3f,.9f,.7f,.9f); line(.7f,.9f,.75f,.25f); line(.45f,.42f,.45f,.72f); line(.6f,.42f,.6f,.72f) }
            "car" -> { line(.15f,.4f,.3f,.15f); line(.3f,.15f,.7f,.15f); line(.7f,.15f,.85f,.4f); drawRoundRect(color, Offset(w*.1f,h*.4f), Size(w*.8f,h*.35f), androidx.compose.ui.geometry.CornerRadius(w*.08f), style = Stroke(stroke)); line(.22f,.75f,.22f,.9f); line(.78f,.75f,.78f,.9f) }
            else -> { drawRoundRect(color, Offset(w*.12f,h*.25f), Size(w*.76f,h*.6f), androidx.compose.ui.geometry.CornerRadius(w*.1f), style = Stroke(stroke)); if (kind == "bag") { drawArc(color, 180f, 180f, false, Offset(w*.32f,h*.03f), Size(w*.36f,h*.42f), style = Stroke(stroke)) } else { line(.6f,.5f,.88f,.5f); drawCircle(color, w*.035f, Offset(w*.68f,h*.6f)) } }
        }
    }
}
