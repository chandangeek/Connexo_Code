/*
 * LinkSurvey.java
 *
 * Created on 11 maart 2004, 14:00
 */

package com.energyict.protocolimpl.pact.core.survey.link;

import com.energyict.protocolimpl.pact.core.meterreading.MeterReadingsInterpreter;
import com.energyict.protocolimpl.pact.core.survey.LoadSurveyInterpreterImpl;

import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class LinkSurvey extends LoadSurveyInterpreterImpl {

    /** Creates a new instance of MeterReadingBlocks */
    public LinkSurvey(MeterReadingsInterpreter mri,TimeZone timeZone) {
       super(mri,timeZone);
    }

    protected void parseData(byte[] data) {
    }
    public int[] doGetEnergyTypeCodes() {
        return null;
    }

    protected int doGetNrOfDays(Date from, Date to) {
        return -1;
    }
    protected int doGetNrOfBlocks(Date from, Date to) {
        return -1;
    }

}
