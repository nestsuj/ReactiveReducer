package nestsuj.apps.reactivereducerdemo.actions.creators

import android.util.TimingLogger
import nestsuj.apps.reactivereducerdemo.AppExecutors
import nestsuj.apps.reactivereducerdemo.actions.LoadProjectsFailedAction
import nestsuj.apps.reactivereducerdemo.actions.LoadProjectsStartedAction
import nestsuj.apps.reactivereducerdemo.actions.LoadProjectsSucceededAction
import nestsuj.apps.reactivereducerdemo.services.Project
import nestsuj.apps.reactivereducerdemo.services.TodolyService
import nestsuj.apps.reactivereducerdemo.states.AppState
import org.rekotlin.AsyncActionCreator
import org.rekotlin.DispatchFunction
import org.rekotlin.StoreType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class ProjectActionCreator @Inject constructor(private val todolyService: TodolyService, private val appExecutors: AppExecutors) {
    fun createLoadProjectsAction() : AsyncActionCreator<AppState, StoreType<AppState>> {
        val timingLogger = TimingLogger("LoadProjectsAction", "Created")

        return { _, store, _ ->
            store.dispatch(LoadProjectsStartedAction(timingLogger))
            timingLogger.addSplit("About to start executing load projects task")
            executeLoadProjectsTask(store.dispatchFunction, timingLogger)
            timingLogger.addSplit("Load projects task is executing")
        }
    }

    private fun executeLoadProjectsTask(dispatch: DispatchFunction, timingLogger: TimingLogger) {
        appExecutors.networkIO().execute {
            todolyService.getProjects().enqueue(object: Callback<List<Project>> {
                override fun onFailure(call: Call<List<Project>>, t: Throwable) {
                    timingLogger.addSplit("Invoking LoadProjectsFailedAction due to thrown exception")
                    dispatch.invoke(LoadProjectsFailedAction(t.message ?: "", t, timingLogger))
                    timingLogger.addSplit("Invoked LoadProjectsFailedAction")
                }

                override fun onResponse(call: Call<List<Project>>, response: Response<List<Project>>) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let {
                            timingLogger.addSplit("Invoking LoadProjectsSucceededAction")
                            dispatch.invoke(LoadProjectsSucceededAction(it, timingLogger))
                            timingLogger.addSplit("Invoked LoadProjectsSucceededAction")
                        }
                    } else {
                        timingLogger.addSplit("Invoking LoadProjectsFailedAction due to failed response")
                        dispatch.invoke(LoadProjectsFailedAction(response.errorBody()?.string()?: response.message(), timingLogger = timingLogger))
                        timingLogger.addSplit("Invoked LoadProjectsFailedAction")
                    }
                }
            })
        }
    }
}