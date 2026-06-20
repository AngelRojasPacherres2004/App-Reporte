package com.example.appreporte

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object MockDataInjector {
    fun injectData() {
        val db = FirebaseFirestore.getInstance()
        val collections = listOf(
            "colegios", "users", "classrooms", "students", 
            "attendance", "posts", "forums", "courses", 
            "complaints", "direct_chats", "grades", "comments"
        )
        clearAndSeed(db, collections, 0)
    }

        for ((name, levels, adminEmail) in schools) {
            val schoolMap = hashMapOf(
                "name" to name,
                "levels" to levels,
                "adminEmail" to adminEmail
            )
            db.collection("colegios").document(name).set(schoolMap, SetOptions.merge())

            // Inyectar el usuario admin del colegio
            db.collection("users").document(adminEmail).set(
                hashMapOf(
                    "email" to adminEmail,
                    "password" to "admin123",
                    "rol" to "admin",
                    "school_id" to name,
                    "phone" to "+51900000000"
                ), SetOptions.merge()
            )
        }
    }

    private fun insertCleanData(db: FirebaseFirestore) {
        // 1. Colegios
        val schools = listOf(
            mapOf("name" to "Colegio San José", "levels" to listOf("Primaria", "Secundaria"), "adminEmail" to "admin@sanjose.com"),
            mapOf("name" to "Colegio Santa María", "levels" to listOf("Primaria", "Secundaria"), "adminEmail" to "admin@santamaria.com")
        )
        for (school in schools) {
            val name = school["name"] as String
            db.collection("colegios").document(name).set(school)
        }

        for ((salonName, schoolName) in salonesList) {
            val docId = "${schoolName.replace(" ", "_")}_${salonName.replace(" ", "_")}"
            val map = hashMapOf(
                "name" to salonName,
                "school_id" to schoolName,
                "tutor" to "docente@reporte.com"
            )
            db.collection("classrooms").document(docId).set(map, SetOptions.merge())
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
            ), SetOptions.merge()
        )
        for ((index, f) in forums.withIndex()) {
            db.collection("forums").document("forum_$index").set(f)
        }

        // 4. Users (Admins, Docentes, Padres, SuperAdmins)
        val users = listOf(
            mapOf("email" to "superadmin@reporte.com", "password" to "superadmin123", "rol" to "superadmin", "school_id" to "Global", "phone" to ""),
            mapOf("email" to "angelrojaspacherres@gmail.com", "password" to "superadmin123", "rol" to "superadmin", "school_id" to "Global", "phone" to ""),
            mapOf("email" to "admin@sanjose.com", "password" to "admin123", "rol" to "admin", "school_id" to "Colegio San José", "phone" to ""),
            mapOf("email" to "admin@santamaria.com", "password" to "admin123", "rol" to "admin", "school_id" to "Colegio Santa María", "phone" to ""),
            mapOf(
                "email" to "docente@sanjose.com", 
                "password" to "docente123", 
                "rol" to "docente", 
                "school_id" to "Colegio San José", 
                "classrooms" to listOf("sanjose_1er_a", "sanjose_2do_a", "sanjose_3er_a"), 
                "phone" to "+51999999999"
            ), SetOptions.merge()
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
                ), SetOptions.merge()
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
                ), SetOptions.merge()
            )

            val pEmail = "padre$i@reporte.com"
            db.collection("users").document(pEmail).set(
                hashMapOf(
                    "email" to pEmail,
                    "password" to "user123",
                    "rol" to "usuario",
                    "school_id" to "Colegio San José",
                    "phone" to "+5192222222$i"
                ), SetOptions.merge()
            )
        )
        for (s in students) {
            val id = s["id"] as String
            db.collection("students").document(id).set(s)
        }

        // 6. Grades (Calificaciones)
        val grades = listOf(
            // Mateo
            mapOf("student_id" to "student_mateo", "subject" to "Matemáticas", "type" to "diaria", "value" to "18", "date" to "2026-06-08", "classroom_id" to "sanjose_1er_a"),
            mapOf("student_id" to "student_mateo", "subject" to "Matemáticas", "type" to "mensual", "value" to "17", "date" to "2026-06-09", "classroom_id" to "sanjose_1er_a"),
            mapOf("student_id" to "student_mateo", "subject" to "Matemáticas", "type" to "bimestral", "value" to "19", "date" to "2026-06-10", "classroom_id" to "sanjose_1er_a"),
            
            mapOf("student_id" to "student_mateo", "subject" to "Comunicación", "type" to "diaria", "value" to "16", "date" to "2026-06-08", "classroom_id" to "sanjose_1er_a"),
            mapOf("student_id" to "student_mateo", "subject" to "Comunicación", "type" to "mensual", "value" to "15", "date" to "2026-06-09", "classroom_id" to "sanjose_1er_a"),
            mapOf("student_id" to "student_mateo", "subject" to "Comunicación", "type" to "bimestral", "value" to "18", "date" to "2026-06-10", "classroom_id" to "sanjose_1er_a"),
            
            mapOf("student_id" to "student_mateo", "subject" to "Ciencia y Tecnología", "type" to "diaria", "value" to "15", "date" to "2026-06-08", "classroom_id" to "sanjose_1er_a"),
            mapOf("student_id" to "student_mateo", "subject" to "Ciencia y Tecnología", "type" to "mensual", "value" to "16", "date" to "2026-06-09", "classroom_id" to "sanjose_1er_a"),
            mapOf("student_id" to "student_mateo", "subject" to "Ciencia y Tecnología", "type" to "bimestral", "value" to "17", "date" to "2026-06-10", "classroom_id" to "sanjose_1er_a"),
            
            // Sofia
            mapOf("student_id" to "student_sofia", "subject" to "Matemáticas", "type" to "diaria", "value" to "19", "date" to "2026-06-08", "classroom_id" to "sanjose_2do_a"),
            mapOf("student_id" to "student_sofia", "subject" to "Matemáticas", "type" to "mensual", "value" to "18", "date" to "2026-06-09", "classroom_id" to "sanjose_2do_a"),
            mapOf("student_id" to "student_sofia", "subject" to "Matemáticas", "type" to "bimestral", "value" to "20", "date" to "2026-06-10", "classroom_id" to "sanjose_2do_a"),
            
            mapOf("student_id" to "student_sofia", "subject" to "Comunicación", "type" to "diaria", "value" to "17", "date" to "2026-06-08", "classroom_id" to "sanjose_2do_a"),
            mapOf("student_id" to "student_sofia", "subject" to "Comunicación", "type" to "mensual", "value" to "16", "date" to "2026-06-09", "classroom_id" to "sanjose_2do_a"),
            mapOf("student_id" to "student_sofia", "subject" to "Comunicación", "type" to "bimestral", "value" to "18", "date" to "2026-06-10", "classroom_id" to "sanjose_2do_a"),
            
            mapOf("student_id" to "student_sofia", "subject" to "Personal Social", "type" to "diaria", "value" to "14", "date" to "2026-06-08", "classroom_id" to "sanjose_2do_a"),
            mapOf("student_id" to "student_sofia", "subject" to "Personal Social", "type" to "mensual", "value" to "15", "date" to "2026-06-09", "classroom_id" to "sanjose_2do_a"),
            mapOf("student_id" to "student_sofia", "subject" to "Personal Social", "type" to "bimestral", "value" to "16", "date" to "2026-06-10", "classroom_id" to "sanjose_2do_a")
        )
        db.collection("students").document("student_juanito").set(student, SetOptions.merge())

        // 7. Attendance (Asistencia)
        val attendance = listOf(
            // Mateo
            mapOf("student_id" to "student_mateo", "date" to "2026-06-08", "status" to "Presente", "course_name" to "1er Grado A - Primaria"),
            mapOf("student_id" to "student_mateo", "date" to "2026-06-09", "status" to "Tardanza", "course_name" to "1er Grado A - Primaria"),
            mapOf("student_id" to "student_mateo", "date" to "2026-06-10", "status" to "Presente", "course_name" to "1er Grado A - Primaria"),
            // Sofia
            mapOf("student_id" to "student_sofia", "date" to "2026-06-08", "status" to "Presente", "course_name" to "2do Grado A - Primaria"),
            mapOf("student_id" to "student_sofia", "date" to "2026-06-09", "status" to "Presente", "course_name" to "2do Grado A - Primaria"),
            mapOf("student_id" to "student_sofia", "date" to "2026-06-10", "status" to "Falta", "course_name" to "2do Grado A - Primaria")
        )
        for ((attId, date, status) in attendance) {
            db.collection("attendance").document(attId).set(
                hashMapOf(
                    "student_id" to "student_juanito",
                    "date" to date,
                    "status" to status,
                    "course_name" to "General"
                ), SetOptions.merge()
            )
        }

        // 8. Courses
        val courses = listOf(
            // Mateo (1er Grado A)
            mapOf(
                "course_name" to "Matemáticas",
                "schedule" to "Lunes 8:00 - 10:00",
                "teacher_name" to "Docente San José",
                "teacher_email" to "docente@sanjose.com",
                "teacher_phone" to "+51999999999",
                "school_id" to "Colegio San José",
                "classroom_id" to "sanjose_1er_a",
                "classroom_name" to "1er Grado A - Primaria"
            ),
            mapOf(
                "course_name" to "Comunicación",
                "schedule" to "Martes 10:00 - 12:00",
                "teacher_name" to "Docente San José",
                "teacher_email" to "docente@sanjose.com",
                "teacher_phone" to "+51999999999",
                "school_id" to "Colegio San José",
                "classroom_id" to "sanjose_1er_a",
                "classroom_name" to "1er Grado A - Primaria"
            ),
            // Sofia (2do Grado A)
            mapOf(
                "course_name" to "Matemáticas",
                "schedule" to "Miércoles 8:00 - 10:00",
                "teacher_name" to "Docente San José",
                "teacher_email" to "docente@sanjose.com",
                "teacher_phone" to "+51999999999",
                "school_id" to "Colegio San José",
                "classroom_id" to "sanjose_2do_a",
                "classroom_name" to "2do Grado A - Primaria"
            ),
            mapOf(
                "course_name" to "Comunicación",
                "schedule" to "Jueves 10:00 - 12:00",
                "teacher_name" to "Docente San José",
                "teacher_email" to "docente@sanjose.com",
                "teacher_phone" to "+51999999999",
                "school_id" to "Colegio San José",
                "classroom_id" to "sanjose_2do_a",
                "classroom_name" to "2do Grado A - Primaria"
            ),
            mapOf(
                "course_name" to "Personal Social",
                "schedule" to "Viernes 8:00 - 10:00",
                "teacher_name" to "Docente San José",
                "teacher_email" to "docente@sanjose.com",
                "teacher_phone" to "+51999999999",
                "school_id" to "Colegio San José",
                "classroom_id" to "sanjose_2do_a",
                "classroom_name" to "2do Grado A - Primaria"
            )
        )
        for ((index, c) in courses.withIndex()) {
            db.collection("courses").document("course_$index").set(c)
        }

        // 9. Posts (Foro)
        val posts = listOf(
            mapOf(
                "id" to "post_reunion",
                "title" to "Reunión Bimestral de Padres",
                "content" to "Estimados padres, este viernes 12 de junio a las 6:00 PM realizaremos la reunión virtual por Zoom para la entrega de libretas.",
                "author" to "docente@sanjose.com",
                "time" to "Hace 1 hora",
                "timestamp" to System.currentTimeMillis() - 3600000,
                "salonName" to "1er Grado A - Primaria",
                "classroomId" to "sanjose_1er_a"
            ),
            mapOf(
                "id" to "post_tareas",
                "title" to "Proyecto de Ciencia y Tecnología",
                "content" to "Recuerden que el plazo máximo para subir el video del proyecto científico escolar vence el próximo lunes.",
                "author" to "docente@sanjose.com",
                "time" to "Hace 2 horas",
                "timestamp" to System.currentTimeMillis() - 7200000,
                "salonName" to "1er Grado A - Primaria",
                "classroomId" to "sanjose_1er_a"
            )
        )
        for (post in posts) {
            val id = post["id"] as String
            db.collection("posts").document(id).set(post)
        }

        // 10. Comments
        val comments = listOf(
            mapOf(
                "postId" to "post_reunion", 
                "author" to "padre@sanjose.com", 
                "content" to "Entendido profesor, ahí estaré.", 
                "time" to "Hace 30 minutos", 
                "timestamp" to System.currentTimeMillis() - 1800000
            ),
            mapOf(
                "postId" to "post_reunion", 
                "author" to "docente@sanjose.com", 
                "content" to "Excelente, nos vemos el viernes.", 
                "time" to "Hace 15 minutos", 
                "timestamp" to System.currentTimeMillis() - 900000
            )
            db.collection("posts").document(postId).set(postMap, SetOptions.merge())

        // 11. Complaints
        val complaint = mapOf(
            "postId" to "Reunión Bimestral de Padres",
            "parentEmail" to "padre@sanjose.com",
            "content" to "Me gustaría solicitar que los enlaces de Zoom se envíen con más anticipación por favor.",
            "status" to "en proceso",
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("complaints").document("complaint_mateo").set(complaint)

            for ((index, comment) in comments.withIndex()) {
                db.collection("posts").document(postId)
                    .collection("comments").document("comment_${postId}_$index").set(
                        hashMapOf(
                            "content" to comment.first,
                            "author" to comment.second,
                            "timestamp" to com.google.firebase.Timestamp.now()
                        ), SetOptions.merge()
                    )
            }
        }
    }
}
