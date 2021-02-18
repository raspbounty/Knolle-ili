package com.raspbounty.knolleili;

import java.util.HashMap;

class Chest {

    String content, roomShort, roomLong, shelfShort, shelfLong, coordsAsString;
    int [] coords;


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
