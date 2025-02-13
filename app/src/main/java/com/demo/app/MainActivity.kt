package com.demo.app

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ScaleDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ProgressBar
import com.demo.app.databinding.ActivityMainBinding
import com.google.android.material.shape.MarkerEdgeTreatment
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.OffsetEdgeTreatment
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.TriangleEdgeTreatment

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    val binding by lazy { ActivityMainBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val sam =  ShapeAppearanceModel.builder()
            .setAllCorners(RoundedCornerTreatment())
            .setAllCornerSizes(10f.dp)
            .setBottomEdge(OffsetEdgeTreatment(MarkerEdgeTreatment(40f.dp), (0f).dp))
            .setTopEdge(TriangleEdgeTreatment(20f.dp, false))
            .build()
        binding.shape.background = MaterialShapeDrawable(sam).apply {
            setTint(Color.GRAY)
            setCornerSize(20f.dp)
            strokeWidth = 2f.dp
            strokeColor = ColorStateList.valueOf(Color.RED)
        }

        binding.pb1.setOnClickListener { (it as ProgressBar).update() }
        binding.pb2.setOnClickListener { (it as ProgressBar).update() }
        binding.pb3.setOnClickListener { (it as ProgressBar).update() }
    }

    private fun ProgressBar.update() {
        progress = (progress + 1) % 100
    }

    private val Float.dp: Float get() = resources.displayMetrics.density * this
}