package com.ahmedmadhoun.wallpaperapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ahmedmadhoun.wallpaperapp.R
import com.ahmedmadhoun.wallpaperapp.databinding.ItemPhotoBinding
import com.ahmedmadhoun.wallpaperapp.model.UnsplashPhoto
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class UnsplashPhotosAdapter(private val listener: OnItemClickListener) :
    PagingDataAdapter<UnsplashPhoto, UnsplashPhotosAdapter.UnsplashPhotosViewHolder>(
        UnsplashPhotosAdapterDiffUtil
    ) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnsplashPhotosViewHolder {
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UnsplashPhotosViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UnsplashPhotosViewHolder, position: Int) {
        val position = getItem(position)

        if (position != null) {
            holder.bind(position)
        }
    }


    inner class UnsplashPhotosViewHolder(private val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item != null) {
                        listener.onItemClick(item)
                    }
                }
            }
        }

        fun bind(unsplashPhoto: UnsplashPhoto) {
            binding.apply {
                Glide.with(itemView).load(unsplashPhoto.urls.regular)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.close)
                    .into(imageView)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(unsplashPhoto: UnsplashPhoto)
    }

    companion object {
        private val UnsplashPhotosAdapterDiffUtil =
            object : DiffUtil.ItemCallback<UnsplashPhoto>() {
                override fun areItemsTheSame(oldItem: UnsplashPhoto, newItem: UnsplashPhoto) =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: UnsplashPhoto, newItem: UnsplashPhoto) =
                    oldItem == newItem
            }
    }

}