/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ConfigInfoRead.java
 *
 * Created on 14 september 2005, 13:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.iec1107.enermete70x;

import com.energyict.protocolimpl.base.DataParser;

import java.io.IOException;
import java.util.Date;
/**
 *
 * @author Koen
 */
public class ConfigInfoRead extends AbstractDataReadingCommand {

    Date date=null;
    int programNumber=0;

    /** Creates a new instance of ConfigInfoRead */
    public ConfigInfoRead(DataReadingCommandFactory drcf) {
        super(drcf);
    }

    public String toString() {
        return getDate()+", "+getProgramNumber();
    }

    public void parse(byte[] data, java.util.TimeZone timeZone) throws java.io.IOException {
        DataParser dp = new DataParser(timeZone);
        programNumber = Integer.parseInt(dp.parseBetweenBrackets(data,0,0));
        date = dp.parseDateTime(dp.parseBetweenBrackets(data,0,1));
    }

    public Date getDate() {
        return date;
    }

    public int getProgramNumber() {
        return programNumber;
    }

    public void retrieveConfigInfoRead() throws IOException {
        retrieve("CIR");
    }
}
