package com.example.dr.dasmap2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

/**
 * Created by dr on 21-5-16.
 */
public class Map {


    public int isCell(Cell[] cells, int newCell) {
        int counter = 0;
        int cellInd = 10;

        for (int i=0; i<cells.length; i++) {
            if (newCell <= (cells[i].prob+counter)) {
                cellInd = i;
                break;
            }
            counter += cells[i].prob;
        }

        return cellInd;
    }

    public Cell[] initCells(Cell[] cells) {
        for (int i=0; i<cells.length; i++) {
            cells[i] = new Cell();
        }

        cells[0].setCell(-1, -1, -1, -1);
        cells[1].setCell(76.8f, 108.8f, 125.6f, 140.8f);
        cells[2].setCell(60f, 108.8f, 76.8f, 140.8f);
        cells[3].setCell(60f, 140.8f, 76.8f, 172.8f);
        cells[4].setCell(11.2f, 140.8f, 60f, 172.8f);
        cells[5].setCell(60f, 172.8f, 76.8f, 204.8f);
        cells[6].setCell(60f, 204.8f, 76.8f, 236.8f);
        cells[7].setCell(60f, 236.8f, 76.8f, 268.8f);
        cells[8].setCell(76.8f, 236.8f, 125.6f, 268.8f);
        cells[9].setCell(60f, 268.8f, 76.8f, 300.8f);
        cells[10].setCell(60f, 300.8f, 76.8f, 332.8f);
        cells[11].setCell(60f, 332.8f, 76.8f, 364.8f);
        cells[12].setCell(60f, 364.8f, 76.8f, 396.8f);
        cells[13].setCell(11.2f, 364.8f, 60.8f, 396.8f);
        cells[14].setCell(60f, 396.8f, 76.8f, 428.8f);
        cells[15].setCell(60f, 428.8f, 76.8f, 460.8f);
        cells[16].setCell(60.8f, 460.8f, 76.8f, 492.8f);
        cells[17].setCell(11.2f, 460.8f, 60.8f, 492.8f);
        cells[18].setCell(101.6f, 460.8f, 125.6f, 492.8f);
        cells[19].setCell(76.8f, 460.8f, 101.6f, 468.8f);
//        cells[19].setCell(76.8f, 460.8f, 101.6f, 492.8f);

//                                 left, up, right, down
        cells[0].setDirBoundaries(true, true, true, true);
        cells[1].setDirBoundaries(false, true, true, true);
        cells[2].setDirBoundaries(true, true, false, false);
        cells[3].setDirBoundaries(false, false, true, false);
        cells[4].setDirBoundaries(true, true, false, true);
        cells[5].setDirBoundaries(true, false, true, false);
        cells[6].setDirBoundaries(true, false, true, false);
        cells[7].setDirBoundaries(true, false, false, false);
        cells[8].setDirBoundaries(false, true, true, true);
        cells[9].setDirBoundaries(true, false, true, false);
        cells[10].setDirBoundaries(true, false, true, false);
        cells[11].setDirBoundaries(true, false, true, false);
        cells[12].setDirBoundaries(false, false, true, false);
        cells[13].setDirBoundaries(true, true, false, true);
        cells[14].setDirBoundaries(true, false, true, false);
        cells[15].setDirBoundaries(true, false, true, false);
        cells[16].setDirBoundaries(false, false, false, true);
        cells[17].setDirBoundaries(true, true, false, true);
        cells[18].setDirBoundaries(false, true, true, true);
        cells[19].setDirBoundaries(false, true, false, true);

        cells[0].prob = 0;
        cells[1].prob = 987;
        cells[2].prob = 340;
        cells[3].prob = 340;
        cells[4].prob = 987;
        cells[5].prob = 340;
        cells[6].prob = 340;
        cells[7].prob = 340;
        cells[8].prob = 987;
        cells[9].prob = 340;
        cells[10].prob = 340;
        cells[11].prob = 340;
        cells[12].prob = 340;
        cells[13].prob = 987;
        cells[14].prob = 340;
        cells[15].prob = 340;
        cells[16].prob = 340;
        cells[17].prob = 987;
        cells[18].prob = 663;
        cells[19].prob = 150;

        return cells;
    }

