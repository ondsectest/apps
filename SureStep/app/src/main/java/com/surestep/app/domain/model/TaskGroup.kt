package com.surestep.app.domain.model

enum class TaskGroup(val label: String) {
    HOME("Home"),
    OFFICE("Office"),
    TRAVEL("Travel"),
    SHOPPING("Shopping"),
    CUSTOM("Custom");

    companion object {
        fun fromName(name: String?): TaskGroup =
            entries.firstOrNull { it.name == name } ?: CUSTOM
    }
}
