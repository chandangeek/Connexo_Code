package com.energyict.protocolimpl.elster.ctr;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.protocol.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 10-aug-2010
 * Time: 11:11:28
 */
public abstract class AbstractMTU155 implements MeterProtocol, RegisterProtocol {

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return getProfileData(null, includeEvents);
    }

    public ProfileData getProfileData(Date fromDate, boolean includeEvents) throws IOException {
        return getProfileData(fromDate, null, includeEvents);
    }

    public Quantity getMeterReading(int i) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(String s) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }

    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getProfileInterval() throws UnsupportedException, IOException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getRegister(String s) throws IOException, UnsupportedException, NoSuchRegisterException {
        throw new UnsupportedException();
    }

    public void setRegister(String s, String s1) throws IOException, NoSuchRegisterException, UnsupportedException {
        throw new UnsupportedException();
    }

    public void initializeDevice() throws IOException, UnsupportedException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setCache(Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getCache() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object fetchCache(int i) throws SQLException, BusinessException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateCache(int i, Object o) throws SQLException, BusinessException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void release() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
