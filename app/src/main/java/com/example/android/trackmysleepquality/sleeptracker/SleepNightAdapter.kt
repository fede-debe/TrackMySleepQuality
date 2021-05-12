package com.example.android.trackmysleepquality.sleeptracker

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.TextItemViewHolder
import com.example.android.trackmysleepquality.database.SleepNight

class SleepNightAdapter: RecyclerView.Adapter<TextItemViewHolder>() {
    // this will be the data that the adapter is adapting for RecyclerView to use. The RV won't use data directly, it won't even know it exists.
    // We'll use the adapter to expose or adapt data into the RV API
    var data = listOf<SleepNight>()
    // update the list whenever the data changes
    set(value) {
        field =  value
        notifyDataSetChanged()
    }

    // the RV needs to know how many items to display, return the total number of items in the dataset held by the adapter.
    override fun getItemCount(): Int = data.size

    // Called by RV to display the data at the specified position. This method update the views held by the ViewHolder to show the item at the position passed.
    override fun onBindViewHolder(holder: TextItemViewHolder, position: Int) {
        // RV told us the position that needs to be bound, we can just look it up in our data property
        val item = data[position]
        // we just set the text of the view held in the ViewHolder. The ViewHolder we provided has a property called textView.
        holder.textView.text = item.sleepQuality.toString()
    }

    // tell RV how to create a new view holder. The RV does everything in terms of ViewHolders.
    // Whenever a RV needs a new ViewHolder it will ask for one, our job is to give it whenever it asks. To display a ViewHolder it needs to be passed to a ViewGroup.
    // to actually make a ViewHolder, you'll need to make a view for it to hold.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextItemViewHolder {
        // android support creating views from XML  from anywhere you have access to another view. The same is valid for the LayoutInflater, you just need to pass a context to it.
        // parent.context = this means  you will create a LayoutInflater based on the parent view. LayoutInflater uses that information to inflate new views correctly.
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.text_item_view, parent, false) as TextView
        return TextItemViewHolder(view)

    }




}