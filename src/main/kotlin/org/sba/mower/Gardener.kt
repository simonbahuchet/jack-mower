package org.sba.mower

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import log

class Gardener(private val scope: CoroutineScope) : CoroutineScope by scope {

    suspend fun mow(inputs: GardenerInput): List<MowerPosition> {
        val gardenerJob = Job()
        val gardenerChannel: SendChannel<GardenerMessage> = actor(
            context = CoroutineName("Gardener") + gardenerJob + Dispatchers.Default,
            onCompletion = {
                log("Gardener actor completed")
            }
        ) {
            var mowers = mutableListOf<Mower>()
            var completionCount = 0
            for (message in channel) {
                when (message) {
                    is CourseStarted -> {
                        log("${message.mower.id} just started")
                        mowers.add(message.mower)
                    }
                    is CourseCompleted -> {
                        log("${message.mower.id} just completed. It's position is ${message.mower.position}")
                        completionCount++
                    }
                }

                // They all completed their mowing. Let's shut them down
                if (completionCount == mowers.size) {
                    mowers.forEach { mower ->
                        mower.inputChannel.send(Stop)
                    }
                    break
                }
            }
        }

        checkMowersCoordinates(inputs)

        var mowers = inputs.mowerInputs.mapIndexed { index, mowerInputs ->

            Mower(
                index,
                inputs.upperRightCellCoordinates,
                mowerInputs.initialPosition,
                mowerInputs.instructions,
                gardenerChannel,
                scope
            )
        }

        mowers.forEach { mower ->
            mower.inputChannel.send(Registration(mowers.filter { it.id != mower.id }))
            mower.inputChannel.send(Start)
        }

        mowers.forEach {
            it.job.join()
        }

        gardenerChannel.close()
        gardenerJob.complete()

        log("The end")

        return mowers.sortedBy { it.id }.map { it.position }
    }

    private fun checkMowersCoordinates(inputs: GardenerInput) {

        inputs.mowerInputs.forEach {
            checkMowerLiesOnLawn(inputs.upperRightCellCoordinates, it.initialPosition.coordinates)
        }

        //Check mowers do not share the same cell
        checkNoCollocation(inputs.mowerInputs.map { it.initialPosition.coordinates })
    }

    private fun checkNoCollocation(mowerCells: List<Coordinates>) {
        val hasDuplicate = mowerCells.any { mowerCell ->
            mowerCells.count { mowerCell == it } > 1
        }
        require(!hasDuplicate) { "There can't be 2 mowers sharing the same location. Please correct the inputs" }
    }

    /**
     * @throws IllegalArgumentException if the mower is off the field. ie. its coordinates are negative or exceed the
     * "upper cell limit"
     */
    private fun checkMowerLiesOnLawn(upperRightCell: Coordinates, mowerCell: Coordinates) {
        require(
            mowerCell.x >= 0 && mowerCell.x <= upperRightCell.x
                    && mowerCell.y >= 0 && mowerCell.y <= upperRightCell.y
        ) {
            "The initial coordinates of the mower cannot be off field. Either correct the upper right cell coordinates or the mower's initial ones"
        }
    }
}

