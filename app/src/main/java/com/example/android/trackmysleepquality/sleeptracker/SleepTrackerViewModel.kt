/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// Coroutines will handle clicking the buttons that triggers database operations

class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

        /**

        DEPRECATED
        The video above shows creating the CoroutineScope uiScope, and attaching the ViewModel Job. Creating own scope is no longer recommended by Google.
        The recommended way is to use a lifecycle-aware coroutine scope ViewModelScope provided by the Architecture components.


         // to handle a coroutine we need a Job, this allows us to cancel the coroutines related to this model when is destroyed.
                private var viewModelJob = Job()

        // method called when the viewModel is destroyed to cancel all the coroutines started by the viewModel
        override fun onCleared() {
                super.onCleared()
                // we tell the Job to cancel all the coroutines
                viewModelJob.cancel()
        }

        //we need a Scope ( determine what thread the coroutine will run (Dispatcher) on and needs to know about the job) to our Coroutine to run in.
        private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob) - DEPRECATED  */

        // we need a variable to handle the current night, LiveData because we want to observe it and Mutable because we want to be able to change it
        private var tonight = MutableLiveData<SleepNight?>()

        // we want to get all nights from the database when we create the viewModel. Room will take care of these LiveData and update it if something changes
        private val nights = database.getAllNights()

        // to see the content of the object inside the TextView, we need to transform this data into a "formatted string". Transformation.map is executed every time nights receives new data from the database
        val nightsString = Transformations.map(nights) { nights ->
                // we parse nights into the map function and define a mapping function as calling formatNights( Utils function)
                formatNights(nights, application.resources) // application.resources will give us access to the string resources
        }

        // we need to have the click Handler inside the viewModel and navigation belongs to the fragment. We set this LiveData that changes when we want to navigate
        // the fragment observe this LiveData and when it changes navigates, then tell the viewModel that it's done and reset the state's variable.
        private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
        val navigationToSleepQuality: LiveData<SleepNight>
        get() = _navigateToSleepQuality

        // reset the navigation variable
        fun doneNavigating(){
                _navigateToSleepQuality.value = null
        }

        // we need "tonight" set as soon as possible to work with it
        init {
                initializeTonight()
        }

        // we use a coroutine to get "tonight" from the database to not block the UI while we are waiting
        private fun initializeTonight(){
                // .launch creates the coroutine without blocking the current thread inside the provided context we get from viewModelScope
                viewModelScope.launch {
                        tonight.value = getTonightFromDatabase()
                }
        }

        // marked as suspend to be able to call it inside a coroutine and not block, we return a SleepNight or null
        private suspend fun getTonightFromDatabase(): SleepNight? {
                // we get the night from the database
                var night = database.getTonight()
                // if the start and end time are not the same ,the night has been completed, otherwise return the night in progress
                if (night?.endTimeMilli != night?.startTimeMilli){
                        night = null
                }
                return night
        }

        // suspend function for database operation
        private suspend fun insert(night: SleepNight){
                database.insert(night)
        }

        // we use another coroutine because this execution is time consuming
        fun onStartTracking() {
                viewModelScope.launch {
                       // create new sleep night to catch the current time as start time
                        val newNight = SleepNight()
                        // insert the newNight inside the database
                        insert(newNight)
                        // we set tonight as the newNight by calling this suspend function operation
                        tonight.value = getTonightFromDatabase()
                }
        }

        fun onStopTracking(){
                viewModelScope.launch {
                        // return@labelSyntax is used for specifying which function among several nested ones this statement returns from (here we specify the return from launch, not the lambda)
                        val oldNight = tonight.value ?: return@launch
                        // we update the endTime as currentTime
                        oldNight.endTimeMilli = System.currentTimeMillis()
                        // we call this function to update the database
                        update(oldNight)
                        // trigger the navigation to the other fragment, this var is not null only when we can set a sleep quality. If we can't set it we don't navigate.
                        _navigateToSleepQuality.value = oldNight

                }
        }

        private suspend fun update(night: SleepNight){
                database.update(night)
        }


        fun onClear(){
                viewModelScope.launch {
                        clear()
                        tonight.value = null
                }
        }

        private suspend fun clear(){
                database.clear()
        }



}

