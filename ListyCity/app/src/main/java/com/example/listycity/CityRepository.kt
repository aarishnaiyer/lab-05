package com.example.listycity

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.FirebaseFirestore

class CityRepository {
    private val db = FirebaseFirestore.getInstance()
    private val citiesRef = db.collection("cities")

    private val _cities = mutableStateListOf<City>()

    val cities: List<City>
        get() = _cities

    init {
        citiesRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            _cities.clear()
            for (doc in snapshot.documents) {
                val name = doc.getString("name") ?: continue
                val province = doc.getString("province") ?: continue
                _cities.add(City(name, province))
            }
        }
    }

    fun addCity(city: City) {
        citiesRef.document(city.name)
            .set(mapOf("name" to city.name, "province" to city.province))
    }

    fun updateCity(oldCity: City, updatedCity: City) {
        val batch = db.batch()

        // The document ID is the city name, so a rename means
        // deleting the old document and writing a new one.
        if (oldCity.name != updatedCity.name) {
            batch.delete(citiesRef.document(oldCity.name))
        }
        batch.set(
            citiesRef.document(updatedCity.name),
            mapOf("name" to updatedCity.name, "province" to updatedCity.province)
        )
        batch.commit()
    }

    fun deleteCity(city: City) {
        citiesRef.document(city.name).delete()
    }
}