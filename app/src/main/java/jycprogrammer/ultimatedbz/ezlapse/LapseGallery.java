package jycprogrammer.ultimatedbz.ezlapse;

import android.content.Context;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by ahesselgrave on 1/31/15.
 */
public class LapseGallery {
    private static LapseGallery sLapseGallery;

    private ArrayList<Lapse> mLapses;
    private Context mAppContext;

    public void add(Lapse l){
        mLapses.add(l);
    }

    public void delete(Lapse l){
        mLapses.remove(l);
    }

    public ArrayList<Lapse> getLapses(){
        return mLapses;
    }

    public Lapse getLapse(UUID id) {
        for(Lapse l : mLapses) {
            if(l.getId().equals(id))
                return l;
        }
        return null;
    }

    private LapseGallery(Context appContext){
        mAppContext = appContext;
        mLapses = new ArrayList<Lapse>();
    }

    public static LapseGallery get(Context c) {
        if (sLapseGallery == null) {
            sLapseGallery = new LapseGallery(c.getApplicationContext());
        }
        return sLapseGallery;
    }

}