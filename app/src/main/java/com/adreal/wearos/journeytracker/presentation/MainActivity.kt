/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.adreal.wearos.journeytracker.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.adreal.wearos.journeytracker.R
import com.adreal.wearos.journeytracker.presentation.model.JourneyModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)
        SharedPreferences.init(this)

        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = Constants.HOME_SCREEN,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { 1000 },
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = FastOutSlowInEasing // Apply easing for smoother motion
                        )
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -1000 },
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = FastOutSlowInEasing
                        )
                    )
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -1000 },
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = FastOutSlowInEasing
                        )
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { 1000 },
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
            ) {
                composable(Constants.HOME_SCREEN) { DisplayRideButtons(navController) }
                composable(
                    Constants.TIMER_SCREEN,
                    arguments = listOf(navArgument(Constants.COMMUTE_TYPE) {
                        defaultValue = Constants.DEFAULT
                    })
                ) { backStackEntry ->
                    TimerScreen(
                        backStackEntry.arguments?.getInt(Constants.COMMUTE_TYPE)
                            ?: Constants.DEFAULT, navController
                    )
                }
                composable(Constants.RECORDS) { DisplayRecords() }
            }
        }
    }
}

@Composable
fun DisplayRideButtons(navController: NavController) {

    LaunchedEffect(Unit) {
        if (isIncompleteJourney()) {
            Log.d("JourneyTracker", "Previous journey is incomplete")
            val commute = SharedPreferences.read("${SharedPreferences.read("id", 0)}_commute", 0)
            navController.navigate("${Constants.TIMER}/${commute}")
        }
    }

    Log.d("JourneyTracker", "Previous journey is completed")
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { navController.navigate("${Constants.TIMER}/${Constants.MOTORCYCLE}") },
                    modifier = Modifier
                        .height(60.dp)
                        .width(60.dp),
                    backgroundColor = Color.DarkGray
                ) {
                    Icon(
                        modifier = Modifier.padding(10.dp),
                        painter = painterResource(id = R.drawable.motorcycle),
                        contentDescription = "Motorcycle"
                    )
                }
                FloatingActionButton(
                    onClick = { navController.navigate("${Constants.TIMER}/${Constants.CYCLE}") },
                    modifier = Modifier
                        .height(60.dp)
                        .width(60.dp),
                    backgroundColor = Color.DarkGray
                ) {
                    Icon(
                        modifier = Modifier.padding(10.dp),
                        painter = painterResource(id = R.drawable.cycle),
                        contentDescription = "Cycle"
                    )
                }
                FloatingActionButton(
                    onClick = { navController.navigate(Constants.RECORDS) },
                    modifier = Modifier
                        .height(60.dp)
                        .width(60.dp),
                    backgroundColor = Color.DarkGray
                ) {
                    Icon(
                        modifier = Modifier.padding(10.dp),
                        painter = painterResource(id = R.drawable.analytics),
                        contentDescription = "Cycle"
                    )
                }
            }
        }
    }
}

fun getTapCount(): Int {
    val id = SharedPreferences.read("id", 0)
    val timestamps = SharedPreferences.read("${id}_timestamps", "").toString()
        .split(",")
        .filter { it.isNotEmpty() }

    Log.d("JourneyTracker", "Timestamp size for tap count: ${timestamps.size}")

    return timestamps.size
}

@Composable
fun TimerScreen(commuteType: Int, navController: NavController) {
    var tapCount by remember { mutableIntStateOf(getTapCount()) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var backgroundColor by remember {
        mutableStateOf(
            getCurrentBackgroundColor(
                commuteType,
                tapCount
            )
        )
    }
    var textColor by remember { mutableStateOf(getTextColor(backgroundColor)) }

    LaunchedEffect(tapCount) {
        // Update UI or perform other actions when tapCount changes
        backgroundColor = getCurrentBackgroundColor(commuteType, tapCount)
        textColor = getTextColor(backgroundColor)
    }

    LaunchedEffect(Unit) {
        if (tapCount == 0) {
            tapCount++
            insertTimeStamp(commuteType)
        }
        while (true) {
            delay(100)
            elapsedSeconds = ((System.currentTimeMillis() - getLatestTimestamp()) / 1000).toInt()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        tapCount++
                        if (isTripEnded(commuteType, tapCount)) {
                            insertTimeStamp(commuteType, 1)
                            navController.navigate(Constants.RECORDS)
                        } else {
                            insertTimeStamp(commuteType)
                            elapsedSeconds = 0
                        }
                    }
                )
            }
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = formatTime(totalSeconds = elapsedSeconds),
            textAlign = TextAlign.Center,
            fontSize = 40.sp,
            color = textColor
        )
    }
}

fun getLatestTimestamp(): Long {
    val id = SharedPreferences.read("id", 0)
    val latestTimestamp =
        SharedPreferences.read("${id}_timestamps", System.currentTimeMillis().toString())
            .toString()
            .split(",").last().toLong()

    return latestTimestamp
}

