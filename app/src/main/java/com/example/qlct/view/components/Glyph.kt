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
            "sun" -> {
                drawCircle(color, w*.2f, Offset(w*.5f,h*.5f), style = Stroke(stroke))
                line(.5f,.05f,.5f,.16f); line(.5f,.84f,.5f,.95f)
                line(.05f,.5f,.16f,.5f); line(.84f,.5f,.95f,.5f)
                line(.18f,.18f,.26f,.26f); line(.74f,.74f,.82f,.82f)
                line(.18f,.82f,.26f,.74f); line(.74f,.26f,.82f,.18f)
            }
            "moon" -> {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w*.7f,h*.12f)
                    cubicTo(w*.1f,-h*.02f,-w*.02f,h*.82f,w*.5f,h*.9f)
                    cubicTo(w*.76f,h*.94f,w*.93f,h*.75f,w*.9f,h*.56f)
                    cubicTo(w*.46f,h*.72f,w*.34f,h*.29f,w*.7f,h*.12f)
                    close()
                }
                drawPath(path,color,style = Stroke(stroke))
            }
            "budget" -> {
                drawRoundRect(color, Offset(w*.18f,h*.1f), Size(w*.64f,h*.8f), androidx.compose.ui.geometry.CornerRadius(w*.08f), style = Stroke(stroke))
                line(.32f,.3f,.68f,.3f); line(.32f,.5f,.55f,.5f); line(.32f,.7f,.6f,.7f)
            }
            "chart" -> { line(.2f,.85f,.2f,.55f); line(.5f,.85f,.5f,.32f); line(.8f,.85f,.8f,.1f) }
            "home" -> { line(.1f,.45f,.5f,.1f); line(.5f,.1f,.9f,.45f); line(.2f,.4f,.2f,.88f); line(.2f,.88f,.8f,.88f); line(.8f,.88f,.8f,.4f); line(.5f,.88f,.5f,.6f) }
            "food" -> { line(.25f,.1f,.25f,.9f); line(.1f,.1f,.1f,.35f); line(.4f,.1f,.4f,.35f); line(.1f,.35f,.4f,.35f); drawOval(color, Offset(w*.65f,h*.1f), Size(w*.25f,h*.38f), style = Stroke(stroke)); line(.78f,.48f,.78f,.9f) }
            "trash" -> { line(.15f,.25f,.85f,.25f); line(.35f,.12f,.65f,.12f); line(.25f,.25f,.3f,.9f); line(.3f,.9f,.7f,.9f); line(.7f,.9f,.75f,.25f); line(.45f,.42f,.45f,.72f); line(.6f,.42f,.6f,.72f) }
            "car" -> { line(.15f,.4f,.3f,.15f); line(.3f,.15f,.7f,.15f); line(.7f,.15f,.85f,.4f); drawRoundRect(color, Offset(w*.1f,h*.4f), Size(w*.8f,h*.35f), androidx.compose.ui.geometry.CornerRadius(w*.08f), style = Stroke(stroke)); line(.22f,.75f,.22f,.9f); line(.78f,.75f,.78f,.9f) }
            else -> { drawRoundRect(color, Offset(w*.12f,h*.25f), Size(w*.76f,h*.6f), androidx.compose.ui.geometry.CornerRadius(w*.1f), style = Stroke(stroke)); if (kind == "bag") { drawArc(color, 180f, 180f, false, Offset(w*.32f,h*.03f), Size(w*.36f,h*.42f), style = Stroke(stroke)) } else { line(.6f,.5f,.88f,.5f); drawCircle(color, w*.035f, Offset(w*.68f,h*.6f)) } }
        }
    }
}
