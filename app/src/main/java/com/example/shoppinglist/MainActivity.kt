package com.example.shoppinglist

import android.content.ContentValues
import android.content.DialogInterface
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shoppinglist.DatabaseReader.FeedReaderContract.FeedEntry.TABLE_NAME
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_item_view.*

/** The main activity of the application. A view with a list of items,
 *  prices, control buttons and summary information */
class MainActivity : AppCompatActivity(), DatabaseHandler {

    /* UI elements*/
    lateinit var recycler: RecyclerView
    lateinit var recyclerAdapter: RecyclerView.Adapter<*>
    lateinit var recyclerManager: RecyclerView.LayoutManager
    lateinit var addItemButton: Button
    lateinit var itemCount: TextView
    lateinit var totalPrice: TextView


    lateinit var dbReadable: SQLiteDatabase
    var total = 0.0
    var data = mutableListOf<Pair<String, String>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val dbHelper = DatabaseReader(applicationContext)

        // Recycler view setup
        recyclerManager = LinearLayoutManager(this)
        recyclerAdapter = Adapter(data, this)
        recycler = findViewById(R.id.recycler)
        recycler.apply {
            setHasFixedSize(true)
            layoutManager = recyclerManager
            adapter = recyclerAdapter
        }

        itemCount = findViewById(R.id.item_count)
        totalPrice = findViewById(R.id.total_price)

        val dbWritable = dbHelper.writableDatabase

        dbReadable = dbHelper.readableDatabase
        val projection = arrayOf(DatabaseReader.FeedReaderContract.FeedEntry.NAME, DatabaseReader.FeedReaderContract.FeedEntry.PRICE)
        val sortOrder = "${DatabaseReader.FeedReaderContract.FeedEntry.PRICE} DESC"

        val res = dbReadable.query(DatabaseReader.FeedReaderContract.FeedEntry.TABLE_NAME,
                        projection, null, arrayOf(), null, null, null
            )

        // Go through the database SELECT query results
        with(res) {
            while (moveToNext()) {
                val itemName = getString(getColumnIndex(DatabaseReader.FeedReaderContract.FeedEntry.NAME))
                val itemPrice = getString(getColumnIndex(DatabaseReader.FeedReaderContract.FeedEntry.PRICE))
                data.add(Pair(itemName, itemPrice))
                total += itemPrice.toDouble()
            }
        }
        // Update total price and item count on screen
        itemCount.setText(data.size.toString())
        totalPrice.setText("%.2f €".format(total))

        // Manage addition of items
        addItemButton = findViewById(R.id.add_item)
        addItemButton.setOnClickListener{ v ->

            var builder = AlertDialog.Builder(this)
            val view = LayoutInflater.from(this).inflate(R.layout.add_item_view,  null , false)
            builder.setView(view)
            builder.setTitle("Add a new item to purchase")
            builder.setPositiveButton("Add", DialogInterface.OnClickListener { dialogInterface: DialogInterface, i: Int ->
                run {
                 val name = view.findViewById<EditText>(R.id.product_name).text.toString()
                  val price = view.findViewById<EditText>(R.id.product_price).text.toString()

                    // Add new values to database
                   val values =  ContentValues().apply {
                        put(DatabaseReader.FeedReaderContract.FeedEntry.NAME, name)
                        put(DatabaseReader.FeedReaderContract.FeedEntry.PRICE,price)

                    }
                    val newRowId = dbWritable.insert(DatabaseReader.FeedReaderContract.FeedEntry.TABLE_NAME, null, values)

                    // Add new item to data and update UI
                    data.add(Pair(name, price))
                    itemCount.setText(data.size.toString())
                    total += price.toDouble()
                    totalPrice.setText("%.2f €".format(total))
                    for ( text in data) {
                        Log.i("ShoppingList", "$name at price $price")
                    }
                Toast.makeText(this, "$name at price $price", Toast.LENGTH_SHORT).show()
            }


            })
            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener{
                dialog,
                    e -> {}
            })
            builder.show()
        }
    }


    /** Delete an item with givne name and price from database */
    override fun delete(name: String, price: String) {

        val selection = "${DatabaseReader.FeedReaderContract.FeedEntry.NAME} LIKE ?"
        val selectionArgs = arrayOf(name)

        val deletedRows = dbReadable.delete(TABLE_NAME, selection, selectionArgs)

        data.remove(Pair(name, price))
        itemCount.setText(data.size.toString())
        total -= price.toDouble()
        totalPrice.setText("%.2f €".format(total))
        recyclerAdapter.notifyDataSetChanged()
        Toast.makeText(applicationContext, "$name deleted",  Toast.LENGTH_SHORT).show()
    }
}