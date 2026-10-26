package com.cutie.weather.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer

data class Member(val name: String, val hair: Color, val eye: Color)

object Members {
    val esin = Member("Esin", Color(0xFFFFD84D), Color(0xFF8B5A2B))
    val zoktay = Member("Zoktay", Color(0xFF4F86C6), Color(0xFF2E2A8C))
    val sueda = Member("Sueda", Color(0xFF7DBB74), Color(0xFF3E8E41))
    val lidya = Member("Lidya", Color(0xFFFF9CCB), Color(0xFFB5527A))
    val mina = Member("Mina", Color(0xFFE0283C), Color(0xFF6B2230))
    val hilal = Member("Hilal", Color(0xFF9B7BC9), Color(0xFF5B3E96))
}

enum class Outfit(val advice: String) {
    RAINCOAT("Yağmurluk ve şemsiye şart! Su geçirmez ayakkabıları unutma ☔"),
    WINTER("Bere, atkı ve kalın mont... Dışarısı buz gibi! 🧣"),
    JACKET("Üstüne bir ceket al, hava biraz serin. 🧥"),
    TEE("Rahat bir tişört yeter, hava ılıman. 👕"),
    SUMMER("Hafif giy, güneş gözlüğünü takmayı unutma! 😎")
}

/** Orijinal, Canvas ile cizilmis tatli karakter. Saç rengi uyeye, kiyafet havaya gore degisir. */
@Composable
fun CuteCharacter(member: Member, outfit: Outfit, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "float")
    val bob by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )
    androidx.compose.foundation.Canvas(modifier.graphicsLayer { translationY = bob * density }) {
        val w = size.width
        val h = size.height
        val k = if (outfit == Outfit.RAINCOAT) 0.86f else 1f
        withTransform({ scale(k, k, Offset(w / 2f, h)) }) {
            drawGirl(member, outfit)
        }
        if (outfit == Outfit.RAINCOAT) {
            val ink = Color(0xFF2A1B2E)
            val tl = Offset(w * 0.02f, 0f)
            val sz = Size(w * 0.96f, h * 0.34f)
            drawArc(Color(0xFF7EC8F0), 180f, 180f, true, tl, sz)
            drawArc(ink, 180f, 180f, false, tl, sz, style = Stroke(width = w * 0.016f))
            drawLine(ink, Offset(w * 0.5f, h * 0.17f), Offset(w * 0.5f, h * 0.20f), strokeWidth = w * 0.016f)
        }
    }
}

