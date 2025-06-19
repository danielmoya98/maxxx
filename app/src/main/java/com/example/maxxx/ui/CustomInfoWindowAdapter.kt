package com.example.maxxx.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.example.maxxx.R

class CustomInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    private val window: View = LayoutInflater.from(context).inflate(R.layout.custom_marker, null)

    private fun render(marker: Marker, view: View) {
        val icon = view.findViewById<ImageView>(R.id.marker_icon)
        val title = view.findViewById<TextView>(R.id.marker_title)
        val button = view.findViewById<ImageButton>(R.id.marker_button)

        // Ejemplo: Cambiar icono según el título o cualquier dato
        // icon.setImageResource(R.drawable.ic_gym) // Ya definido en XML

        title.text = marker.title ?: "Lugar"

        // Puedes manejar el botón si quieres, aunque no responde a clicks desde el InfoWindow
        // porque los InfoWindows no son interactivos directamente.
        // Para hacerlo interactivo, necesitas implementar un listener en el marker click.

        // Por ejemplo, esconder el botón si quieres:
        // button.visibility = View.GONE
    }

    override fun getInfoWindow(marker: Marker): View? {
        render(marker, window)
        return window
    }

    override fun getInfoContents(marker: Marker): View? {
        // Usualmente devuelves null aquí si usas getInfoWindow()
        return null
    }
}