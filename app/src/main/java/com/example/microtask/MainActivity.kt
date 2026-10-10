package com.example.microtask

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ব্যাকগ্রাউন্ডে নোটিফিকেশন পাঠানোর শিডিউলার চালু
        scheduleBackgroundNotifications(this)

        setContent {
            MicroTaskApp()
        }
    }
}

// ================= THEME COLORS =================
val AppDarkBg = Color(0xFF020713)
val CardDarkBg = Color(0xFF061324)
val CyanAccent = Color(0xFF00D2FF)
val PrimaryBlue = Color(0xFF0066FF)
val SuccessGreen = Color(0xFF00D25B)
val PurpleAccent = Color(0xFF7B2CBF)
val InvezoLime = Color(0xFFD8FF38)
val InvezoPaper = Color(0xFFF8F8F7)

data class UserSession(val name: String, val identity: String, val uid: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicroTaskApp() {
    val context = LocalContext.current
    var userSession by remember { mutableStateOf<UserSession?>(null) }
    var isAuthSheetOpen by remember { mutableStateOf(false) }
    var authSheetView by remember { mutableStateOf("register") }

    var isBalanceHidden by remember { mutableStateOf(false) }
    var showDepositModal by remember { mutableStateOf(false) }
    var showReferModal by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppDarkBg)
    ) {
        if (userSession == null) {
            InvezoAuthScreen(
                onOpenSheet = { view ->
                    authSheetView = view
                    isAuthSheetOpen = true
                }
            )
        } else {
            MainDashboard(
                user = userSession!!,
                isBalanceHidden = isBalanceHidden,
                onToggleBalance = { isBalanceHidden = !isBalanceHidden },
                onLogout = {
                    userSession = null
                    Toast.makeText(context, "সফলভাবে লগআউট হয়েছে", Toast.LENGTH_SHORT).show()
                },
                onOpenDeposit = { showDepositModal = true },
                onOpenRefer = { showReferModal = true }
            )
        }

        if (isAuthSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { isAuthSheetOpen = false },
                containerColor = InvezoPaper,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                InvezoAuthSheetContent(
                    initialView = authSheetView,
                    onSuccess = { user ->
                        userSession = user
                        isAuthSheetOpen = false
                        Toast.makeText(context, "স্বাগতম, ${user.name}!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        if (showDepositModal) {
            DepositModalDialog(onDismiss = { showDepositModal = false })
        }

        if (showReferModal) {
            ReferModalDialog(
                uid = userSession?.uid ?: "784219",
                onDismiss = { showReferModal = false }
            )
        }
    }
}

// ----------------- INVEZO AUTH -----------------
@Composable
fun InvezoAuthScreen(onOpenSheet: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(InvezoPaper)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text("invezo.", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF101010), modifier = Modifier.padding(top = 16.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.TrendingUp, null, modifier = Modifier.size(80.dp), tint = Color(0xFF101010))
            Spacer(modifier = Modifier.height(20.dp))
            Text("Let's find good\nthings together.", fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Color(0xFF101010))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Start your journey with a simple, secure MicroTask account.", fontSize = 13.sp, textAlign = TextAlign.Center, color = Color(0xFF555555))
        }

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { onOpenSheet("register") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = InvezoLime),
                shape = RoundedCornerShape(50)
            ) {
                Text("Create Account", color = Color(0xFF101010), fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = { onOpenSheet("login") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text("Sign In", color = Color(0xFF101010), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InvezoAuthSheetContent(initialView: String, onSuccess: (UserSession) -> Unit) {
    var currentView by remember { mutableStateOf(initialView) }
    var name by remember { mutableStateOf("") }
    var identity by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(if (currentView == "register") "Create your account" else "Welcome back", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF101010))
        Spacer(modifier = Modifier.height(12.dp))

        if (currentView == "register") {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50))
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(value = identity, onValueChange = { identity = it }, label = { Text("Mobile number or Gmail") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { IconButton(onClick = { showPassword = !showPassword }) { Icon(if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff, null) } },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50)
        )

        if (currentView == "register") {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirm Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50))
        }

        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = {
                val genUid = (100000..999999).random().toString()
                val uName = if (name.isNotEmpty()) name else identity.substringBefore("@")
                onSuccess(UserSession(uName.ifEmpty { "Tanvir Ahmed" }, identity, genUid))
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = InvezoLime),
            shape = RoundedCornerShape(50)
        ) {
            Text(if (currentView == "register") "Create Account" else "Sign In", color = Color(0xFF101010), fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = { currentView = if (currentView == "register") "login" else "register" }) {
            Text(if (currentView == "register") "Already have an account? Sign In" else "New here? Create Account", color = Color(0xFF101010))
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ----------------- MAIN DASHBOARD -----------------
@Composable
fun MainDashboard(
    user: UserSession,
    isBalanceHidden: Boolean,
    onToggleBalance: () -> Unit,
    onLogout: () -> Unit,
    onOpenDeposit: () -> Unit,
    onOpenRefer: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        bottomBar = {
            BottomBar(onOpenRefer = onOpenRefer)
        },
        containerColor = AppDarkBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(Color.White), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MonetizationOn, null, tint = Color(0xFF00B074))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row {
                                Text("Micro", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("Task", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Text("Small Tasks Big Earnings", color = Color(0xFF8FA5C4), fontSize = 9.sp)
                        }
                    }
                    Row {
                        IconButton(onClick = { Toast.makeText(context, "3টি নতুন নোটিফিকেশন!", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.Notifications, null, tint = Color.White)
                        }
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.AccountCircle, null, tint = CyanAccent, modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }

            // BALANCE CARD
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
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(0.2f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("My Balance", color = Color(0xFFD8ECFF), fontSize = 11.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            null, tint = Color.White, modifier = Modifier.size(15.dp).clickable { onToggleBalance() }
                                        )
                                    }
                                    Text(if (isBalanceHidden) "$ ••••" else "$ 12.45", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                    Text(if (isBalanceHidden) "≈ •••• BDT" else "≈ 1,240.00 BDT", color = Color(0xFFD6E2F5), fontSize = 10.sp)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(user.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        cb.setPrimaryClip(ClipData.newPlainText("UID", user.uid))
                                        Toast.makeText(context, "UID Copied: ${user.uid}", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text("UID: ${user.uid}", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ContentCopy, null, tint = Color.White, modifier = Modifier.size(11.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF061124).copy(0.7f)).padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Text("Today: $2.35", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Total: $42.80", color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Level: Silver", color = PurpleAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ACTIONS
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onOpenDeposit,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B074)),
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Deposit") }
                    Button(
                        onClick = { Toast.makeText(context, "Cash Out coming soon!", Toast.LENGTH_SHORT).show() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B2CBF)),
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Withdraw") }
                }
            }

            // TASKS
            item {
                TaskItem(title = "Watch Videos & Earn", desc = "YouTube, Facebook, TikTok", badge = "$0.05 - $0.30", color = CyanAccent)
            }
            item {
                TaskItem(title = "Typing Task", desc = "Type simple text & earn money", badge = "$0.10 - $0.50", color = PurpleAccent)
            }
            item {
                TaskItem(title = "Telegram Task", desc = "Join official community channel", badge = "$0.20 - $1.00", color = PrimaryBlue)
            }
        }
    }
}

@Composable
fun TaskItem(title: String, desc: String, badge: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(desc, color = Color(0xFF839EC4), fontSize = 9.sp)
            }
            Text(badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(color).padding(horizontal = 8.dp, vertical = 4.dp))
        }
    }
}

