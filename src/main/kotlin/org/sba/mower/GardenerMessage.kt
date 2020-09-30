package org.sba.mower

sealed class GardenerMessage
data class CourseStarted(val mower: Mower) : GardenerMessage()
data class CourseCompleted(val mower: Mower) : GardenerMessage()
