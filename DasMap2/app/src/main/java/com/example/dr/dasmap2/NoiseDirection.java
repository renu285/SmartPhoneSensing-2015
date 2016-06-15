package com.example.dr.dasmap2;

/**
 * Created by dr on 12-6-16.
 */
public class NoiseDirection {
    int[] probDistribution = {1, 2, 1, 7, 5, 3, 4, 6, 1, 1, 8, 10, 9, 5, 12, 15, 26, 12,
            31, 36, 51, 42, 57, 128, 129, 183, 170, 160, 118, 97,
            104, 83, 137, 116, 129, 178, 211, 192, 201, 212, 216,
            202, 214, 221, 222, 284, 308, 302, 337, 406, 421, 426,
            387, 374, 314, 233, 203, 168, 128, 130, 87, 97, 77, 79,
            31, 25, 54, 27, 33, 31, 20, 15, 5, 7, 1, 4, 5, 5, 2, 1, 1, 1, 3};



    int sum = 0;
    int rSum = 0;


    void init() {
        for(int i=0; i<probDistribution.length; i++) {
            sum += probDistribution[i];
        }
    }

    int getValue(int r) {
        int noiseValue = 0;
        for (int i=0; i<probDistribution.length-1; i++){
            if (r < rSum + probDistribution[i+1]) {
                noiseValue = -50 + i;
                i = probDistribution.length;
            }
        }

        return noiseValue;
    }

}
