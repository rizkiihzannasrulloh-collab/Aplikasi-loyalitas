package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Customer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NasiCardStroke
import com.example.ui.theme.NasiCream
import com.example.ui.theme.NasiOrange
import com.example.ui.theme.NasiOrangeDark
import com.example.ui.theme.NasiOrangeLight
import com.example.ui.theme.NasiPurple
import com.example.ui.theme.NasiPurpleContainer
import com.example.ui.theme.NasiYellow
import com.example.util.QrCodeHelper

@Composable
fun LoyaltyCardView(
    customer: Customer,
    modifier: Modifier = Modifier,
    initialShowBack: Boolean = false,
    onCardClick: (() -> Unit)? = null
) {
    var showQrSide by remember { mutableStateOf(initialShowBack) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, shape = RoundedCornerShape(20.dp), spotColor = NasiPurple.copy(alpha = 0.35f))
            .testTag("loyalty_card"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(2.5.dp, NasiCardStroke),
        colors = CardDefaults.cardColors(containerColor = NasiCream)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Left Checkerboard Border
                CheckerboardBorder(
                    modifier = Modifier.fillMaxHeight(),
                    columns = 2,
                    squareSize = 10.dp,
                    color1 = NasiPurple,
                    color2 = Color.White
                )

                // Card Main Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    if (!showQrSide) {
                        CardFrontSide(
                            customer = customer,
                            onToggleSide = { showQrSide = true }
                        )
                    } else {
                        CardBackQrSide(
                            customer = customer,
                            onToggleSide = { showQrSide = false }
                        )
                    }
                }

                // Right Checkerboard Border
                CheckerboardBorder(
                    modifier = Modifier.fillMaxHeight(),
                    columns = 2,
                    squareSize = 10.dp,
                    color1 = NasiPurple,
                    color2 = Color.White
                )
            }
        }
    }
}

@Composable
private fun CardFrontSide(
    customer: Customer,
    onToggleSide: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Top Header: Name & Expiry Date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.1f)) {
                Text(
                    text = "Nama:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NasiPurple
                )
                Text(
                    text = customer.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NasiPurple,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(1.5.dp)
                        .background(NasiPurple)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(
                    text = "Masa berlaku:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NasiPurple
                )
                Text(
                    text = QrCodeHelper.formatDate(customer.expiresAt),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (customer.isExpired) ErrorRed else NasiPurple
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(1.5.dp)
                        .background(if (customer.isExpired) ErrorRed else NasiPurple)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Center Grid: 8 Stamp Boxes + 1 Free Reward Box
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 8 Boxes arranged in 2 rows of 4
            Column(
                modifier = Modifier.weight(4f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: stamps 1, 2, 3, 4
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 1..4) {
                        StampSlotBox(
                            slotIndex = i,
                            isStamped = customer.currentStamps >= i,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Row 2: stamps 5, 6, 7, 8
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 5..8) {
                        StampSlotBox(
                            slotIndex = i,
                            isStamped = customer.currentStamps >= i,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 9th Box: GRATIS 1 Box
            GratisSlotBox(
                isReady = customer.isRewardReady,
                modifier = Modifier.weight(1.35f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Section: Title + Business Rules & Flip Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1.1f)) {
                Text(
                    text = "KARTU LOYALITAS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = NasiPurple,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "BELI 8\nGRATIS 1",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = NasiPurple,
                    lineHeight = 19.sp
                )
            }

            Column(modifier = Modifier.weight(1.3f)) {
                Text(
                    text = "• Berlaku penggunaan pribadi & tidak dipindahtangankan.",
                    fontSize = 8.5.sp,
                    lineHeight = 11.sp,
                    color = NasiPurple.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "• Tunjukkan kartu saat bertransaksi.",
                    fontSize = 8.5.sp,
                    lineHeight = 11.sp,
                    color = NasiPurple.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Interactive button to show QR code
                Surface(
                    onClick = onToggleSide,
                    shape = RoundedCornerShape(8.dp),
                    color = NasiPurple,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Tampilkan QR",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Buka QR",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StampSlotBox(
    slotIndex: Int,
    isStamped: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isStamped) NasiOrange else NasiYellow
    val borderColor = NasiPurple

    Box(
        modifier = modifier
            .aspectRatio(1.25f)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.8.dp, borderColor, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isStamped) {
            // Stamped Effect
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(NasiPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Stamped",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            // Slot number subtle indicator
            Text(
                text = "$slotIndex",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NasiPurple.copy(alpha = 0.45f)
            )
        }
    }
}

@Composable
private fun GratisSlotBox(
    isReady: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isReady) 1.06f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val bgColor = if (isReady) NasiOrangeLight else NasiYellow
    val borderColor = if (isReady) NasiOrangeDark else NasiPurple

    Box(
        modifier = modifier
            .aspectRatio(0.68f)
            .scale(pulseScale)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "GRATIS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = if (isReady) NasiOrangeDark else NasiPurple,
                letterSpacing = 0.5.sp
            )

            RiceBowlIllustration(
                size = 38.dp,
                showPlate = true,
                showSparkles = isReady,
                showFace = true
            )

            if (isReady) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = NasiOrangeDark
                ) {
                    Text(
                        text = "KLAIM!",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            } else {
                Text(
                    text = "1 Porsi",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = NasiPurple.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun CardBackQrSide(
    customer: Customer,
    onToggleSide: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    val qrBitmap: Bitmap? = remember(customer.id) {
        QrCodeHelper.generateQrBitmap(
            content = customer.id,
            size = 400,
            foregroundColor = NasiPurple,
            backgroundColor = Color.White
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Brand Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "RESTORAN BURGER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NasiPurple,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "NASI COKOT",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = NasiPurple
                )
            }

            Surface(
                onClick = onToggleSide,
                shape = RoundedCornerShape(8.dp),
                color = NasiPurpleContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Flip,
                        contentDescription = "Lihat Kartu",
                        tint = NasiPurple,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Lihat Kartu",
                        color = NasiPurple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Center: Customer QR Code Box
        Surface(
            modifier = Modifier
                .size(140.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(2.dp, NasiPurple)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code Pelanggan ${customer.name}",
                        modifier = Modifier
                            .size(126.dp)
                            .padding(4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Customer Code with Copy Button
        Surface(
            onClick = {
                clipboardManager.setText(AnnotatedString(customer.id))
                copied = true
            },
            shape = RoundedCornerShape(20.dp),
            color = NasiPurpleContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = customer.id,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = NasiPurple
                )
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Salin Kode",
                    tint = NasiPurple,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Text(
            text = if (copied) "Kode berhasil disalin!" else "Tunjukkan QR ini ke kasir saat memesan",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = if (copied) NasiOrangeDark else NasiPurple.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 3.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Card Footer
        Text(
            text = "📞 082299231446  •  📸 @nasicokot",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = NasiPurple.copy(alpha = 0.7f)
        )
    }
}
