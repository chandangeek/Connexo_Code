package com.energyict.genericprotocolimpl.lgadvantis;

import java.util.ArrayList;
import java.util.List;

import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;

public class ReadResult {

    private List registerValues = new ArrayList();
    private ProfileData profileData;
    
    public List getRegisterValues() {
        return registerValues;
    }
    
    public void addRegisterValue(RegisterValue value ) {
        this.registerValues.add(value);
    }
    
    public void addAllRegisterValues( List list ) {
        this.registerValues.addAll( list );
    }
    
    public ProfileData getProfileData() {
        return profileData;
    }
    
    public void setProfileData(ProfileData profileData) {
        this.profileData = profileData;
    }
    
}
