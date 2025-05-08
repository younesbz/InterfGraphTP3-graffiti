package com.univeiffel.tp3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.univeiffel.tp3.ui.theme.TP3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TP3Theme {
                LocalSketchManager()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppScreen() {
    var selectedColor by remember { mutableStateOf(Color.Red) }
    var sketch by remember { mutableStateOf(Sketch.createEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Select a Color",
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
        )

        ColorPalette(selectedColor, colorList) { clickedColor ->
            selectedColor = clickedColor
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Selected Color: $selectedColor",
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(2.dp, Color.Black)
        ) {
            SketchViewer(sketch)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(2.dp, Color.Black)
        ) {
            PointerCapturer (selectedColor = selectedColor){ position, isNewPath ->
                if (isNewPath) {
                    sketch = sketch + selectedColor
                }
                sketch = sketch + position
            }
        }
    }
}

@Composable
fun SketchViewer(sketch: Sketch) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        sketch.paths.forEach { path ->
            for (i in 0 until path.size - 1) {
                drawLine(
                    start = path[i],
                    end = path[i + 1],
                    color = path.color,
                    strokeWidth = 5f
                )
            }
        }
    }
}

@Composable
fun ColorPalette(
    selectedColor: Color?,
    colorList: List<Color>,
    onClickedColor: (Color) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        items(colorList) { color ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onClickedColor(color) }
                    .border(
                        width = if (color == selectedColor) 4.dp else 2.dp,
                        color = if (color == selectedColor) Color.Red else Color.Transparent
                    )
                    .background(color)
            )
        }
    }
}
@Composable
fun PointerCapturer(
    modifier: Modifier = Modifier,
    selectedColor: Color,
    onNewPointerPosition: (Offset, Boolean) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(selectedColor,Unit) {
                awaitPointerEventScope {
                    while (true) {
                        // Attendre le premier appui (nouveau tracé)
                        val down = awaitFirstDown()
                        val boxSize = size.toSize()
                        val clampedStart = clampOffset(down.position, boxSize)
                        onNewPointerPosition(clampedStart, true) // nouveau tracé (true)

                        // Tant que l'utilisateur maintient le doigt
                        do {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                if (change.pressed) {
                                    val clampedMove = clampOffset(change.position, boxSize)
                                    onNewPointerPosition(clampedMove, false) // continuation (false)
                                }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
            }
    )
}

// Fonction utilitaire pour borner les coordonnées
fun clampOffset(position: Offset, boxSize: androidx.compose.ui.geometry.Size): Offset {
    val clampedX = position.x.coerceIn(0f, boxSize.width)
    val clampedY = position.y.coerceIn(0f, boxSize.height)
    return Offset(clampedX, clampedY)
}

@Composable
fun ActiveDrawer(selectedColor: Color, modifier: Modifier = Modifier)
{
    var sketch by remember { mutableStateOf(Sketch.createEmpty()) }

    Box(modifier
    ) {

        SketchViewer(sketch)


        PointerCapturer (modifier = modifier,
            selectedColor = selectedColor
        ){ position, isNewPath ->
                if (isNewPath) {
                    println("Adding new path with color: $selectedColor")
                    sketch = sketch + selectedColor
                }

                sketch = sketch + position

        }
    }

}
@Composable
fun LocalSketchManager() {
    var selectedColor by remember { mutableStateOf(Color.Black) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Select a Color",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        ColorPalette(
            selectedColor = selectedColor,
            colorList = colorList
        ) { clickedColor ->
            selectedColor = clickedColor
            println("Adding new path with color: $selectedColor")
        }

        Spacer(modifier = Modifier.height(16.dp))

        ActiveDrawer(
            selectedColor = selectedColor,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(2.dp, Color.Black)
        )
    }
}




val colorList = listOf(
    Color.Black, Color.White, Color.Red, Color.Green, Color.Blue,
    Color.Yellow, Color.Cyan, Color.Magenta, Color.Gray, Color.LightGray,
    Color.DarkGray, Color(0xFFFFA500), // Orange
    Color(0xFFFFC0CB), // Pink
    Color(0xFF800080), // Purple
    Color(0xFF8A2BE2), // Violet
    Color(0xFFA52A2A), // Brown
    Color(0xFF00FF00), // Lime
    Color(0xFF000080)  // Navy
)
