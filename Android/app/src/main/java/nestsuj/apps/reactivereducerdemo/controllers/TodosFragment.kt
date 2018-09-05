package nestsuj.apps.reactivereducerdemo.controllers


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_todo_items.view.*
import kotlinx.android.synthetic.main.item_todo.view.*
import nestsuj.apps.reactivereducerdemo.AppController

import nestsuj.apps.reactivereducerdemo.R
import nestsuj.apps.reactivereducerdemo.actions.creators.TodoActionCreator
import nestsuj.apps.reactivereducerdemo.extensions.log
import nestsuj.apps.reactivereducerdemo.services.Todo
import nestsuj.apps.reactivereducerdemo.states.AppState
import nestsuj.apps.reactivereducerdemo.states.ProjectsStatus
import nestsuj.apps.reactivereducerdemo.states.TodosState
import nestsuj.apps.reactivereducerdemo.states.TodosStatus
import org.rekotlin.Store
import org.rekotlin.StoreSubscriber
import javax.inject.Inject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class TodosFragment : Fragment(), StoreSubscriber<TodosState> {
    @Inject
    lateinit var mainStore: Store<AppState>

    @Inject
    lateinit var todoItemActionCreator: TodoActionCreator

    private lateinit var todoItemAdapter: TodoItemAdapter

    private var projectId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppController.appComponent.inject(this)

        arguments?.let {
            projectId = TodosFragmentArgs.fromBundle(it).projectId
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_todo_items, container, false)
        todoItemAdapter = TodoItemAdapter()
        view.todo_items.adapter = todoItemAdapter
        view.todo_items.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        log("View created")

        return view
    }

    override fun onResume() {
        super.onResume()

        mainStore.subscribe(this) {subscription ->
            subscription.select {appState ->
                appState.todosState
            }.skipRepeats { oldState, newState ->
                oldState == newState
            }
        }
    }

    override fun onDestroyView() {
        view?.todo_items?.adapter = null
        super.onDestroyView()
    }

    override fun onStop() {
        super.onStop()

        mainStore.unsubscribe(this)
    }

    override fun newState(state: TodosState) {
        log("Received new state: $state")

        when (state.status) {
            TodosStatus.LOADING -> { /* DO NOTHING - or rather fix progress bar*/}
            TodosStatus.SUCCESS -> {
                todoItemAdapter.notifyDataSetChanged()
                projectId?.let {
                    mainStore.dispatch(todoItemActionCreator.createFetchTodosAction(it))
                    if (projectId == state.projectId)  {
                        todoItemAdapter.submitList(state.todos)
                    }
                }
            }
            TodosStatus.FAILURE -> Toast.makeText(context, state.errorMessage, Toast.LENGTH_LONG).show()
        }
    }
}

class TodoItemAdapter : ListAdapter<Todo, TodoItemViewHolder>(TodoItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TodoItemViewHolder(inflater.inflate(R.layout.item_todo, parent, false))
    }

    override fun onBindViewHolder(holder: TodoItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class TodoItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(todo: Todo) {
        itemView.item_todo_title.text = todo.content
    }
}

class TodoItemDiffCallback : DiffUtil.ItemCallback<Todo>() {
    override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
        return oldItem == newItem
    }

}