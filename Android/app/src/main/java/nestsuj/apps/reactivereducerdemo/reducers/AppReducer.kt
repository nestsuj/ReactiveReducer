package nestsuj.apps.reactivereducerdemo.reducers

import android.util.Log
import nestsuj.apps.reactivereducerdemo.actions.*
import nestsuj.apps.reactivereducerdemo.states.*
import org.rekotlin.Action

fun appReducer(action: Action, oldState: AppState?): AppState {
    Log.d("appReducer", "received action -> $action")

    if (action is TimedAction) {
        action.timingLogger.addSplit("Received ${action.javaClass.simpleName} in reducer chain")
    }

    val state = oldState ?: AppState(AuthenticationState(), ProjectsState(), TodosState())

    val newState = state.copy(
            authenticationState = (::authenticationReducer)(action, state.authenticationState),
            projectsState = (::projectsReducer)(action, state.projectsState),
            todosState = (::todosReducer)(action, state.todosState)
    )

    if (action is TimedAction) {
        action.timingLogger.addSplit("Finished ${action.javaClass.simpleName} in reducer chain")
    }

    if (action is FinalTimedAction) {
        action.timingLogger.dumpToLog()
    }

    return newState
}

fun authenticationReducer(action: Action, state: AuthenticationState?): AuthenticationState {
    val newState = state ?: AuthenticationState()

    when (action) {
        is LoginStartedAction -> return newState.copy(username = action.username, loginStatus = LoginStatus.LOADING)
        is LoginSucceededAction -> return newState.copy(username = action.username, loginStatus = LoginStatus.LOGGED_IN)
        is LoginFailedAction -> return newState.copy(username = action.username, loginStatus = LoginStatus.FAILED, errorMessage = action.message)
    }

    return newState
}

fun projectsReducer(action: Action, state: ProjectsState?) : ProjectsState {
    val newState = state ?: ProjectsState()

    when (action) {
        is LoadProjectsStartedAction -> return newState.copy(status = ProjectsStatus.LOADING)
        is LoadProjectsSucceededAction -> return newState.copy(projects = action.projects, status = ProjectsStatus.SUCCESS)
        is LoadProjectsFailedAction -> return newState.copy(status = ProjectsStatus.FAILURE, errorMessage = action.message)
    }

    return newState
}

fun todosReducer(action: Action, state: TodosState?) : TodosState {
    val newState = state ?: TodosState()

    when (action) {
        is FetchTodosStartedAction -> return newState.copy(projectId = action.projectId, status = TodosStatus.LOADING)
        is FetchTodosSuccededAction -> return newState.copy(projectId = action.projectId, todos = action.todos, status = TodosStatus.SUCCESS)
        is FetchTodosFailedAction -> return newState.copy(projectId = action.projectId, errorMessage = action.message, status = TodosStatus.FAILURE)
    }

    return newState
}