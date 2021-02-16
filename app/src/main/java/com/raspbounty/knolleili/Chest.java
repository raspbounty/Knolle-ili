package com.raspbounty.knolleili;

import java.util.HashMap;

class Chest {

    String content, roomShort, roomLong, shelfShort, shelfLong;
    int [] coords;


    public Chest(String mContent, String mRoomShort, String mRackShort, String mX, String mY, String mRoomLong, String mRackLong) {
        content = mContent;
        roomShort = mRoomShort;
        shelfShort = mRackShort;
        coords = new int[]{Integer.parseInt(mX), Integer.parseInt(mY.split("\\.")[0])};
        roomLong = mRoomLong;
        shelfLong = mRackLong;
    }

    public String coordsToString(){
        return this.coords[0] + ", " + this.coords[1];
    }

    public String locationToString(){
        return this.roomShort + "" + this.shelfShort;
    }

    public String getImageName(){
        return this.roomShort.toLowerCase() + "_" + this.shelfShort.toLowerCase();
    }
}
