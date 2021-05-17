package com.example.android.trackmysleepquality.sleeptracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.databinding.ListItemSleepNightBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// we use this constants to check itemViewType, the number doesn't matter but it needs to be unique
// The RV will use these to keep track of each type of item, each ViewHolder it's holding is.
private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

// we define the clickListener here to get the click event out of the fragment. In this way the adapter doesn't care about how clicks get handled, it just takes a callback.
// To display a Header, we need to use any type of View Holder. We must change "SleepNightAdapter.ViewHolder" with "RecyclerView..ViewHolder" to achieve it. I added a new viewHolder
// for headers to your adapter.
class SleepNightAdapter(val clickListener: SleepNightListener) : ListAdapter<DataItem, RecyclerView.ViewHolder>(SleepNightDiffCallback()){
    // the adapter now is holding DataItem instead of SleepNight
    /**  DEPRECATED
     * The ListAdapter class can be used instead of the RV.Adapter. It helps you to build a RV Adapter that's backed by a list. ListAdapter will take care of keeping track of
     * the list for you and notifying the adapter when the list is updated. 2 generics arguments: 1) Is the type of the list that it's holding(SleepNight), 2) Is the ViewHolder
     * just like the RV.Adapter, in addition there's a constructor parameter that takes the item callback. The ListAdapter will use this to figure out what changed random list
     * get updated. Now that we are Subclassing ListAdapter we don't need to define the field data. LA will take care of keeping track of the list for us. LA can also figure out
     * the number of items from the entire list, so we don't need to override getItemCount().
     *
     * Inside onBindViewHolder we  can't use the list "data" anymore, instead LA provides a method called getItem that can be used to get an item. -> getItem(position).
     *
     * In this way we implemented DiffUtil inside the adapter, and it use DiffUtil to calculate minimum changes when the list gets updated. In order to update the list through
     * the Observer inside the Fragment, we can use a method called submitList to tell a new version of the list is available. It will detect every item we added, moved, removed,
     * or changed and update the items shown by RV. Now the app is going faster than before. DiffUtil and ListAdapter figured out what needed changed and the RV figured out how
     * animate the change.
     *
     *
    // this will be the data that the adapter is adapting for RecyclerView to use. The RV won't use data directly, it won't even know it exists.
    // We'll use the adapter to expose or adapt data into the RV API
    // update the list with a custom setter whenever the data changes, and to notify it we can call this adapter method. If the items were more complex than textBoxes, this way can be a pretty slow operation.
    var data = listOf<SleepNight>()
    set(value) {
        field =  value
        notifyDataSetChanged()
    }

    // the RV needs to know how many items to display, return the total number of items in the dataset held by the adapter.
    override fun getItemCount(): Int = data.size */

    // we want to perform this out of the UI thread, we define the Coroutines Scope with Dispatchers.Default, in order to move the list conversions to a background thread.
    private val adapterScope = CoroutineScope(Dispatchers.Default)
    // tell RV how to create a new view holder. The RV does everything in terms of ViewHolders.
    // Whenever a RV needs a new ViewHolder it will ask for one, our job is to give it whenever it asks. To display a ViewHolder it needs to be passed to a ViewGroup.
    // to actually make a ViewHolder, you'll need to make a view for it to hold.
    // it returns now a RecyclerView.ViewHolder for header and sleepNight items
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // we change this to return the right ViewHolder for the Header and the Item
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }


