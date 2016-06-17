package com.example.dr.dasmap2;

/**
 * Created by dr on 20-5-16.
 */
public class Particle {
    public float xPos = 0;
    public float yPos = 0;
    public int cell = 0;
    public boolean[] dirBoundary = {true, true, true, true};
    public float[] boundaries = {0f, 0f, 140f, 600f};
    public boolean newLocation = false;
    public int age = -1;
    public int closeParticles = 0;

    public boolean wallHit = false;

    public boolean isActive = true;


    Particle(float x, float y) {
        this.xPos = x;
        this.yPos = y;
    }

    public void setPos(int newX, int newY) {
        xPos = newX;
        yPos = newY;
    }

    public float[] getPos() {
        float ar[] = new float[2];
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
        cells[newInd].particleCellCounter += 1;
        isActive = true;
    }

    public void move(float distX, float distY) {
        xPos = xPos + distX;
        yPos = yPos + distY;
    }

    public boolean moveXY(float distX, float distY, Cell[] cells, Map map) {
        xPos = xPos + distX;
        yPos = yPos + distY;
        wallHit = false;
        int newInd;

        if (xPos > boundaries[2]) {
            if (dirBoundary[2] == true) {
                isActive = false;
                wallHit = true;
//                cells[cell].particleCellCounter --;
            }
            else {
                newLocation = true;
//                cells[cell].particleCellCounter --;
//                newInd = map.whichCell(cells, xPos, yPos);
//                cells[newInd].particleCellCounter ++;
            }
        }

        if (xPos < boundaries[0]) {
            if (dirBoundary[0] == true) {
                isActive = false;
                wallHit = true;
//                cells[cell].particleCellCounter --;
            }
            else {
                newLocation = true;
//                cells[cell].particleCellCounter --;
//                newInd = map.whichCell(cells, xPos, yPos);
//                cells[newInd].particleCellCounter ++;
            }
        }

        if (yPos > boundaries[3]) {
            if (dirBoundary[3] == true) {
                isActive = false;
                wallHit = true;
//                cells[cell].particleCellCounter --;
            }
            else {
                newLocation = true;
//                cells[cell].particleCellCounter --;
//                newInd = map.whichCell(cells, xPos, yPos);
//                cells[newInd].particleCellCounter ++;
            }
        }

        if (yPos < boundaries[1]) {
            if (dirBoundary[1] == true) {
                isActive = false;
                wallHit = true;
//                cells[cell].particleCellCounter --;
            }
            else {
                newLocation = true;
//                cells[cell].particleCellCounter --;
//                newInd = map.whichCell(cells, xPos, yPos);
//                cells[newInd].particleCellCounter ++;
            }
        }

        return wallHit;
    }

    public void activate() {
        isActive = true;
    }

    public void disable() {
        isActive = false;
    }

}