private fun DrawScope.drawGirl(member: Member, outfit: Outfit) {
    val w = size.width
    val h = size.height
    val ink = Color(0xFF2A1B2E)
    val skin = Color(0xFFFFD9C7)
    val line = Stroke(width = w * 0.016f)

    val bodyColor = when (outfit) {
        Outfit.RAINCOAT -> Color(0xFFFFD84D)
        Outfit.WINTER -> Color(0xFFF6E9EE)
        Outfit.JACKET -> Color(0xFF86B7D9)
        Outfit.TEE -> Color.White
        Outfit.SUMMER -> Color(0xFFFFC2D9)
    }
    val legColor = when (outfit) {
        Outfit.RAINCOAT, Outfit.WINTER, Outfit.JACKET -> Color(0xFF5B5875)
        Outfit.TEE -> Color(0xFF7FA6D6)
        Outfit.SUMMER -> Color.White
    }
    val armColor = if (outfit == Outfit.TEE || outfit == Outfit.SUMMER) skin else bodyColor

    // arka sac
    drawOval(member.hair, Offset(w * 0.04f, h * 0.05f), Size(w * 0.92f, h * 0.60f))
    drawOval(ink, Offset(w * 0.04f, h * 0.05f), Size(w * 0.92f, h * 0.60f), style = line)
    val flickL = Path().apply {
        moveTo(w * 0.10f, h * 0.38f)
        lineTo(w * 0.00f, h * 0.60f)
        lineTo(w * 0.26f, h * 0.52f)
        close()
    }
    val flickR = Path().apply {
        moveTo(w * 0.90f, h * 0.38f)
        lineTo(w * 1.00f, h * 0.60f)
        lineTo(w * 0.74f, h * 0.52f)
        close()
    }
    drawPath(flickL, member.hair)
    drawPath(flickL, ink, style = line)
    drawPath(flickR, member.hair)
    drawPath(flickR, ink, style = line)

    // bacaklar ve ayakkabilar
    drawRoundRect(legColor, Offset(w * 0.38f, h * 0.80f), Size(w * 0.09f, h * 0.12f), CornerRadius(w * 0.03f))
    drawRoundRect(legColor, Offset(w * 0.53f, h * 0.80f), Size(w * 0.09f, h * 0.12f), CornerRadius(w * 0.03f))
    drawOval(ink, Offset(w * 0.34f, h * 0.915f), Size(w * 0.15f, h * 0.06f))
    drawOval(ink, Offset(w * 0.51f, h * 0.915f), Size(w * 0.15f, h * 0.06f))

    // govde
    drawRoundRect(bodyColor, Offset(w * 0.32f, h * 0.62f), Size(w * 0.36f, h * 0.20f), CornerRadius(w * 0.06f))
    when (outfit) {
        Outfit.TEE -> drawRect(Color(0xFFFFB6D5), Offset(w * 0.32f, h * 0.69f), Size(w * 0.36f, h * 0.035f))
        Outfit.JACKET -> drawLine(ink, Offset(w * 0.5f, h * 0.63f), Offset(w * 0.5f, h * 0.82f), strokeWidth = w * 0.014f)
        Outfit.RAINCOAT -> {
            drawCircle(ink, w * 0.012f, Offset(w * 0.5f, h * 0.68f))
            drawCircle(ink, w * 0.012f, Offset(w * 0.5f, h * 0.74f))
        }
        Outfit.SUMMER -> {
            val skirt = Path().apply {
                moveTo(w * 0.32f, h * 0.74f)
                lineTo(w * 0.68f, h * 0.74f)
                lineTo(w * 0.75f, h * 0.85f)
                lineTo(w * 0.25f, h * 0.85f)
                close()
            }
            drawPath(skirt, bodyColor)
            drawPath(skirt, ink, style = line)
        }
        Outfit.WINTER -> Unit
    }
    drawRoundRect(ink, Offset(w * 0.32f, h * 0.62f), Size(w * 0.36f, h * 0.20f), CornerRadius(w * 0.06f), style = line)

    // kollar
    drawOval(armColor, Offset(w * 0.20f, h * 0.65f), Size(w * 0.14f, h * 0.07f))
    drawOval(ink, Offset(w * 0.20f, h * 0.65f), Size(w * 0.14f, h * 0.07f), style = line)
    drawOval(armColor, Offset(w * 0.66f, h * 0.60f), Size(w * 0.15f, h * 0.07f))
    drawOval(ink, Offset(w * 0.66f, h * 0.60f), Size(w * 0.15f, h * 0.07f), style = line)

    // kafa
    val headTL = Offset(w * 0.14f, h * 0.12f)
    val headSize = Size(w * 0.72f, h * 0.50f)
    drawOval(skin, headTL, headSize)
    drawOval(ink, headTL, headSize, style = line)

    // gozler
    val eyeY = h * 0.40f
    for (cx in listOf(w * 0.34f, w * 0.66f)) {
        val rx = w * 0.12f
        val ry = w * 0.14f
        drawOval(Color.White, Offset(cx - rx, eyeY - ry), Size(rx * 2f, ry * 2f))
        drawOval(ink, Offset(cx - rx, eyeY - ry), Size(rx * 2f, ry * 2f), style = line)
        drawCircle(member.eye, w * 0.09f, Offset(cx, eyeY + w * 0.01f))
        drawCircle(ink, w * 0.058f, Offset(cx, eyeY + w * 0.01f))
        drawCircle(Color.White, w * 0.024f, Offset(cx + w * 0.025f, eyeY - w * 0.015f))
    }

    // kakul
    drawArc(member.hair, 180f, 180f, true, Offset(w * 0.14f, h * 0.12f), Size(w * 0.72f, h * 0.30f))
    for (cx in listOf(0.23f, 0.41f, 0.59f, 0.77f)) {
        val tri = Path().apply {
            moveTo(w * (cx - 0.09f), h * 0.265f)
            lineTo(w * (cx + 0.09f), h * 0.265f)
            lineTo(w * cx, h * 0.315f)
            close()
        }
        drawPath(tri, member.hair)
    }
    drawArc(ink, 180f, 180f, false, Offset(w * 0.14f, h * 0.12f), Size(w * 0.72f, h * 0.30f), style = line)

    // yanaklar ve agiz
    drawCircle(Color(0x66FF7F9F), w * 0.04f, Offset(w * 0.24f, h * 0.50f))
    drawCircle(Color(0x66FF7F9F), w * 0.04f, Offset(w * 0.76f, h * 0.50f))
    drawArc(
        color = ink,
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(w * 0.44f, h * 0.525f),
        size = Size(w * 0.12f, w * 0.06f),
        style = Stroke(width = w * 0.014f, cap = StrokeCap.Round)
    )

    // aksesuarlar
    when (outfit) {
        Outfit.WINTER -> {
            // bere
            val tl = Offset(w * 0.10f, h * 0.03f)
            val sz = Size(w * 0.80f, h * 0.36f)
            drawArc(Color(0xFFFF9CC0), 180f, 180f, true, tl, sz)
            drawArc(ink, 180f, 180f, false, tl, sz, style = line)
            drawRoundRect(Color(0xFFFFC2D9), Offset(w * 0.10f, h * 0.185f), Size(w * 0.80f, h * 0.045f), CornerRadius(w * 0.02f))
            drawCircle(Color.White, w * 0.06f, Offset(w * 0.5f, h * 0.03f))
            drawCircle(ink, w * 0.06f, Offset(w * 0.5f, h * 0.03f), style = line)
            // atki
            val scarf = Color(0xFFE05A6D)
            drawRoundRect(scarf, Offset(w * 0.28f, h * 0.60f), Size(w * 0.44f, h * 0.06f), CornerRadius(w * 0.03f))
            drawRoundRect(scarf, Offset(w * 0.56f, h * 0.63f), Size(w * 0.09f, h * 0.13f), CornerRadius(w * 0.02f))
        }
        Outfit.SUMMER -> {
            // gunes gozlugu
            val lens = Color(0xFFC2477A)
            drawRoundRect(lens, Offset(w * 0.20f, h * 0.14f), Size(w * 0.26f, h * 0.07f), CornerRadius(w * 0.03f))
            drawRoundRect(lens, Offset(w * 0.54f, h * 0.14f), Size(w * 0.26f, h * 0.07f), CornerRadius(w * 0.03f))
            drawLine(ink, Offset(w * 0.46f, h * 0.17f), Offset(w * 0.54f, h * 0.17f), strokeWidth = w * 0.014f)
        }
        else -> Unit
    }
}
