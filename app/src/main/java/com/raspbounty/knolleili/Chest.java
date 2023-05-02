package com.raspbounty.knolleili;

import android.content.Context;
import android.util.Pair;

import java.util.Arrays;

class Chest {
    String content, coordsAsString;
    ShortLong rack, room;
    int [] coords;
    final private String [] roomsShort, racksShort, roomsLong, racksLong;

    int visibility;

    public Chest(Context ctx, String mContent, String mRoom, String mRack, int mX, int mY, int mVisibility) {
        racksShort = new String[]{"R1", "S1", "S2", "S3", "W1", "W2", "T1"};
        roomsShort = new String[]{"M", "K", "W", "D", "P"};
        racksLong = new String[]{ctx.getResources().getString(R.string.all_r1), ctx.getResources().getString(R.string.all_s1),
                ctx.getResources().getString(R.string.all_s2), ctx.getResources().getString(R.string.all_s3),
                ctx.getResources().getString(R.string.all_w1), ctx.getResources().getString(R.string.all_w2), ctx.getResources().getString(R.string.all_t1)};
        roomsLong = new String[]{ctx.getResources().getString(R.string.all_m), ctx.getResources().getString(R.string.all_k),
                ctx.getResources().getString(R.string.all_k), ctx.getResources().getString(R.string.all_d), ctx.getResources().getString(R.string.all_p)};

        content = mContent;
        room = getRoomPair(mRoom);;
        rack = getRackPair(mRack);
        coords = new int[]{mX, mY};
        coordsAsString = mX + ", " + mY;

        visibility = mVisibility;
    }

    private ShortLong getRackPair(String mRack){
        String rackShort, rackLong;
        int indexInShort = Arrays.asList(racksShort).indexOf(mRack);
        int indexInLong = Arrays.asList(racksLong).indexOf(mRack);

        if(indexInShort != -1){
            rackShort = mRack;
            rackLong = racksLong[indexInShort];
        } else if(indexInLong != -1){
            rackShort = racksShort[indexInLong];
            rackLong = mRack;
        }else{
            rackLong = "error";
            rackShort = "error";
        }
        return new ShortLong(rackShort, rackLong);

    }

    private ShortLong getRoomPair(String mRoom){
        String roomShort, roomLong;
        int indexInShort = Arrays.asList(roomsShort).indexOf(mRoom);
        int indexInLong = Arrays.asList(roomsLong).indexOf(mRoom);

        if(indexInShort != -1){
            roomShort = mRoom;
            roomLong = roomsLong[indexInShort];
        } else if(indexInLong != -1){
            roomShort = roomsShort[indexInLong];
            roomLong = mRoom;
        }else{
            roomLong = "error";
            roomShort = "error";
        }
        return new ShortLong(roomShort, roomLong);
    }

    public String locationToString(){
        return this.room.shrt + "" + this.rack.shrt;
    }

    public String getImageName(){
        return this.room.shrt.toLowerCase() + "_" + this.rack.shrt.toLowerCase();
    }
}