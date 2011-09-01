package br.org.indt.ndg.lwuit.model;

import java.util.Vector;

/**
 *
 * @author mluz
 */
public class Survey implements DisplayableItem{ //TODO can be merged with XformSurvey

    private String mTitle;
    private int mId;

    public String getDisplayableName() {
        return mTitle;
    }

    public void setIdNumber(int aId) {
        mId = aId;
    }

    public void setTitle(String aTitle) {
        mTitle = aTitle;
    }

    public int getId() {
        return mId;
    }
}
