
package com.example.snapshots.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.snapshots.databinding.FragmentProfileBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth

/**
 * It is the class that manage the profileFragment. In this screen user see his own email and a
 * button to logout.
 */
class ProfileFragment : Fragment() {

    private lateinit var mBinding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentProfileBinding.inflate(inflater, container, false)
        return mBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.tvName.text = FirebaseAuth.getInstance().currentUser?.displayName
        mBinding.tvEmail.text = FirebaseAuth.getInstance().currentUser?.email

        mBinding.btnLogout.setOnClickListener { singOut() }

    }

    /**
     * Signout the current session
     */
    private fun singOut() {
        //if context is not null (context? = context != null), let do the following code
        context?.let {
            AuthUI.getInstance().signOut(it)
                .addOnSuccessListener {
                    Toast.makeText(context, "Good bye...", Toast.LENGTH_SHORT).show()
                }
        }

    }

}































