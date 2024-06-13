package com.adreal.wearos.journeytracker.presentation.model

data class JourneyModel (
    val id : Int,
    val commute : Int,
    val timestamps : List<Long>
)