package com.nipunapps.musicknob

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nipunapps.musicknob.ui.theme.MusicKnobTheme
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicKnobTheme {
                // A surface container using the 'background' color from the theme
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color(0xFF121212))
                ) {
                    // v -> Volume r -> rotation
                    Row(
                        modifier = Modifier
                            .border(
                                1.dp, Color.Green, RoundedCornerShape(10.dp)
                            )
                            .padding(26.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,

                        ) {
                        var rotation by remember {
                            mutableStateOf(25f)
                        }
                        var volume by remember {
                            mutableStateOf(0f)
                        }
                        var barsCount = 26
                        MusicKnob(
                            modifier = Modifier
                                .size(120.dp),
                            rotation = rotation
                        ) { v, r ->
                            volume = v
                            rotation = r
                        }
                        Spacer(modifier = Modifier.size(20.dp))
                        SeekBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp),
                            activeBars = (barsCount * volume).roundToInt(),
                            barsCount = barsCount
                        ) { r, v ->
                            rotation = (r + 25f)
                            volume = v
                        }
                    }

                }
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun SeekBar(
    modifier: Modifier = Modifier,
    activeBars: Int = 0,
    barsCount: Int = 10,
    onValueChange: (Float, Float) -> Unit
) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        val barWidth = remember {
            constraints.maxWidth / (2f * barsCount)
        }
        Canvas(modifier = modifier
            .pointerInteropFilter { event ->
                val touchX = event.x
                when (event.action) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE -> {
                        if (touchX in 0f..constraints.maxWidth.toFloat()) {
                            val percent = touchX / constraints.maxWidth
                            onValueChange(percent * 310f, percent)
                            true
                        } else {
                            false
                        }
                    }
                    else -> false
                }
            }) {
            for (i in 0 until barsCount) {
                drawRoundRect(
                    color = if (i in 0..activeBars) Color.Green else Color.DarkGray,
                    topLeft = Offset(i * barWidth * 2f + barWidth / 2f, 0f),
                    size = Size(barWidth, constraints.maxHeight.toFloat()),
                    cornerRadius = CornerRadius.Zero
                )
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun MusicKnob(
    modifier: Modifier = Modifier,
    limitingAngle: Float = 25f,
    rotation: Float = 25f,
    onValueChange: (Float, Float) -> Unit
) {
    var touchX by remember {
        mutableStateOf(0f)
    }
    var touchY by remember {
        mutableStateOf(0f)
    }
    var centerX by remember {
        mutableStateOf(0f)
    }
    var centerY by remember {
        mutableStateOf(0f)
    }

    Image(
        painter = painterResource(
            R.drawable.music_knob
        ),
        contentDescription = "Music knob",
        modifier = modifier
            .size(120.dp)
            .onGloballyPositioned {
                val windowBounds = it.boundsInWindow()
                centerX = windowBounds.size.width / 2f
                centerY = windowBounds.size.height / 2f
            }
            .pointerInteropFilter { event ->
                touchX = event.x
                touchY = event.y
                val angle = -atan2(centerX - touchX, centerY - touchY) * (180f / PI).toFloat()
                when (event.action) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE -> {
                        if (angle !in -limitingAngle..limitingAngle) {
                            val fixedAngle = if (angle in -180f..limitingAngle) {
                                360f + angle
                            } else {
                                angle
                            }
                            val percent =
                                (fixedAngle - limitingAngle) / (360f - 2 * limitingAngle)
                            onValueChange(percent, fixedAngle)
                            true
                        } else false
                    }
                    else -> false
                }
            }
            .rotate(rotation)
    )
}