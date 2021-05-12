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
import com.example.android.trackmysleepquality.databinding.FragmentSleepTrackerBinding
import com.google.android.material.snackbar.Snackbar

class SleepTrackerFragment : Fragment() {

    private lateinit var binding: FragmentSleepTrackerBinding
    private lateinit var sleepTrackerViewModel: SleepTrackerViewModel
    private lateinit var adapter: SleepNightAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_sleep_tracker, container, false)

        setUpDatabase()
        setUpObserver()
        setUpSnackBar()

        return binding.root
    }

    private fun setUpDatabase(){
        // get the application - this method throws an illegal argument exception if the value is null
        val application = requireNotNull(this.activity).application

        // reference to a data source via reference to the DAO
        val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao

        // create an instance of the Factory, we pass the dataSource as well as the application
        val viewModelFactory = SleepTrackerViewModelFactory(dataSource, application)

        // Now that we have a Factory we ask the ViewModelProvider for a SleepTrackerViewModel (still need to connect the ViewModel to the user interface)
        sleepTrackerViewModel = ViewModelProvider(this, viewModelFactory).get(SleepTrackerViewModel::class.java)

        // set variable that access through the binding object to the ViewModel
        binding.sleepTrackerViewModel = sleepTrackerViewModel

        adapter = SleepNightAdapter()
        binding.sleepList.adapter = adapter

        // specify a current activity as the lifecycle owner of the binding ( binding can now observe LiveData updates)
        binding.lifecycleOwner = this
    }

    private fun setUpObserver() {
        // we need to observe "navigateSleepQuality" so we know when to navigate.
        sleepTrackerViewModel.navigationToSleepQuality.observe(viewLifecycleOwner, Observer { night ->
            night?.let {
                this.findNavController().navigate(SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepQualityFragment(night.nightId))
                sleepTrackerViewModel.doneNavigating() // reset the navigation's variable for the next navigation.
            }
        })

        sleepTrackerViewModel.nights.observe(viewLifecycleOwner, Observer {
            adapter.data = it
        })
    }

    private fun setUpSnackBar() {

        sleepTrackerViewModel.showSnackBarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    getString(R.string.cleared_message),
                    Snackbar.LENGTH_SHORT
                ).show()
                sleepTrackerViewModel.doneShowingSnackBar()
            }
        })

    }

}
