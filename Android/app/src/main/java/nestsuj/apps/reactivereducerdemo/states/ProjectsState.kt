package nestsuj.apps.reactivereducerdemo.states

import nestsuj.apps.reactivereducerdemo.services.Project
import org.rekotlin.StateType

data class ProjectsState(
        val projects: List<Project> = emptyList(),
        val status: ProjectsStatus = ProjectsStatus.SUCCESS,
        val errorMessage: String? = null
) : StateType

enum class ProjectsStatus {
    SUCCESS,
    FAILURE,
    LOADING,
}