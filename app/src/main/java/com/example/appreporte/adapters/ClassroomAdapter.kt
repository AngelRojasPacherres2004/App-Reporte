package com.example.appreporte

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ClassroomAdapter(
    private var rawClassrooms: List<Map<String, Any>> = emptyList(),
    private val onDeleteClick: (String) -> Unit,
    private val onItemClick: ((String, String) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val expandedLevels = mutableSetOf<String>()
    private val expandedGrades = mutableSetOf<String>() // format: "Level|Grade"
    private val expandedSections = mutableSetOf<String>() // format: "Level|Grade|Sections"
    private var displayList: List<ClassroomListItem> = emptyList()

    sealed class ClassroomListItem {
        data class LevelHeader(val level: String, val isExpanded: Boolean) : ClassroomListItem()
        data class GradeHeader(val level: String, val grade: String, val isExpanded: Boolean) : ClassroomListItem()
        data class SectionHeader(val level: String, val grade: String, val isExpanded: Boolean) : ClassroomListItem()
        data class ClassroomItem(val id: String, val name: String, val isInsideSection: Boolean) : ClassroomListItem()
    }

    companion object {
        const val TYPE_LEVEL = 1
        const val TYPE_GRADE = 2
        const val TYPE_SECTION = 3
        const val TYPE_CLASSROOM = 4
    }

    override fun getItemViewType(position: Int): Int {
        return when (displayList[position]) {
            is ClassroomListItem.LevelHeader -> TYPE_LEVEL
            is ClassroomListItem.GradeHeader -> TYPE_GRADE
            is ClassroomListItem.SectionHeader -> TYPE_SECTION
            is ClassroomListItem.ClassroomItem -> TYPE_CLASSROOM
        }
    }

    class LevelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvLevelName)
        val ivExpand: android.widget.ImageView = view.findViewById(R.id.ivLevelExpand)
    }

    class GradeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvGradeName)
        val ivExpand: android.widget.ImageView = view.findViewById(R.id.ivGradeExpand)
    }

    class SectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivExpand: android.widget.ImageView = view.findViewById(R.id.ivSectionExpand)
    }

    class ClassroomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvClassroomName)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteClassroom)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_LEVEL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_level_header, parent, false)
                LevelViewHolder(view)
            }
            TYPE_GRADE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_grade_header, parent, false)
                GradeViewHolder(view)
            }
            TYPE_SECTION -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_section_header, parent, false)
                SectionViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_classroom, parent, false)
                ClassroomViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = displayList[position]
        when (holder) {
            is LevelViewHolder -> {
                item as ClassroomListItem.LevelHeader
                holder.tvName.text = item.level
                holder.ivExpand.rotation = if (item.isExpanded) 180f else 0f
                holder.itemView.setOnClickListener {
                    if (item.isExpanded) expandedLevels.remove(item.level) else expandedLevels.add(item.level)
                    rebuildList()
                }
            }
            is GradeViewHolder -> {
                item as ClassroomListItem.GradeHeader
                holder.tvName.text = item.grade
                holder.ivExpand.rotation = if (item.isExpanded) 180f else 0f
                holder.itemView.setOnClickListener {
                    val key = "${item.level}|${item.grade}"
                    if (item.isExpanded) expandedGrades.remove(key) else expandedGrades.add(key)
                    rebuildList()
                }
            }
            is SectionViewHolder -> {
                item as ClassroomListItem.SectionHeader
                holder.ivExpand.rotation = if (item.isExpanded) 180f else 0f
                holder.itemView.setOnClickListener {
                    val key = "${item.level}|${item.grade}|Sections"
                    if (item.isExpanded) expandedSections.remove(key) else expandedSections.add(key)
                    rebuildList()
                }
            }
            is ClassroomViewHolder -> {
                item as ClassroomListItem.ClassroomItem
                holder.tvName.text = item.name
                
                val params = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
                params.marginStart = if (item.isInsideSection) 160 else 100 
                holder.itemView.layoutParams = params

                holder.btnDelete.setOnClickListener { onDeleteClick(item.id) }
                holder.itemView.setOnClickListener { onItemClick?.invoke(item.id, item.name) }
            }
        }
    }

    override fun getItemCount() = displayList.size

    fun updateClassroomsRaw(newRaw: List<Map<String, Any>>) {
        this.rawClassrooms = newRaw
        rebuildList()
    }

    private fun rebuildList() {
        val newList = mutableListOf<ClassroomListItem>()
        
        val byLevel = rawClassrooms.groupBy { it["level"]?.toString() ?: "Sin Nivel" }
        for ((level, listByLevel) in byLevel.toSortedMap(compareBy { it })) {
            val isLevelExpanded = expandedLevels.contains(level)
            newList.add(ClassroomListItem.LevelHeader(level, isLevelExpanded))
            
            if (isLevelExpanded) {
                val byGrade = listByLevel.groupBy { it["grade"]?.toString() ?: "Sin Grado" }
                for ((grade, listByGrade) in byGrade.toSortedMap(compareBy { it })) {
                    val key = "$level|$grade"
                    val isGradeExpanded = expandedGrades.contains(key)
                    newList.add(ClassroomListItem.GradeHeader(level, grade, isGradeExpanded))
                    
                    if (isGradeExpanded) {
                        if (level.equals("Inicial", ignoreCase = true)) {
                            for (classroom in listByGrade) {
                                val id = classroom["id"]?.toString() ?: ""
                                val name = classroom["name"]?.toString() ?: ""
                                newList.add(ClassroomListItem.ClassroomItem(id, name, false))
                            }
                        } else {
                            val sectionKey = "$level|$grade|Sections"
                            val isSectionExpanded = expandedSections.contains(sectionKey)
                            newList.add(ClassroomListItem.SectionHeader(level, grade, isSectionExpanded))
                            
                            if (isSectionExpanded) {
                                for (classroom in listByGrade.sortedBy { it["name"].toString() }) {
                                    val id = classroom["id"]?.toString() ?: ""
                                    // Make name shorter if possible by replacing " - Secundaria"
                                    var name = classroom["name"]?.toString() ?: ""
                                    name = name.replace(" - Secundaria", "").replace(" - Primaria", "")
                                    newList.add(ClassroomListItem.ClassroomItem(id, name, true))
                                }
                            }
                        }
                    }
                }
            }
        }
        
        displayList = newList
        notifyDataSetChanged()
    }

    // Backward compatibility for other activities using the old method
    fun updateClassrooms(newClassrooms: List<Pair<String, String>>) {
        val mappedList = newClassrooms.map { pair ->
            mapOf(
                "id" to pair.first,
                "name" to pair.second,
                "level" to "General",
                "grade" to "Listado"
            )
        }
        updateClassroomsRaw(mappedList)
    }
}
