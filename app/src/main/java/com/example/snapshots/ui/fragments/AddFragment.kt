package com.example.snapshots.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.example.snapshots.R
import com.example.snapshots.databinding.FragmentAddBinding
import com.example.snapshots.entities.Snapshot
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

/**
 * It is the class that manage the addFragment. Screen where the user open the gallery to pick a
 * image, add a title and upload to firebase
 */
class AddFragment : Fragment() {

    private val PATH_SNAPSHOT = "snapshots"

    private lateinit var mBinding: FragmentAddBinding
    private lateinit var mStorageReference: StorageReference
    private lateinit var mDatabaseReference: DatabaseReference

    private var mPhotoSelectUri : Uri? = null

    private val galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

        if(it.resultCode == Activity.RESULT_OK) {
                mPhotoSelectUri = it.data?.data
                mBinding.imgPhoto.setImageURI(mPhotoSelectUri)
                mBinding.tilTitle.visibility = View.VISIBLE
                mBinding.tvMessage.text = getString(R.string.post_message_valid_title)
            }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentAddBinding.inflate(inflater, container, false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mStorageReference = FirebaseStorage.getInstance().reference
        mDatabaseReference = FirebaseDatabase.getInstance().reference.child(PATH_SNAPSHOT)

        mBinding.btnPost.setOnClickListener { postSnapshot() }
        mBinding.btnSelect.setOnClickListener { openGallery() }
    }

    /**
     * Open the gallery of the device to select a image
     */
    private fun openGallery() {

        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResult.launch(intent)
    }


    /**
     * Upload a post to firebase. Check that everything is ok and send a message to the user
     * when it is successfully
     */
    private fun postSnapshot() {

        mBinding.progressBar.visibility = View.VISIBLE

        val key = mDatabaseReference.push().key!!

        //create a reference for the image upload to firebase,ref = "snapshots/$userId/imgkey"
        //in this way create a folder for every user and save their images on their own folder
        val storageReference = mStorageReference.child(PATH_SNAPSHOT)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(key)

        if(mPhotoSelectUri != null) {
            storageReference.putFile(mPhotoSelectUri!!)
                .addOnProgressListener {
                    val progress = (100 * it.bytesTransferred / it.totalByteCount).toDouble()

                    mBinding.progressBar.progress = progress.toInt()
                    mBinding.tvMessage.text = "$progress%"
                }

                .addOnCompleteListener{
                    mBinding.progressBar.visibility = View.INVISIBLE
                }
                .addOnSuccessListener {
                    Snackbar.make(mBinding.root, "Photo upload successfully", Snackbar.LENGTH_SHORT).show()

                    it.storage.downloadUrl.addOnSuccessListener { result ->
                        saveSnapshot(key, result.toString(), mBinding.etTitle.text.toString().trim())
                        mBinding.tilTitle.visibility = View.GONE
                        mBinding.tvMessage.text = getString(R.string.post_message_title)
                    }

                }

                .addOnFailureListener{
                    Snackbar.make(mBinding.root, "An error occurred", Snackbar.LENGTH_SHORT).show()
                }
        }

    }

    /**
     * Save an object in firebase database
     * @param key
     * @param url the url where the object will be saved
     * @param title the snapshot's title
     */
    private fun saveSnapshot(key: String, url:String, title:String) {

        val snapshot = Snapshot(ownerUid = FirebaseAuth.getInstance().currentUser!!.uid,title= title, photoUrl = url)
        mDatabaseReference.child(key).setValue(snapshot)

    }


}






















