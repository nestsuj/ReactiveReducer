package nestsuj.apps.reactivereducerdemo.di

import dagger.Component
import nestsuj.apps.reactivereducerdemo.controllers.LoginFragment
import nestsuj.apps.reactivereducerdemo.controllers.MainActivity
import nestsuj.apps.reactivereducerdemo.controllers.TodosFragment
import nestsuj.apps.reactivereducerdemo.controllers.ProjectsFragment
import javax.inject.Singleton

@Component(modules = [ApplicationModule::class, ServiceModule::class, ReduxModule::class])
@Singleton
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(loginFragment: LoginFragment)
    fun inject(projectsFragment: ProjectsFragment)
    fun inject(todosFragment: TodosFragment)
}