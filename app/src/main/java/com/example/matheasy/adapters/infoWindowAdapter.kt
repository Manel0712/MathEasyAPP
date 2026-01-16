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
import jp.wasabeef.glide.transformations.BlurTransformation

class infoWindowAdapter(mContext: Context) : GoogleMap.InfoWindowAdapter {
    var view: View = LayoutInflater.from(mContext).inflate(R.layout.infowindowcustom, null)

    private fun setInfoWindowText(marker: Marker) {
        if (marker.tag == null) return;

        //val monument: monuments = marker.tag as monuments

        val binding = InfowindowcustomBinding.bind(view);

        binding.title.text = "Espanya-Mataro"
        binding.background.setImageResource(R.drawable.p1010255)
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