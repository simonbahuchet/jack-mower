package org.sba.mower

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import log
import org.sba.mower.MowerInstruction.*
import java.util.concurrent.CancellationException

class Mower(
    val gardenerChannel: SendChannel<GardenerMessage>,
    val scope: CoroutineScope
) : CoroutineScope by scope {

    lateinit var id: String
        private set

    private var waitingForMovementAck = false

    // In milliseconds
    private val waitDuration: Long = 300
    private val forwardDelay: Long = 100
    private val rotationDelay: Long = 200

    lateinit var lawnUpperRightCell: Coordinates
        private set

    lateinit var position: MowerPosition
        private set

    private lateinit var instructions: List<MowerInstruction>

    private lateinit var instructionsIterator: Iterator<MowerInstruction>

    private lateinit var otherMowers: List<Mower>

    val job = Job()

    lateinit var inputChannel: SendChannel<MowerMessage>

    constructor(
        identifier: Int,
        lawnUpperRightCell: Coordinates,
        position: MowerPosition,
        instructions: List<MowerInstruction>,
        gardenerChannel: SendChannel<GardenerMessage>,
        scope: CoroutineScope
    ) : this(gardenerChannel, scope) {
        this.id = "Mower-$identifier"
        this.lawnUpperRightCell = lawnUpperRightCell
        this.position = position
        this.instructions = instructions

        this.inputChannel = actor(
            context = CoroutineName(this.id) + job + Dispatchers.Default,
            onCompletion = {
                log("$id actor completed")
            }
        ) {
            for (message in channel) {
                log("Received $message")
                when (message) {
                    is Registration -> {
                        log("There is (are) ${message.mowers.size} other mower(s) with me: ${message.mowers.joinToString { it.id }}")
                        otherMowers = message.mowers
                    }
                    Start -> start()
                    Stop -> complete()
                    is CellOccupationRequest -> message.responseChannel.send(
                        if (position.coordinates == message.target) OccupiedCell(id) else UnOccupiedCell(id)
                    )
                }
            }
        }
    }

    private suspend fun start() {
        log("Let's start")

        // Init the iterator
        instructionsIterator = instructions.iterator()


        // Acknowledge the gardener
        gardenerChannel.send(CourseStarted(this))


        // Start with the first instruction
        log("Start with the 1st instruction")
        handleInstruction()
    }

    private suspend fun handleInstruction() {
        if (waitingForMovementAck) {
            delay(waitDuration)
            return
        }

        if (instructionsIterator.hasNext()) {
            when (val instruction = instructionsIterator.next()) {
                FORWARD -> {
                    val nextCell = position.getNextCell()
                    if (nextCell.x < 0 || nextCell.y < 0 || nextCell.x > lawnUpperRightCell.x || nextCell.y > lawnUpperRightCell.y) {
                        log("Discard FORWARD instruction since we would get off the lawn")
                        handleInstruction()
                    } else {
                        requestCell(nextCell)
                    }
                }
                else -> {
                    rotate(instruction)
                    handleInstruction()
                }
            }

        } else {
            log("Run out of instructions. Job completed")
            gardenerChannel.send(CourseCompleted(this))
        }
    }

    private suspend fun requestCell(cell: Coordinates) {

        // wait for their answers
        this.waitingForMovementAck = true

        // request the cell against all the other mowers

        val cellRequester = actor<CellOccupationResponse>(
            context = CoroutineName("$id-receiver") + Dispatchers.Default,
            capacity = otherMowers.size,
            onCompletion = {
                log("$id-receiver actor completed")
            }
        ) {
            var counter = 0
            for (message in channel) {
                when (message) {
                    is OccupiedCell -> {
                        log("Cannot move forward $cell since it's occupied by ${message.mowerId}")

                        // Do not wait any longer
                        channel.cancel(CancellationException("Already got a negative answer from ${message.mowerId}"))

                        //discard the move and go to the next
                        waitingForMovementAck = false
                        handleInstruction()

                        break
                    }
                    is UnOccupiedCell -> {
                        log("${message.mowerId} replied it does not occupy $cell")
                        counter++
                    }
                }
                if (counter == otherMowers.size) {
                    log("All the other mowers replied. Path is clear")

                    // All clear, let's move
                    waitingForMovementAck = false
                    move(cell)
                    handleInstruction()

                    channel.close()
                    break
                }
            }
        }

        launch(CoroutineName("$id-requester") + Dispatchers.Default) {
            otherMowers.forEach { otherMower ->
                log("Querying ${otherMower.id} to request $cell")
                otherMower.inputChannel.send(CellOccupationRequest(id, cell, cellRequester))
            }
        }
    }

    private fun move(cell: Coordinates) {
        log("$id is moving to $cell")
        Thread.sleep(forwardDelay)
        position.move(cell)
        log("$id is now on $cell")
    }

    private fun rotate(rotationInstruction: MowerInstruction) {
        log("$id is rotating $rotationInstruction")
        Thread.sleep(rotationDelay)
        when (rotationInstruction) {
            RIGHT -> this.position.rotateRight()
            LEFT -> this.position.rotateLeft()
        }
        log("$id is now heading ${this.position.orientation}")
    }

    private fun complete() {
        log("Shutting down mower $id")
        inputChannel.close()
        job.complete()
    }

    override fun toString(): String {
        return "{id:$id}"
    }
}
