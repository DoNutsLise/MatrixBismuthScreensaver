package com.donuts.matrixbismuthscreensaver;

import java.util.List;

/**
 * class to store text for each column of the matrix.
 * text in each column has its own properties: fontSize, style, speed of falling, etc.
 */

public class MatrixColumnModel {
    public List<Character> columnCharsList; // this list of chars hold the chars of the column which are already on the screen
    public List<Character> charsPoolList; // characters from this list will appear on the screen as the characters fall (new characters at the bottom); originally they were just randomly generated and there was no need in this list, but since we want to insert certain words in the falling code we have to use this list.
    public int textInitialHeightIndex; // from which pixel (vertical) characters start to fall down.
    public int textFinalHeightIndex; // at which pixel (vertical) characters disappear from the screen.
    public int textFontSize; // font size of the text in this column.
    public int textFontStyle; // font style of the text in the column, e.g. arial, times new roman, etc.
    public int textFallingSpeed; // each column has its own speed of falling.
    public int counter; // counts chars drawn from the pool of chars
    public List<Integer> charsVertPosIndexList; // a list of indexes of positions in the column for every char in the text string (we need to track their movement along the screen as they drop)
}
