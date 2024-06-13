/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.adreal.wearos.journeytracker.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

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
                    SecondScreen(
                        backStackEntry.arguments?.getInt(Constants.COMMUTE_TYPE)
                            ?: Constants.DEFAULT, navController
                    )
                }
            }
        }
    }
}

@Composable
fun DisplayRideButtons(navController: NavController) {
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
            }
        }
    }
}

@Composable
fun SecondScreen(commuteType: Int, navController: NavController) {
    var tapCount by remember { mutableIntStateOf(0) }
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

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedSeconds++
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
                            navController.popBackStack()
                        } else {
                            elapsedSeconds = 0
                            backgroundColor = getCurrentBackgroundColor(commuteType, tapCount)
                            textColor = getTextColor(backgroundColor)
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

@Composable
fun formatTime(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

fun isTripEnded(commuteType: Int, tapCount: Int): Boolean {
    return when (commuteType) {
        Constants.MOTORCYCLE -> tapCount >= 2
        Constants.CYCLE -> tapCount >= 3
        else -> false
    }
}

fun getCurrentBackgroundColor(commuteType: Int, tapCount: Int): Color {
    when (commuteType) {
        Constants.MOTORCYCLE -> {
            return when (tapCount) {
                0 -> Color.Blue
                1 -> Color.Yellow
                else -> Color.Red
            }
        }

        Constants.CYCLE -> {
            return when (tapCount) {
                0 -> Color.Yellow
                1 -> Color.Blue
                2 -> Color.Yellow
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

