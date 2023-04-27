package com.raspbounty.knolleili;

import android.content.Context;
import android.content.res.Resources;

import java.util.HashMap;

class Chest {

    String content, roomShort, roomLong, shelfShort, shelfLong, coordsAsString;
    int [] coords;


    public Chest(Context ctx, String mContent, String mRoomShort, String mRackShort, int mX, int mY) {
        content = mContent;
        roomShort = mRoomShort;
        shelfShort = mRackShort;
        coords = new int[]{mX, mY};
        switch (mRoomShort){
            case "M": roomLong = ctx.getResources().getString(R.string.all_m);
                break;
            case "K": roomLong = ctx.getResources().getString(R.string.all_k);
                break;
            case "W": roomLong = ctx.getResources().getString(R.string.all_w);
                break;
            case "D": roomLong = ctx.getResources().getString(R.string.all_d);
                break;
            case "P": roomLong = ctx.getResources().getString(R.string.all_p);
                break;
            default:
                roomLong = ctx.getResources().getString(R.string.all_error);
        }

        switch (mRackShort){
            case "R1": shelfLong = ctx.getResources().getString(R.string.all_r1);
                break;
            case "S1": shelfLong = ctx.getResources().getString(R.string.all_s1);
                break;
            case "S2": shelfLong = ctx.getResources().getString(R.string.all_s2);
                break;
            case "S3": shelfLong = ctx.getResources().getString(R.string.all_s3);
                break;
            case "W1": shelfLong = ctx.getResources().getString(R.string.all_w1);
                break;
            case "W2": shelfLong = ctx.getResources().getString(R.string.all_w2);
                break;
            case "T1": shelfLong = ctx.getResources().getString(R.string.all_t1);
                break;
            default:
                roomLong = ctx.getResources().getString(R.string.all_error);
        }
        coordsAsString = mX + ", " + mY;
    }

    public Chest(String mContent, String mRoomShort, String mRackShort, int mX, int mY, String mRoomLong, String mRackLong, String mCoordsAsString) {
        content = mContent;
        roomShort = mRoomShort;
        shelfShort = mRackShort;
        coords = new int[]{mX, mY};
        roomLong = mRoomLong;
        shelfLong = mRackLong;
        coordsAsString = mCoordsAsString;
    }



    public String locationToString(){
        return this.roomShort + "" + this.shelfShort;
    }

    public String getImageName(){
        return this.roomShort.toLowerCase() + "_" + this.shelfShort.toLowerCase();
    }
}
