package com.example.geminiapi

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.min
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
// Import color palette from SciFiColors.kt
import com.example.geminiapi.NeonBlue
import com.example.geminiapi.DeepPurple
import com.example.geminiapi.NeonPink
import com.example.geminiapi.SpaceBlue
import com.example.geminiapi.StarYellow
import com.example.geminiapi.CosmicRed
import com.example.geminiapi.AquaGreen

// Visualization components are implemented in this file

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SciFiPlotScreen(
    onNavigateBack: () -> Unit
) {
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "scifiTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Interaction states
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var selectedVisualization by remember { mutableStateOf(0) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SpaceBlue,
                        Color(0xFF000820)
                    )
                )
            )
    ) {
        // Background star effect
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            // Draw stars
            repeat(100) {
                val x = (Math.random() * canvasWidth).toFloat()
                val y = (Math.random() * canvasHeight).toFloat()
                val radius = (Math.random() * 2 + 1).toFloat()
                val alpha = (Math.random() * 0.8 + 0.2).toFloat()
                
                drawCircle(
                    color = Color.White.copy(alpha = alpha * pulseAlpha),
                    radius = radius,
                    center = Offset(x, y)
                )
            }
            
            // Draw nebula effect
            val nebulaPath = Path().apply {
                moveTo(canvasWidth * 0.1f, canvasHeight * 0.3f)
                cubicTo(
                    canvasWidth * 0.3f, canvasHeight * 0.2f,
                    canvasWidth * 0.6f, canvasHeight * 0.4f,
                    canvasWidth * 0.9f, canvasHeight * 0.3f
                )
            }
            
            drawPath(
                path = nebulaPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        NeonPink.copy(alpha = 0.1f * pulseAlpha),
                        DeepPurple.copy(alpha = 0.15f * pulseAlpha),
                        NeonBlue.copy(alpha = 0.1f * pulseAlpha)
                    )
                ),
                style = Stroke(width = 80f)
            )
        }
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(DeepPurple.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = NeonBlue
                    )
                }
                
                Text(
                    text = "SCI-FI PLOT VISUALIZER",
                    color = NeonBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                
                // Spacer for alignment
                Spacer(modifier = Modifier.size(48.dp))
            }
            
            // Visualization area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SpaceBlue.copy(alpha = 0.7f))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale *= zoom
                            offset += pan
                        }
                    }
            ) {
                when (selectedVisualization) {
                    0 -> {
                        // Galactic Timeline Arc
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .offset(offset.x.dp, offset.y.dp)
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val timelineY = canvasHeight * 0.5f
                                
                                // Draw timeline axis
                                drawLine(
                                    color = NeonBlue.copy(alpha = 0.8f),
                                    start = Offset(canvasWidth * 0.05f, timelineY),
                                    end = Offset(canvasWidth * 0.95f, timelineY),
                                    strokeWidth = 4f * scale,
                                    cap = StrokeCap.Round
                                )
                                
                                // Draw time markers
                                val markers = 10
                                for (i in 0..markers) {
                                    val x = canvasWidth * (0.05f + 0.9f * i / markers)
                                    
                                    // Draw marker line
                                    drawLine(
                                        color = NeonBlue.copy(alpha = 0.6f),
                                        start = Offset(x, timelineY - 10f * scale),
                                        end = Offset(x, timelineY + 10f * scale),
                                        strokeWidth = 2f * scale
                                    )
                                }
                                
                                // Draw events
                                val events = listOf(
                                    Triple(0.12f, "First Colony", NeonPink),
                                    Triple(0.25f, "Quantum Drive", AquaGreen),
                                    Triple(0.38f, "Alien Contact", StarYellow),
                                    Triple(0.52f, "Galactic War", CosmicRed),
                                    Triple(0.67f, "Peace Treaty", NeonBlue),
                                    Triple(0.83f, "AI Singularity", DeepPurple)
                                )
                                
                                events.forEach { (position, name, color) ->
                                    val x = canvasWidth * position
                                    val yOffset = if (events.indexOf(Triple(position, name, color)) % 2 == 0) -60f else 60f
                                    
                                    // Draw connection line
                                    drawLine(
                                        color = color.copy(alpha = 0.6f * pulseAlpha),
                                        start = Offset(x, timelineY),
                                        end = Offset(x, timelineY + yOffset * scale),
                                        strokeWidth = 2f * scale
                                    )
                                    
                                    // Draw event circle
                                    drawCircle(
                                        color = color.copy(alpha = pulseAlpha),
                                        radius = 10f * scale,
                                        center = Offset(x, timelineY + yOffset * scale)
                                    )
                                }
                            }
                            
                            // Title overlay
                            Text(
                                text = "GALACTIC TIMELINE ARC",
                                color = NeonBlue,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }
                    1 -> {
                        // Character Relationship Web
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp)
                                    .offset(offset.x.dp, offset.y.dp)
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val centerX = canvasWidth / 2
                                val centerY = canvasHeight / 2
                                val radius = minOf(canvasWidth, canvasHeight) * 0.4f * scale
                                
                                // Characters data: name, angle, importance (0-1)
                                val characters = listOf(
                                    Triple("Commander Vex", 0f + rotationAngle, 1.0f),
                                    Triple("Dr. Elara", 60f + rotationAngle, 0.8f),
                                    Triple("Zorn (Alien)", 120f + rotationAngle, 0.9f),
                                    Triple("Admiral Krell", 180f + rotationAngle, 0.7f),
                                    Triple("Nova (AI)", 240f + rotationAngle, 0.85f),
                                    Triple("Rebel Leader", 300f + rotationAngle, 0.75f)
                                )
                                
                                // Draw character nodes
                                characters.forEach { (name, angle, importance) ->
                                    val x = centerX + radius * (importance as Float) * cos(Math.toRadians(angle.toDouble())).toFloat()
                                    val y = centerY + radius * (importance as Float) * sin(Math.toRadians(angle.toDouble())).toFloat()
                                    
                                    // Determine node color based on character type
                                    val nodeColor = when {
                                        name.contains("Commander") -> NeonBlue
                                        name.contains("Dr.") -> AquaGreen
                                        name.contains("Alien") -> StarYellow
                                        name.contains("Admiral") -> CosmicRed
                                        name.contains("AI") -> DeepPurple
                                        else -> NeonPink
                                    }
                                    
                                    // Draw node
                                    drawCircle(
                                        color = nodeColor.copy(alpha = pulseAlpha),
                                        radius = 15f * scale * importance,
                                        center = Offset(x, y)
                                    )
                                    
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.2f * pulseAlpha),
                                        radius = 18f * scale * importance,
                                        center = Offset(x, y),
                                        style = Stroke(width = 2f * scale)
                                    )
                                }
                                
                                // Draw relationships between characters
                                drawLine(
                                    color = NeonBlue.copy(alpha = 0.6f * pulseAlpha),
                                    start = Offset(centerX - radius * 0.5f, centerY - radius * 0.5f),
                                    end = Offset(centerX + radius * 0.5f, centerY - radius * 0.5f),
                                    strokeWidth = 2f * scale
                                )
                                
                                drawLine(
                                    color = CosmicRed.copy(alpha = 0.6f * pulseAlpha),
                                    start = Offset(centerX - radius * 0.5f, centerY + radius * 0.5f),
                                    end = Offset(centerX + radius * 0.5f, centerY + radius * 0.5f),
                                    strokeWidth = 2f * scale
                                )
                                
                                // Draw curved relationship
                                val path = Path().apply {
                                    moveTo(centerX - radius * 0.5f, centerY)
                                    
                                    // Control point for curve
                                    val controlX = centerX
                                    val controlY = centerY + radius * 0.3f
                                    
                                    quadraticBezierTo(controlX, controlY, centerX + radius * 0.5f, centerY)
                                }
                                
                                drawPath(
                                    path = path,
                                    color = NeonPink.copy(alpha = 0.6f * pulseAlpha),
                                    style = Stroke(width = 2f * scale)
                                )
                            }
                            
                            // Title overlay
                            Text(
                                text = "CHARACTER RELATIONSHIP WEB",
                                color = NeonPink,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }
                    2 -> {
                        // Tech Tree Evolution
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp)
                                    .offset(offset.x.dp, offset.y.dp)
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                
                                // Define tech nodes
                                // Format: level, horizontal position (0-1), name, color
                                val techNodes = listOf(
                                    // Level 1 (root)
                                    Pair(Pair(1, 0.5f), Pair("Steam-Punk Drives", AquaGreen)),
                                    
                                    // Level 2
                                    Pair(Pair(2, 0.3f), Pair("Fusion Engines", NeonBlue)),
                                    Pair(Pair(2, 0.7f), Pair("Quantum Sensors", DeepPurple)),
                                    
                                    // Level 3
                                    Pair(Pair(3, 0.2f), Pair("FTL Travel", StarYellow)),
                                    Pair(Pair(3, 0.5f), Pair("Neural Interface", NeonPink)),
                                    Pair(Pair(3, 0.8f), Pair("Nano-Fabrication", AquaGreen)),
                                    
                                    // Level 4
                                    Pair(Pair(4, 0.1f), Pair("Time Dilation", DeepPurple)),
                                    Pair(Pair(4, 0.4f), Pair("Consciousness Transfer", NeonPink)),
                                    Pair(Pair(4, 0.7f), Pair("Quantum AI", CosmicRed)),
                                    Pair(Pair(4, 0.9f), Pair("Planetary Terraforming", AquaGreen))
                                )
                                
                                // Draw connections between tech nodes
                                // Level 1 to 2
                                drawLine(
                                    color = AquaGreen.copy(alpha = 0.6f * pulseAlpha),
                                    start = Offset(canvasWidth * 0.5f, canvasHeight * 0.2f),
                                    end = Offset(canvasWidth * 0.3f, canvasHeight * 0.4f),
                                    strokeWidth = 2f * scale
                                )
                                
                                drawLine(
                                    color = AquaGreen.copy(alpha = 0.6f * pulseAlpha),
                                    start = Offset(canvasWidth * 0.5f, canvasHeight * 0.2f),
                                    end = Offset(canvasWidth * 0.7f, canvasHeight * 0.4f),
                                    strokeWidth = 2f * scale
                                )
                                
                                // Level 2 to 3
                                drawLine(
                                    color = NeonBlue.copy(alpha = 0.6f * pulseAlpha),
                                    start = Offset(canvasWidth * 0.3f, canvasHeight * 0.4f),
                                    end = Offset(canvasWidth * 0.2f, canvasHeight * 0.6f),
                                    strokeWidth = 2f * scale
                                )
                                
                                drawLine(
                                    color = NeonBlue.copy(alpha = 0.6f * pulseAlpha),
                                    start = Offset(canvasWidth * 0.3f, canvasHeight * 0.4f),
                                    end = Offset(canvasWidth * 0.5f, canvasHeight * 0.6f),
                                    strokeWidth = 2f * scale
                                )
                                
                                drawLine(
                                    color = DeepPurple.copy(alpha = 0.6f * pulseAlpha),
                                    start = Offset(canvasWidth * 0.7f, canvasHeight * 0.4f),
                                    end = Offset(canvasWidth * 0.5f, canvasHeight * 0.6f),
                                    strokeWidth = 2f * scale
                                )
                                
                                drawLine(
                                    color = DeepPurple.copy(alpha = 0.6f * pulseAlpha),
                                    start = Offset(canvasWidth * 0.7f, canvasHeight * 0.4f),
                                    end = Offset(canvasWidth * 0.8f, canvasHeight * 0.6f),
                                    strokeWidth = 2f * scale
                                )
                                
                                // Draw tech nodes
                                techNodes.forEach { (levelPos, nameColor) ->
                                    val (level, position) = levelPos
                                    val (name, color) = nameColor
                                    
                                    val y = canvasHeight * (0.2f + (level - 1) * 0.2f)
                                    val x = canvasWidth * position
                                    
                                    // Draw node
                                    drawCircle(
                                        color = color.copy(alpha = pulseAlpha),
                                        radius = 12f * scale,
                                        center = Offset(x, y)
                                    )
                                    
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.2f * pulseAlpha),
                                        radius = 15f * scale,
                                        center = Offset(x, y),
                                        style = Stroke(width = 2f * scale)
                                    )
                                }
                            }
                            
                            // Title overlay
                            Text(
                                text = "TECH-TREE EVOLUTION",
                                color = AquaGreen,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }
                    3 -> {
                        // Stellar Conflict Map
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp)
                                    .offset(offset.x.dp, offset.y.dp)
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                
                                // Draw star grid
                                val gridLines = 8
                                val gridSpacing = min(canvasWidth, canvasHeight) / gridLines
                                
                                // Draw grid lines
                                for (i in 0..gridLines) {
                                    // Horizontal lines
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.1f),
                                        start = Offset(0f, i * gridSpacing),
                                        end = Offset(canvasWidth, i * gridSpacing),
                                        strokeWidth = 1f * scale
                                    )
                                    
                                    // Vertical lines
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.1f),
                                        start = Offset(i * gridSpacing, 0f),
                                        end = Offset(i * gridSpacing, canvasHeight),
                                        strokeWidth = 1f * scale
                                    )
                                }
                                
                                // Draw star systems
                                val starSystems = listOf(
                                    // x, y, radius, faction (0=neutral, 1=alliance, 2=empire)
                                    listOf(0.2f, 0.3f, 0.03f, 1),
                                    listOf(0.35f, 0.2f, 0.02f, 1),
                                    listOf(0.5f, 0.15f, 0.04f, 1),
                                    listOf(0.7f, 0.25f, 0.025f, 1),
                                    listOf(0.8f, 0.4f, 0.035f, 0),
                                    listOf(0.7f, 0.6f, 0.03f, 2),
                                    listOf(0.5f, 0.7f, 0.045f, 2),
                                    listOf(0.3f, 0.65f, 0.025f, 2),
                                    listOf(0.2f, 0.5f, 0.02f, 0)
                                )
                                
                                // Draw fleet movements
                                val fleetMovements = listOf(
                                    // fromX, fromY, toX, toY, faction (1=alliance, 2=empire)
                                    listOf(0.2f, 0.3f, 0.35f, 0.2f, 1),
                                    listOf(0.5f, 0.15f, 0.7f, 0.25f, 1),
                                    listOf(0.7f, 0.6f, 0.5f, 0.7f, 2),
                                    listOf(0.3f, 0.65f, 0.2f, 0.5f, 2)
                                )
                                
                                // Draw fleet movements
                                fleetMovements.forEach { movement ->
                                    val fromX = canvasWidth * (movement[0] as Number).toFloat()
                                    val fromY = canvasHeight * (movement[1] as Number).toFloat()
                                    val toX = canvasWidth * (movement[2] as Number).toFloat()
                                    val toY = canvasHeight * (movement[3] as Number).toFloat()
                                    
                                    val color = if ((movement[4] as Number).toInt() == 1) NeonBlue else CosmicRed
                                    
                                    // Draw movement path
                                    val path = Path().apply {
                                        moveTo(fromX, fromY)
                                        
                                        // Control point for curve
                                        val controlX = (fromX + toX) / 2 + (toY - fromY) * 0.2f
                                        val controlY = (fromY + toY) / 2 - (toX - fromX) * 0.2f
                                        
                                        quadraticBezierTo(controlX, controlY, toX, toY)
                                    }
                                    
                                    drawPath(
                                        path = path,
                                        color = color.copy(alpha = 0.6f * pulseAlpha),
                                        style = Stroke(width = 2f * scale)
                                    )
                                }
                                
                                // Draw star systems
                                starSystems.forEach { system ->
                                    val x = canvasWidth * (system[0] as Number).toFloat()
                                    val y = canvasHeight * (system[1] as Number).toFloat()
                                    val radius = min(canvasWidth, canvasHeight) * (system[2] as Number).toFloat() * scale
                                    
                                    // Star color based on faction
                                    val color = when ((system[3] as Number).toInt()) {
                                        1 -> NeonBlue
                                        2 -> CosmicRed
                                        else -> StarYellow
                                    }
                                    
                                    // Draw star system
                                    drawCircle(
                                        color = color.copy(alpha = pulseAlpha),
                                        radius = radius,
                                        center = Offset(x, y)
                                    )
                                    
                                    // Draw glow effect
                                    drawCircle(
                                        color = color.copy(alpha = 0.3f * pulseAlpha),
                                        radius = radius * 1.5f,
                                        center = Offset(x, y)
                                    )
                                    
                                    drawCircle(
                                        color = color.copy(alpha = 0.1f * pulseAlpha),
                                        radius = radius * 2.5f,
                                        center = Offset(x, y)
                                    )
                                }
                            }
                            
                            // Title overlay
                            Text(
                                text = "STELLAR CONFLICT MAP",
                                color = CosmicRed,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }
                    4 -> {
                        // Planetary World-Builder
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp)
                                    .offset(offset.x.dp, offset.y.dp)
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val centerX = canvasWidth / 2
                                val centerY = canvasHeight / 2
                                val planetRadius = min(canvasWidth, canvasHeight) * 0.3f * scale
                                
                                // Draw planet
                                drawCircle(
                                    color = StarYellow.copy(alpha = 0.3f * pulseAlpha),
                                    radius = planetRadius,
                                    center = Offset(centerX, centerY)
                                )
                                
                                drawCircle(
                                    color = StarYellow.copy(alpha = 0.1f * pulseAlpha),
                                    radius = planetRadius * 1.2f,
                                    center = Offset(centerX, centerY)
                                )
                                
                                // Draw planet surface details (random patterns)
                                val random = java.util.Random(42) // Fixed seed for consistent patterns
                                repeat(20) {
                                    val angle = random.nextFloat() * 360f
                                    val distance = random.nextFloat() * planetRadius * 0.8f
                                    val featureSize = random.nextFloat() * planetRadius * 0.1f + planetRadius * 0.02f
                                    
                                    val featureX = centerX + distance * cos(Math.toRadians(angle.toDouble())).toFloat()
                                    val featureY = centerY + distance * sin(Math.toRadians(angle.toDouble())).toFloat()
                                    
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.1f * pulseAlpha),
                                        radius = featureSize,
                                        center = Offset(featureX, featureY)
                                    )
                                }
                                
                                // Draw event pins
                                val events = listOf(
                                    Triple(centerX - planetRadius * 0.5f, centerY - planetRadius * 0.3f, AquaGreen),
                                    Triple(centerX + planetRadius * 0.2f, centerY - planetRadius * 0.4f, NeonBlue),
                                    Triple(centerX + planetRadius * 0.6f, centerY + planetRadius * 0.1f, CosmicRed),
                                    Triple(centerX - planetRadius * 0.1f, centerY + planetRadius * 0.5f, DeepPurple)
                                )
                                
                                events.forEach { (x, y, color) ->
                                    // Draw pin
                                    drawCircle(
                                        color = color.copy(alpha = pulseAlpha),
                                        radius = 8f * scale,
                                        center = Offset(x, y)
                                    )
                                    
                                    // Draw pin connection
                                    drawLine(
                                        color = color.copy(alpha = 0.6f * pulseAlpha),
                                        start = Offset(x, y),
                                        end = Offset(x, y - 20f * scale),
                                        strokeWidth = 2f * scale
                                    )
                                    
                                    // Draw pin head
                                    drawCircle(
                                        color = color.copy(alpha = 0.8f * pulseAlpha),
                                        radius = 4f * scale,
                                        center = Offset(x, y - 20f * scale)
                                    )
                                }
                            }
                            
                            // Title overlay
                            Text(
                                text = "PLANETARY WORLD-BUILDER",
                                color = StarYellow,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }
                    5 -> {
                        // Emotional Arc Chart
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp)
                                    .offset(offset.x.dp, offset.y.dp)
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                
                                // Emotional data: chapter, hope (0-1), fear (0-1), resolve (0-1)
                                val emotionalData = listOf(
                                    listOf(1, 0.8f, 0.2f, 0.5f),
                                    listOf(2, 0.6f, 0.4f, 0.6f),
                                    listOf(3, 0.4f, 0.7f, 0.4f),
                                    listOf(4, 0.2f, 0.9f, 0.3f),
                                    listOf(5, 0.1f, 0.8f, 0.5f),
                                    listOf(6, 0.3f, 0.6f, 0.7f),
                                    listOf(7, 0.6f, 0.4f, 0.9f),
                                    listOf(8, 0.8f, 0.2f, 0.7f),
                                    listOf(9, 0.9f, 0.1f, 0.8f)
                                )
                                
                                val xStep = canvasWidth / (emotionalData.size + 1)
                                val maxY = canvasHeight * 0.8f
                                val baseY = canvasHeight * 0.9f
                                
                                // Draw horizontal axis (chapters)
                                drawLine(
                                    color = Color.White.copy(alpha = 0.5f),
                                    start = Offset(xStep * 0.5f, baseY),
                                    end = Offset(canvasWidth - xStep * 0.5f, baseY),
                                    strokeWidth = 2f * scale
                                )
                                
                                // Draw chapter markers
                                emotionalData.forEachIndexed { index, point ->
                                    val x = xStep * (index + 1)
                                    
                                    // Draw chapter marker
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.3f),
                                        start = Offset(x, baseY - 5f * scale),
                                        end = Offset(x, baseY + 5f * scale),
                                        strokeWidth = 1f * scale
                                    )
                                }
                                
                                // Draw emotional lines
                                val hopePath = Path()
                                val fearPath = Path()
                                val resolvePath = Path()
                                
                                emotionalData.forEachIndexed { index, point ->
                                    val x = xStep * (index + 1)
                                    val hopeY = baseY - maxY * (point[1] as Number).toFloat()
                                    val fearY = baseY - maxY * (point[2] as Number).toFloat()
                                    val resolveY = baseY - maxY * (point[3] as Number).toFloat()
                                    
                                    if (index == 0) {
                                        hopePath.moveTo(x, hopeY)
                                        fearPath.moveTo(x, fearY)
                                        resolvePath.moveTo(x, resolveY)
                                    } else {
                                        hopePath.lineTo(x, hopeY)
                                        fearPath.lineTo(x, fearY)
                                        resolvePath.lineTo(x, resolveY)
                                    }
                                    
                                    // Draw data points
                                    drawCircle(
                                        color = NeonBlue.copy(alpha = pulseAlpha),
                                        radius = 4f * scale,
                                        center = Offset(x, hopeY)
                                    )
                                    
                                    drawCircle(
                                        color = CosmicRed.copy(alpha = pulseAlpha),
                                        radius = 4f * scale,
                                        center = Offset(x, fearY)
                                    )
                                    
                                    drawCircle(
                                        color = AquaGreen.copy(alpha = pulseAlpha),
                                        radius = 4f * scale,
                                        center = Offset(x, resolveY)
                                    )
                                }
                                
                                // Draw paths
                                drawPath(
                                    path = hopePath,
                                    color = NeonBlue.copy(alpha = 0.6f * pulseAlpha),
                                    style = Stroke(width = 2f * scale)
                                )
                                
                                drawPath(
                                    path = fearPath,
                                    color = CosmicRed.copy(alpha = 0.6f * pulseAlpha),
                                    style = Stroke(width = 2f * scale)
                                )
                                
                                drawPath(
                                    path = resolvePath,
                                    color = AquaGreen.copy(alpha = 0.6f * pulseAlpha),
                                    style = Stroke(width = 2f * scale)
                                )
                                
                                // Draw legend
                                drawLine(
                                    color = NeonBlue.copy(alpha = 0.8f),
                                    start = Offset(xStep * 0.5f, canvasHeight * 0.1f),
                                    end = Offset(xStep * 0.5f + 20f * scale, canvasHeight * 0.1f),
                                    strokeWidth = 2f * scale
                                )
                                
                                drawLine(
                                    color = CosmicRed.copy(alpha = 0.8f),
                                    start = Offset(xStep * 0.5f, canvasHeight * 0.15f),
                                    end = Offset(xStep * 0.5f + 20f * scale, canvasHeight * 0.15f),
                                    strokeWidth = 2f * scale
                                )
                                
                                drawLine(
                                    color = AquaGreen.copy(alpha = 0.8f),
                                    start = Offset(xStep * 0.5f, canvasHeight * 0.2f),
                                    end = Offset(xStep * 0.5f + 20f * scale, canvasHeight * 0.2f),
                                    strokeWidth = 2f * scale
                                )
                            }
                            
                            // Title overlay
                            Text(
                                text = "EMOTIONAL ARC CHART",
                                color = DeepPurple,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }
                }
                
                // Info overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepPurple.copy(alpha = 0.7f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Pinch to zoom • Drag to pan",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
            
            // Bottom navigation tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(SpaceBlue.copy(alpha = 0.7f)),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VisualizationTab("Timeline", 0, selectedVisualization) { selectedVisualization = 0 }
                VisualizationTab("Characters", 1, selectedVisualization) { selectedVisualization = 1 }
                VisualizationTab("Tech Tree", 2, selectedVisualization) { selectedVisualization = 2 }
                VisualizationTab("Star Map", 3, selectedVisualization) { selectedVisualization = 3 }
                VisualizationTab("Worlds", 4, selectedVisualization) { selectedVisualization = 4 }
                VisualizationTab("Emotions", 5, selectedVisualization) { selectedVisualization = 5 }
            }
        }
    }
}

@Composable
fun VisualizationTab(
    name: String,
    index: Int,
    selectedIndex: Int,
    onClick: () -> Unit
) {
    val isSelected = index == selectedIndex
    val color = if (isSelected) NeonBlue else Color.White.copy(alpha = 0.6f)
    val bgColor = if (isSelected) DeepPurple.copy(alpha = 0.5f) else Color.Transparent
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = color,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
