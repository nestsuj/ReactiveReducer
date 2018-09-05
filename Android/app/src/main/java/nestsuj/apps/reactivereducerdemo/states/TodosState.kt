package nestsuj.apps.reactivereducerdemo.states

import nestsuj.apps.reactivereducerdemo.services.Todo
import org.rekotlin.StateType

data class TodosState(
        val projectId : Int? = null,
        val todos: List<Todo> = emptyList(),
        val status: TodosStatus = TodosStatus.SUCCESS,
        val errorMessage: String? = null
) : StateType

enum class TodosStatus {
    SUCCESS,
    FAILURE,
    LOADING,
}