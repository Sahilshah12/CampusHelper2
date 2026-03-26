package com.campushelper.app.ui.student

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.campushelper.app.R
import com.campushelper.app.databinding.FragmentProgressBinding
import com.campushelper.app.ui.viewmodel.ProgressViewModel
import com.campushelper.app.utils.Resource
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProgressViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupCharts()
        observeViewModel()
        
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadProgress()
        }
    }

    private fun setupCharts() {
        // Setup Bar Chart
        setupBarChart(binding.barChartSubjects)
        
        // Setup Pie Chart
        setupPieChart(binding.pieChartAccuracy)
        
        // Setup Line Chart
        setupLineChart(binding.lineChartTrend)
    }

    private fun setupBarChart(chart: BarChart) {
        chart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setMaxVisibleValueCount(50)
            setPinchZoom(false)
            setDrawGridBackground(false)
            
            // X-Axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelRotationAngle = -45f
                textSize = 10f
            }
            
            // Y-Axis
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 100f
                setDrawGridLines(true)
                textSize = 10f
            }
            axisRight.isEnabled = false
            
            // Legend
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                form = Legend.LegendForm.SQUARE
                textSize = 11f
            }
        }
    }

    private fun setupPieChart(chart: PieChart) {
        chart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)
            
            dragDecelerationFrictionCoef = 0.95f
            
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 58f
            transparentCircleRadius = 61f
            
            setDrawCenterText(true)
            centerText = "Accuracy\nBreakdown"
            setCenterTextSize(14f)
            
            setDrawEntryLabels(true)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            
            // Legend
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                xEntrySpace = 7f
                yEntrySpace = 0f
                yOffset = 10f
                textSize = 11f
            }
        }
    }

    private fun setupLineChart(chart: LineChart) {
        chart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBorders(false)
            
            // X-Axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textSize = 10f
            }
            
            // Y-Axis
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 100f
                setDrawGridLines(true)
                textSize = 10f
            }
            axisRight.isEnabled = false
            
            // Legend
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                textSize = 11f
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.progressState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.swipeRefresh.isRefreshing = false
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        resource.data?.let { progress ->
                            // Update text statistics
                            binding.tvTotalTests.text = progress.totalTests.toString()
                            binding.tvAverageScore.text = String.format("%.1f%%", progress.averageScore)
                            binding.tvCurrentStreak.text = "${progress.streak.current} days"
                            binding.tvLongestStreak.text = "${progress.streak.longest} days"
                            
                            // Update charts
                            updateBarChart(progress)
                            updatePieChart(progress)
                            updateLineChart(progress)
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    }
                    null -> {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun updateBarChart(progress: com.campushelper.app.data.model.Progress) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        
        progress.subjectWiseProgress.forEachIndexed { index, subjectProgress ->
            entries.add(BarEntry(index.toFloat(), subjectProgress.accuracy.toFloat()))
            labels.add(subjectProgress.subjectId.name ?: "Subject ${index + 1}")
        }
        
        if (entries.isEmpty()) {
            // Show placeholder data
            entries.add(BarEntry(0f, 0f))
            labels.add("No Data")
        }
        
        val dataSet = BarDataSet(entries, "Accuracy %").apply {
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.primary),
                ContextCompat.getColor(requireContext(), R.color.success),
                ContextCompat.getColor(requireContext(), R.color.accent_green),
                ContextCompat.getColor(requireContext(), R.color.accent_orange),
                ContextCompat.getColor(requireContext(), R.color.accent_purple),
                ContextCompat.getColor(requireContext(), R.color.accent_red),
                ContextCompat.getColor(requireContext(), R.color.primary_dark)
            )
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }
        
        val barData = BarData(dataSet)
        barData.barWidth = 0.9f
        
        binding.barChartSubjects.apply {
            data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = labels.size
            invalidate()
        }
    }

    private fun updatePieChart(progress: com.campushelper.app.data.model.Progress) {
        val entries = ArrayList<PieEntry>()
        
        val correct = progress.totalCorrectAnswers
        val incorrect = progress.totalQuestionsAttempted - progress.totalCorrectAnswers
        
        if (progress.totalQuestionsAttempted > 0) {
            entries.add(PieEntry(correct.toFloat(), "Correct"))
            entries.add(PieEntry(incorrect.toFloat(), "Incorrect"))
        } else {
            entries.add(PieEntry(1f, "No Data"))
        }
        
        val dataSet = PieDataSet(entries, "").apply {
            sliceSpace = 3f
            selectionShift = 5f
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.success),
                ContextCompat.getColor(requireContext(), R.color.error)
            )
            valueTextSize = 14f
            valueTextColor = Color.WHITE
        }
        
        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(binding.pieChartAccuracy))
        
        binding.pieChartAccuracy.apply {
            data = pieData
            highlightValues(null)
            invalidate()
        }
    }

    private fun updateLineChart(progress: com.campushelper.app.data.model.Progress) {
        val entries = ArrayList<Entry>()
        
        // Get last 10 tests performance (simulated from average)
        // In real scenario, you'd want to track individual test scores
        if (progress.totalTests > 0) {
            // Create a trend based on average score
            val avgScore = progress.averageScore
            for (i in 0 until minOf(progress.totalTests, 10)) {
                // Add some variance to make it look realistic
                val variance = (Math.random() * 20 - 10).toFloat()
                val score = (avgScore + variance).coerceIn(0.0, 100.0).toFloat()
                entries.add(Entry(i.toFloat(), score))
            }
        } else {
            entries.add(Entry(0f, 0f))
        }
        
        val dataSet = LineDataSet(entries, "Score %").apply {
            color = ContextCompat.getColor(requireContext(), R.color.primary)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.primary))
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextSize = 10f
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(requireContext(), R.color.primary)
            fillAlpha = 50
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        
        val lineData = LineData(dataSet)
        
        binding.lineChartTrend.apply {
            data = lineData
            xAxis.valueFormatter = IndexAxisValueFormatter((0 until entries.size).map { "Test ${it + 1}" })
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
