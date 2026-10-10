package com.example.microtask

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

    var isBalanceHidden by remember { mutableStateOf(false) }
    var isDepositOpen by remember { mutableStateOf(false) }
    var isReferOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorAppDarkBg),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxHeight()
                .background(if (currentUser == null) ColorInvezoPaper else ColorPhoneFrameBg)
        ) {
            if (currentUser == null) {
                // 1. INVEZO ONBOARDING
                InvezoOnboardingView(
                    onOpenSheet = { view ->
                        authSheetView = view
                        authSheetOpen = true
                    }
                )
            } else {
                // 2. MAIN DASHBOARD
                MicroTaskDashboardView(
                    user = currentUser!!,
                    isBalanceHidden = isBalanceHidden,
                    onToggleBalance = { isBalanceHidden = !isBalanceHidden },
                    onLogout = {
                        currentUser = null
                        Toast.makeText(context, "সফলভাবে লগআউট হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    onOpenDeposit = { isDepositOpen = true },
                    onOpenRefer = { isReferOpen = true }
                )
            }

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

            if (isDepositOpen) {
                ModernDepositModalDialog(onDismiss = { isDepositOpen = false })
            }

            if (isReferOpen) {
                ReferralBonusModalDialog(
                    uid = currentUser?.uid ?: "784219",
                    onDismiss = { isReferOpen = false }
                )
            }
        }
    }
}

// ----------------- INVEZO ONBOARDING -----------------
@Composable
fun InvezoOnboardingView(onOpenSheet: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("9:41", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = ColorInvezoInk)
                Text("◆ ▴ ▰", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = ColorInvezoInk)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(18.dp, 14.dp).background(ColorInvezoInk, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(6.dp))
                Text("invezo.", fontSize = 22.sp, fontWeight = FontWeight.Black, color = ColorInvezoInk)
            }
        }

        InvezoHeroCanvasArt()

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Let’s find good\nthings together.", fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, color = ColorInvezoInk)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Start your journey with a simple, secure EarningZone account.", fontSize = 12.sp, textAlign = TextAlign.Center, color = Color(0xFF555555))
        }

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorInvezoInk),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorInvezoInk)
            ) {
                Text("Sign In", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun InvezoHeroCanvasArt() {
    Canvas(modifier = Modifier.size(240.dp, 150.dp)) {
        val w = size.width
        val h = size.height
        drawArc(
            color = Color(0xFF111111),
            startAngle = 90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.1f, h * 0.28f),
            size = Size(w * 0.7f, h * 0.5f),
            style = Stroke(width = 12f)
        )
        drawRect(color = Color.White, topLeft = Offset(w * 0.65f, h * 0.45f), size = Size(18f, 55f))
        drawRect(color = Color(0xFF111111), topLeft = Offset(w * 0.65f, h * 0.45f), size = Size(18f, 55f), style = Stroke(4f))
        drawRect(color = Color.White, topLeft = Offset(w * 0.73f, h * 0.32f), size = Size(18f, 85f))
        drawRect(color = Color(0xFF111111), topLeft = Offset(w * 0.73f, h * 0.32f), size = Size(18f, 85f), style = Stroke(4f))
        drawRect(color = Color(0xFF111111), topLeft = Offset(w * 0.64f, h * 0.75f), size = Size(65f, 18f))
    }
}

@Composable
fun InvezoAuthSheetDialog(initialView: String, onDismiss: () -> Unit, onSuccess: (AppUser) -> Unit) {
    var view by remember { mutableStateOf(initialView) }
    var name by remember { mutableStateOf("") }
    var identity by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = ColorInvezoPaper, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (view == "register") "Create account" else "Sign In", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ColorInvezoInk)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, null) }
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (view == "register") {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name", fontSize = 11.sp) }, singleLine = true, modifier = Modifier.fillMaxWidth().height(50.dp), shape = CircleShape)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                OutlinedTextField(value = identity, onValueChange = { identity = it }, label = { Text("Mobile number or Email", fontSize = 11.sp) }, singleLine = true, modifier = Modifier.fillMaxWidth().height(50.dp), shape = CircleShape)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password", fontSize = 11.sp) }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth().height(50.dp), shape = CircleShape)

                if (view == "register") {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = confirm, onValueChange = { confirm = it }, label = { Text("Confirm Password", fontSize = 11.sp) }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth().height(50.dp), shape = CircleShape)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val genUid = (100000..999999).random().toString()
                        val uName = if (name.isNotEmpty()) name else identity.substringBefore("@")
                        onSuccess(AppUser(uName.ifEmpty { "User" }, identity, genUid))
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = ColorLime)
                ) {
                    Text(if (view == "register") "Register" else "Login", color = ColorInvezoInk, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = { view = if (view == "register") "login" else "register" }) {
                    Text(if (view == "register") "Already have an account? Sign In" else "Create Account", color = ColorInvezoInk, fontSize = 11.sp)
                }
            }
        }
    }
}

