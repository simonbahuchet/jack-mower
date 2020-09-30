package org.sba.mower

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource


class MowerPositionTest {

    @ParameterizedTest
    @CsvSource("SOUTH,WEST", "WEST,NORTH", "NORTH,EAST", "EAST,SOUTH")
    fun `Mower rotating right`(
        initialOrientation: Orientation,
        expectedOrientation: Orientation
    ) {
        //Given
        val mower = MowerPosition(Coordinates(0, 1), initialOrientation)

        // When
        mower.rotateRight()

        // Then
        Assertions.assertEquals(expectedOrientation, mower.orientation)
    }

    @ParameterizedTest
    @CsvSource("WEST,SOUTH", "NORTH,WEST", "EAST,NORTH", "SOUTH,EAST")
    fun `Mower rotating left`(
        initialOrientation: Orientation,
        expectedOrientation: Orientation
    ) {
        //Given
        val mower = MowerPosition(Coordinates(0, 1), initialOrientation)

        // When
        mower.rotateLeft()

        // Then
        Assertions.assertEquals(expectedOrientation, mower.orientation)
    }

    @ParameterizedTest
    @CsvSource("WEST,WEST", "NORTH,NORTH", "EAST,EAST", "SOUTH,SOUTH")
    fun `Mower moving forward should keep its orientation`(
        initialOrientation: Orientation,
        expectedOrientation: Orientation
    ) {
        //Given
        val mower = MowerPosition(Coordinates(0, 1), initialOrientation)

        // When
        mower.move()

        // Then
        Assertions.assertEquals(expectedOrientation, mower.orientation)
    }

    @ParameterizedTest
    @CsvSource("1,1,WEST,0,1", "1,1,NORTH,1,2", "1,1,SOUTH,1,0", "1,1,EAST,2,1")
    fun `Mower moving forward should update the coordinates`(
        x: Int, y: Int, orientation: Orientation, expectedX: Int, expectedY: Int
    ) {
        //Given
        val mower = MowerPosition(Coordinates(x, y), orientation)

        // When
        mower.move()

        // Then
        Assertions.assertEquals(Coordinates(expectedX, expectedY), mower.coordinates)
    }
}
