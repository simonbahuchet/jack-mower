package org.sba.mower

import kotlinx.coroutines.channels.SendChannel

sealed class MowerMessage

data class Registration(
    val mowers: List<Mower>
) : MowerMessage()

object Start : MowerMessage() {
    override fun toString(): String {
        return "StartMessage"
    }
}

object Stop : MowerMessage() {
    override fun toString(): String {
        return "StopMessage"
    }
}

data class CellOccupationRequest(
    val requesterId: String,
    val target: Coordinates,
    val responseChannel: SendChannel<CellOccupationResponse>
) : MowerMessage()

sealed class CellOccupationResponse
data class UnOccupiedCell(val mowerId: String) : CellOccupationResponse()
data class OccupiedCell(val mowerId: String) : CellOccupationResponse()