@Composable
fun formatTime(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

fun isTripEnded(commuteType: Int, tapCount: Int): Boolean {
    return when (commuteType) {
        Constants.MOTORCYCLE -> tapCount >= 3
        Constants.CYCLE -> tapCount >= 4
        else -> false
    }
}

fun getCurrentBackgroundColor(commuteType: Int, tapCount: Int): Color {
    when (commuteType) {
        Constants.MOTORCYCLE -> {
            return when (tapCount) {
                1 -> Color.Blue
                2 -> Color.Yellow
                else -> Color.Red
            }
        }

        Constants.CYCLE -> {
            return when (tapCount) {
                1 -> Color.Yellow
                2 -> Color.Blue
                3 -> Color.Yellow
                else -> Color.Red
            }
        }
    }

    return Color.DarkGray
}

fun getTextColor(backgroundColor: Color): Color {
    // Choose a contrasting text color based on background color brightness
    return if (backgroundColor.luminance() > 0.5f) {
        Color.Black
    } else {
        Color.White
    }
}

fun insertTimeStamp(commuteType: Int, isFinished: Int = 0) {
    val id = SharedPreferences.read("id", 0)

    if (SharedPreferences.read("${id}_commute", 0) == 0) {
        SharedPreferences.write("${id}_commute", commuteType)
    }

    val timestampsList = SharedPreferences.read("${id}_timestamps", "").toString()

    if (timestampsList.isEmpty()) {
        SharedPreferences.write("${id}_timestamps", System.currentTimeMillis().toString())
    } else {
        SharedPreferences.write(
            "${id}_timestamps",
            "${timestampsList},${System.currentTimeMillis()}"
        )
    }

    if (isFinished == 1) {
        SharedPreferences.write("id", id + 1)
    }
}

@Composable
fun DisplayRecords() {
    val id = SharedPreferences.read("id", 0)
    val records = mutableListOf<JourneyModel>()

    for (i in (id - 1) downTo 0) {
        val commute = SharedPreferences.read("${i}_commute", 0)
        val timestamps = SharedPreferences.read("${i}_timestamps", "").toString().split(",")
        records.add(JourneyModel(i, commute, timestamps.map { it.toLong() }))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(records) { record ->
            JourneyCard(record)
        }
    }
}

@Composable
fun JourneyCard(journey: JourneyModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(11.dp) // Rounded corners with 11.dp radius
    ) {
        Column(
            modifier = Modifier
                .background(Color.DarkGray)
                .padding(11.dp)
        ) {
            val endTime = formatTimestamp(journey.timestamps.lastOrNull())
            val totalTime = calculateTotalTime(journey.timestamps)

            Text(
                text = if (journey.commute == Constants.MOTORCYCLE) "Motorcycle" else "Cycle",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (journey.commute == Constants.MOTORCYCLE) {
                Text(
                    text = "Riding Started",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(journey.timestamps[0]),
                    fontSize = 11.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Riding Finished / Walking Started",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(journey.timestamps[1]),
                    fontSize = 11.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Walking Finished",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = endTime,
                    fontSize = 11.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Journey Time",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = totalTime,
                    fontSize = 11.sp,
                    color = Color.White
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Walking Started",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(journey.timestamps[0]),
                    fontSize = 11.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Walking Finished / Riding Started",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(journey.timestamps[1]),
                    fontSize = 11.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Riding Finished / Walking Started",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(journey.timestamps[2]),
                    fontSize = 11.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Walking Finished",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(journey.timestamps[3]),
                    fontSize = 11.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Journey Time",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = totalTime,
                    fontSize = 11.sp,
                    color = Color.White
                )
            }
        }
    }
}


fun formatTimestamp(timestamp: Long?): String {
    return if (timestamp != null) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.format(Date(timestamp))
    } else {
        "N/A"
    }
}

fun isIncompleteJourney(): Boolean {
    val id = SharedPreferences.read("id", 0)
    val timestamps = SharedPreferences.read("${id}_timestamps", "").toString()
        .split(",")
        .filter { it.isNotEmpty() }

    val commuteType = SharedPreferences.read("${id}_commute", 0)

    Log.d("JourneyTracker", "Timestamp size: ${timestamps.size}")
    Log.d("timestamp value", timestamps.toString())

    if (timestamps.isEmpty()) {
        return false
    } else if (commuteType == Constants.MOTORCYCLE && timestamps.size < 3) {
        return true
    } else if (commuteType == Constants.CYCLE && timestamps.size < 4) {
        return true
    }

    return false
}

fun calculateTotalTime(timestamps: List<Long>): String {
    return if (timestamps.isNotEmpty()) {
        val totalTimeMillis = timestamps.last() - timestamps.first()
        val seconds = (totalTimeMillis / 1000) % 60
        val minutes = (totalTimeMillis / (1000 * 60)) % 60
        val hours = (totalTimeMillis / (1000 * 60 * 60)) % 24
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        "N/A"
    }
}

