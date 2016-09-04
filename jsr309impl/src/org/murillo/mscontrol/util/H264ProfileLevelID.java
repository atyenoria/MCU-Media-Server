/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.util;

/**
 *
 * @author Sergio
 */
public class H264ProfileLevelID {
    private Integer profile;
    private Integer constrains;
    private Integer level;

    public H264ProfileLevelID(String profileLevelId)
    {
        //Get each of the components
        profile     = Integer.parseInt(profileLevelId.substring(0,2), 16);
        constrains  = Integer.parseInt(profileLevelId.substring(2,4), 16);
        level       = Integer.parseInt(profileLevelId.substring(4,6), 16);
    }

    public Integer getConstrains() {
        return constrains;
    }

    public void setConstrains(Integer constrains) {
        this.constrains = constrains;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getProfile() {
        return profile;
    }

    public void setProfile(Integer profile) {
        this.profile = profile;
    }

    @Override
    public String toString()
    {
        //Convert to the hex string
        return String.format("%2x%2x%2x",profile,constrains,level);
    }
}
