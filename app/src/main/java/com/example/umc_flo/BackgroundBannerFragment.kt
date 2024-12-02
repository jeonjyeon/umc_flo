package com.example.umc_flo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.umc_flo.databinding.FrgmentBackgroundbannerBinding
import com.example.umc_flo.databinding.FrgmentBannerBinding

class BackgroundBannerFragment(val imgRes : Int) :Fragment() {

    lateinit var binding : FrgmentBackgroundbannerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FrgmentBackgroundbannerBinding.inflate(inflater, container, false)

        binding.homePanelBackgroundIv.setImageResource(imgRes)

        return binding.root
    }

}