package com.example.umc_flo

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class LockerVPAdapter(fragment: Fragment):FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return  when(position){
                0 -> StoreFragment()
                else -> MusicFragment()
//                1 -> MusicFragment()
//                else -> SavedAlbumFragment()
            }

        }


}




