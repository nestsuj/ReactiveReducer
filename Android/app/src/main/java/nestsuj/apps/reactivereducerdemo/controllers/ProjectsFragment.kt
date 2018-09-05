package nestsuj.apps.reactivereducerdemo.controllers


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_projects.view.*
import kotlinx.android.synthetic.main.item_project.view.*
import nestsuj.apps.reactivereducerdemo.AppController

import nestsuj.apps.reactivereducerdemo.R
import nestsuj.apps.reactivereducerdemo.actions.creators.ProjectActionCreator
import nestsuj.apps.reactivereducerdemo.extensions.log
import nestsuj.apps.reactivereducerdemo.services.Project
import nestsuj.apps.reactivereducerdemo.states.AppState
import nestsuj.apps.reactivereducerdemo.states.ProjectsState
import nestsuj.apps.reactivereducerdemo.states.ProjectsStatus
import org.rekotlin.Store
import org.rekotlin.StoreSubscriber
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 *
 */
class ProjectsFragment : Fragment(), StoreSubscriber<ProjectsState> {
    @Inject
    lateinit var mainStore: Store<AppState>

    @Inject
    lateinit var projectActionCreator: ProjectActionCreator

    private lateinit var projectItemAdapter: ProjectItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppController.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_projects, container, false)
        projectItemAdapter = ProjectItemAdapter {
            project ->
            val action = ProjectsFragmentDirections.actionProjectsFragmentToProjectDetailsFragment(project.id)
            view?.findNavController()?.navigate(action)

        }
        view.project_items.adapter = projectItemAdapter
        view.project_items.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        log("View created")

        return view
    }

    override fun onResume() {
        super.onResume()

        mainStore.subscribe(this) {subscription ->
            subscription.select {appState ->
                appState.projectsState
            }.skipRepeats { oldState, newState ->
                oldState == newState
            }
        }
    }

    override fun onDestroyView() {
        view?.project_items?.adapter = null
        super.onDestroyView()
    }

    override fun onStop() {
        super.onStop()

        mainStore.unsubscribe(this)
    }

    override fun newState(state: ProjectsState) {
        log("Received new state: $state")

        when (state.status) {
            ProjectsStatus.LOADING -> { /* DO NOTHING - or rather fix progress bar*/}
            ProjectsStatus.SUCCESS -> {
                projectItemAdapter.submitList(state.projects)
                projectItemAdapter.notifyDataSetChanged()
                if (state.projects.isEmpty()) {
                    mainStore.dispatch(projectActionCreator.createLoadProjectsAction())
                }
            }
            ProjectsStatus.FAILURE -> Toast.makeText(context, state.errorMessage, Toast.LENGTH_LONG).show()
        }
    }
}

class ProjectItemAdapter(private val clickListener : (Project) -> Unit ) : ListAdapter<Project, ProjectItemViewHolder>(ProjectDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ProjectItemViewHolder(inflater.inflate(R.layout.item_project, parent, false))
    }

    override fun onBindViewHolder(holder: ProjectItemViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

}

class ProjectItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(project: Project, clickListener: (Project) -> Unit) {
        itemView.item_project_title.text = project.content
        itemView.setOnClickListener { clickListener(project)}
    }
}

class ProjectDiffCallback : DiffUtil.ItemCallback<Project>() {
    override fun areItemsTheSame(oldItem: Project, newItem: Project): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Project, newItem: Project): Boolean {
        return oldItem == newItem
    }

}