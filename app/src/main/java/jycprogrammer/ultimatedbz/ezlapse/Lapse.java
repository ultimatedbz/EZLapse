package jycprogrammer.ultimatedbz.ezlapse;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by ahesselgrave on 1/31/15.
 */
public class Lapse {
    private UUID mId;
    private String mTitle;
    private ArrayList<Photo> mPhoto;

    public Lapse(String title) {
        mId = UUID.randomUUID();
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public UUID getId(){
        return mId;
    }

    public int getSize(){
        return mPhoto.size();
    }

    public void setTitle(String title){
        mTitle = title;
    }
}