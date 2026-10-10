package com.example.EarningZone

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MicroTaskRootApp()
        }
    }
}

// ================= HAX COLORS FROM HTML =================
val ColorAppDarkBg = Color(0xFF020713)
val ColorPhoneFrameBg = Color(0xFF030A16)
val ColorFrameBorder = Color(0xFF162438)
val ColorCardDarkBg = Color(0xFF061324)
val ColorCyanAccent = Color(0xFF00D2FF)
val ColorLime = Color(0xFFD8FF38)
val ColorInvezoPaper = Color(0xFFF8F8F7)
val ColorInvezoInk = Color(0xFF101010)

data class AppUser(val name: String, val identity: String, val uid: String)

@Composable
fun MicroTaskRootApp() {
    val context = LocalContext.current
    var currentUser by remember { mutableStateOf<AppUser?>(null) }
    var authSheetOpen by remember { mutableStateOf(false) }
    var authSheetView by remember { mutableStateOf("register") }

    // Dashboard Modal States
    var isBalanceHidden by remember { mutableStateOf(false) }
    var isDepositOpen by remember { mutableStateOf(false) }
    var isReferOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorAppDarkBg),
        contentAlignment = Alignment.Center
    ) {
        // মোবাইল ফ্রেম রেপ্লিকেশন
        Box(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(36.dp))
                .border(4.dp, ColorFrameBorder, RoundedCornerShape(36.dp))
                .background(if (currentUser == null) ColorInvezoPaper else ColorPhoneFrameBg)
        ) {
            if (currentUser == null) {
                // 1. INVEZO ONBOARDING SCREEN
                InvezoOnboardingView(
                    onOpenSheet = { view ->
                        authSheetView = view
                        authSheetOpen = true
                    }
                )
            } else {
                // 2. MAIN MICROTASK DASHBOARD
                MicroTaskDashboardView(
                    user = currentUser!!,
                    isBalanceHidden = isBalanceHidden,
                    onToggleBalance = { isBalanceHidden = !isBalanceHidden },
                    onLogout = {
                        currentUser = null
                        Toast.makeText(context, "সফলভাবে লগআউট করা হয়েছে।", Toast.LENGTH_SHORT).show()
                    },
                    onOpenDeposit = { isDepositOpen = true },
                    onOpenRefer = { isReferOpen = true }
                )
            }

            // Invezo Auth Sheet
            if (authSheetOpen) {
                InvezoAuthSheetDialog(
                    initialView = authSheetView,
                    onDismiss = { authSheetOpen = false },
                    onSuccess = { user ->
                        currentUser = user
                        authSheetOpen = false
                        Toast.makeText(context, "স্বাগতম, ${user.name}!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Deposit 3-Step Modal
            if (isDepositOpen) {
                ModernDepositModalDialog(onDismiss = { isDepositOpen = false })
            }

            // Refer 3-Tab Modal
            if (isReferOpen) {
                ReferralBonusModalDialog(
                    uid = currentUser?.uid ?: "784219",
                    onDismiss = { isReferOpen = false }
                )
            }
        }
    }
}

// =========================================================================
// 1. INVEZO ONBOARDING VIEW (আসল আর্টওয়ার্ক ও ব্র্যান্ড সহ)
// =========================================================================
@Composable
fun InvezoOnboardingView(onOpenSheet: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Status & Brand
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("9:41", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = ColorInvezoInk)
                Text("◆ ▴ ▰", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = ColorInvezoInk)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp, 14.dp)
                        .background(ColorInvezoInk, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("invezo.", fontSize = 21.sp, fontWeight = FontWeight.Black, color = ColorInvezoInk, letterSpacing = (-0.8).sp)
            }
        }

        // Hero Artwork (Canvas দিয়ে আঁকা আসল আর্টওয়ার্ক)
        InvezoHeroCanvasArt()

        // Intro Text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Let’s find good\nthings together.",
                fontSize = 25.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = ColorInvezoInk
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Start your journey with a simple, secure MicroTask account.",
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF555555)
            )
        }

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onOpenSheet("register") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = ColorLime)
            ) {
                Text("Create Account", color = ColorInvezoInk, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
            OutlinedButton(
                onClick = { onOpenSheet("login") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = CircleShape,
                border = Stroke(1.5f).let { androidx.compose.foundation.BorderStroke(1.5.dp, ColorInvezoInk) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorInvezoInk)
            ) {
                Text("Sign In", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
            Text(
                "By signing up, you agree to our Terms of Service and Privacy Policy.",
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF666666),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun InvezoHeroCanvasArt() {
    Canvas(modifier = Modifier.size(240.dp, 160.dp)) {
        val w = size.width
        val h = size.height

        // Swoosh curve
        drawArc(
            color = Color(0xFF111111),
            startAngle = 90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.1f, h * 0.28f),
            size = Size(w * 0.7f, h * 0.5f),
            style = Stroke(width = 14f)
        )

        // 3 Bars
        drawRect(color = Color.White, topLeft = Offset(w * 0.65f, h * 0.45f), size = Size(20f, 60f))
        drawRect(color = Color(0xFF111111), topLeft = Offset(w * 0.65f, h * 0.45f), size = Size(20f, 60f), style = Stroke(4f))

        drawRect(color = Color.White, topLeft = Offset(w * 0.73f, h * 0.32f), size = Size(20f, 90f))
        drawRect(color = Color(0xFF111111), topLeft = Offset(w * 0.73f, h * 0.32f), size = Size(20f, 90f), style = Stroke(4f))

        drawRect(color = Color.White, topLeft = Offset(w * 0.81f, h * 0.18f), size = Size(20f, 120f))
        drawRect(color = Color(0xFF111111), topLeft = Offset(w * 0.81f, h * 0.18f), size = Size(20f, 120f), style = Stroke(4f))

        // Base
        drawRect(color = Color(0xFF111111), topLeft = Offset(w * 0.64f, h * 0.75f), size = Size(65f, 20f))
    }
}

@Composable
fun InvezoAuthSheetDialog(
    initialView: String,
    onDismiss: () -> Unit,
    onSuccess: (AppUser) -> Unit
) {
    var view by remember { mutableStateOf(initialView) }
    var name by remember { mutableStateOf("") }
    var identity by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = ColorInvezoPaper
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(36.dp, 4.dp).background(Color(0xFFDDDDDD), CircleShape))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color.Gray)
                    }
                }

                if (view == "register") {
                    Text("Create your account", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = ColorInvezoInk)
                    Text("Enter your details to get started", fontSize = 11.sp, color = Color(0xFF777777))
                    Spacer(modifier = Modifier.height(10.dp))

                    InvezoInputField("Full name", name, "Enter your full name") { name = it }
                    InvezoInputField("Mobile number or Gmail", identity, "01XXXXXXXXX or name@gmail.com") { identity = it }
                    InvezoPasswordField("Password", password, "Create a password (8+ characters)", passVisible, { passVisible = !passVisible }) { password = it }
                    InvezoPasswordField("Confirm password", confirm, "Enter password again", passVisible, { passVisible = !passVisible }) { confirm = it }

                    if (errorMsg.isNotEmpty()) {
                        Text(errorMsg, color = Color(0xFFB91C1C), fontSize = 11.sp, modifier = Modifier.padding(vertical = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (identity.length < 5) errorMsg = "সঠিক ইমেইল বা মোবাইল নাম্বার লিখুন"
                            else if (password.length < 8) errorMsg = "পাসওয়ার্ড অন্তত ৮ ডিজিট হতে হবে"
                            else if (password != confirm) errorMsg = "পাসওয়ার্ড দুটি মিলছে না"
                            else {
                                val genUid = (100000..999999).random().toString()
                                val uName = name.ifEmpty { identity.substringBefore("@") }
                                onSuccess(AppUser(uName, identity, genUid))
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = ColorLime)
                    ) {
                        Text("Create Account", color = ColorInvezoInk, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    TextButton(onClick = { view = "login" }) {
                        Text("Already have an account? Sign In", color = ColorInvezoInk, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // LOGIN VIEW
                    Text("Welcome back", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = ColorInvezoInk)
                    Text("Sign in to continue to your account", fontSize = 11.sp, color = Color(0xFF777777))
                    Spacer(modifier = Modifier.height(10.dp))

                    InvezoInputField("Mobile number or Gmail", identity, "Your phone number or email") { identity = it }
                    InvezoPasswordField("Password", password, "Enter your password", passVisible, { passVisible = !passVisible }) { password = it }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (identity.isNotEmpty() && password.isNotEmpty()) {
                                val genUid = (100000..999999).random().toString()
                                onSuccess(AppUser(identity.substringBefore("@"), identity, genUid))
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = ColorLime)
                    ) {
                        Text("Sign In", color = ColorInvezoInk, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    TextButton(onClick = { view = "register" }) {
                        Text("New here? Create Account", color = ColorInvezoInk, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InvezoInputField(label: String, value: String, placeholder: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorInvezoInk)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 11.sp, color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = CircleShape
        )
    }
}

@Composable
fun InvezoPasswordField(
    label: String,
    value: String,
    placeholder: String,
    visible: Boolean,
    onToggle: () -> Unit,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorInvezoInk)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 11.sp, color = Color.Gray) },
            singleLine = true,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggle) {
                    Icon(if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = Color.Gray)
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = CircleShape
        )
    }
}

// =========================================================================
// 2. MAIN MICROTASK DASHBOARD (হুবহু কালার ও লেআউট)
// =========================================================================
@Composable
fun MicroTaskDashboardView(
    user: AppUser,
    isBalanceHidden: Boolean,
    onToggleBalance: () -> Unit,
    onLogout: () -> Unit,
    onOpenDeposit: () -> Unit,
    onOpenRefer: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            MicroTaskBottomNavBar(onOpenRefer = onOpenRefer)
        },
        containerColor = ColorPhoneFrameBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // TOP HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // হাতের টাকার লোগো
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .border(1.5.dp, ColorCyanAccent, RoundedCornerShape(11.dp))
                                .background(Color.White)
                        ) {
                            AsyncImage(
                                model = "https://www.shutterstock.com/image-vector/money-logo-design-vector-illustrative-260nw-2007522590.jpg",
                                contentDescription = "Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(9.dp))
                        Column {
                            Row {
                                Text("Micro", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp)
                                Text("Task", color = ColorCyanAccent, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp)
                            }
                            Text("Small Tasks Big Earnings", color = Color(0xFF9CB1CF), fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // বেল নোটিফিকেশন ব্যাজ
                        Box(modifier = Modifier.clickable { Toast.makeText(context, "3টি নোটিফিকেশন আছে", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Outlined.Notifications, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            Box(
                                modifier = Modifier
                                    .size(15.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .background(Color(0xFFFF334B), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("3", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        // প্রোফাইল এভাটার
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFF0077FF), Color(0xFF00C6FF))))
                                .clickable { onLogout() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // MAIN BALANCE CARD (130deg Gradient)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier
                            .background(Brush.linearGradient(listOf(Color(0xFF00AAFF), Color(0xFF0051FF), Color(0xFF8E15EF))))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(13.dp))
                                        .background(Color.White.copy(0.22f))
                                        .border(1.dp, Color.White.copy(0.3f), RoundedCornerShape(13.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = "https://img.icons8.com/?size=100&id=L0TH_5m8bo5r&format=png&color=ffffff",
                                        contentDescription = "Wallet",
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("My Balance", color = Color(0xFFD8ECFF), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            null,
                                            tint = Color(0xFFC9E6FF),
                                            modifier = Modifier.size(14.dp).clickable { onToggleBalance() }
                                        )
                                    }
                                    Text(
                                        text = if (isBalanceHidden) "$ ••••" else "$ 12.45",
                                        color = Color.White,
                                        fontSize = 23.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = if (isBalanceHidden) "≈ •••• BDT" else "≈ 1,240.00 BDT",
                                        color = Color(0xFFD6E2F5),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // User Name & UID
                            Column(horizontalAlignment = Alignment.End) {
                                Text(user.name, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        cb.setPrimaryClip(ClipData.newPlainText("UID", user.uid))
                                        Toast.makeText(context, "UID Copied: ${user.uid}", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text("UID: ${user.uid}", color = Color(0xFFA8F0FF), fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ContentCopy, null, tint = Color.White, modifier = Modifier.size(11.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Sub Stats Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF061124))
                                .padding(vertical = 8.dp, horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SubStatTile("Today Earn", "$ 2.35", "https://img.icons8.com/?size=100&id=34013&format=png&color=ffffff", Brush.linearGradient(listOf(Color(0xFF00D25B), Color(0xFF00943F))))
                            SubStatTile("Total Earn", "$ 42.80", "https://img.icons8.com/?size=100&id=13555&format=png&color=ffffff", Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF0088FF))))
                            SubStatTileLevel("Level", "Silver")
                        }
                    }
                }
            }

            // ACTIONS (DEPOSIT & WITHDRAW)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    ActionTile(
                        title = "Deposit",
                        sub = "Add Money",
                        iconUrl = "https://img.icons8.com/?size=100&id=SywK0qRgyexK&format=png&color=ffffff",
                        gradient = Brush.linearGradient(listOf(Color(0xFF00B355), Color(0xFF007736))),
                        modifier = Modifier.weight(1f),
                        onClick = onOpenDeposit
                    )
                    ActionTile(
                        title = "Withdraw",
                        sub = "Cash Out",
                        iconUrl = "https://img.icons8.com/?size=100&id=KGhSnVZPf0Vl&format=png&color=ffffff",
                        gradient = Brush.linearGradient(listOf(Color(0xFF7C1DF7), Color(0xFF4C07A3))),
                        modifier = Modifier.weight(1f),
                        onClick = { Toast.makeText(context, "Withdrawal request opens soon!", Toast.LENGTH_SHORT).show() }
                    )
                }
            }

            // TASK 1: WATCH VIDEOS & EARN (৪টি প্ল্যাটফর্ম সহ)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorCardDarkBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF112642))
                ) {
                    Column(modifier = Modifier.padding(11.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Brush.linearGradient(listOf(ColorCyanAccent, Color(0xFF0066FF)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(9.dp))
                                Column {
                                    Text("Watch Videos & Earn", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                    Text("Watch videos from popular platforms", color = Color(0xFF839EC4), fontSize = 8.5.sp)
                                }
                            }
                            Text(
                                "$0.05 – $0.30",
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clip(CircleShape).background(Color(0xFF009E47)).padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(9.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            VideoTile("YouTube", Color(0xFFFF0000), Icons.Default.PlayArrow, Modifier.weight(1f))
                            VideoTile("Facebook", Color(0xFF1877F2), Icons.Default.ThumbUp, Modifier.weight(1f))
                            VideoTile("Instagram", Color(0xFFE1306C), Icons.Default.CameraAlt, Modifier.weight(1f))
                            VideoTile("TikTok", Color(0xFF010101), Icons.Default.Audiotrack, Modifier.weight(1f))
                        }
                    }
                }
            }

            // TASK 2: TYPING TASK
            item {
                SingleTaskCard("Typing Task", "Type simple text & earn money", "$0.10 – $0.50", Icons.Default.Keyboard, Brush.linearGradient(listOf(Color(0xFFC43BF0), Color(0xFF7B1FA2))))
            }

            // TASK 3: TELEGRAM TASK
            item {
                SingleTaskCard("Telegram Task", "Join our official channel & groups", "$0.20 – $1.00", Icons.Default.Send, Brush.linearGradient(listOf(Color(0xFF2AABEE), Color(0xFF1682C7))))
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }
        }
    }
}

@Composable
fun SubStatTile(title: String, value: String, iconUrl: String, gradient: Brush) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(gradient).padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(model = iconUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
        }
        Spacer(modifier = Modifier.width(7.dp))
        Column {
            Text(title, color = Color(0xFF8DA4C4), fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun SubStatTileLevel(title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MilitaryTech, null, tint = Color(0xFF1E293B), modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(7.dp))
        Column {
            Text(title, color = Color(0xFF8DA4C4), fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun ActionTile(title: String, sub: String, iconUrl: String, gradient: Brush, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier.background(gradient).padding(11.dp, 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(0.2f)).padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(model = iconUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
                }
                Spacer(modifier = Modifier.width(9.dp))
                Column {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(sub, color = Color.White.copy(0.85f), fontSize = 8.5.sp)
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun VideoTile(name: String, bg: Color, icon: ImageVector, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF07182E))
            .border(1.dp, Color(0xFF142A47), RoundedCornerShape(12.dp))
            .padding(vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(33.dp).clip(RoundedCornerShape(8.dp)).background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(17.dp))
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(name, color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
        Text("+ Earn", color = Color(0xFF00D25B), fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SingleTaskCard(title: String, desc: String, badge: String, icon: ImageVector, iconBg: Brush) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ColorCardDarkBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF112642))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(11.dp, 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(9.dp))
                Column {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    Text(desc, color = Color(0xFF839EC4), fontSize = 8.5.sp)
                }
            }
            Text(
                badge,
                color = Color.White,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(CircleShape).background(Color(0xFF0066FF)).padding(horizontal = 9.dp, vertical = 4.dp)
            )
        }
    }
}

// =========================================================================
// 3. BOTTOM NAVIGATION BAR
// =========================================================================
@Composable
fun MicroTaskBottomNavBar(onOpenRefer: () -> Unit) {
    Surface(
        color = Color(0xFF020813),
        modifier = Modifier.fillMaxWidth().height(70.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavIconText("Home", Icons.Default.Home, ColorCyanAccent, true) {}
            NavIconText("Tasks", Icons.Default.Assignment, Color(0xFFB757FF), false) {}

            // সেন্টার ফ্লোটিং বাবল
            Box(
                modifier = Modifier
                    .offset(y = (-15).dp)
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(ColorCyanAccent, Color(0xFF6F17E3))))
                    .border(3.dp, ColorPhoneFrameBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = "https://img.icons8.com/?size=100&id=YiP7pNVh3jYW&format=png&color=ffffff",
                    contentDescription = "Support",
                    modifier = Modifier.size(28.dp)
                )
            }

            NavIconText("History", Icons.Default.History, Color(0xFF00D25B), false) {}
            NavIconImgText("Refer", "https://img.icons8.com/?size=100&id=G44HkjZjDMBJ&format=png&color=ffffff", Color(0xFFFF9800), onOpenRefer)
        }
    }
}

@Composable
fun NavIconText(label: String, icon: ImageVector, color: Color, active: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Icon(icon, null, tint = if (active) ColorCyanAccent else Color(0xFF627B9E), modifier = Modifier.size(22.dp))
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun NavIconImgText(label: String, url: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        AsyncImage(model = url, contentDescription = null, modifier = Modifier.size(25.dp))
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

// =========================================================================
// 4. DEPOSIT 3-STEP MODAL DIALOG (হুবহু ৩টি ধাপ সহ)
// =========================================================================
@Composable
fun ModernDepositModalDialog(onDismiss: () -> Unit) {
    var step by remember { mutableStateOf(1) }
    var method by remember { mutableStateOf("Nagad") }
    var amount by remember { mutableStateOf("100") }
    var sender by remember { mutableStateOf("") }
    var trx by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    val themeColor = when(method) {
        "bkash" -> Color(0xFFE2136E)
        "binance" -> Color(0xFFF0B90B)
        else -> Color(0xFFFF6B00)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E0A05),
            modifier = Modifier.fillMaxWidth().padding(10.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Header
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Deposit Funds", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (submitted) {
                    Text("🎉 Deposit Submitted!", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Pending admin verification.", color = Color.Gray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) {
                        Text("Return to Dashboard")
                    }
                } else {
                    // Stepper Track
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text("Step $step of 3", color = ColorCyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    when(step) {
                        1 -> {
                            Text("Select Payment Gateway", color = Color.White, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { method = "Nagad"; step = 2 }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00)), modifier = Modifier.weight(1f)) { Text("Nagad") }
                                Button(onClick = { method = "bkash"; step = 2 }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2136E)), modifier = Modifier.weight(1f)) { Text("bKash") }
                                Button(onClick = { method = "binance"; step = 2 }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0B90B)), modifier = Modifier.weight(1f)) { Text("Binance", color = Color.Black) }
                            }
                        }
                        2 -> {
                            Text("Choose Deposit Amount", color = Color.White, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = amount, onValueChange = { amount = it },
                                label = { Text("Amount (৳)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { step = 1 }, modifier = Modifier.weight(1f)) { Text("Back") }
                                Button(onClick = { step = 3 }, colors = ButtonDefaults.buttonColors(containerColor = themeColor), modifier = Modifier.weight(1f)) { Text("Next") }
                            }
                        }
                        3 -> {
                            Text("Summary: $method - ৳$amount", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Recipient: 01876543210", color = ColorCyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(value = sender, onValueChange = { sender = it }, label = { Text("Sender Phone") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = trx, onValueChange = { trx = it }, label = { Text("Transaction ID (TrxID)") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { if (sender.isNotEmpty() && trx.isNotEmpty()) submitted = true },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Submit Deposit") }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 5. REFERRAL BONUS MODAL (হুবহু ৩টি ট্যাব সহ)
// =========================================================================
@Composable
fun ReferralBonusModalDialog(uid: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("collect") }
    val refLink = "https://microtask.app/?user=$uid"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().heightIn(max = 580.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Referral Bonus", color = Color(0xFF102D5F), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Link Card
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F4F9))) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(refLink, fontSize = 10.sp, color = Color(0xFF304B76), modifier = Modifier.weight(1f), maxLines = 1)
                        Button(
                            onClick = {
                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cb.setPrimaryClip(ClipData.newPlainText("Refer", refLink))
                                Toast.makeText(context, "Referral link copied!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102D5F)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Copy", fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tabs
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFE1E7EF))) {
                    TabBtn("To be collected", activeTab == "collect", Modifier.weight(1f)) { activeTab = "collect" }
                    TabBtn("Received", activeTab == "received", Modifier.weight(1f)) { activeTab = "received" }
                    TabBtn("Total", activeTab == "total", Modifier.weight(1f)) { activeTab = "total" }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Content List
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    when (activeTab) {
                        "collect" -> {
                            item { ReferItemRow("Rahim Khan", "Your 5% bonus", "৳15.00", true) }
                        }
                        "received" -> {
                            item { ReferItemRow("Karim Ullah", "Commission received", "৳25.00", false) }
                        }
                        "total" -> {
                            item { ReferTotalRow("Rahim Khan", "UID: 629140") }
                            item { ReferTotalRow("Karim Ullah", "UID: 841290") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabBtn(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(if (selected) Color(0xFF102F63) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) Color.White else Color(0xFF70809E), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ReferItemRow(name: String, desc: String, amt: String, canClaim: Boolean) {
    Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                Text(desc, fontSize = 9.5.sp, color = Color.Gray)
            }
            if (canClaim) {
                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102F63))) {
                    Text("Claim $amt", fontSize = 10.sp)
                }
            } else {
                Text(amt, color = Color(0xFF07895C), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ReferTotalRow(name: String, uid: String) {
    Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                Text(uid, fontSize = 9.5.sp, color = Color.Gray)
            }
            Text("Active ✓", color = Color(0xFF07895C), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
