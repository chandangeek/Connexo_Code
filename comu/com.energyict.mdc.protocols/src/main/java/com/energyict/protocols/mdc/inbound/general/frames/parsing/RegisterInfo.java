package com.energyict.protocols.mdc.inbound.general.frames.parsing;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.device.data.RegisterValue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 26/06/12
 * Time: 15:36
 * Author: khe
 */
public class RegisterInfo {

    private String info;
    private ObisCode obisCode;
    private Date readTime;

    public RegisterInfo(ObisCode obisCode, String info, Date readTime) {
        this.obisCode = obisCode;
        this.info = info;
        this.readTime = readTime;
    }

    public RegisterValue parse() {
        String[] registerInfos = info.split(" ");
        if (registerInfos.length >= 2) {
            int amount = Integer.parseInt(registerInfos[0]);
            Unit unit = Unit.get(registerInfos[1]);          //TODO test
            Date eventTimeStamp = getEventTimeStamp(registerInfos);
            return new RegisterValue(obisCode, new Quantity(amount, unit), eventTimeStamp, readTime);
        }
        return null;
    }

    private Date getEventTimeStamp(String[] registerInfos) {
        if (registerInfos.length > 2) {  //Third element is optional
            SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                return formatter.parse(registerInfos[2]);
            } catch (ParseException e) {
                return null;
            }
        }
        return null;
    }
}