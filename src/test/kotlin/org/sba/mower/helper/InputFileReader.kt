package org.sba.mower.helper

import org.sba.mower.*
import org.sba.mower.MowerInstruction.*
import org.sba.mower.Orientation.EAST
import org.sba.mower.Orientation.NORTH

fun mockInputNominalCase(): GardenerInput {
    return GardenerInput(
        Coordinates(5, 5),
        listOf(
            MowerInput(
                initialPosition = MowerPosition(Coordinates(1, 2), NORTH),
                listOf(LEFT, FORWARD, LEFT, FORWARD, LEFT, FORWARD, LEFT, FORWARD, FORWARD)
            ),
            MowerInput(
                initialPosition = MowerPosition(Coordinates(3, 3), EAST),
                listOf(FORWARD, FORWARD, RIGHT, FORWARD, FORWARD, RIGHT, FORWARD, RIGHT, RIGHT, FORWARD)
            )
        )
    )
}

fun mockInputOffField(): GardenerInput {
    return GardenerInput(
        Coordinates(0, 0),
        listOf(
            MowerInput(
                initialPosition = MowerPosition(Coordinates(0, 0), NORTH),
                listOf(LEFT, LEFT, LEFT, FORWARD)
            ),
            MowerInput(
                initialPosition = MowerPosition(Coordinates(1, 0), EAST),
                listOf(RIGHT)
            )
        )
    )
}

fun mockInputCollocation(): GardenerInput {
    return GardenerInput(
        Coordinates(5, 5),
        listOf(
            MowerInput(
                initialPosition = MowerPosition(Coordinates(1, 2), NORTH),
                listOf(LEFT, FORWARD, LEFT, FORWARD, LEFT, FORWARD, LEFT, FORWARD, FORWARD)
            ),
            MowerInput(
                initialPosition = MowerPosition(Coordinates(1, 2), EAST),
                listOf(FORWARD, FORWARD, RIGHT, FORWARD, FORWARD, RIGHT, FORWARD, RIGHT, RIGHT, FORWARD)
            )
        )
    )
}

fun resultNominalCase(): MowerFleetState {
    return MowerFleetState(
        positions = listOf(
            MowerPosition(Coordinates(1, 3), NORTH),
            MowerPosition(Coordinates(5, 1), EAST),
        )
    )
}

fun readInputs(path: String): GardenerInput {
    val lines = GardenerTest::class.java.getResourceAsStream(path).bufferedReader().readLines()

    // The first line corresponds to the upper right corner of the lawn
    val upperRightCellCoordinates = MowerInput.convertCoordinates(lines.head)

    // The rest of the file describes the multiple mowers that are on the lawn.
    // Each mower is described on two lines
    val mowerInputs: MutableList<MowerInput> = mutableListOf()
    var firstLine = ""
    for (line in lines.tail) {
        if (firstLine.isEmpty()) {
            firstLine = line
        } else {
            mowerInputs.add(MowerInput(firstLine, line))
            firstLine = ""
        }
    }

    return GardenerInput(upperRightCellCoordinates, mowerInputs)
}

fun readResults(path: String): MowerFleetState {
    val lines = GardenerTest::class.java.getResourceAsStream(path).bufferedReader().readLines()

    // The rest of the file describes the multiple mowers that are on the lawn.
    val positions: MutableList<MowerPosition> = mutableListOf()
    for (line in lines) {
        positions.add(MowerInput.convertPosition(line))
    }

    return MowerFleetState(positions)
}

/**
 * the final position and orientation of each mower is output in the order that the mower appeared in the input.
 */
data class MowerFleetState(
    val positions: List<MowerPosition>
)
