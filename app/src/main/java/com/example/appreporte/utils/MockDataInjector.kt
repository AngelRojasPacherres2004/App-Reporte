package com.example.appreporte.utils

import com.google.firebase.firestore.FirebaseFirestore

object MockDataInjector {
    fun injectData() {
        val db = FirebaseFirestore.getInstance()

        // ────────────────────────────────────────────────────────────
        // 1. INYECTAR 10 COLEGIOS ORDENADOS
        // ────────────────────────────────────────────────────────────
        val schools = listOf(
            Triple("Colegio San José", listOf("Inicial", "Primaria", "Secundaria"), "admin@sanjose.com"),
            Triple("Colegio Santa María", listOf("Primaria", "Secundaria"), "admin@santamaria.com"),
            Triple("Colegio San Agustín", listOf("Inicial", "Primaria"), "admin@sanagustin.com"),
            Triple("Colegio Champagnat", listOf("Primaria", "Secundaria"), "admin@champagnat.com"),
            Triple("Colegio Pamer", listOf("Secundaria"), "admin@pamer.com"),
            Triple("Colegio Trilce", listOf("Primaria", "Secundaria"), "admin@trilce.com"),
            Triple("Colegio Innova Schools", listOf("Inicial", "Primaria", "Secundaria"), "admin@innova.com"),
            Triple("I.E. Fe y Alegría", listOf("Inicial", "Primaria", "Secundaria"), "admin@feyalegria.com"),
            Triple("I.E. Claretiano", listOf("Primaria", "Secundaria"), "admin@claretiano.com"),
            Triple("I.E. Maristas", listOf("Inicial", "Primaria", "Secundaria"), "admin@maristas.com")
        )

        for ((name, levels, adminEmail) in schools) {
            val schoolMap = hashMapOf(
                "name" to name,
                "levels" to levels,
                "adminEmail" to adminEmail
            )
            db.collection("colegios").document(name).set(schoolMap)

            // Inyectar el usuario admin del colegio
            db.collection("users").document(adminEmail).set(
                hashMapOf(
                    "email" to adminEmail,
                    "password" to "admin123",
                    "rol" to "admin",
                    "school_id" to name,
                    "phone" to "+51900000000"
                )
            )
        }

        // ────────────────────────────────────────────────────────────
        // 2. INYECTAR SALONES / AULAS (SOBRECARGA REALISTA)
        // ────────────────────────────────────────────────────────────
        // Vamos a inyectar más de 30 salones distribuidos en los colegios
        val salonesList = listOf(
            // Colegio San José (Sobrecarga de prueba)
            Pair("3 Años - Inicial", "Colegio San José"),
            Pair("4 Años - Inicial", "Colegio San José"),
            Pair("5 Años - Inicial", "Colegio San José"),
            Pair("1er Grado A - Primaria", "Colegio San José"),
            Pair("1er Grado B - Primaria", "Colegio San José"),
            Pair("2do Grado A - Primaria", "Colegio San José"),
            Pair("2do Grado B - Primaria", "Colegio San José"),
            Pair("3er Grado A - Primaria", "Colegio San José"),
            Pair("4to Grado A - Primaria", "Colegio San José"),
            Pair("5to Grado A - Primaria", "Colegio San José"),
            Pair("6to Grado A - Primaria", "Colegio San José"),
            Pair("1er Año A - Secundaria", "Colegio San José"),
            Pair("1er Año B - Secundaria", "Colegio San José"),
            Pair("2do Año A - Secundaria", "Colegio San José"),
            Pair("3er Año A - Secundaria", "Colegio San José"),
            Pair("4to Año A - Secundaria", "Colegio San José"),
            Pair("5to Año A - Secundaria", "Colegio San José"),

            // Colegio Santa María
            Pair("1er Grado A - Primaria", "Colegio Santa María"),
            Pair("2do Grado A - Primaria", "Colegio Santa María"),
            Pair("3er Grado A - Primaria", "Colegio Santa María"),
            Pair("1er Año A - Secundaria", "Colegio Santa María"),
            Pair("2do Año A - Secundaria", "Colegio Santa María"),
            Pair("5to Año A - Secundaria", "Colegio Santa María"),

            // Colegio Innova Schools
            Pair("4 Años - Inicial", "Colegio Innova Schools"),
            Pair("1er Grado A - Primaria", "Colegio Innova Schools"),
            Pair("6to Grado A - Primaria", "Colegio Innova Schools"),
            Pair("5to Año A - Secundaria", "Colegio Innova Schools")
        )

        for ((salonName, schoolName) in salonesList) {
            val docId = "${schoolName.replace(" ", "_")}_${salonName.replace(" ", "_")}"
            val map = hashMapOf(
                "name" to salonName,
                "school_id" to schoolName,
                "tutor" to "docente@reporte.com"
            )
            db.collection("classrooms").document(docId).set(map)
        }

        // ────────────────────────────────────────────────────────────
        // 3. USUARIOS DOCENTES Y PADRES DE FAMILIA
        // ────────────────────────────────────────────────────────────
        // Docente demo para probar salones
        db.collection("users").document("docente@reporte.com").set(
            hashMapOf(
                "email" to "docente@reporte.com",
                "password" to "docente123",
                "rol" to "docente",
                "school_id" to "Colegio San José",
                "phone" to "+51999999999"
            )
        )
        // Padre demo para probar reportes e hijo
        db.collection("users").document("user@reporte.com").set(
            hashMapOf(
                "email" to "user@reporte.com",
                "password" to "user123",
                "rol" to "usuario",
                "school_id" to "Colegio San José",
                "phone" to "+51999999999"
            )
        )

        // Inyectar superadministradores en Firestore
        val superAdmins = listOf("superadmin@reporte.com", "Angelrojaspacherres@gmail.com")
        for (saEmail in superAdmins) {
            db.collection("users").document(saEmail).set(
                hashMapOf(
                    "email" to saEmail,
                    "password" to "superadmin123",
                    "rol" to "superadmin",
                    "school_id" to "Global",
                    "phone" to ""
                )
            )
        }

        // Registrar más usuarios docentes y padres para simular volumen
        for (i in 1..5) {
            val docEmail = "docente$i@reporte.com"
            db.collection("users").document(docEmail).set(
                hashMapOf(
                    "email" to docEmail,
                    "password" to "docente123",
                    "rol" to "docente",
                    "school_id" to "Colegio San José",
                    "phone" to "+5191111111$i"
                )
            )

            val pEmail = "padre$i@reporte.com"
            db.collection("users").document(pEmail).set(
                hashMapOf(
                    "email" to pEmail,
                    "password" to "user123",
                    "rol" to "usuario",
                    "school_id" to "Colegio San José",
                    "phone" to "+5192222222$i"
                )
            )
        }

        // ────────────────────────────────────────────────────────────
        // 4. ALUMNOS, ASISTENCIA Y CALIFICACIONES DE PRUEBA
        // ────────────────────────────────────────────────────────────
        val salonJose = "Colegio_San_José_1er_Grado_A_-_Primaria"

        val student = hashMapOf(
            "names" to "Juanito",
            "lastnames" to "Perez",
            "dni" to "12345678",
            "parent_email" to "user@reporte.com",
            "classroom_id" to salonJose,
            "school_id" to "Colegio San José"
        )
        db.collection("students").document("student_juanito").set(student)

        // Asistencias
        val attendance = listOf(
            Triple("att_1", "2026-05-20", "Presente"),
            Triple("att_2", "2026-05-21", "Tardanza"),
            Triple("att_3", "2026-05-22", "Presente"),
            Triple("att_4", "2026-05-23", "Falta Justificada")
        )
        for ((attId, date, status) in attendance) {
            db.collection("attendance").document(attId).set(
                hashMapOf(
                    "student_id" to "student_juanito",
                    "date" to date,
                    "status" to status,
                    "course_name" to "General"
                )
            )
        }

        // ────────────────────────────────────────────────────────────
        // 5. INYECTAR FOROS DE DISCUSIÓN (ESTRUCTURA DE PUBLICACIONES)
        // ────────────────────────────────────────────────────────────
        // Añadiremos publicaciones activas al foro del aula
        val posts = listOf(
            mapOf(
                "id" to "post_1",
                "title" to "Reunión Extraordinaria de Padres de Familia",
                "content" to "Estimados padres, este viernes 29 de mayo a las 6:00 PM tendremos la primera reunión bimestral virtual para revisar el rendimiento académico de los alumnos. El link de Zoom se enviará por esta vía.",
                "author" to "docente@reporte.com"
            ),
            mapOf(
                "id" to "post_2",
                "title" to "Concurso de Ciencia y Tecnología: Inscripciones abiertas",
                "content" to "Invitamos a todos los alumnos a inscribir sus proyectos para la feria científica escolar de este año. La temática es libre y el plazo máximo de inscripción es el 15 de junio. ¡Esperamos su participación!",
                "author" to "docente@reporte.com"
            ),
            mapOf(
                "id" to "post_3",
                "title" to "Entrega de Libros de Trabajo de Actividades",
                "content" to "Se comunica que la entrega de los textos escolares de matemáticas y comunicación de nivel secundario se realizará este lunes en la portería del colegio de 8:00 AM a 1:00 PM.",
                "author" to "admin@sanjose.com"
            )
        )

        for (post in posts) {
            val postId = post["id"] ?: ""
            val title = post["title"] ?: ""
            val content = post["content"] ?: ""
            val author = post["author"] ?: ""

            val postMap = hashMapOf(
                "id" to postId,
                "title" to title,
                "content" to content,
                "author" to author,
                "timestamp" to com.google.firebase.Timestamp.now(),
                "classroomId" to salonJose
            )
            db.collection("posts").document(postId).set(postMap)

            // Comentarios simulando comunidad activa (Estrés del Foro)
            val comments = when (postId) {
                "post_1" -> listOf(
                    Pair("Muchas gracias por el aviso, estaré presente sin falta.", "user@reporte.com"),
                    Pair("¿La reunión será grabada? Trabajo a esa hora.", "padre1@reporte.com"),
                    Pair("Excelente, nos vemos el viernes.", "padre2@reporte.com")
                )
                "post_2" -> listOf(
                    Pair("Mi hijo ya tiene su idea de proyecto. ¡Qué gran oportunidad!", "user@reporte.com"),
                    Pair("¿Pueden ser equipos de 3 alumnos?", "padre3@reporte.com")
                )
                else -> listOf(
                    Pair("Entendido, iré a recogerlos a primera hora.", "padre4@reporte.com")
                )
            }

            for ((index, comment) in comments.withIndex()) {
                db.collection("posts").document(postId)
                    .collection("comments").document("comment_${postId}_$index").set(
                        hashMapOf(
                            "content" to comment.first,
                            "author" to comment.second,
                            "timestamp" to com.google.firebase.Timestamp.now()
                        )
                    )
            }
        }
    }
}