// we override this method to return the right header or item constant, depending on the type of the current item
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.SleepNightItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    // convert a list sleepNights to a list data item. After we get the list we need to get back to the UI thread to call the submit list method.
    fun addHeaderAndSubmitList(list: List<SleepNight>?){
        adapterScope.launch {
            // create the list in the background
            val items = when (list) {
                null -> listOf(DataItem.Header)
                else -> listOf(DataItem.Header) + list.map { DataItem.SleepNightItem(it)  }
            }
            // get back to the UI thread to update the UI
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }

    }

    // Called by RV to display the data at the specified position. This method update the views held by the ViewHolder to show the item at the position passed.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        /** RV told us the position that needs to be bound, we can just look it up in our data property
        val item = getItem(position)
        // in this way we encapsulate the code to actually display the View
        holder.bind(item)*/
        // In this way it takes a RecyclerView.ViewHolder and we specify the logic for each sleepNight item
        when (holder) {
            is ViewHolder -> {
                val nightItem = getItem(position) as DataItem.SleepNightItem
                holder.bind(clickListener, nightItem.sleepNight)
            }
        }
    }

    // now that we have a layout that display a RV item, we need to create a ViewHolder that can display it. Every time we bind the ViewHolder, we need to access all the attributes of the item.
    // private constructor that can only be called inside the class. Must define the binding ad property(val) if not you get an error.
    class ViewHolder private constructor(val binding: ListItemSleepNightBinding): RecyclerView.ViewHolder(binding.root) { // RV doesn't know anything about data binding. Instead we pass the root view of the binding which is the constraint layout.
        // now that we define the ViewHolder we are ready to update SleepNightAdapter to use it.
        // in this way we are hiding the details of how to update the views into the ViewHolder which has the view.
        // In this way the adapter  doesn't have to worry about them. In this way  we can add more viewHolders to the adapter without complicating it and call a similar method to this one.
        fun bind(clickListener: SleepNightListener, item: SleepNight) {
            binding.sleep = item
            binding.clickListener = clickListener
            binding.executePendingBindings()

           /**
            * I MOVED THIS BLOCK OF CODE INSIDE THE BINDING ADAPTER TO ADAPT THESE VIEWS AND BIND IT TO THE ADAPTER
            * val res = itemView.context.resources
            // dataBinding to declare the Views
            binding.sleepLength.text = convertDurationToFormatted(item.startTimeMilli, item.endTimeMilli, res)
            binding.qualityString.text = convertNumericQualityToString(item.sleepQuality, res)
            binding.qualityImage.setImageResource(
                when (item.sleepQuality) {
                    0 -> R.drawable.ic_sleep_0
                    1 -> R.drawable.ic_sleep_1
                    2 -> R.drawable.ic_sleep_2
                    3 -> R.drawable.ic_sleep_3
                    4 -> R.drawable.ic_sleep_4
                    5 -> R.drawable.ic_sleep_5
                    else -> R.drawable.ic_sleep_active
                }
            )*/
        }
        // details of which layout to inflate. Even if the constructor of the class is private, since the function is inside the companion object, it can still call the constructor, another class couldn't.
        // Since we want to call from on the ViewHolder class and not on an instance of a ViewHolder, we need to convert the function to a companion object
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
               val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSleepNightBinding.inflate(layoutInflater, parent, false)// inflated with dataBinding
                return ViewHolder(binding) // wrap the view in a holder and pass it back to the RV
            }
        }
    }

    // TextHolder class
    class TextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object{
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.header, parent, false)
                return TextViewHolder(view)
            }
        }
    }
}

// DiffUtil  has a class called item callback that you extend in order to figure out the difference between two items,  we pass SleepNight as generic parameter.
// We need to change the DiffUtil to use the DataItem class
class SleepNightDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    // this method will check ,by using the ids of the item, if an item was moved, removed or edit.
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        // if the oldItem has the same Id of newItem, we'll return true because the item are the same. Otherwise, false.
        // By checking the ID, DiffUtil will know the  difference between an item being edit, removed, or  moved.
        return oldItem.id == newItem.id
    }

    // We use this method to know if the content of an item have changed or if they are equal.
    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        // for this app we don't need to do anything custom, we only check if the oldItem is equal to the newItem.
        // This will perform an equality check on the items. It will check all the fields because we defined SleepNight as a Data class( automatically define equals and other methods for us).
        return oldItem == newItem
    }

}

// class created to handle the click on items by the user, this will trigger the method of this class with the selected item.
// The ViewHolder informs the fragment that a click happened. We don't hold an entire reference to the object since having the night ID gives us the ability to access the data anytime we want from the database.
// We need to have a reference to the click listener inside the XML file and call it from data binding
class SleepNightListener(val clickListener: (sleepId: Long) -> Unit) {
    fun onClick(night: SleepNight) = clickListener(night.nightId)
}

// a sealed class defines a closed type, all the subclasses of data item must be defined in this file. As result number of subclasses is known to the compiler and it's not
// possible to another part of the code to define new type of dataItem(possibly breaking the adapter). We create a data holder class that represents either a sleepNight or a header.
sealed class DataItem {
    // inside the class we create 2 classes that represent the different types of data items available.
    // sleepNight item, it's a wrap around sleepNight to get a single value called sleepNight. to make it part of the sealed class needs to extend DataItem().
    data class SleepNightItem(val sleepNight: SleepNight): DataItem() {
        override val id = sleepNight.nightId
    }
    // since the header has no actual data, it's declared as an object. There will only ever be one instance of a header.
    object Header: DataItem(){
        override val id = Long.MIN_VALUE
    }
    // the callback needs to know the ID of each item, abstract to be override from both methods and return an ID
    abstract val id: Long
}