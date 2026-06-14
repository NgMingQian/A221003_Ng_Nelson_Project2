@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.a221003_ng_nelson_project2

import android.os.Bundle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.a221003_ng_nelson_project2.data.AppDatabase
import com.example.a221003_ng_nelson_project2.data.IncidentEntity
import com.example.a221003_ng_nelson_project2.firebase.FirebaseRepository
import com.example.a221003_ng_nelson_project2.location.LocationHelper
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.a221003_ng_nelson_project2.ui.theme.A221003_Ng_Nelson_Project2Theme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History

data class Incident(
    val username: String,
    val type: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    var username by mutableStateOf("")
        private set

    var incidentList = mutableStateListOf<Incident>()
        private set

    private val dao = AppDatabase.getDatabase(application).incidentDao()
    private val firebaseRepo = FirebaseRepository()
    init {
        loadIncidents()
    }
    fun updateUsername(name: String) {
        username = name
    }
    fun loadIncidents() {
        viewModelScope.launch {
            val data = dao.getAllIncidents()
            incidentList.clear()

            data.forEach {
                incidentList.add(
                    Incident(
                        username = it.username,
                        type = it.type,
                        description = it.description,
                        latitude = it.latitude,
                        longitude = it.longitude
                    )
                )
            }
        }
    }

    fun addIncident(
        type: String,
        description: String,
        latitude: Double,
        longitude: Double
    ) {

        val incident = Incident(username, type, description, latitude, longitude)

        val roomIncident = IncidentEntity(
            username = username,
            type = type,
            description = description,
            latitude = latitude,
            longitude = longitude
        )

        viewModelScope.launch {

            // 1. SAVE TO ROOM
            dao.insertIncident(roomIncident)

            // 2. SAVE TO FIREBASE
            firebaseRepo.uploadIncident(roomIncident)

            // 3. RELOAD DATA AFTER INSERT (IMPORTANT FIX)
            loadIncidents()
        }
    }
}

sealed class Screen(val route: String) {

    object Login : Screen("login")

    object Home : Screen("home")

    object Report : Screen("report")

    object Summary : Screen("summary")

    object Detail : Screen("detail")

    object Location : Screen("location")

    object Weather : Screen("weather")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            A221003_Ng_Nelson_Project2Theme {

