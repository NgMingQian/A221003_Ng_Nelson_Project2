package com.example.a221003_ng_nelson_project2.firebase

import com.example.a221003_ng_nelson_project2.data.IncidentEntity
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()
    private val weatherCollection = db.collection("community_weather")
    private val incidentCollection = db.collection("incidents")

    fun uploadWeather(city: String, temp: Double) {
        val data = hashMapOf(
            "city" to city,
            "temperature" to temp,
            "timestamp" to System.currentTimeMillis()
        )

        weatherCollection.add(data)
    }

    fun uploadIncident(incident: IncidentEntity) {
        val data = hashMapOf(
            "username" to incident.username,
            "type" to incident.type,
            "description" to incident.description,
            "latitude" to incident.latitude,
            "longitude" to incident.longitude,
            "timestamp" to System.currentTimeMillis()
        )

        incidentCollection.add(data)
            .addOnSuccessListener {
                android.util.Log.d("FIREBASE", "Upload successful")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("FIREBASE", "Upload failed: ${e.message}", e)
            }
    }
}