    public int whichCell(Cell[] cells, float x, float y) {
        int cellIndex = 0;
        for (int i=0; i<cells.length; i++) {
            if ( (x>=cells[i].x1 && x<cells[i].x2) && (y>=cells[i].y1 && y<cells[i].y2) ) {
                cellIndex = i;
            }
        }
        return cellIndex;
    }

    public void drawMap(Bitmap bg, Canvas canvas, ImageView mapView) {
        Paint paint = new Paint();
        paint.setStrokeWidth(0);
        paint.setColor(Color.parseColor("#EDEDED"));
        canvas.drawRect(0, 0, 140, 600, paint);

        paint.setColor(Color.parseColor("#000000"));
//        Hall
        canvas.drawLine(60,12,60,588, paint);
        canvas.drawLine(60,588,76.8f,588, paint);
        canvas.drawLine(60,12,76.8f,12, paint);
        canvas.drawLine(76.8f,12,76.8f,588, paint);

//        C1
        canvas.drawLine(76.8f,108.8f,125.6f,108.8f, paint);
        canvas.drawLine(125.6f,108.8f,125.6f,140.8f, paint);
        canvas.drawLine(76.8f,140.8f,125.6f,140.8f, paint);

//        C8
        canvas.drawLine(76.8f,236.8f,125.6f,236.8f, paint);
        canvas.drawLine(125.6f,236.8f,125.6f,268.8f, paint);
        canvas.drawLine(76.8f,268.8f,125.6f,268.8f, paint);

//        C18
        canvas.drawLine(76.8f,460f,125.6f,460f, paint);
        canvas.drawLine(125.6f,460f,125.6f,492f, paint);
        canvas.drawLine(101.6f,492f,125.6f,492f, paint);
        canvas.drawLine(101.6f,492f,101.6f,468f, paint);
        canvas.drawLine(76.8f,468f,101.6f,468f, paint);

//        C4
        canvas.drawLine(60,140.8f,11.2f,140.8f, paint);
        canvas.drawLine(11.2f,140.8f,11.2f,172.8f, paint);
        canvas.drawLine(11.2f,172.8f,60,172.8f, paint);

//        C13
        canvas.drawLine(60,364,11.2f,364, paint);
        canvas.drawLine(11.2f,364,11.2f,396, paint);
        canvas.drawLine(11.2f,396,60,396, paint);

//        C17
        canvas.drawLine(60,460,11.2f,460, paint);
        canvas.drawLine(11.2f,460,11.2f,492, paint);
        canvas.drawLine(11.2f,492,60,492, paint);

//        Hall Cells
        paint.setColor(Color.parseColor("#A3A3A3"));
        canvas.drawLine(61,108.8f,76.8f,108.8f,paint);
        canvas.drawLine(61,140.8f,76.8f,140.8f,paint);
        canvas.drawLine(61,172.8f,76.8f,172.8f,paint);
        canvas.drawLine(61,204.8f,76.8f,204.8f,paint);
        canvas.drawLine(61,236.8f,76.8f,236.8f,paint);
        canvas.drawLine(61,268.8f,76.8f,268.8f,paint);
        canvas.drawLine(61,300.8f,76.8f,300.8f,paint);
        canvas.drawLine(61,332.8f,76.8f,332.8f,paint);
        canvas.drawLine(61,364.8f,76.8f,364.8f,paint);
        canvas.drawLine(61,396.8f,76.8f,396.8f,paint);
        canvas.drawLine(61,428.8f,76.8f,428.8f,paint);
        canvas.drawLine(61,460.8f,76.8f,460.8f,paint);
        canvas.drawLine(61,492.8f,76.8f,492.8f,paint);

//        Hall/Cell doors
        canvas.drawLine(76.8f,108.8f,76.8f,140.8f, paint);
        canvas.drawLine(60,140.8f,61,172.8f, paint);
        canvas.drawLine(76.8f,236.8f,76.8f,268.8f, paint);
        canvas.drawLine(60f,364.8f,61f,396.8f, paint);
        canvas.drawLine(76.8f,460f,76.8f,468f, paint);
        canvas.drawLine(60f,460.8f,61f,492.8f, paint);

        mapView.setBackgroundDrawable(new BitmapDrawable(bg));
    }
}
