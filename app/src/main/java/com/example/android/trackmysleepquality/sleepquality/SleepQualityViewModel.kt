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

package com.example.android.trackmysleepquality.sleepquality

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import kotlinx.coroutines.launch

class SleepQualityViewModel(
    // we need to pass the key we got from the navigation
    private val sleepNightKey: Long = 0L,
    val database: SleepDatabaseDao) : ViewModel() {

        // event variable with the backing property
        private val _navigateToSleepTracker = MutableLiveData<Boolean?>()
        val navigateToSleepTracker: LiveData<Boolean?>
        get() =_navigateToSleepTracker

    // reset the status when the user is back to the other fragment
    fun doneNavigating() {
        _navigateToSleepTracker.value = null
    }

    fun onSetSleepQuality(quality: Int) {
        viewModelScope.launch{
            // get tonight using the sleepNightKey
            val tonight = database.get(sleepNightKey)
            tonight.sleepQuality = quality // set sleepQuality
            database.update(tonight) // update the database
            _navigateToSleepTracker.value = true // trigger navigation
        }
    }

    }