package nestsuj.apps.reactivereducerdemo.states

import org.rekotlin.StateType

data class AppState(
        var authenticationState: AuthenticationState,
        var projectsState: ProjectsState,
        var todosState: TodosState
): StateType

data class AuthenticationState(
        val username: String? = null,
        val loginStatus: LoginStatus = LoginStatus.LOGGED_OUT,
        val errorMessage: String? = null
): StateType

enum class LoginStatus {
    LOGGED_IN,
    LOADING,
    FAILED,
    LOGGED_OUT
}