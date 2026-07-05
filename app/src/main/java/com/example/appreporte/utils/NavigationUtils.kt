package com.example.appreporte.utils

import android.content.Context
import android.content.Intent
import com.example.appreporte.ui.activities.common.PerfilActivity
import com.example.appreporte.ui.activities.common.InicioActivity
import com.example.appreporte.ui.activities.admin.AdminDashboardActivity
import com.example.appreporte.ui.activities.admin.SuperAdminDashboardActivity
import com.example.appreporte.ui.activities.docente.DocenteDashboardActivity
import com.example.appreporte.ui.activities.padre.PadreDashboardActivity
import com.example.appreporte.ui.activities.padre.ChatbotPadreActivity
import com.example.appreporte.ui.activities.padre.PadreReporteActivity
import com.example.appreporte.ui.activities.foro.ForoSalonesActivity
import com.example.appreporte.ui.activities.foro.ForoDetalleActivity
import com.example.appreporte.ui.activities.common.NotificacionesActivity
import com.example.appreporte.ui.activities.foro.PostDetalleActivity
import com.example.appreporte.ui.activities.common.DirectChatActivity
import com.example.appreporte.ui.activities.docente.GestionReportesSalonesActivity
import com.example.appreporte.ui.activities.admin.ComplaintsActivity
import com.example.appreporte.ui.activities.docente.GestionAlumnosSalonesActivity
import com.example.appreporte.ui.activities.docente.AlumnosListaActivity

object NavigationUtils {

    const val USER_EMAIL = "USER_EMAIL"
    const val USER_ROL = "USER_ROL"
    const val SCHOOL_ID = "SCHOOL_ID"
    const val STUDENT_ID = "STUDENT_ID"
    const val CLASSROOM_ID = "CLASSROOM_ID"
    const val SALON_NAME = "SALON_NAME"
    const val POST_ID = "POST_ID"
    const val POST_AUTHOR = "POST_AUTHOR"
    const val POST_TITLE = "POST_TITLE"
    const val POST_CONTENT = "POST_CONTENT"
    const val POST_TIME = "POST_TIME"

    fun navigateToDashboard(context: Context, email: String?, rol: String?, schoolId: String?) {
        val targetActivity = when (rol?.lowercase()) {
            "superadmin" -> SuperAdminDashboardActivity::class.java
            "admin" -> InicioActivity::class.java
            "docente" -> DocenteDashboardActivity::class.java
            else -> PadreDashboardActivity::class.java
        }
        val intent = Intent(context, targetActivity)
        intent.putExtra(USER_EMAIL, email)
        intent.putExtra(USER_ROL, rol)
        intent.putExtra(SCHOOL_ID, schoolId)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(intent)
    }

    fun navigateToPerfil(context: Context, email: String?, rol: String?, schoolId: String?) {
        val intent = Intent(context, PerfilActivity::class.java)
        intent.putExtra(USER_EMAIL, email)
        intent.putExtra(USER_ROL, rol)
        intent.putExtra(SCHOOL_ID, schoolId)
        context.startActivity(intent)
    }

    fun navigateToForo(context: Context, email: String?, rol: String?, schoolId: String?) {
        val intent = Intent(context, ForoSalonesActivity::class.java)
        intent.putExtra(USER_EMAIL, email)
        intent.putExtra(USER_ROL, rol)
        intent.putExtra(SCHOOL_ID, schoolId)
        context.startActivity(intent)
    }

    fun navigateToAsistente(context: Context, email: String?, studentId: String?) {
        val intent = Intent(context, ChatbotPadreActivity::class.java)
        intent.putExtra(USER_EMAIL, email)
        intent.putExtra(STUDENT_ID, studentId)
        context.startActivity(intent)
    }

    fun navigateToReportes(context: Context, email: String?, role: String?, studentId: String?) {
        val intent = Intent(context, PadreReporteActivity::class.java)
        intent.putExtra(USER_EMAIL, email)
        intent.putExtra(USER_ROL, role)
        intent.putExtra(STUDENT_ID, studentId)
        context.startActivity(intent)
    }

