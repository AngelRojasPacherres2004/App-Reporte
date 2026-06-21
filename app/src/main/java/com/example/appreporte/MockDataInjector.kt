package com.example.appreporte

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object MockDataInjector {
    fun injectData() {
        val db = FirebaseFirestore.getInstance()
        insertCleanData(db)
    }

    private fun insertCleanData(db: FirebaseFirestore) {
        // 1. Colegios
        val schools = listOf(
            mapOf("name" to "Colegio San José", "levels" to listOf("Primaria", "Secundaria"), "adminEmail" to "admin@sanjose.com"),
            mapOf("name" to "Colegio Santa María", "levels" to listOf("Primaria", "Secundaria"), "adminEmail" to "admin@santamaria.com")
        )
        for (school in schools) {
            val name = school["name"] as String
            db.collection("colegios").document(name).set(school, SetOptions.merge())
        }

        // 2. Classrooms
        val classrooms = listOf(
            mapOf("id" to "sanjose_1er_a", "name" to "1er Grado A - Primaria", "school_id" to "Colegio San José", "tutor" to "docente@sanjose.com"),
            mapOf("id" to "sanjose_2do_a", "name" to "2do Grado A - Primaria", "school_id" to "Colegio San José", "tutor" to "docente@sanjose.com"),
            mapOf("id" to "santamaria_1er_a", "name" to "1er Grado A - Primaria", "school_id" to "Colegio Santa María", "tutor" to "admin@santamaria.com")
        )
        for (c in classrooms) {
            val id = c["id"] as String
            db.collection("classrooms").document(id).set(c, SetOptions.merge())
        }

        // 3. Users
        val users = listOf(
            mapOf("email" to "admin@sanjose.com", "password" to "admin123", "rol" to "admin", "school_id" to "Colegio San José"),
            mapOf("email" to "docente@sanjose.com", "password" to "docente123", "rol" to "docente", "school_id" to "Colegio San José", "classrooms" to listOf("sanjose_1er_a", "sanjose_2do_a")),
            mapOf("email" to "padre@sanjose.com", "password" to "padre123", "rol" to "usuario", "school_id" to "Colegio San José", "phone" to "+51987654321")
        )
        for (u in users) {
            val email = u["email"] as String
            db.collection("users").document(email).set(u, SetOptions.merge())
        }

        // 4. Students
        val students = listOf(
            mapOf("id" to "student_mateo", "names" to "Mateo", "lastnames" to "Rojas", "dni" to "70001001", "classroom_id" to "sanjose_1er_a", "parent_email" to "padre@sanjose.com", "school_id" to "Colegio San José"),
            mapOf("id" to "student_sofia", "names" to "Sofia", "lastnames" to "Pérez", "dni" to "70001002", "classroom_id" to "sanjose_2do_a", "parent_email" to "padre@sanjose.com", "school_id" to "Colegio San José")
        )
        for (s in students) {
            val id = s["id"] as String
            db.collection("students").document(id).set(s, SetOptions.merge())
        }

        // 5. Grades
        val grades = listOf(
            mapOf("student_id" to "student_mateo", "subject" to "Matemáticas", "type" to "diaria", "value" to "18", "date" to "2026-06-08"),
            mapOf("student_id" to "student_mateo", "subject" to "Comunicación", "type" to "diaria", "value" to "16", "date" to "2026-06-08")
        )
        for ((index, g) in grades.withIndex()) {
            db.collection("grades").document("grade_mateo_$index").set(g, SetOptions.merge())
        }
    }
}
