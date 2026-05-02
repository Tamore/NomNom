package com.nomnom.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nomnom.app.ui.theme.*
import kotlinx.coroutines.delay
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.nomnom.app.R

// ─────────────────────────────────────────────────────────────────────────────
// SHIMMER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue  = 1200f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )
    return Brush.linearGradient(
        colors = listOf(NomNomShimmer1, NomNomShimmer2, NomNomShimmer1),
        start  = Offset(translateAnim - 400f, 0f),
        end    = Offset(translateAnim, 0f)
    )
}

@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    Card(
        modifier = modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CSSurfaceLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(brush)
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.fillMaxWidth(0.7f).height(16.dp).clip(RoundedCornerShape(8.dp)).background(brush))
                Box(Modifier.fillMaxWidth(0.45f).height(12.dp).clip(RoundedCornerShape(8.dp)).background(brush))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PRESS-SCALE CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NomNomCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),   // CS: rounded-xl
    containerColor: Color = CSSurfaceLowest,                 // white cards on linen
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "cardScale"
    )
    Card(
        onClick    = onClick,
        modifier   = modifier.scale(scale),
        shape      = shape,
        colors     = CardDefaults.cardColors(containerColor = containerColor),
        elevation  = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 0.dp),
        interactionSource = interactionSource
    ) {
        Column(content = content)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BUTTON
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NomNomButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = CSSage,              // sage green primary
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "btnScale"
    )
    Button(
        onClick      = onClick,
        enabled      = enabled,
        modifier     = modifier.height(52.dp).scale(scale),
        shape        = RoundedCornerShape(50.dp),            // pill-shaped
        colors       = ButtonDefaults.buttonColors(
            containerColor         = containerColor,
            disabledContainerColor = CSSurfaceVariant,
            contentColor           = CSOnSage,
            disabledContentColor   = CSOutline
        ),
        interactionSource = interactionSource,
        content      = content
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// TEXT FIELD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NomNomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    isError: Boolean = false,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
) {
    OutlinedTextField(
        value             = value,
        onValueChange     = onValueChange,
        modifier          = modifier,
        placeholder       = { Text(placeholder, color = CSOutline, fontSize = 14.sp) },
        label             = if (label != null) ({ Text(label, color = CSOnSurfaceVariant) }) else null,
        leadingIcon       = leadingIcon,
        trailingIcon      = trailingIcon,
        singleLine        = singleLine,
        maxLines          = maxLines,
        isError           = isError,
        keyboardOptions   = keyboardOptions,
        visualTransformation = visualTransformation,
        shape             = RoundedCornerShape(12.dp),
        colors            = OutlinedTextFieldDefaults.colors(
            focusedBorderColor       = CSSage,
            unfocusedBorderColor     = CSOutlineVariant,
            focusedTextColor         = CSOnSurface,
            unfocusedTextColor       = CSOnSurface,
            cursorColor              = CSSage,
            focusedContainerColor    = CSSurfaceLowest,
            unfocusedContainerColor  = CSSurfaceLow,
            errorBorderColor         = CSError,
            errorContainerColor      = CSErrorContainer,
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// CHIPS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RecipeChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(CSSurfaceContainer, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, color = CSOnSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun GoldChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFFFFF8DC), RoundedCornerShape(20.dp))  // champagne
            .border(1.dp, CSGold.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, color = CSGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TagChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.94f else 1f, label = "tagScale")

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) CSSage else CSSurfaceContainer)
            .border(
                width  = 1.dp,
                color  = if (selected) CSSage else CSOutlineVariant,
                shape  = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text       = text,
            color      = if (selected) CSOnSage else CSOnSurfaceVariant,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// INGREDIENT CHECK ROW
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IngredientCheckRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val checkScale by animateFloatAsState(
        targetValue   = if (checked) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label         = "checkScale"
    )
    val textAlpha by animateFloatAsState(if (checked) 0.4f else 1f, label = "textAlpha")
    val checkColor by animateColorAsState(
        targetValue   = if (checked) CSSage else CSSurfaceContainer,
        animationSpec = tween(200),
        label         = "checkColor"
    )

    Row(
        modifier          = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication        = null
            ) { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circle checkbox
        Box(
            modifier        = Modifier
                .size(22.dp)
                .scale(checkScale)
                .clip(CircleShape)
                .background(checkColor)
                .border(
                    width  = if (checked) 0.dp else 1.5.dp,
                    color  = CSOutlineVariant,
                    shape  = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Text("✓", color = CSOnSage, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text           = text,
            color          = CSOnSurface.copy(alpha = textAlpha),
            fontSize       = 15.sp,
            fontWeight     = FontWeight.Normal,
            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
            modifier       = Modifier.weight(1f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SECTION HEADER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier          = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(20.dp)
                .background(CSSage, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text       = text,
            color      = CSOnSurface,
            fontSize   = 17.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = NotoSerif
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STAGGERED LIST ITEM (animation wrapper)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AnimatedListItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 55L)
        visible = true
    }
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter   = androidx.compose.animation.fadeIn(tween(300)) +
                  androidx.compose.animation.slideInVertically(
                      initialOffsetY = { it / 3 },
                      animationSpec  = spring(
                          dampingRatio = Spring.DampingRatioLowBouncy,
                          stiffness    = Spring.StiffnessMediumLow
                      )
                  ),
        modifier = modifier
    ) {
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HERO GRADIENT BOX (emoji placeholder until real images in Phase 7)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RecipeHeroBox(
    imageUrl: String?,
    height: Dp = 260.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(CSSurfaceContainer),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Gradient placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CSSurfaceContainer, CSSurfaceLow, CSSurface)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🍳", fontSize = 96.sp, modifier = Modifier.alpha(0.2f))
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(CSHeroGradient))
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CIRCULAR THUMBNAIL (for recipe list items)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RecipeThumbnail(imageUrl: String?, size: Dp = 56.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size / 3))
            .background(CSSurfaceContainer),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text("🍽️", fontSize = (size.value * 0.42f).sp, modifier = Modifier.alpha(0.6f))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STEP CARD (recipe detail / cook mode)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StepCard(number: Int, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .background(CSSurfaceLowest, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier        = Modifier
                .size(28.dp)
                .background(CSSage, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = "$number",
                color      = CSOnSage,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text     = text,
            color    = CSOnSurface,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f),
            style    = MaterialTheme.typography.bodyLarge
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PROGRESS STEP INDICATOR (wizard)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun WizardStepIndicator(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier            = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment   = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isDone    = index < currentStep
            val isCurrent = index == currentStep
            val width by animateDpAsState(
                targetValue   = if (isCurrent) 28.dp else 8.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label         = "stepWidth$index"
            )
            val color by animateColorAsState(
                targetValue   = when {
                    isDone || isCurrent -> CSSage
                    else                -> CSOutlineVariant
                },
                animationSpec = tween(200),
                label         = "stepColor$index"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COOK MODE TIMER RING
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TimerRing(
    totalMs: Long,
    remainingMs: Long,
    running: Boolean,
    size: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    val progress = if (totalMs > 0L) remainingMs.toFloat() / totalMs.toFloat() else 0f
    val animatedProgress by animateFloatAsState(progress, tween(500), label = "timerProgress")

    val mins = (remainingMs / 60000).toInt()
    val secs = ((remainingMs % 60000) / 1000).toInt()

    val pulseTransition = rememberInfiniteTransition(label = "timerPulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue  = 0.6f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "pulseAlpha"
    )

    Box(
        modifier        = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 7.dp.toPx()
            drawCircle(color = CSSurfaceContainer, style = Stroke(strokePx))
            drawArc(
                color      = CSSage,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter  = false,
                style      = Stroke(strokePx, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = "%d:%02d".format(mins, secs),
                color      = CSOnSurface.copy(alpha = if (running) pulseAlpha else 1f),
                fontSize   = (size.value * 0.2f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