                val navController = rememberNavController()
                val viewModel: MainViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Login.route,
                    modifier = Modifier.fillMaxSize()
                ) {

                    composable(Screen.Login.route) {
                        LoginScreen { username ->
                            viewModel.updateUsername(username)
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }

                    composable(Screen.Home.route) {
                        HomeScreen(
                            username = viewModel.username,
                            onReportClick = {
                                navController.navigate(Screen.Report.route)
                            },
                            navController = navController
                        )
                    }

                    composable(Screen.Location.route) {
                        LocationScreen()
                    }

                    composable(Screen.Weather.route) {
                        WeatherScreen()
                    }

                    composable(Screen.Report.route) {
                        ReportScreen(
                            viewModel = viewModel,
                            onDone = {
                                navController.navigate(Screen.Summary.route)
                            }
                        )
                    }

                    composable(Screen.Summary.route) {
                        SummaryScreen(viewModel) { index ->
                            navController.navigate("detail/$index")
                        }
                    }

                    composable("detail/{index}") { backStackEntry ->
                        val index = backStackEntry.arguments
                            ?.getString("index")
                            ?.toIntOrNull() ?: 0

                        DetailScreen(viewModel, index)
                    }
                }
            }
        }
    }

    @Composable
    fun LocationScreen() {

        val context = androidx.compose.ui.platform.LocalContext.current

        var lat by remember { mutableStateOf<Double?>(null) }
        var lon by remember { mutableStateOf<Double?>(null) }

        val locationHelper = remember {
            LocationHelper(context)
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { _ ->
            // optional
        }

        LaunchedEffect(Unit) {
            permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text("GPS Location", fontSize = 24.sp)

            Spacer(modifier = Modifier.height(20.dp))

            // ✅ FIX HERE (use lat/lon instead of latitude/longitude)
            Text("Latitude: ${lat ?: "Not set"}")
            Text("Longitude: ${lon ?: "Not set"}")

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    locationHelper.getCurrentLocation { latitude, longitude ->
                        lat = latitude
                        lon = longitude
                    }
                }
            ) {
                Text("Get Current Location")
            }
        }
    }

    @Composable
    fun WeatherScreen() {

        var temperature by remember {
            mutableStateOf("Loading...")
        }

        LaunchedEffect(Unit) {

            try {

                val response =
                    com.example.a221003_ng_nelson_project2.api.RetrofitClient
                        .api
                        .getWeather(
                            2.9300,
                            101.7800
                        )


                temperature =
                    "${response.current.temperature_2m} °C"

            } catch (e: Exception) {

                e.printStackTrace()

                temperature =
                    "Error: ${e.javaClass.simpleName}"
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                "Current Weather",
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                temperature,
                fontSize = 20.sp
            )
        }
    }

    @Composable
    fun LoginScreen(onLoginClick: (String) -> Unit) {

        var username by remember { mutableStateOf("") }

        Box(modifier = Modifier.fillMaxSize()) {

            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text("Community Safety Login", fontSize = 24.sp, color = Color.White)

                Spacer(modifier = Modifier.height(20.dp))

                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Enter Your Name") }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(onClick = {
                    onLoginClick(username)
                }) {
                    Text("Login")
                }
            }
        }
    }

    @Composable
    fun HomeScreen(
        username: String,
        onReportClick: () -> Unit,
        navController: androidx.navigation.NavController
    ) {

        Box(modifier = Modifier.fillMaxSize()) {

            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Spacer(modifier = Modifier.height(40.dp))

                // Welcome + History
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Welcome, $username!",
                        fontSize = 26.sp,
                        color = Color.White
                    )

                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Summary.route)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Report + GPS buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    Button(onClick = onReportClick) {
                        Text("Report Incident")
                    }

                    Button(
                        onClick = {
                            navController.navigate(Screen.Location.route)
                        }
                    ) {
                        Text("GPS Location")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Weather button
                Button(
                    onClick = {
                        navController.navigate(Screen.Weather.route)
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Check Weather")
                }

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "Safety Resources",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(15.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FeatureBox("Police", R.drawable.police)
                    FeatureBox("Hospital", R.drawable.hospital)
                }

                Spacer(modifier = Modifier.height(15.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FeatureBox("Fire Station", R.drawable.fire)
                    FeatureBox("Rela", R.drawable.rela)
                }

                Spacer(modifier = Modifier.height(15.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FeatureBox("Embassy", R.drawable.embassy)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    @Composable
    fun ReportScreen(viewModel: MainViewModel, onDone: () -> Unit) {

        val context = androidx.compose.ui.platform.LocalContext.current
        val locationHelper = remember { LocationHelper(context) }

        var description by remember { mutableStateOf("") }

        // ✅ FIX: use nullable so we know if GPS is ready
        var lat by remember { mutableStateOf<Double?>(null) }
        var lon by remember { mutableStateOf<Double?>(null) }

        val incidentTypes = listOf("Robbery", "Fire", "Accident")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Report Incident", fontSize = 20.sp)

            Spacer(modifier = Modifier.height(20.dp))

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Enter Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ✅ GET LOCATION BUTTON
            Button(
                onClick = {
                    locationHelper.getCurrentLocation { latitude, longitude ->
                        lat = latitude
                        lon = longitude
                    }
                }
            ) {
                Text(
                    if (lat == null) "Get Current Location"
                    else "Location Captured ✓"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Optional display (GOOD for demo marks)
            Text("Lat: ${lat ?: "Not set"}")
            Text("Lon: ${lon ?: "Not set"}")

            Spacer(modifier = Modifier.height(20.dp))

            // ❗ INCIDENT BUTTONS (FIXED SAFETY CHECK)
            incidentTypes.forEach { type ->

                Button(
                    onClick = {

                        // ✅ only allow if GPS is ready
                        if (lat != null && lon != null) {

                            viewModel.addIncident(
                                type,
                                description,
                                lat!!,
                                lon!!
                            )

                            description = ""
                            onDone()
                        }

                    },
                    enabled = lat != null && lon != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(type)
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    @Composable
    fun FeatureBox(title: String, imageRes: Int) {

        var expanded by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier.size(if (expanded) 150.dp else 130.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            onClick = { expanded = !expanded }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier.size(50.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(title)

                if (expanded) {
                    Text("Tap for help", fontSize = 12.sp)
                }
            }
        }
    }

    @Composable
    fun SummaryScreen(viewModel: MainViewModel, onItemClick: (Int) -> Unit) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Spacer(modifier = Modifier.height(10.dp))

            Text("Incident List", fontSize = 20.sp)

            Spacer(modifier = Modifier.height(20.dp))

            viewModel.incidentList.forEachIndexed { index, incident ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = CardDefaults.cardElevation(6.dp),
                    onClick = { onItemClick(index) }
                ) {

                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Type: ${incident.type}", color = Color.White)
                        Text("User: ${incident.username}", color = Color.White)
                        Text("Desc: ${incident.description}", color = Color.White)
                    }
                }
            }
        }
    }

    @Composable
    fun DetailScreen(viewModel: MainViewModel, index: Int) {

        val incident = viewModel.incidentList[index]

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text("Incident Detail", fontSize = 20.sp)

            Spacer(modifier = Modifier.height(10.dp))

            Text("User: ${incident.username}")
            Text("Type: ${incident.type}")
            Text("Description: ${incident.description}")

            Spacer(modifier = Modifier.height(10.dp))

            Text("Latitude: ${incident.latitude}")
            Text("Longitude: ${incident.longitude}")
        }
    }
}
