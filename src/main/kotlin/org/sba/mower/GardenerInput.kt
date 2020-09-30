package org.sba.mower

/**
 * The first line corresponds to the upper right corner of the lawn. The bottom left corner is implicitly (0, 0).
 * The rest of the file describes the multiple mowers that are on the lawn.
 */
data class GardenerInput(
    val upperRightCellCoordinates: Coordinates,
    val mowerInputs: List<MowerInput>
)

data class MowerInput(
    val initialPosition: MowerPosition,
    val instructions: List<MowerInstruction>
) {
    constructor(position: String, instructions: String) : this(
        convertPosition(position),
        instructions.mapNotNull { convertInstruction(it) }
    )

    companion object {

        const val DELIMITER = " "

        fun convertInstruction(s: Char): MowerInstruction? {
            return when (s) {
                'F' -> MowerInstruction.FORWARD
                'L' -> MowerInstruction.LEFT
                'R' -> MowerInstruction.RIGHT
                else -> null
            }
        }

        fun convertPosition(line: String): MowerPosition {
            val inputs = line.split(DELIMITER)
            return MowerPosition(convertCoordinates(inputs[0], inputs[1]), convertOrientation(inputs[2]))
        }

        fun convertCoordinates(line: String): Coordinates {
            val inputs = line.split(DELIMITER)
            return convertCoordinates(inputs[0], inputs[1])
        }

        private fun convertCoordinates(first: String, second: String): Coordinates {
            return try {
                val x = first.toInt()
                val y = second.toInt()
                Coordinates(x, y)
            } catch (nfe: NumberFormatException) {
                Coordinates(0, 0)
            }
        }

        private fun convertOrientation(s: String): Orientation {
            return when (s) {
                "N" -> Orientation.NORTH
                "S" -> Orientation.SOUTH
                "E" -> Orientation.EAST
                "W" -> Orientation.WEST
                else -> Orientation.NORTH
            }
        }
    }
}
