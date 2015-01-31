package jycprogrammer.ultimatedbz.ezlapse;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by ahesselgrave on 1/31/15.
 */
public class Lapse {
    private UUID mId;
    private String mTitle;
    private ArrayList<Photo> mPhotos;

    public Lapse(String title, Date date, String firstFilePath) {
        mId = UUID.randomUUID();
        mTitle = title;
        Photo temp = new Photo(firstFilePath, date);
        mPhotos = new ArrayList<Photo>();
        mPhotos.add(temp);
    }

    public String getTitle() {
        return mTitle;
    }

    public UUID getId(){
        return mId;
    }

    public int getPhotoNum(){
        return mPhotos.size();
    }

    public void setTitle(String title){
        mTitle = title;
    }
    public String getLatest(){
        return mPhotos.get(mPhotos.size() - 1).getFilePath();
    }
}