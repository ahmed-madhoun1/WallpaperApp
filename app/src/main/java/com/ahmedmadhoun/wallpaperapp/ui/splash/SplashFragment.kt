package com.ahmedmadhoun.wallpaperapp.ui.splash

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ahmedmadhoun.wallpaperapp.R
import com.ahmedmadhoun.wallpaperapp.databinding.FragmentSplashBinding
import com.ahmedmadhoun.wallpaperapp.databinding.FragmentUnsplashPhotosBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val TAG = "AM"
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSplashBinding.bind(view)

        binding.apply {
            imageViewLogo.alpha = 0f
            imageViewLogo.animate().setDuration(1500).alpha(1f).withEndAction {
                val action = SplashFragmentDirections.actionSplashFragmentToUnsplashPhotosFragment()
                findNavController().navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}