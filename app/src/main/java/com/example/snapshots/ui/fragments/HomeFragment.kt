package com.example.snapshots.ui.fragments

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
import com.example.snapshots.utils.HomeAux
import com.example.snapshots.R
import com.example.snapshots.databinding.FragmentHomeBinding
import com.example.snapshots.databinding.ItemSnapshotBinding
import com.example.snapshots.entities.Snapshot
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

/**
 * It is the class that manage the homeFragment, where the user see the posts
 */
@Suppress("NAME_SHADOWING")
class HomeFragment : Fragment(), HomeAux {

    private val PATH_SNAPSHOT = "snapshots"

    private lateinit var mBinding: FragmentHomeBinding
    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>

    private lateinit var mLayoutManager: RecyclerView.LayoutManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup the Firebase Recycler view, it is the provided by firebase
        val query = FirebaseDatabase.getInstance().reference.child("snapshots")



        //Customize the snapshot object, to set the id of the object equal to the key of this object
        //(this key is how firebase named the object in data base, the "branch's name" )
        val options = FirebaseRecyclerOptions.Builder<Snapshot>().setQuery(query) {
            val snapshot = it.getValue(Snapshot::class.java)
            snapshot!!.id = it.key!!
            snapshot
        }.build()

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
                    binding.cbLike.text = snapshot.likeList.keys.size.toString() //likes number
                    FirebaseAuth.getInstance().currentUser?.let {
                        binding.cbLike.isChecked = snapshot.likeList
                            .containsKey(it.uid)
                    }
                    Glide.with(mContext)
                        .load(snapshot.photoUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(binding.ivPhoto)

                    //check if the post's owner is the login user, if its not, hide the delete button
                    binding.btnDelete.visibility =
                        if(model.ownerUid == FirebaseAuth.getInstance().currentUser!!.uid){
                            View.VISIBLE
                        } else {
                            View.INVISIBLE
                        }
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
     * When user clicks on the same fragment that he is in, it will go to the top of this fragment
     */
    override fun goToTop() {
        mBinding.recyclerView.smoothScrollToPosition(0)
    }

    /**
     * remove a post from the database
     * @param snapshot the post which is going to be removed
     */
    private fun deleteSnapshot(snapshot: Snapshot){

        //get the ref of the post in firebase storage
        val storageSnapshotRef = FirebaseStorage.getInstance().reference
            .child(PATH_SNAPSHOT)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(snapshot.id)

        val databaseReference = FirebaseDatabase.getInstance().reference.child("snapshots")

        //delete the image from firebase storage and then form db
        storageSnapshotRef.delete().addOnCompleteListener {
            if(it.isSuccessful) {
                databaseReference.child(snapshot.id).removeValue()
            } else {
                Snackbar.make(mBinding.root, "Could not remove the photo", Toast.LENGTH_SHORT).show()
            }
        }



    }


    /**
     * add or remove if a user likes or not likes a post
     */
    private fun setLikes(snapshot: Snapshot, checked: Boolean) {

        //post's reference
        val databaseReference = FirebaseDatabase.getInstance().reference.child("snapshots")

        //if user likes, then unlike else likes
        if(checked) {
            databaseReference.child(snapshot.id).child("likeList")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .setValue(true)

        } else {
            databaseReference.child(snapshot.id).child("likeList")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .setValue(null)
        }

    }



    /**
     * Adapter for the recyclerView but using the adapter provided by firebase
     */
    inner class SnapshotHolder(view: View) : RecyclerView.ViewHolder(view) {

        val binding = ItemSnapshotBinding.bind(view)

        fun setListener(snapshot: Snapshot){

            binding.btnDelete.setOnClickListener { deleteSnapshot(snapshot) }

            binding.cbLike.setOnCheckedChangeListener { _, checked ->
                setLikes(snapshot, checked)
            }
        }

    }


}












