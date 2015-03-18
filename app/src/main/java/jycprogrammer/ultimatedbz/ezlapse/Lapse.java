package jycprogrammer.ultimatedbz.ezlapse;

import java.io.File;
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

    public Lapse(String title){
        mId = UUID.randomUUID();
        mTitle = title;
        mPhotos = new ArrayList<Photo>();
    }

    public String getTitle() {
        return mTitle;
    }

    public UUID getId(){
        return mId;
    }

    public ArrayList<Photo> getPhotos(){
        return mPhotos;
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

    public void add(Photo photo){
        mPhotos.add(photo);
    }
    public void deleteLapse(){
        File f = new File(mPhotos.get(0).getFilePath());
        File parentFile = new File(f.getParentFile().getAbsolutePath());
        deleteDirectory(parentFile);
    }

    public void deletePhoto(Photo i) {
        if(mPhotos.size() == 1)
            deleteLapse();
        File f = new File(i.getFilePath());
        f.delete();
        mPhotos.remove(i);
    }

    public static boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return( path.delete() );
    }
}