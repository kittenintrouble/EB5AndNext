package com.eb5.app.ui.quizzes

import com.eb5.app.data.model.QuizCatalog
import com.eb5.app.data.model.QuizQuestion
import com.eb5.app.data.model.QuizTopic
import com.eb5.app.data.model.QuizTrack

object QuizSeedData {

    fun defaultCatalog(): QuizCatalog {
        val quizzes = listOf(
            QuizTopic(
                id = "seed_program_basics",
                termId = null,
                title = "EB-5 Program Fundamentals",
                summary = "Understand the goal of the EB-5 program and baseline investor requirements.",
                category = "EB-5 Basics",
                subcategory = "Program Basics",
                trackIds = listOf("seed_track_foundations"),
                goalTags = listOf("deal_ready"),
                format = "Multi",
                level = "L",
                durationMinutes = 5,
                recommendedArticles = emptyList(),
                requiredArticles = emptyList(),
                tags = listOf("basics", "timeline"),
                questions = listOf(
                    QuizQuestion(
                        question = "What is the primary policy objective of the EB-5 Immigrant Investor Program?",
                        options = listOf(
                            "Provide guaranteed profits for immigrant investors",
                            "Stimulate the U.S. economy through job-creating investment",
                            "Fund state education initiatives",
                            "Offer automatic citizenship after investment"
                        ),
                        correctAnswerIndex = 1
                    ),
                    QuizQuestion(
                        question = "How many full-time U.S. jobs must a qualifying EB-5 investment create?",
                        options = listOf("5", "10", "15", "20"),
                        correctAnswerIndex = 1
                    )
                )
            ),
            QuizTopic(
                id = "seed_investment_structures",
                termId = null,
                title = "Direct vs. Regional Center Investment",
                summary = "Compare investor responsibilities and job counting between direct and regional center projects.",
                category = "Investment",
                subcategory = "Investment Models",
                trackIds = listOf("seed_track_foundations"),
                goalTags = listOf("deal_ready", "risk"),
                format = "Scenario",
                level = "M",
                durationMinutes = 6,
                tags = listOf("structures"),
                questions = listOf(
                    QuizQuestion(
                        question = "Which feature is unique to regional center EB-5 projects?",
                        options = listOf(
                            "Ability to count indirect job creation",
                            "Lower investment thresholds than TEA projects",
                            "No need for USCIS annual reporting",
                            "Guaranteed repayment by the center"
                        ),
                        correctAnswerIndex = 0
                    ),
                    QuizQuestion(
                        question = "How does a direct EB-5 investor typically participate in the enterprise?",
                        options = listOf(
                            "They take an active managerial role",
                            "They delegate all decisions to a fund administrator",
                            "They invest through government bonds",
                            "They pool capital via overseas agents"
                        ),
                        correctAnswerIndex = 0
                    )
                )
            ),
            QuizTopic(
                id = "seed_source_of_funds",
                termId = null,
                title = "Source of Funds Essentials",
                summary = "Outline lawful source expectations and typical documentation for EB-5 capital.",
                category = "Risk Management",
                subcategory = "Legal and Financial Safeguards",
                trackIds = listOf("seed_track_compliance"),
                goalTags = listOf("compliance", "source_funds"),
                format = "Multi",
                level = "M",
                durationMinutes = 7,
                tags = listOf("compliance"),
                questions = listOf(
                    QuizQuestion(
                        question = "What is the cornerstone requirement for demonstrating a lawful source of EB-5 funds?",
                        options = listOf(
                            "A notarized letter from any bank",
                            "A credible documentation trail linking funds to lawful activity",
                            "A regional center certification",
                            "Tax-free treatment of proceeds"
                        ),
                        correctAnswerIndex = 1
                    ),
                    QuizQuestion(
                        question = "Which documents typically accompany gifted EB-5 capital?",
                        options = listOf(
                            "Gift deed and donor financial records",
                            "Only a verbal confirmation",
                            "A developer guarantee",
                            "Proof of cryptocurrency transfer"
                        ),
                        correctAnswerIndex = 0
                    )
                )
            )
        )

        val tracks = listOf(
            QuizTrack(
                id = "seed_track_foundations",
                title = "EB-5 Foundations Quickstart",
                description = "A short path to understand program purpose and key decision points before deeper research.",
                quizIds = listOf("seed_program_basics", "seed_investment_structures"),
                estimatedDurationMinutes = 12,
                goalTag = "deal_ready",
                category = "EB-5 Basics",
                unlock = null,
                certificate = null
            ),
            QuizTrack(
                id = "seed_track_compliance",
                title = "Compliance Essentials",
                description = "Review core documentation and lawful source expectations for investor readiness.",
                quizIds = listOf("seed_source_of_funds"),
                estimatedDurationMinutes = 7,
                goalTag = "compliance",
                category = "Risk Management",
                unlock = null,
                certificate = null
            )
        )

        return QuizCatalog(quizzes = quizzes, tracks = tracks)
    }
}
