package com.campushelper.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.campushelper.app.R
import com.campushelper.app.data.model.QuestionReview
import com.campushelper.app.databinding.ItemQuestionReviewBinding

class QuestionReviewAdapter : ListAdapter<QuestionReview, QuestionReviewAdapter.QuestionViewHolder>(QuestionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuestionReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    class QuestionViewHolder(private val binding: ItemQuestionReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(question: QuestionReview, questionNumber: Int) {
            binding.apply {
                // Question number and difficulty
                tvQuestionNumber.text = "Question $questionNumber"
                
                // Difficulty badge
                tvDifficulty.text = when (question.difficulty?.lowercase()) {
                    "easy" -> "🟢 Easy"
                    "medium" -> "🟡 Medium"
                    "hard" -> "🔴 Hard"
                    else -> "⚪ N/A"
                }
                
                // Result badge
                val isCorrect = question.userAnswer == question.correctAnswer
                if (isCorrect) {
                    tvResult.text = "✓"
                    tvResult.setTextColor(ContextCompat.getColor(root.context, R.color.success))
                    tvResult.setBackgroundResource(R.drawable.bg_option_correct)
                } else {
                    tvResult.text = "✗"
                    tvResult.setTextColor(ContextCompat.getColor(root.context, R.color.error))
                    tvResult.setBackgroundResource(R.drawable.bg_option_wrong)
                }
                
                // Question text
                tvQuestion.text = question.question
                
                // Options
                tvOptionA.text = "A. ${question.options[0]}"
                tvOptionB.text = "B. ${question.options[1]}"
                tvOptionC.text = "C. ${question.options[2]}"
                tvOptionD.text = "D. ${question.options[3]}"
                
                // Highlight correct and user's answer
                highlightOption(tvOptionA, 0, question.correctAnswer, question.userAnswer)
                highlightOption(tvOptionB, 1, question.correctAnswer, question.userAnswer)
                highlightOption(tvOptionC, 2, question.correctAnswer, question.userAnswer)
                highlightOption(tvOptionD, 3, question.correctAnswer, question.userAnswer)
                
                // Answer info
                val userAnswerText = if (question.userAnswer != null) {
                    "${getOptionLetter(question.userAnswer)}. ${question.options.getOrNull(question.userAnswer) ?: "N/A"}"
                } else {
                    "Not answered"
                }
                tvYourAnswer.text = userAnswerText
                tvYourAnswer.setTextColor(
                    if (isCorrect) {
                        ContextCompat.getColor(root.context, R.color.success)
                    } else {
                        ContextCompat.getColor(root.context, R.color.error)
                    }
                )
                
                val correctAnswerText = "${getOptionLetter(question.correctAnswer)}. ${question.options[question.correctAnswer]}"
                tvCorrectAnswer.text = correctAnswerText
            }
        }
        
        private fun highlightOption(
            textView: android.widget.TextView,
            index: Int,
            correctAnswer: Int,
            userAnswer: Int?
        ) {
            when {
                index == correctAnswer -> {
                    // Correct answer - green background
                    textView.setBackgroundResource(R.drawable.bg_option_correct)
                    textView.setTextColor(ContextCompat.getColor(textView.context, R.color.white))
                }
                index == userAnswer && userAnswer != correctAnswer -> {
                    // Wrong answer - red background
                    textView.setBackgroundResource(R.drawable.bg_option_wrong)
                    textView.setTextColor(ContextCompat.getColor(textView.context, R.color.white))
                }
                else -> {
                    // Default
                    textView.setBackgroundResource(R.drawable.bg_option_default)
                    textView.setTextColor(ContextCompat.getColor(textView.context, R.color.text_primary))
                }
            }
        }
        
        private fun getOptionLetter(index: Int): String {
            return when (index) {
                0 -> "A"
                1 -> "B"
                2 -> "C"
                3 -> "D"
                else -> "?"
            }
        }
    }

    class QuestionDiffCallback : DiffUtil.ItemCallback<QuestionReview>() {
        override fun areItemsTheSame(oldItem: QuestionReview, newItem: QuestionReview): Boolean {
            return oldItem.question == newItem.question
        }

        override fun areContentsTheSame(oldItem: QuestionReview, newItem: QuestionReview): Boolean {
            return oldItem == newItem
        }
    }
}
