
package com.example.android.trackmysleepquality.sleeptracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.databinding.FragmentSleepTrackerBinding
import com.google.android.material.snackbar.Snackbar

class SleepTrackerFragment : Fragment() {

    private lateinit var binding: FragmentSleepTrackerBinding
    private lateinit var sleepTrackerViewModel: SleepTrackerViewModel
    private lateinit var adapter: SleepNightAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sleep_tracker, container, false)

        setUpUI()
        setUpObserver()

        return binding.root
    }

    private fun setUpUI(){
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

        // add a GridLayout to the RV and set it as LayoutManager
        val manager = GridLayoutManager(activity, 3)
        binding.sleepList.layoutManager = manager

        // we only define a callback to display the nightId related to the item that was clicked in a Toast.
        adapter = SleepNightAdapter(SleepNightListener {
                nightId -> sleepTrackerViewModel.onSleepNightClicked(nightId)
        })

        // tell the RV about the adapter
        binding.sleepList.adapter = adapter

        // specify a current activity as the lifecycle owner of the binding ( binding can now observe LiveData updates)
        binding.lifecycleOwner = this
    }

    private fun setUpObserver() {
        // we need to observe "navigateSleepQuality" so we know when to navigate.
        sleepTrackerViewModel.navigateToSleepQuality.observe(viewLifecycleOwner, Observer { night ->
            night?.let {
                this.findNavController().navigate(SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepQualityFragment(night.nightId))
                sleepTrackerViewModel.doneNavigating() // reset the navigation's variable for the next navigation.
            }
        })

        sleepTrackerViewModel.navigateToSleepDataQuality.observe(viewLifecycleOwner, Observer { night ->
            night?.let {
                this.findNavController().navigate(SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepDetailFragment(night))
                sleepTrackerViewModel.onSleepDataQualityNavigated()
            }
        })

        // we need to tell the adapter what data it should be adapting.The viewModel has a list of sleepNights available already and we can observe it.
        //   with "viewLifecycleOwner" we can make sure that the  observer is only around when the RV is still on screen. When we get a NOT null value, we just assign it to the adapter's data
        sleepTrackerViewModel.nights.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

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
