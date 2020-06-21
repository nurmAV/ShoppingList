package com.example.shoppinglist

import android.provider.BaseColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.shoppinglist.DatabaseReader.FeedReaderContract.FeedEntry.TABLE_NAME

/** Adapter that maps two String Pairs representing the name and price of an item to a UI element */
class Adapter(private val data: MutableList<Pair<String, String>>, private val dbHandler: DatabaseHandler) : RecyclerView.Adapter<Adapter.ViewHolder>() {


    /** A ViewHolder that keeps track of the UI elements within the layout*/
    class ViewHolder(val v : View) : RecyclerView.ViewHolder(v) {
        val textView: TextView = v.findViewById(R.id.name)
        val price: TextView = v.findViewById(R.id.price)
        val remove: Button = v.findViewById(R.id.remove_item)

    }
    /* Inflate the UI element from XML*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return ViewHolder(textView)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    /* Set texts and event handlers for UI elements */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = data[position].first
        holder.price.text = "%.2f €".format(data[position].second.toDouble())
        holder.remove.setOnClickListener {e -> dbHandler.delete(data[position].first, data[position].second)
        }


            }

}