    fun navigateToForoDetalle(context: Context, email: String?, role: String?, studentId: String?, classroomId: String?, salonName: String?) {
        val intent = Intent(context, ForoDetalleActivity::class.java)
        intent.putExtra(USER_EMAIL, email)
        intent.putExtra(USER_ROL, role)
        intent.putExtra(STUDENT_ID, studentId)
        intent.putExtra(CLASSROOM_ID, classroomId)
        intent.putExtra(SALON_NAME, salonName)
        context.startActivity(intent)
    }

    fun navigateToNotificaciones(context: Context, email: String?) {
        val intent = Intent(context, NotificacionesActivity::class.java)
        intent.putExtra(USER_EMAIL, email)
        context.startActivity(intent)
    }

    fun navigateToDirectChat(context: Context, currentEmail: String?, targetEmail: String?) {
        val intent = Intent(context, DirectChatActivity::class.java)
        intent.putExtra("CURRENT_EMAIL", currentEmail)
        intent.putExtra("TARGET_EMAIL", targetEmail)
        intent.putExtra(USER_EMAIL, currentEmail)
        context.startActivity(intent)
    }

    fun navigateToGestionReportes(context: Context, schoolId: String?, email: String?, rol: String?) {
        val intent = Intent(context, GestionReportesSalonesActivity::class.java)
        intent.putExtra(SCHOOL_ID, schoolId)
        intent.putExtra(USER_EMAIL, email)
        intent.putExtra(USER_ROL, rol)
        context.startActivity(intent)
    }

    fun navigateToComplaints(context: Context, email: String?, rol: String?, schoolId: String?) {
        val intent = Intent(context, ComplaintsActivity::class.java)
        intent.putExtra(USER_EMAIL, email)
        intent.putExtra(USER_ROL, rol)
        intent.putExtra(SCHOOL_ID, schoolId)
        context.startActivity(intent)
    }

    fun navigateToGestionAlumnos(context: Context, email: String?, rol: String?, schoolId: String?, openAddDialog: Boolean = false) {
        val intent = Intent(context, GestionAlumnosSalonesActivity::class.java)
        intent.putExtra(USER_EMAIL, email)
        intent.putExtra(USER_ROL, rol)
        intent.putExtra(SCHOOL_ID, schoolId)
        intent.putExtra("OPEN_ADD_DIALOG", openAddDialog)
        context.startActivity(intent)
    }

    fun navigateToAlumnosLista(context: Context, classroomId: String?, classroomName: String?, email: String?, rol: String?, schoolId: String?) {
        val intent = Intent(context, AlumnosListaActivity::class.java)
        intent.putExtra(CLASSROOM_ID, classroomId)
        intent.putExtra(SALON_NAME, classroomName)
        intent.putExtra(USER_EMAIL, email)
        intent.putExtra(USER_ROL, rol)
        intent.putExtra(SCHOOL_ID, schoolId)
        context.startActivity(intent)
    }

    fun navigateToPostDetalle(
        context: Context,
        email: String?,
        role: String?,
        studentId: String?,
        classroomId: String?,
        postId: String?,
        author: String?,
        title: String?,
        content: String?,
        time: String?
    ) {
        val intent = Intent(context, PostDetalleActivity::class.java)
        intent.putExtra(USER_EMAIL, email)
        intent.putExtra(USER_ROL, role)
        intent.putExtra(STUDENT_ID, studentId)
        intent.putExtra(CLASSROOM_ID, classroomId)
        intent.putExtra(POST_ID, postId)
        intent.putExtra(POST_AUTHOR, author)
        intent.putExtra(POST_TITLE, title)
        intent.putExtra(POST_CONTENT, content)
        intent.putExtra(POST_TIME, time)
        context.startActivity(intent)
    }
}
