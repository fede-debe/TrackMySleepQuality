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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.databinding.FragmentSleepQualityBinding

class SleepQualityFragment : Fragment() {

    private lateinit var binding: FragmentSleepQualityBinding
    private lateinit var sleepQualityViewModel: SleepQualityViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_sleep_quality, container, false)

        setUpDatabase()
        setUpObserver()

        return binding.root
    }

    private fun setUpDatabase(){
        // we get the application
        val application = requireNotNull(this.activity).application

        // after we get the application we also need to get the arguments that come with the navigation
        val arguments = SleepQualityFragmentArgs.fromBundle(arguments!!)

        // get the data source
        val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao

        // create the factory and pass the dataSource and the key
        val viewModelFactory = SleepQualityViewModelFactory(arguments.sleepNightKey,  dataSource)

        // get the viewModel reference
        sleepQualityViewModel = ViewModelProvider(
            this, viewModelFactory)
            .get(SleepQualityViewModel::class.java)

        // add viewModel to data binding
        binding.sleepQualityViewModel = sleepQualityViewModel
    }

    private fun setUpObserver(){
        sleepQualityViewModel.navigateToSleepTracker.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                this.findNavController().navigate(SleepQualityFragmentDirections.actionSleepQualityFragmentToSleepTrackerFragment())
                sleepQualityViewModel.doneNavigating()
            }
        })
    }
}
