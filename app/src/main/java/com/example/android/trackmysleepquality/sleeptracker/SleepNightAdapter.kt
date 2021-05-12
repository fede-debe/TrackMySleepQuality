package com.example.android.trackmysleepquality.sleeptracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.convertDurationToFormatted
import com.example.android.trackmysleepquality.convertNumericQualityToString
import com.example.android.trackmysleepquality.database.SleepNight

class SleepNightAdapter: RecyclerView.Adapter<SleepNightAdapter.ViewHolder>() {
    // this will be the data that the adapter is adapting for RecyclerView to use. The RV won't use data directly, it won't even know it exists.
    // We'll use the adapter to expose or adapt data into the RV API
    // update the list with a custom setter whenever the data changes, and to notify it we can call this adapter method. If the items were more complex than textBoxes, this way can be a pretty slow operation.
    var data = listOf<SleepNight>()
    set(value) {
        field =  value
        notifyDataSetChanged()
    }

    // the RV needs to know how many items to display, return the total number of items in the dataset held by the adapter.
    override fun getItemCount(): Int = data.size

    // Called by RV to display the data at the specified position. This method update the views held by the ViewHolder to show the item at the position passed.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // RV told us the position that needs to be bound, we can just look it up in our data property
        val item = data[position]
        // in this way we encapsulate the code to actually display the View
        holder.bind(item)
    }

    // tell RV how to create a new view holder. The RV does everything in terms of ViewHolders.
    // Whenever a RV needs a new ViewHolder it will ask for one, our job is to give it whenever it asks. To display a ViewHolder it needs to be passed to a ViewGroup.
    // to actually make a ViewHolder, you'll need to make a view for it to hold.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // android support creating views from XML  from anywhere you have access to another view. The same is valid for the LayoutInflater, you just need to pass a context to it.
        // parent.context = this means  you will create a LayoutInflater based on the parent view. LayoutInflater uses that information to inflate new views correctly.
        return ViewHolder.from(parent) // cleaner way to have a viewHolder and encapsulate the details of inflation and what layout to the ViewHolder class
    }

    // now that we have a layout that display a RV item, we need to create a ViewHolder that can display it. Every time we bind the ViewHolder, we need to access all the attributes of the item.
    class ViewHolder private constructor(itemView: View): RecyclerView.ViewHolder(itemView) {
        val sleepLength: TextView = itemView.findViewById(R.id.sleep_length)
        val quality: TextView = itemView.findViewById(R.id.quality_string)
        val qualityImage: ImageView = itemView.findViewById(R.id.quality_image)
        // now that we define the ViewHolder we are ready to update SleepNightAdapter to use it.

        // in this way we are hiding the details of how to update the views into the ViewHolder which has the view.
        // In this way the adapter  doesn't have to worry about them. In this way  we can add more viewHolders to the adapter without complicating it and call a similar method to this one.
        fun bind(item: SleepNight) {

            val res = itemView.context.resources
            sleepLength.text = convertDurationToFormatted(item.startTimeMilli, item.endTimeMilli, res)
            quality.text = convertNumericQualityToString(item.sleepQuality, res)

            qualityImage.setImageResource(
                when (item.sleepQuality) {
                    0 -> R.drawable.ic_sleep_0
                    1 -> R.drawable.ic_sleep_1
                    2 -> R.drawable.ic_sleep_2
                    3 -> R.drawable.ic_sleep_3
                    4 -> R.drawable.ic_sleep_4
                    5 -> R.drawable.ic_sleep_5
                    else -> R.drawable.ic_sleep_active
                })
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_sleep_night, parent, false)  // as TextView - in this way we made a view, we don't need to cast because we are inflating a constraint layout
                return ViewHolder(view) // wrap the view in a holder and pass it back to the RV
            }
        }
    }


}