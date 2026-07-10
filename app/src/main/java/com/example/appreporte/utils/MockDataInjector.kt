package com.example.appreporte.utils

import com.google.firebase.firestore.FirebaseFirestore

object MockDataInjector {

    fun injectData() {
        val db = FirebaseFirestore.getInstance()
        val collections = listOf(
            "colegios", "classrooms", "forums", "users", "students", "grades", 
            "attendance", "posts", "comments", "complaints"
        )
        clearAndSeed(db, collections, 0)
    }

    private fun clearAndSeed(db: FirebaseFirestore, collections: List<String>, index: Int) {
        if (index < collections.size) {
            val col = collections[index]
            db.collection(col).get()
                .addOnSuccessListener { snapshot ->
                    val batch = db.batch()
                    snapshot.documents.forEach { batch.delete(it.reference) }
                    batch.commit().addOnSuccessListener {
                        clearAndSeed(db, collections, index + 1)
                    }.addOnFailureListener {
                        clearAndSeed(db, collections, index + 1)
                    }
                }
                .addOnFailureListener {
                    clearAndSeed(db, collections, index + 1)
                }
        } else {
            insertCleanData(db)
        }
    }

    private fun insertCleanData(db: FirebaseFirestore) {
        // 1. Colegios
        val schools = listOf(
            mapOf("name" to "Colegio Los Pinos", "levels" to listOf("Inicial", "Primaria", "Secundaria"), "adminEmail" to "admin@lospinos.edu.pe")
        )
        for (school in schools) {
            val name = school["name"] as String
            db.collection("colegios").document(name).set(school)
        }

        // 2. Classrooms (Salones jerárquicos)
        val classrooms = listOf(
            mapOf("id" to "lospinos_inicial_3a", "name" to "3 Años A - Inicial", "school_id" to "Colegio Los Pinos", "level" to "Inicial", "grade" to "3 Años"),
            mapOf("id" to "lospinos_pri_1a", "name" to "1er Grado A - Primaria", "school_id" to "Colegio Los Pinos", "level" to "Primaria", "grade" to "1er Grado"),
            mapOf("id" to "lospinos_pri_2a", "name" to "2do Grado A - Primaria", "school_id" to "Colegio Los Pinos", "level" to "Primaria", "grade" to "2do Grado"),
            mapOf("id" to "lospinos_sec_1a", "name" to "1er Año A - Secundaria", "school_id" to "Colegio Los Pinos", "level" to "Secundaria", "grade" to "1er Año")
        )
        for (c in classrooms) {
            val id = c["id"] as String
            db.collection("classrooms").document(id).set(c)
        }

        // 3. Forums
        val forums = listOf(
            mapOf("name" to "Avisos Generales", "schoolId" to "Colegio Los Pinos", "createdBy" to "admin@lospinos.edu.pe")
        )
        for ((index, f) in forums.withIndex()) {
            db.collection("forums").document("forum_$index").set(f)
        }

        // 4. Users (Contraseñas robustas según Regex)
        val users = listOf(
            mapOf("email" to "superadmin@reporte.com", "password" to "SuperAdmin@2026", "rol" to "superadmin", "school_id" to "Global", "phone" to "+51900000000"),
            mapOf("email" to "admin@lospinos.edu.pe", "password" to "AdminPinos@2026", "rol" to "admin", "school_id" to "Colegio Los Pinos", "phone" to "+51911111111"),
            mapOf(
                "email" to "profesor.carlos@lospinos.edu.pe", 
                "password" to "Docente@2026", 
                "rol" to "docente", 
                "school_id" to "Colegio Los Pinos", 
                "classrooms" to listOf("lospinos_pri_1a", "lospinos_pri_2a"), 
                "phone" to "+51922222222"
            ),
            mapOf("email" to "padre.rodriguez@gmail.com", "password" to "PadreFam@2026", "rol" to "usuario", "school_id" to "Colegio Los Pinos", "phone" to "+51933333333")
        )
        for (u in users) {
            val email = u["email"] as String
            db.collection("users").document(email).set(u)
        }

        // 5. Students
        val students = listOf(
            mapOf("id" to "student_lucas_rod", "names" to "Lucas", "lastnames" to "Rodríguez", "name" to "Lucas Rodríguez", "dni" to "70000001", "parent_email" to "padre.rodriguez@gmail.com", "classroom_id" to "lospinos_pri_1a", "classroom_name" to "1er Grado A - Primaria", "school_id" to "Colegio Los Pinos"),
            mapOf("id" to "student_mia_rod", "names" to "Mía", "lastnames" to "Rodríguez", "name" to "Mía Rodríguez", "dni" to "70000002", "parent_email" to "padre.rodriguez@gmail.com", "classroom_id" to "lospinos_inicial_3a", "classroom_name" to "3 Años A - Inicial", "school_id" to "Colegio Los Pinos"),
            mapOf("id" to "student_mateo_rod", "names" to "Mateo", "lastnames" to "Rodríguez", "name" to "Mateo Rodríguez", "dni" to "70000003", "parent_email" to "padre.rodriguez@gmail.com", "classroom_id" to "lospinos_sec_1a", "classroom_name" to "1er Año A - Secundaria", "school_id" to "Colegio Los Pinos"),
            mapOf("id" to "student_valeria_g", "names" to "Valeria", "lastnames" to "Gómez", "name" to "Valeria Gómez", "dni" to "70000004", "parent_email" to "padre.gomez@gmail.com", "classroom_id" to "lospinos_pri_2a", "classroom_name" to "2do Grado A - Primaria", "school_id" to "Colegio Los Pinos"),
            mapOf("id" to "student_diego_g", "names" to "Diego", "lastnames" to "Gómez", "name" to "Diego Gómez", "dni" to "70000005", "parent_email" to "padre.gomez@gmail.com", "classroom_id" to "lospinos_sec_1a", "classroom_name" to "1er Año A - Secundaria", "school_id" to "Colegio Los Pinos")
        )
        for (s in students) {
            val id = s["id"] as String
            db.collection("students").document(id).set(s)
        }

        // 6. Grades (Calificaciones realistas)
        val grades = listOf(
            mapOf("student_id" to "student_lucas_rod", "subject" to "Matemáticas", "type" to "diaria", "value" to "18", "date" to "2026-06-08", "classroom_id" to "lospinos_pri_1a"),
            mapOf("student_id" to "student_lucas_rod", "subject" to "Comunicación", "type" to "mensual", "value" to "16", "date" to "2026-06-09", "classroom_id" to "lospinos_pri_1a"),
            mapOf("student_id" to "student_lucas_rod", "subject" to "Ciencias", "type" to "bimestral", "value" to "19", "date" to "2026-06-10", "classroom_id" to "lospinos_pri_1a"),
            mapOf("student_id" to "student_mateo_rod", "subject" to "Física", "type" to "diaria", "value" to "14", "date" to "2026-06-10", "classroom_id" to "lospinos_sec_1a"),
            mapOf("student_id" to "student_mateo_rod", "subject" to "Historia", "type" to "diaria", "value" to "15", "date" to "2026-06-10", "classroom_id" to "lospinos_sec_1a"),
            mapOf("student_id" to "student_valeria_g", "subject" to "Matemáticas", "type" to "diaria", "value" to "20", "date" to "2026-06-10", "classroom_id" to "lospinos_pri_2a")
        )
        for ((index, grade) in grades.withIndex()) {
            db.collection("grades").document("grade_gen_$index").set(grade)
        }

        // 7. Attendance
        val attendance = mapOf("student_id" to "student_lucas_rod", "date" to "2026-06-10", "status" to "Presente", "classroom_id" to "lospinos_pri_1a", "timestamp" to System.currentTimeMillis())
        db.collection("attendance").document("att_lucas_1").set(attendance)

        // 8. Posts (Publicaciones de los salones)
        val postsList = listOf(
            mapOf("salonName" to "1er Grado A - Primaria", "author" to "profesor.carlos@lospinos.edu.pe", "title" to "Excursión al zoológico", "content" to "Estimados padres, este viernes iremos al zoológico. No olviden enviar autorización.", "time" to "AHORA", "timestamp" to System.currentTimeMillis()),
            mapOf("salonName" to "1er Año A - Secundaria", "author" to "profesor.carlos@lospinos.edu.pe", "title" to "Feria de Ciencias", "content" to "Los proyectos de ciencias deben presentarse la próxima semana sin falta.", "time" to "HACE 2 HORAS", "timestamp" to System.currentTimeMillis())
        )
        for ((index, post) in postsList.withIndex()) {
            db.collection("posts").document("post_gen_$index").set(post)
        }
    }
}
