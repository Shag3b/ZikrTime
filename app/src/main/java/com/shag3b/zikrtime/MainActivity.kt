package com.shag3b.zikrtime

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.*
import com.shag3b.zikrtime.notification.NotificationPermissionManager
import com.shag3b.zikrtime.notification.ZikrNotificationHelper
import com.shag3b.zikrtime.ui.permission.NotificationPermissionDialog
import com.shag3b.zikrtime.ui.permission.NotificationsDisabledScreen
import com.shag3b.zikrtime.ui.permission.PermissionPermanentlyDeniedDialog
import com.shag3b.zikrtime.ui.screens.DoneScreen
import com.shag3b.zikrtime.ui.screens.HomeScreen
import com.shag3b.zikrtime.ui.screens.LanguageSelectionScreen
import com.shag3b.zikrtime.ui.screens.ZikrScreen
import com.shag3b.zikrtime.utils.LocationManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    // Notification permission launcher (Android 13+) - internal so it can be accessed from composables
    internal val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        handleNotificationPermissionResult(isGranted)
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false

        if (coarseGranted || fineGranted) {
            // Permission granted, fetch location
            fetchLocation()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val language = newBase.getSharedPreferences("zikr_prefs", Context.MODE_PRIVATE)
            .getString("app_language", "ar") ?: "ar"

        val locale = Locale.forLanguageTag(language)
        Locale.setDefault(locale)

        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)

        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ZikrNotificationHelper.createChannels(this)

        // SIMPLE FIRST-TIME SETUP FIX:
        // The app will NEVER have both periods FALSE at the same time under normal operation
        // If both are FALSE → first install, reinstall, or data clear → set both to TRUE
        val prefs = getSharedPreferences("zikr_prefs", Context.MODE_PRIVATE)
        val morningCompleted = prefs.getBoolean("morningCompleted", false)
        val eveningCompleted = prefs.getBoolean("eveningCompleted", false)

        var isFirstTimeSetup = false
        if (!morningCompleted && !eveningCompleted) {
            prefs.edit()
                .putBoolean("morningCompleted", true)
                .putBoolean("eveningCompleted", true)
                .putLong("first_setup_time", System.currentTimeMillis())
                .apply()
            isFirstTimeSetup = true
        }

        // Schedule reset alarms for prayer times
        BootReceiver.scheduleResetAlarms(this)

        // Schedule WorkManager fallback (checks every 15 minutes if AlarmManager fails)
        com.shag3b.zikrtime.worker.WorkManagerScheduler.scheduleFallbackChecks(this)

        // CRITICAL: Check SCHEDULE_EXACT_ALARM permission on Android 12+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    val intent = android.content.Intent(
                        android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        android.net.Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                } catch (e: Exception) {
                    // Silently handle - not critical
                }
            }
        }

        // Request location permission if not granted
        requestLocationPermissionIfNeeded()

        // CRITICAL: Skip notification logic on first-time setup
        // This prevents race condition where reset alarms fire immediately after setup
        // and change the completion status before notification check runs
        if (isFirstTimeSetup) {
            setContent {
                ZikrApp()
            }
            return
        }


        // Show notification and start reminders if it's azkar time and not completed
        val shouldOpenMorning = TimeChecker.shouldOpenMorning(this)
        val shouldOpenEvening = TimeChecker.shouldOpenEvening(this)

        if (shouldOpenMorning || shouldOpenEvening) {
            // FIX: Determine which period we're ACTUALLY in, not just default to morning
            // Check the current time period first, then completion status
            val isMorning = when {
                TimeChecker.isInEveningPeriod(this) && !DailyStateManager.isEveningDone(this) -> false // Evening period active
                TimeChecker.isInMorningPeriod(this) && !DailyStateManager.isMorningDone(this) -> true  // Morning period active
                shouldOpenMorning -> true  // Fallback to morning if both somehow true
                else -> false              // Fallback to evening
            }

            val prefs = getSharedPreferences("zikr_prefs", Context.MODE_PRIVATE)
            val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

            // Check if initial notification was shown today
            val initialNotifKey = if (isMorning) "initial_notif_morning" else "initial_notif_evening"
            val lastInitialNotif = prefs.getString(initialNotifKey, "")

            if (lastInitialNotif != today) {
                // First time opening app during this period today - show initial notification
                ZikrNotificationHelper.showInitialPeriodNotification(this, isMorning)
                prefs.edit().putString(initialNotifKey, today).apply()
            } else {
                // Already shown initial notification - show ongoing
                ZikrNotificationHelper.showOngoingNotification(this, isMorning)
            }

            // Start reminder scheduler only once per day per period
            val scheduledKey = if (shouldOpenMorning) "scheduled_morning" else "scheduled_evening"
            val lastScheduled = prefs.getString(scheduledKey, "")

            // Only schedule if not already scheduled today
            if (lastScheduled != today) {
                if (shouldOpenMorning) {
                    ReminderScheduler.scheduleMorningReminders(this)
                } else {
                    ReminderScheduler.scheduleEveningReminders(this)
                }
                prefs.edit().putString(scheduledKey, today).apply()
            }
        }

        setContent {
            ZikrApp()
        }
    }

    private fun requestLocationPermissionIfNeeded() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        } else {
            // Already have permission, fetch location
            fetchLocation()
        }
    }

    private fun fetchLocation() {
        // Launch coroutine to fetch location asynchronously
        @optIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch {
            try {
                LocationManager.getLocation(this@MainActivity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Handle notification permission result
     */
    private fun handleNotificationPermissionResult(isGranted: Boolean) {
        // Permission state will be handled by ZikrApp composable
    }
}

@Composable
fun ZikrApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = context as? MainActivity
    val lifecycleOwner = LocalLifecycleOwner.current

    // Notification permission state
    var hasNotificationPermission by remember {
        mutableStateOf(NotificationPermissionManager.hasNotificationPermission(context))
    }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showPermanentlyDeniedDialog by remember { mutableStateOf(false) }
    var showBlockingScreen by remember { mutableStateOf(false) }

    // Re-check permission when app resumes (e.g., returning from settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val permissionGranted = NotificationPermissionManager.hasNotificationPermission(context)
                hasNotificationPermission = permissionGranted

                if (permissionGranted) {
                    // Permission now granted - hide all dialogs
                    showPermissionDialog = false
                    showPermanentlyDeniedDialog = false
                    showBlockingScreen = false
                } else {
                    // Still no permission - update blocking screen state
                    showBlockingScreen = !NotificationPermissionManager.areNotificationsEnabled(context)
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Check permission on initial composition
    LaunchedEffect(Unit) {
        // Check if notifications are enabled
        val permissionGranted = NotificationPermissionManager.hasNotificationPermission(context)
        hasNotificationPermission = permissionGranted

        if (!permissionGranted) {
            // Check if this is first launch or if permission was permanently denied
            if (activity != null) {
                val isPermanentlyDenied = NotificationPermissionManager.isPermissionPermanentlyDenied(activity)

                if (isPermanentlyDenied) {
                    showPermanentlyDeniedDialog = true
                } else if (NotificationPermissionManager.isFirstPermissionCheck(context)) {
                    // First time - show rationale
                    showPermissionDialog = true
                } else {
                    // User came back from settings or denied before
                    showBlockingScreen = !NotificationPermissionManager.areNotificationsEnabled(context)
                }
            }
        }
    }

    // Show appropriate UI based on permission state
    when {
        showBlockingScreen && !hasNotificationPermission -> {
            // Blocking screen when notifications are disabled
            NotificationsDisabledScreen(
                onOpenSettingsClick = {
                    NotificationPermissionManager.openNotificationSettings(context)
                }
            )
        }

        showPermanentlyDeniedDialog -> {
            // Permission permanently denied
            PermissionPermanentlyDeniedDialog(
                onOpenSettingsClick = {
                    showPermanentlyDeniedDialog = false
                    NotificationPermissionManager.openNotificationSettings(context)
                    showBlockingScreen = true
                },
                onExitClick = {
                    activity?.finish()
                }
            )
        }

        showPermissionDialog -> {
            // First time permission request
            NotificationPermissionDialog(
                onEnableClick = {
                    showPermissionDialog = false
                    if (activity != null) {
                        NotificationPermissionManager.requestNotificationPermission(
                            activity,
                            activity.notificationPermissionLauncher
                        )
                    }
                },
                onExitClick = {
                    activity?.finish()
                }
            )
        }

        hasNotificationPermission -> {
            // Permission granted - show normal app
            ZikrAppContent(navController, context)
        }

        else -> {
            // Loading or checking permission
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun ZikrAppContent(navController: androidx.navigation.NavHostController, context: Context) {
    // Determine start destination - evaluate fresh each time
    // Do NOT use remember {} here as we want it to re-evaluate when activity recreates
    val startDestination = if (!FirstLaunchManager.isLanguageSelected(context)) {
        "lang"
    } else {
        when {
            TimeChecker.isInMorningPeriod(context) && !DailyStateManager.isMorningDone(context) -> "zikr_morning"
            TimeChecker.isInEveningPeriod(context) && !DailyStateManager.isEveningDone(context) -> "zikr_evening"
            else -> "home"
        }
    }


    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable("lang") {
            LanguageSelectionScreen {
                navController.navigate("home") {
                    popUpTo("lang") { inclusive = true }
                }
            }
        }

        composable("home") {
            HomeScreen(
                onMorningClick = { navController.navigate("zikr_morning") },
                onEveningClick = { navController.navigate("zikr_evening") }
            )
        }

        composable("zikr_morning") {
            ZikrScreen(
                isMorning = true,
                onFinished = {
                    // Mark as completed
                    DailyStateManager.markMorningDone(context)

                    // Update statistics (only counts once per day)
                    StatsManager.onMorningCompleted(context)

                    // Clear saved progress
                    AzkarProgressManager.clearMorningProgress(context)

                    // Cancel all notifications
                    ZikrNotificationHelper.cancelAllNotifications(context)

                    // Stop reminders
                    ReminderScheduler.cancelMorningReminders(context)

                    navController.navigate("done_morning")
                }
            )
        }

        composable("zikr_evening") {
            ZikrScreen(
                isMorning = false,
                onFinished = {
                    // Mark as completed
                    DailyStateManager.markEveningDone(context)

                    // Update statistics (only counts once per day)
                    StatsManager.onEveningCompleted(context)

                    // Clear saved progress
                    AzkarProgressManager.clearEveningProgress(context)

                    // Cancel all notifications
                    ZikrNotificationHelper.cancelAllNotifications(context)

                    // Stop reminders
                    ReminderScheduler.cancelEveningReminders(context)

                    navController.navigate("done_evening")
                }
            )
        }

        composable("done_morning") {
            DoneScreen(
                isMorning = true,
                onBack = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("done_evening") {
            DoneScreen(
                isMorning = false,
                onBack = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
