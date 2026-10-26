package com.cutie.weather.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(val x: Float, val y: Float, val k: Int, val size: Float)

/** Arka plan gorseli: yavasca nefes alir/kayar, hava degisince yumusakca gecis yapar, ustunde hava efektleri. */
@Composable
fun AnimatedBackground(scene: Scene, night: Boolean) {
    val t = rememberInfiniteTransition(label = "bg")
    val s by t.animateFloat(
        initialValue = 1.10f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(tween(9000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "s"
    )
    val dx by t.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(12000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dx"
    )
    Crossfade(targetState = scene, animationSpec = tween(800), label = "bgfade") { sc ->
        Image(
            painter = painterResource(sc.image),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = s
                    scaleY = s
                    translationX = dx * density
                },
            contentScale = ContentScale.Crop
        )
    }
    if (night) NightOverlay()
    WeatherParticles(scene)
}

/** Yagmur damlalari, kar taneleri, firtinada simsek, sisli havada kayan sis, digerlerinde pariltilar. */
@Composable
fun WeatherParticles(scene: Scene) {
    val transition = rememberInfiniteTransition(label = "particles")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "t"
    )
    val items = remember {
        val rnd = Random(21)
        List(70) { Particle(rnd.nextFloat(), rnd.nextFloat(), 2 + rnd.nextInt(3), rnd.nextFloat()) }
    }
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val twoPi = (2.0 * PI).toFloat()
        when (scene) {
            Scene.RAIN, Scene.STORM -> {
                val c = Color.White.copy(alpha = if (scene == Scene.STORM) 0.45f else 0.6f)
                for (p in items) {
                    val y = ((p.y + t * p.k) % 1f) * h
                    val x = p.x * w - (y / h) * w * 0.05f
                    drawLine(c, Offset(x, y), Offset(x - w * 0.012f, y + h * 0.035f), strokeWidth = 2.5f)
                }
                if (scene == Scene.STORM) {
                    val flash = when {
                        t > 0.30f && t < 0.33f -> 0.35f
                        t > 0.36f && t < 0.38f -> 0.25f
                        t > 0.72f && t < 0.75f -> 0.30f
                        else -> 0f
                    }
                    if (flash > 0f) drawRect(Color.White.copy(alpha = flash))
                }
            }
            Scene.SNOW -> {
                for (i in 0 until 45) {
                    val p = items[i]
                    val kk = if (p.k > 2) 2 else 1
                    val y = ((p.y + t * kk) % 1f) * h
                    val x = p.x * w + sin(t * twoPi * kk + p.size * twoPi) * w * 0.02f
                    drawCircle(Color.White.copy(alpha = 0.85f), w * (0.004f + 0.006f * p.size), Offset(x, y))
                }
            }
            Scene.FOG -> {
                for (i in 0 until 3) {
                    val x = ((t + i / 3f) % 1f) * w * 1.6f - w * 0.5f
                    drawOval(
                        Color.White.copy(alpha = 0.16f),
                        Offset(x, h * (0.22f + 0.22f * i)),
                        Size(w * 0.8f, h * 0.07f)
                    )
                }
            }
            else -> {
                for (i in 0 until 14) {
                    val p = items[i]
                    val a = 0.5f + 0.5f * sin(t * twoPi * 2f + p.size * twoPi)
                    drawSparkle(w * p.x, h * p.y * 0.8f, w * (0.008f + 0.012f * p.size), Color.White.copy(alpha = a * 0.9f))
                }
            }
        }
    }
}

private data class Spark(val x: Float, val y: Float, val r: Float)

/** Gece oldugunda gorselin ustune koyu mavi tonlama ve yanip sonen yildizlar ekler. */
@Composable
fun NightOverlay() {
    val transition = rememberInfiniteTransition(label = "twinkle")
    val twinkle by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tw"
    )
    val sparks = remember {
        val rnd = Random(11)
        List(34) { Spark(rnd.nextFloat(), rnd.nextFloat() * 0.6f, 0.006f + rnd.nextFloat() * 0.014f) }
    }
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xCC0B1038), Color(0x990B1038), Color(0xB30B1038))
            )
        )
        for (i in sparks.indices) {
            val s = sparks[i]
            val a = if (i % 2 == 0) twinkle else (1.35f - twinkle).coerceIn(0.3f, 1f)
            drawSparkle(w * s.x, h * s.y, w * s.r, Color(0xFFFFE08A).copy(alpha = a))
        }
    }
}

private fun DrawScope.drawSparkle(cx: Float, cy: Float, r: Float, color: Color) {
    val p = Path().apply {
        moveTo(cx, cy - r)
        quadraticBezierTo(cx, cy, cx + r, cy)
        quadraticBezierTo(cx, cy, cx, cy + r)
        quadraticBezierTo(cx, cy, cx - r, cy)
        quadraticBezierTo(cx, cy, cx, cy - r)
        close()
    }
    drawPath(p, color)
}
