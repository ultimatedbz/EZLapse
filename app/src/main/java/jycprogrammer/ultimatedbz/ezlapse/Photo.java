package jycprogrammer.ultimatedbz.ezlapse;

import java.util.Date;
import java.util.UUID;

/**
 * Created by ahesselgrave on 1/31/15.
 */
public class Photo {
    private String mFilePath;
    private UUID mId;
    private Date mDate;

    public Photo( String filePath, Date date){
        setId(UUID.randomUUID());
        setFilePath(filePath);
        setDate(date);
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }


}
