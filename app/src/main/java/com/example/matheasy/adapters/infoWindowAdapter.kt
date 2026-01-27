package com.example.matheasy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.example.matheasy.R
import com.example.matheasy.databinding.InfowindowcustomBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.example.matheasy.models.LocationItem
import com.example.matheasy.models.MarkerInfo
import jp.wasabeef.glide.transformations.BlurTransformation

class infoWindowAdapter(private val mContext: Context) : GoogleMap.InfoWindowAdapter {
    var view: View = LayoutInflater.from(mContext).inflate(R.layout.infowindowcustom, null)

    private fun setInfoWindowText(marker: Marker) {
        if (marker.tag == null) return;

        val Marker: MarkerInfo = marker.tag as MarkerInfo

        val binding = InfowindowcustomBinding.bind(view);

        binding.title.text = Marker.title
        val image = mContext.resources.getIdentifier(Marker.image, "drawable", mContext.packageName)
        binding.background.setImageResource(image)
    }

    override fun getInfoWindow(p0: Marker): View {
        setInfoWindowText(p0)
        return view
    }

    override fun getInfoContents(p0: Marker): View {
        setInfoWindowText(p0)
        return view
    }
}