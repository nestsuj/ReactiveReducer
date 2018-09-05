package nestsuj.apps.reactivereducerdemo.controllers

import android.os.Bundle
import android.util.TimingLogger
import android.view.Menu
import android.view.MenuItem
import android.view.View.*
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import nestsuj.apps.reactivereducerdemo.AppController
import nestsuj.apps.reactivereducerdemo.R
import nestsuj.apps.reactivereducerdemo.extensions.log
import nestsuj.apps.reactivereducerdemo.states.AppState
import nestsuj.apps.reactivereducerdemo.states.AuthenticationState
import nestsuj.apps.reactivereducerdemo.states.LoginStatus
import org.rekotlin.Store
import org.rekotlin.StoreSubscriber
import javax.inject.Inject


class MainActivity : AppCompatActivity(), StoreSubscriber<AuthenticationState> {
    @Inject
    lateinit var mainStore: Store<AppState>

    private lateinit var timingLogger: TimingLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        timingLogger = TimingLogger(this.javaClass.simpleName, "onCreate - started")

        AppController.appComponent.inject(this)
        timingLogger.addSplit("onCreate - injected")

        setContentView(R.layout.activity_main)
        timingLogger.addSplit("onCreate - content view set")

        setupActionBarWithNavController(main_nav_host_fragment.findNavController())
        bottom_navigation.setupWithNavController(main_nav_host_fragment.findNavController())

        timingLogger.addSplit("onCreate - configured menus")

        timingLogger.addSplit("onCreate - subscribed to state changes")
    }

    override fun onResume() {
        super.onResume()

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.let {
            menuInflater.inflate(R.menu.navigation, it)
            return true
        }

        return false
    }

    override fun newState(state: AuthenticationState) {
        log("Received new state: $state")

        this.bottom_navigation.visibility = if (state.loginStatus == LoginStatus.LOGGED_IN) { VISIBLE } else { GONE }
        this.progressbar.visibility = if (state.loginStatus == LoginStatus.LOADING) { VISIBLE } else { GONE }

        if (state.loginStatus != LoginStatus.LOGGED_IN && main_nav_host_fragment.findNavController().currentDestination?.id != R.id.loginFragment) {

            main_nav_host_fragment.findNavController().navigate(R.id.action_global_authenticationGraph)
            timingLogger.addSplit("newState - Navigating to login fragment")
            timingLogger.dumpToLog()
        }
    }

    override fun onSupportNavigateUp() = main_nav_host_fragment.findNavController().navigateUp()
}
