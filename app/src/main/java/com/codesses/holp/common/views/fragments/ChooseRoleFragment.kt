package com.codesses.holp.common.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.codesses.holp.R
import com.codesses.holp.common.utils.IntentKeys
import com.codesses.holp.databinding.FragmentChooseRoleBinding

class ChooseRoleFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentChooseRoleBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChooseRoleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnUser -> {
                findNavController().navigate(
                    ChooseRoleFragmentDirections
                        .actionChooseRoleFragmentToUserMainFragment()
                )
            }
            R.id.btnDriver -> {
                findNavController().navigate(
                    ChooseRoleFragmentDirections
                        .actionChooseRoleFragmentToDriverMainFragment()
                )
            }
        }
    }

    private fun setupView() {
        binding.btnUser.setOnClickListener(this)
        binding.btnDriver.setOnClickListener(this)
    }

}