@Composable
fun BottomBar(onOpenRefer: () -> Unit) {
    Surface(color = Color(0xFF020813), modifier = Modifier.fillMaxWidth().height(65.dp)) {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Home, null, tint = CyanAccent)
            Icon(Icons.Default.Assignment, null, tint = Color(0xFFB757FF))
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(CyanAccent), contentAlignment = Alignment.Center) {
                // সংশোধনকৃত Headphones আইকন
                Icon(Icons.Default.Headphones, null, tint = Color.Black)
            }
            Icon(Icons.Default.History, null, tint = SuccessGreen)
            Icon(Icons.Default.Group, null, tint = Color(0xFFFF9800), modifier = Modifier.clickable { onOpenRefer() })
        }
    }
}

// ----------------- DEPOSIT MODAL -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositModalDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var selectedMethod by remember { mutableStateOf("Nagad") }
    var trxId by remember { mutableStateOf("") }

    AlertDialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF1E0A05), modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Deposit Funds", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { selectedMethod = "Nagad" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00))) { Text("Nagad") }
                    Button(onClick = { selectedMethod = "bKash" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2136E))) { Text("bKash") }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Send Money: 01876543210 ($selectedMethod)", color = CyanAccent, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = trxId, onValueChange = { trxId = it }, label = { Text("Transaction ID (TrxID)") })
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (trxId.length >= 6) {
                            Toast.makeText(context, "Deposit Submitted! Pending verification.", Toast.LENGTH_LONG).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, "সঠিক TrxID দিন!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) { Text("Submit Deposit") }
            }
        }
    }
}

// ----------------- REFER MODAL -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferModalDialog(uid: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val link = "https://microtask.app/?user=$uid"

    AlertDialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White, modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Referral Bonus", color = Color(0xFF102D5F), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Earn 5% bonus from your referred friends!", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = link, onValueChange = {}, readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("Refer", link))
                            Toast.makeText(context, "Link Copied!", Toast.LENGTH_SHORT).show()
                        }) { Icon(Icons.Default.ContentCopy, null) }
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102D5F))) { Text("Close") }
            }
        }
    }
}

// ================= ব্যাকগ্রাউন্ড নোটিফিকেশন সিস্টেম =================
class BackgroundNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        showBackgroundNotification(context)
        return Result.success()
    }

    private fun showBackgroundNotification(context: Context) {
        val channelId = "microtask_channel_id"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "MicroTask Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Task & Earnings Notification"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("🎉 নতুন টাস্ক যোগ হয়েছে!")
            .setContentText("আজকের ভিডিও ও টাইপিং টাস্ক সম্পন্ন করে ব্যালেন্স বাড়িয়ে নিন।")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

fun scheduleBackgroundNotifications(context: Context) {
    val notificationRequest = PeriodicWorkRequestBuilder<BackgroundNotificationWorker>(
        2, TimeUnit.HOURS
    ).build()

    WorkManager.getInstance(context).enqueue(notificationRequest)
}
