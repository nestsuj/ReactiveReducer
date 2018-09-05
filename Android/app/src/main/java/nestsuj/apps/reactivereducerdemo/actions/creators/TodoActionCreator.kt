package nestsuj.apps.reactivereducerdemo.actions.creators

import android.util.Log
import android.util.TimingLogger
import nestsuj.apps.reactivereducerdemo.AppExecutors
import nestsuj.apps.reactivereducerdemo.actions.FetchTodosFailedAction
import nestsuj.apps.reactivereducerdemo.actions.FetchTodosStartedAction
import nestsuj.apps.reactivereducerdemo.actions.FetchTodosSuccededAction
import nestsuj.apps.reactivereducerdemo.services.Todo
import nestsuj.apps.reactivereducerdemo.services.TodolyService
import nestsuj.apps.reactivereducerdemo.states.AppState
import org.rekotlin.AsyncActionCreator
import org.rekotlin.DispatchFunction
import org.rekotlin.StoreType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class TodoActionCreator @Inject constructor(
        private val todolyService: TodolyService,
        private val appExecutors: AppExecutors
) {
    fun createFetchTodosAction(projectId : Int) : AsyncActionCreator<AppState, StoreType<AppState>> {
        val timingLogger = TimingLogger("FetchTodosAction", "Created")

        return { _, store, _ ->
            store.dispatch(FetchTodosStartedAction(projectId, timingLogger))
            timingLogger.addSplit("About to start executing fetch todos task")
            executeFetchTodosTask(projectId, store.dispatchFunction, timingLogger)
            timingLogger.addSplit("Fetch todos task is executing")
        }
    }

    private fun executeFetchTodosTask(projectId: Int, dispatch: DispatchFunction, timingLogger: TimingLogger) {
        appExecutors.networkIO().execute {
            todolyService.getTodos(projectId).enqueue(object: Callback<List<Todo>> {
                override fun onFailure(call: Call<List<Todo>>, t: Throwable) {
                    Log.d("TodoActionCreator", "Failed request for url: ${call.request().url()}")
                    timingLogger.addSplit("Invoking FetchTodosFailedAction due to thrown exception")
                    dispatch.invoke(FetchTodosFailedAction(projectId, t.message ?: t.message ?: "Unknown error", t, timingLogger))
                    timingLogger.addSplit("Invoked FetchTodosFailedAction")
                }

                override fun onResponse(call: Call<List<Todo>>, response: Response<List<Todo>>) {
                    Log.d("TodoActionCreator", "Successfully requested url: ${call.request().url()}")

                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let {
                            timingLogger.addSplit("Invoking FetchTodosSucceededAction")
                            dispatch.invoke(FetchTodosSuccededAction(projectId, it, timingLogger))
                            timingLogger.addSplit("Invoked FetchTodosSucceededAction")
                        }
                    } else {
                        timingLogger.addSplit("Invoking FetchTodosFailedAction due to failed response")
                        dispatch.invoke(FetchTodosFailedAction(projectId, response.errorBody()?.string()?: response.message(), timingLogger = timingLogger))
                        timingLogger.addSplit("Invoked FetchTodosFailedAction")
                    }
                }

            })

        }
    }
}