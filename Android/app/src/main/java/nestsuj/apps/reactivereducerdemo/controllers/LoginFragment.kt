package nestsuj.apps.reactivereducerdemo.controllers


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_login.view.*
import nestsuj.apps.reactivereducerdemo.AppController

import nestsuj.apps.reactivereducerdemo.R
import nestsuj.apps.reactivereducerdemo.actions.creators.AuthActionCreator
import nestsuj.apps.reactivereducerdemo.extensions.log
import nestsuj.apps.reactivereducerdemo.states.AppState
import nestsuj.apps.reactivereducerdemo.states.AuthenticationState
import nestsuj.apps.reactivereducerdemo.states.LoginStatus
import org.rekotlin.Store
import org.rekotlin.StoreSubscriber
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 *
 */
class LoginFragment : Fragment(), StoreSubscriber<AuthenticationState> {
    @Inject
    lateinit var mainStore: Store<AppState>

    @Inject
    lateinit var authActionCreator: AuthActionCreator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppController.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        view.login_as_auric_button.setOnClickListener {
            mainStore.dispatch(authActionCreator.createLoginAction("auric2273@gmail.com", "c3Tu0U"))
        }

        view.login_button.setOnClickListener {
            mainStore.dispatch(
                    authActionCreator.createLoginAction(view.username.text.toString(), view.password.text.toString())
            )
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        (context as? MainActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)

        mainStore.subscribe(this) { subscription ->
            subscription.select { appState ->
                appState.authenticationState
            }.skipRepeats { oldState, newState ->
                oldState == newState
            }
        }
    }

    override fun onStop() {
        super.onStop()

        mainStore.unsubscribe(this)
    }

    override fun newState(state: AuthenticationState) {
        log("New state: $state")
        when (state.loginStatus) {
            LoginStatus.LOGGED_IN -> findNavController().popBackStack()
            LoginStatus.FAILED -> {
                //Failed to login user ${state.username} due to error ${state.errorMessage}
                view?.error_message?.text = getString(R.string.display_authentication_error)
                        .replace("{username}", state.username)
                        .replace("{errorMessage}", state.errorMessage)
            }
            else -> { /* DO NOTHING */ }
        }
    }
}

private fun String.replace(pattern: String, text: String?): String {
    return this.replace(pattern, text ?: "", true)
}
