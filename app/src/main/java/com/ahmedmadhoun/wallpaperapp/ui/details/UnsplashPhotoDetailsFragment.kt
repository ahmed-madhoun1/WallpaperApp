package com.ahmedmadhoun.wallpaperapp.ui.details

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.ahmedmadhoun.wallpaperapp.R
import com.ahmedmadhoun.wallpaperapp.databinding.FragmentUnsplashPhotoDetailsBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.fragment_unsplash_photo_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File


class UnsplashPhotoDetailsFragment : Fragment(R.layout.fragment_unsplash_photo_details) {

    companion object {
        private const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1
    }


    private val args by navArgs<UnsplashPhotoDetailsFragmentArgs>()
    private var _binding: FragmentUnsplashPhotoDetailsBinding? = null
    private val binding get() = _binding!!
    var msg: String? = ""
    var lastMsg = ""
    private lateinit var job: Job

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentUnsplashPhotoDetailsBinding.bind(view)

        setPhotoToImageView()

        binding.apply {
            btnDownloadPhoto.setOnClickListener {
                askPermission()
            }
        }


    }

    private fun downloadPhoto(url: String) {
        val directory = File(Environment.DIRECTORY_PICTURES)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val downloadManager =
            requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(url)
        val request = DownloadManager.Request(downloadUri).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE).apply {
                setAllowedOverRoaming(false)
                setTitle(url.substring(url.lastIndexOf("/") + 1))
                setDescription("")
                setDestinationInExternalPublicDir(
                    directory.toString(),
                    url.substring(url.lastIndexOf("/") + 1)
                )
            }
        }

        val downloadId = downloadManager.enqueue(request)
        val query = DownloadManager.Query().setFilterById(downloadId)

        job = CoroutineScope(IO).launch {
            var downloading = true
            while (downloading) {
                val cursor: Cursor = downloadManager.query(query)
                cursor.moveToFirst()
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                msg = statusMessage(status)
                if (msg != lastMsg) {
                    job = CoroutineScope(Main).launch {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                    lastMsg = msg ?: ""
                }
                cursor.close()
            }
        }


    }

    private fun statusMessage(status: Int): String? {
        var msg = ""
        msg = when (status) {
            DownloadManager.STATUS_FAILED -> "Download has been failed, please try again"
            DownloadManager.STATUS_PAUSED -> "Paused"
            DownloadManager.STATUS_PENDING -> "Pending"
            DownloadManager.STATUS_RUNNING -> "Downloading..."
            DownloadManager.STATUS_SUCCESSFUL -> "Image downloaded successfully"
            else -> "There's nothing to download"
        }
        return msg
    }

    private fun askPermission() {
        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    downloadPhoto(args.unsplashPhoto.urls.full)
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Permission required")
                        .setMessage("Permission required to save photos from the Web.")
                        .setPositiveButton("Accept") { dialog, id ->
                            ActivityCompat.requestPermissions(
                                requireActivity(),
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                            )
                            dialog.dismiss()
                        }
                        .setNegativeButton("Deny") { dialog, id -> dialog.cancel() }
                        .show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    request: PermissionRequest?,
                    token: PermissionToken?
                ) {
                }
            }).check()
    }

    private fun setPhotoToImageView() {
        binding.apply {
            Glide.with(this@UnsplashPhotoDetailsFragment)
                .load(args.unsplashPhoto.urls.full)
                .error(R.drawable.close)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.isVisible = false
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.isVisible = false
                        btnDownloadPhoto.isVisible = true
                        return false
                    }

                })
                .into(image_view_details)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::job.isInitialized) {
            job.cancel()
        }
    }


}