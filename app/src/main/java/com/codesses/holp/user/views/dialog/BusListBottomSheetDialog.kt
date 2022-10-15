/*
 *
 * Created by Saad Iftikhar on 8/25/21, 6:05 PM
 * Copyright (c) 2021. All rights reserved
 *
 */

package com.codesses.holp.user.views.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.codesses.holp.R
import com.codesses.holp.databinding.BottomSheetDialogBusListBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BusListBottomSheetDialog : BottomSheetDialogFragment(), View.OnClickListener {


    val args: BusListBottomSheetDialogArgs by navArgs()

    //    Instance
    companion object {
        const val TAG = "CustomBottomSheetDialogFragment"
    }

    private lateinit var binding: BottomSheetDialogBusListBinding


    override fun getTheme(): Int = R.style.CustomBottomSheetTheme


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireActivity(), theme)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = BottomSheetDialogBusListBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.clBus -> {
                findNavController().navigate(
                    BusListBottomSheetDialogDirections
                        .actionBusListBottomSheetToBusDetailsFragment()
                        .setStartLat(args.startLat)
                        .setStartLng(args.startLng)
                        .setEndLat(args.endLat)
                        .setEndLng(args.endLng)
                )
            }
        }
    }


    private fun setupView() {
        binding.clBus.setOnClickListener(this)
    }
}