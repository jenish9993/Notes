package com.example.notes.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.notes.R
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Canvas
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.notes.Adapter.NoteRecyclerAdapter
import com.example.notes.model.Notes
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ktx.getValue
import java.util.*
import kotlin.collections.HashMap


import androidx.core.content.ContextCompat
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


class MainActivity : AppCompatActivity(),NoteRecyclerAdapter.NoteListner {

    val TAG="MainActivity"
    lateinit var fab:FloatingActionButton
    lateinit var recyclerView: RecyclerView
    lateinit var notesAdapter: NoteRecyclerAdapter

  override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(FirebaseAuth.getInstance().currentUser==null){
            startLoginActivity()
        }
        fab=findViewById(R.id.fab)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        initRecyclerAdapter()
    }

    private fun initRecyclerAdapter() {
        var query:Query = FirebaseDatabase.getInstance().reference
            .child("Notes")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        val options:FirebaseRecyclerOptions<Notes> = FirebaseRecyclerOptions.Builder<Notes>()
            .setQuery(query,Notes::class.java)
            .build()

        notesAdapter = NoteRecyclerAdapter(options,this)

        recyclerView.adapter = notesAdapter

        var itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

    }

    var simpleCallback:ItemTouchHelper.SimpleCallback =
        object:ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if(direction == ItemTouchHelper.RIGHT){
                    Toast.makeText(this@MainActivity,"Swipe to Right",Toast.LENGTH_LONG).show()
                    notesAdapter.deleteItem(viewHolder.adapterPosition)
                }
                else{
                    Toast.makeText(this@MainActivity,"Swipe to Left",Toast.LENGTH_LONG).show()
                    notesAdapter.editItem(viewHolder.adapterPosition)
                }
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean)
            {
                RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.red))
                    .addSwipeLeftBackgroundColor(R.color.green)
                    .addSwipeLeftActionIcon(R.drawable.add)
                    .addActionIcon(R.drawable.ic_baseline_delete_24)
                    .create()
                    .decorate()
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

        }

    override fun onStart() {
        super.onStart()

        fab.setOnClickListener {
            createAlertDialog()
        }

        notesAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        notesAdapter.stopListening()
    }

    private fun createAlertDialog() {
        var editText:EditText = EditText(this)

        AlertDialog.Builder(this)
            .setTitle("Add Note")
            .setView(editText)
            .setPositiveButton("Add",object : DialogInterface.OnClickListener{
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    addNotetoFirebaseDatabase(editText.text.toString())

                }

            })
            .setNegativeButton("Cancel",null)
            .create()
            .show()
    }

    private fun addNotetoFirebaseDatabase(text:String) {


        Log.d(TAG,"inside add notes")
        val ref=FirebaseDatabase.getInstance().reference

        val notes=Notes(text,
            false,
            System.currentTimeMillis())

        ref.child("Notes")
            .child(FirebaseAuth.getInstance().uid.toString())
            .child(UUID.randomUUID().toString())
            .setValue(notes)
            .addOnSuccessListener {
                Log.d(TAG,"addOnSuccessListener : Notes Added Successfully.")
                Toast.makeText(this,"Notes Added successfully.",Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Log.d(TAG,"addOnFailureListener : ${it.message}")
                Toast.makeText(this,"Error : ${it.message}",Toast.LENGTH_LONG).show()
            }
    }

    fun startLoginActivity(){
        var intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_actionbar,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_logout -> {
                //Logout from the activity
                AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            startLoginActivity()
                        }else{
                            Log.d(TAG,"addOnComplateListener : ${it.exception}")
                        }
                    }

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun handleCheckedChange(isCheck: Boolean, dataSnapshot: DataSnapshot) {
        Log.d("MainActivity","Checked Message")

        val mapOf = HashMap<String,Any>()
        mapOf.put("completed",isCheck)

        dataSnapshot.ref.updateChildren(mapOf)
            .addOnSuccessListener {
                Log.d("MainActivity","onSuccess : Checkbox updated")
            }
            .addOnFailureListener {
                Log.d("MainActivity","onFailure : Checkbox is not updated")
            }
    }

    override fun handleEditClickListner(dataSnapshot: DataSnapshot) {
        Log.d("MainActivity","Edit item")

        val note=dataSnapshot.getValue<Notes>()
        val editText:EditText = EditText(this)
        editText.setText(note!!.text)
        editText.setSelection(note.text!!.length)

        AlertDialog.Builder(this)
            .setTitle("Edit Note")
            .setView(editText)
            .setPositiveButton("Done"){dialogInterface,i ->

                val newNoteText = editText.text.toString()

                note.text = newNoteText

                dataSnapshot.ref.setValue(note)
                    .addOnSuccessListener {
                        Log.d("MainActivity","onSuccess : Note updated")
                    }
                    .addOnFailureListener {
                        Log.d("MainActivity","onFailure : Note is not updated")
                    }

            }
            .setNegativeButton("Cancel",null)
            .show()
    }

    override fun handleDeleteListner(dataSnapshot: DataSnapshot) {
        dataSnapshot.ref.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this,"Note Deleted Successfully",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this,"Note not Deleted ",Toast.LENGTH_SHORT).show()
            }
    }
}