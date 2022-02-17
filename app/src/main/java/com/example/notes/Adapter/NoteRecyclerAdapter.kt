package com.example.notes.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.notes.R
import com.example.notes.model.Notes
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import org.w3c.dom.Text
import java.text.DateFormat

class NoteRecyclerAdapter(options: FirebaseRecyclerOptions<Notes>,val noteListner: NoteListner) :
    FirebaseRecyclerAdapter<Notes, NoteRecyclerAdapter.NoteViewHolder>(options) {

    class NoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {


        val txtNote:TextView = itemView.findViewById(R.id.txtnote)
        val txtDate:TextView = itemView.findViewById(R.id.txtDate)
        val cbIsCompleted:CheckBox = itemView.findViewById(R.id.cbIsCompleted)
        val itemLayout:LinearLayout = itemView.findViewById(R.id.itemLayout)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout,parent,false)

        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int, notes: Notes) {
        holder.txtNote.setText(notes.text)

        val date:CharSequence = android.text.format.DateFormat.format("EEEE, MMM d,yyyy h:mm:ss",notes.currentTime!!)

        holder.txtDate.setText(date)

        holder.cbIsCompleted.isChecked = notes.isCompleted!!

        holder.cbIsCompleted.setOnCheckedChangeListener { compoundButton, b ->
            val dataSnapShot = snapshots.getSnapshot(holder.adapterPosition)
            noteListner.handleCheckedChange(b,dataSnapShot)
        }
        holder.itemLayout.setOnClickListener {
            val dataSnapshot = snapshots.getSnapshot(holder.adapterPosition)
            noteListner.handleEditClickListner(dataSnapshot)
        }
    }

    public fun deleteItem(position: Int){
        Log.d("DeleteItem",position.toString())
        noteListner.handleDeleteListner(snapshots.getSnapshot(position))
    }

    public fun editItem(position: Int){
        Log.d("EditItem",position.toString())
        noteListner.handleEditClickListner(snapshots.getSnapshot(position))
    }

    interface NoteListner{
        public fun handleCheckedChange(isCheck: Boolean,dataSnapshot: DataSnapshot)
        public fun handleEditClickListner(dataSnapshot: DataSnapshot)
        public fun handleDeleteListner(dataSnapshot: DataSnapshot)
    }
}