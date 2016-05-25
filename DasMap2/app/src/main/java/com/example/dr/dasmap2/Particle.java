package com.example.dr.dasmap2;

/**
 * Created by dr on 20-5-16.
 */
public class Particle {
    private int xPos = 0;
    private int yPos = 0;
    public int cell = 0;
    public boolean[] dirBoundary = {true, true, true, true};
    public float[] boundaries = {0f, 0f, 140f, 600f};

    public boolean isActive = true;


    Particle(int x, int y) {
        this.xPos = x;
        this.yPos = y;
    }

    public void setPos(int newX, int newY) {
        xPos = newX;
        yPos = newY;
    }

    public int[] getPos() {
        int ar[] = new int[2];
        ar[0] = xPos;
        ar[1] = yPos;
        return ar;
    }

    public void setBoundaries(Cell[] cells, int cellInd) {
        boundaries[0] = cells[cellInd].x1;
        boundaries[1] = cells[cellInd].y1;
        boundaries[2] = cells[cellInd].x2;
        boundaries[3] = cells[cellInd].y2;
        dirBoundary[0] = cells[cellInd].dirBoundaries[0];
        dirBoundary[1] = cells[cellInd].dirBoundaries[1];
        dirBoundary[2] = cells[cellInd].dirBoundaries[2];
        dirBoundary[3] = cells[cellInd].dirBoundaries[3];
    }

    public void bringToLife(Cell[] cells, int newInd) {
        dirBoundary[0] = cells[newInd].dirBoundaries[0];
        dirBoundary[1] = cells[newInd].dirBoundaries[1];
        dirBoundary[2] = cells[newInd].dirBoundaries[2];
        dirBoundary[3] = cells[newInd].dirBoundaries[3];
        boundaries[0] = cells[newInd].x1;
        boundaries[1] = cells[newInd].y1;
        boundaries[2] = cells[newInd].x2;
        boundaries[3] = cells[newInd].y2;
        isActive = true;
    }

    public void moveXY(int distX, int distY) {
        xPos = xPos + distX;
        yPos = yPos + distY;

        if (xPos > boundaries[2] && dirBoundary[2] == true) {
            isActive = false;
        }
        if (xPos < boundaries[0] && dirBoundary[0] == true) {
            isActive = false;
        }
        if (yPos > boundaries[3] && dirBoundary[3] == true) {
            isActive = false;
        }
        if (yPos < boundaries[1] && dirBoundary[1] == true) {
            isActive = false;
        }
    }

    public void activate() {
        isActive = true;
    }

    public void disable() {
        isActive = false;
    }

}
