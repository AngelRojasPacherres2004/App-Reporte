package com.example.appreporte

import com.google.firebase.firestore.FirebaseFirestore

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
            mapOf("name" to "Colegio San José", "levels" to listOf("Primaria", "Secundaria"), "adminEmail" to "admin@sanjose.com"),
            mapOf("name" to "Colegio Santa María", "levels" to listOf("Primaria", "Secundaria"), "adminEmail" to "admin@santamaria.com")
        )
        for (school in schools) {
            val name = school["name"] as String
            db.collection("colegios").document(name).set(school)
        }

        // 2. Classrooms (Salones)
        val classrooms = listOf(
            mapOf("id" to "sanjose_1er_a", "name" to "1er Grado A - Primaria", "school_id" to "Colegio San José"),
            mapOf("id" to "sanjose_2do_a", "name" to "2do Grado A - Primaria", "school_id" to "Colegio San José"),
            mapOf("id" to "sanjose_3er_a", "name" to "3er Grado A - Primaria", "school_id" to "Colegio San José"),
            mapOf("id" to "santamaria_1er_a", "name" to "1er Año A - Secundaria", "school_id" to "Colegio Santa María")
        )
        for (c in classrooms) {
            val id = c["id"] as String
            db.collection("classrooms").document(id).set(c)
        }

        // 3. Forums (Foros independientes)
        val forums = listOf(
            mapOf("name" to "Club de Deportes", "schoolId" to "Colegio San José", "createdBy" to "admin@sanjose.com"),
            mapOf("name" to "Consejo de Padres", "schoolId" to "Colegio San José", "createdBy" to "admin@sanjose.com")
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
            ),
            mapOf(
                "email" to "profesora.maria@sanjose.com", 
                "password" to "docente123", 
                "rol" to "docente", 
                "school_id" to "Colegio San José", 
                "classrooms" to listOf("sanjose_1er_a"), 
                "phone" to "+51987654321"
            ),
            mapOf("email" to "padre@sanjose.com", "password" to "padre123", "rol" to "usuario", "school_id" to "Colegio San José", "phone" to "+51988888888")
        )
        for (u in users) {
            val email = u["email"] as String
            db.collection("users").document(email).set(u)
        }

        // 5. Students (Alumnos)
        val students = listOf(
            mapOf(
                "id" to "student_mateo",
                "names" to "Mateo",
                "lastnames" to "Rojas",
                "name" to "Mateo Rojas",
                "dni" to "71234567",
                "parent_email" to "padre@sanjose.com",
                "classroom_id" to "sanjose_1er_a",
                "classroom_name" to "1er Grado A - Primaria",
                "school_id" to "Colegio San José"
            ),
            mapOf(
                "id" to "student_sofia",
                "names" to "Sofia",
                "lastnames" to "Rojas",
                "name" to "Sofia Rojas",
                "dni" to "76543210",
                "parent_email" to "padre@sanjose.com",
                "classroom_id" to "sanjose_2do_a",
                "classroom_name" to "2do Grado A - Primaria",
                "school_id" to "Colegio San José"
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
        for ((index, g) in grades.withIndex()) {
            db.collection("grades").document("grade_$index").set(g)
        }

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
        for ((index, att) in attendance.withIndex()) {
            db.collection("attendance").document("att_$index").set(att)
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
        )
        for ((index, comm) in comments.withIndex()) {
            db.collection("comments").document("comment_$index").set(comm)
        }

        // 11. Complaints
        val complaint = mapOf(
            "postId" to "Reunión Bimestral de Padres",
            "parentEmail" to "padre@sanjose.com",
            "content" to "Me gustaría solicitar que los enlaces de Zoom se envíen con más anticipación por favor.",
            "status" to "en proceso",
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("complaints").document("complaint_mateo").set(complaint)

        // 12. Direct Chats
        val chat1Id = "docente@sanjose.com_padre@sanjose.com"
        db.collection("direct_chats").document(chat1Id).set(mapOf("participants" to listOf("docente@sanjose.com", "padre@sanjose.com")))
        
        val chat1Messages = listOf(
            mapOf(
                "sender" to "padre@sanjose.com", 
                "content" to "Buenas tardes profesor, una consulta sobre las notas de Mateo.", 
                "timestamp" to System.currentTimeMillis() - 7200000
            ),
            mapOf(
                "sender" to "docente@sanjose.com", 
                "content" to "Hola, claro que sí. Mateo va muy bien, solo le falta completar su promedio en Ciencia y Tecnología.", 
                "timestamp" to System.currentTimeMillis() - 3600000
            )
        )
        for ((index, m) in chat1Messages.withIndex()) {
            db.collection("direct_chats").document(chat1Id).collection("messages").document("msg_$index").set(m)
        }

        val chat2Id = "admin@sanjose.com_superadmin@reporte.com"
        db.collection("direct_chats").document(chat2Id).set(mapOf("participants" to listOf("admin@sanjose.com", "superadmin@reporte.com")))
        
        val chat2Messages = listOf(
            mapOf(
                "sender" to "admin@sanjose.com", 
                "content" to "Hola Superadmin, tenemos un nuevo salón de 3er grado y ya registré al docente.", 
                "timestamp" to System.currentTimeMillis() - 86400000
            ),
            mapOf(
                "sender" to "superadmin@reporte.com", 
                "content" to "Excelente. Cualquier inconveniente técnico me avisas.", 
                "timestamp" to System.currentTimeMillis() - 43200000
            )
        )
        for ((index, m) in chat2Messages.withIndex()) {
            db.collection("direct_chats").document(chat2Id).collection("messages").document("msg_$index").set(m)
        }
    }
}
