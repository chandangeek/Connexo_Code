package com.elster.genericprotocolimpl.dlms.ek280.debug;

import com.elster.protocolimpl.dlms.Dlms;
import com.energyict.cbo.BusinessException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights
 * Date: 14/06/11
 * Time: 14:20
 */
public class DummyDlms extends Dlms {

    @Override
    public void connect() throws IOException {

    }

    @Override
    public void disconnect() throws IOException {

    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timezone, Logger logger) throws IOException {

    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return MessageResult.createFailed(messageEntry);
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return new RegisterValue(obisCode, obisCode.toString());
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    @Override
    public void updateCache(int arg0, Object arg1) throws SQLException, BusinessException {

    }

    @Override
    public void setTime() throws IOException {

    }

    @Override
    public Date getTime() throws IOException {
        return new Date(System.currentTimeMillis() - (new Random().nextInt() / 10000));
    }

    @Override
    public String getProtocolVersion() {
        return "$Date$";
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return new ProfileData();
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "v1.0.0-dummy";
    }
}
