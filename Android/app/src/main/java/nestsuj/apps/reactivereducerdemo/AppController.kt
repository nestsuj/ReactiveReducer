package nestsuj.apps.reactivereducerdemo

import android.app.Application
import android.util.TimingLogger
import com.squareup.leakcanary.LeakCanary
import nestsuj.apps.reactivereducerdemo.di.*

private const val TODOLY_SERVICE_URL = "https://todo.ly/api/"

class AppController : Application() {
    override fun onCreate() {
        super.onCreate()

        val timingLogger = TimingLogger(this.javaClass.simpleName, "onCreate - started")

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)

        timingLogger.addSplit("onCreate - leak canary installed")

        instance = this

        appComponent = DaggerAppComponent.builder()
                .applicationModule(ApplicationModule(this))
                .serviceModule(ServiceModule(TODOLY_SERVICE_URL))
                .reduxModule(ReduxModule())
                .build()

        timingLogger.addSplit("onCreate - app component configured")
    }

    companion object {
        lateinit var instance: AppController
        lateinit var appComponent: AppComponent
    }
}