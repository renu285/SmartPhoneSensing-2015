package com.example.dr.dasmap2;

/**
 * Created by dr on 22-5-16.
 */
public class Cell {
    public float x1, x2, y1, y2;
    public boolean[] dirBoundaries = {false, false, false, false};
    public int prob = 0;


    public void setCell(float x, float y, float xx, float yy) {
        x1 = x; x2 = xx; y1 = y; y2 = yy;
    }

    public void setDirBoundaries(boolean db0, boolean db1, boolean db2, boolean db3) {
        dirBoundaries[0] = db0; dirBoundaries[1] = db1;
        dirBoundaries[2] = db2; dirBoundaries[3] = db3;
    }

}
