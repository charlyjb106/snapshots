package com.example.snapshots

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.snapshots.databinding.FragmentHomeBinding
import com.example.snapshots.databinding.ItemSnapshotBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase


class HomeFragment : Fragment() {

    private lateinit var mBinding: FragmentHomeBinding
    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>

    private lateinit var mLayoutManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup the Firebase Recycler view, it is the provided by firebase
        val query = FirebaseDatabase.getInstance().reference.child("snapshots")



        //Customize the snapshot object, to set the id of the object equal to the key of this object
        //(this key is how firebase named the object in data base, the "branch's name" )
        val options = FirebaseRecyclerOptions.Builder<Snapshot>().setQuery(query, SnapshotParser {
            val snapshot = it.getValue(Snapshot::class.java)
            snapshot!!.id = it.key!!
            snapshot
        }).build()

        //This is default way to get all the properties
        //val options = FirebaseRecyclerOptions.Builder<Snapshot>()
        //.setQuery(query,Snapshot::class.java).build


        //Display the firebase adapter
        mFirebaseAdapter = object : FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>(options){

            private lateinit var mContext: Context

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnapshotHolder {

                mContext = parent.context

                val view = LayoutInflater.from(mContext).inflate(R.layout.item_snapshot, parent, false)

                return SnapshotHolder(view)

            }

            override fun onBindViewHolder(holder: SnapshotHolder, position: Int, model: Snapshot) {

                val snapshot = getItem(position)

                with(holder){
                    setListener(snapshot)

                    binding.tvTitle.text = snapshot.title
                    Glide.with(mContext)
                        .load(snapshot.photoUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(binding.ivPhoto)
                }

            }

            @SuppressLint("NotifyDataSetChanged")//firebase internal error, app crash while selecting a image
            override fun onDataChanged() {
                super.onDataChanged()
                mBinding.progressBar.visibility = View.GONE
                notifyDataSetChanged()
            }

            override fun onError(error: DatabaseError) {
                super.onError(error)
                Toast.makeText(mContext, error.message, Toast.LENGTH_SHORT).show()
            }

        }

        mLayoutManager = LinearLayoutManager(context)

        mBinding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = mLayoutManager
            adapter = mFirebaseAdapter
        }


    }

    override fun onStart() {
        super.onStart()
        mFirebaseAdapter.startListening()
    }


    override fun onStop() {
        super.onStop()
        mFirebaseAdapter.stopListening()
    }


    /**
     * remove a post from the database
     * @param snapshot the post which is going to be removed
     */
    private fun deleteSnapshot(snapshot: Snapshot){

        val databaseReference = FirebaseDatabase.getInstance().reference.child("snapshots")
        databaseReference.child(snapshot.id).removeValue()

    }


    private fun setLikes(snapshot: Snapshot, checked: Boolean) {

    }



    /**
     * Adapter for the recyclerView but using the adapter provided by firebase
     */
    inner class SnapshotHolder(view: View) : RecyclerView.ViewHolder(view) {

        val binding = ItemSnapshotBinding.bind(view)

        fun setListener(snapshot: Snapshot){

            binding.btnDelete.setOnClickListener { deleteSnapshot(snapshot) }
        }

    }


}