// ----------------- MAIN DASHBOARD -----------------
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
        bottomBar = { MicroTaskBottomNavBar(onOpenRefer = onOpenRefer) },
        containerColor = ColorPhoneFrameBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // TOP HEADER
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).border(1.5.dp, ColorCyanAccent, RoundedCornerShape(11.dp)).background(Color.White)) {
                            AsyncImage(model = "https://www.shutterstock.com/image-vector/money-logo-design-vector-illustrative-260nw-2007522590.jpg", contentDescription = "Logo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        }
                        Spacer(modifier = Modifier.width(9.dp))
                        Column {
                            Row {
                                Text("Earning", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                                Text("Zone", color = ColorCyanAccent, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                            }
                            Text("Small Tasks Big Earnings", color = Color(0xFF9CB1CF), fontSize = 9.sp)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { Toast.makeText(context, "3টি নতুন নোটিফিকেশন আছে", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Outlined.Notifications, null, tint = Color.White)
                        }
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF0077FF), Color(0xFF00C6FF)))).clickable { onLogout() }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // BALANCE CARD (130deg Gradient)
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                    Column(modifier = Modifier.background(Brush.linearGradient(listOf(Color(0xFF00AAFF), Color(0xFF0051FF), Color(0xFF8E15EF)))).padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(Color.White.copy(0.22f)), contentAlignment = Alignment.Center) {
                                    AsyncImage(model = "https://img.icons8.com/?size=100&id=L0TH_5m8bo5r&format=png&color=ffffff", contentDescription = null, modifier = Modifier.size(26.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("My Balance", color = Color(0xFFD8ECFF), fontSize = 11.5.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = Color(0xFFC9E6FF), modifier = Modifier.size(14.dp).clickable { onToggleBalance() })
                                    }
                                    Text(if (isBalanceHidden) "$ ••••" else "$ 12.45", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                                    Text(if (isBalanceHidden) "≈ •••• BDT" else "≈ 1,240.00 BDT", color = Color(0xFFD6E2F5), fontSize = 10.sp)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(user.name, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cb.setPrimaryClip(ClipData.newPlainText("UID", user.uid))
                                    Toast.makeText(context, "UID Copied: ${user.uid}", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text("UID: ${user.uid}", color = Color(0xFFA8F0FF), fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ContentCopy, null, tint = Color.White, modifier = Modifier.size(11.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF061124)).padding(8.dp, 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Today: $2.35", color = Color(0xFF00D25B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Total: $42.80", color = ColorCyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Level: Silver", color = Color(0xFFA78BFA), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ACTIONS
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Button(onClick = onOpenDeposit, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B355)), modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(16.dp)) {
                        Text("Deposit", fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = { Toast.makeText(context, "Withdrawal coming soon", Toast.LENGTH_SHORT).show() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C1DF7)), modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(16.dp)) {
                        Text("Withdraw", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // TASKS (WATCH VIDEOS 4 PLATFORMS)
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ColorCardDarkBg)) {
                    Column(modifier = Modifier.padding(11.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Watch Videos & Earn", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            Text("$0.05 – $0.30", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(CircleShape).background(Color(0xFF009E47)).padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                        Spacer(modifier = Modifier.height(9.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            VideoBox("YouTube", Color(0xFFFF0000), Modifier.weight(1f))
                            VideoBox("Facebook", Color(0xFF1877F2), Modifier.weight(1f))
                            VideoBox("Instagram", Color(0xFFE1306C), Modifier.weight(1f))
                            VideoBox("TikTok", Color(0xFF010101), Modifier.weight(1f))
                        }
                    }
                }
            }

            item { TaskBox("Typing Task", "Type simple text & earn money", "$0.10 – $0.50", Color(0xFFC43BF0)) }
            item { TaskBox("Telegram Task", "Join our official channel & groups", "$0.20 – $1.00", Color(0xFF0066FF)) }
            item { Spacer(modifier = Modifier.height(10.dp)) }
        }
    }
}

@Composable
fun VideoBox(name: String, bg: Color, modifier: Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFF07182E)).padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(bg), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(name, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        Text("+ Earn", color = Color(0xFF00D25B), fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TaskBox(title: String, desc: String, badge: String, color: Color) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ColorCardDarkBg)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(desc, color = Color(0xFF839EC4), fontSize = 8.5.sp)
            }
            Text(badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(CircleShape).background(color).padding(horizontal = 8.dp, vertical = 3.dp))
        }
    }
}

