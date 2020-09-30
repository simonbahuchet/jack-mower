package org.sba.mower

import org.sba.mower.Orientation.*

/**
 * The first line contains the mower's starting position and orientation in the format "X Y O".
 * X and Y are the coordinates and O is the orientation.
 */
data class MowerPosition(
    var coordinates: Coordinates = Coordinates(0, 0),
    var orientation: Orientation = NORTH
) {
    fun rotateRight() {
        orientation = when (orientation) {
            NORTH -> EAST
            SOUTH -> WEST
            WEST -> NORTH
            EAST -> SOUTH
        }
    }

    fun rotateLeft() {
        orientation = when (orientation) {
            NORTH -> WEST
            SOUTH -> EAST
            WEST -> SOUTH
            EAST -> NORTH
        }
    }

    fun getNextCell(): Coordinates {
        return when (orientation) {
            NORTH -> Coordinates(coordinates.x, coordinates.y + 1)
            SOUTH -> Coordinates(coordinates.x, coordinates.y - 1)
            WEST -> Coordinates(coordinates.x - 1, coordinates.y)
            EAST -> Coordinates(coordinates.x + 1, coordinates.y)
        }
    }

    fun move(cell: Coordinates? = null) {
        check(cell == null || cell == getNextCell()) {
            "The order is not consistent"
        }
        this.coordinates = getNextCell()
    }
}

data class Coordinates(val x: Int, val y: Int)

enum class Orientation { NORTH, SOUTH, WEST, EAST }

enum class MowerInstruction { FORWARD, LEFT, RIGHT }