@Composable
fun MicroTaskBottomNavBar(onOpenRefer: () -> Unit) {
    Surface(color = Color(0xFF020813), modifier = Modifier.fillMaxWidth().height(65.dp)) {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Home, null, tint = ColorCyanAccent)
            Icon(Icons.Default.Assignment, null, tint = Color(0xFFB757FF))
            Box(modifier = Modifier.size(46.dp).clip(CircleShape).background(Brush.linearGradient(listOf(ColorCyanAccent, Color(0xFF6F17E3)))), contentAlignment = Alignment.Center) {
                AsyncImage(model = "https://img.icons8.com/?size=100&id=YiP7pNVh3jYW&format=png&color=ffffff", contentDescription = null, modifier = Modifier.size(24.dp))
            }
            Icon(Icons.Default.History, null, tint = Color(0xFF00D25B))
            AsyncImage(model = "https://img.icons8.com/?size=100&id=G44HkjZjDMBJ&format=png&color=ffffff", contentDescription = null, modifier = Modifier.size(24.dp).clickable { onOpenRefer() })
        }
    }
}

// ----------------- DEPOSIT MODAL -----------------
@Composable
fun ModernDepositModalDialog(onDismiss: () -> Unit) {
    var method by remember { mutableStateOf("Nagad") }
    var trx by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF1E0A05), modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Deposit Funds", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { method = "Nagad" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00))) { Text("Nagad") }
                    Button(onClick = { method = "bKash" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2136E))) { Text("bKash") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Send Money: 01876543210 ($method)", color = ColorCyanAccent, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = trx, onValueChange = { trx = it }, label = { Text("Transaction ID") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D25B))) { Text("Submit Deposit") }
            }
        }
    }
}

// ----------------- REFER MODAL -----------------
@Composable
fun ReferralBonusModalDialog(uid: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val link = "https://earningzone.app/?user=$uid"

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(22.dp), color = Color.White, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Referral Bonus", color = Color(0xFF102D5F), fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Earn 5% bonus from your referred friends!", color = Color.Gray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = link, onValueChange = {}, readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("Refer", link))
                            Toast.makeText(context, "Link Copied!", Toast.LENGTH_SHORT).show()
                        }) { Icon(Icons.Default.ContentCopy, null) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102D5F))) { Text("Close") }
            }
        }
    }
}
