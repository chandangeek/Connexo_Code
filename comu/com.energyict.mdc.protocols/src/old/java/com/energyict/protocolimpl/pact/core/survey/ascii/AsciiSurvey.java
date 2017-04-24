/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AsciiSurvey.java
 *
 * Created on 11 maart 2004, 13:59
 */

package com.energyict.protocolimpl.pact.core.survey.ascii;

import com.energyict.protocolimpl.pact.core.meterreading.MeterReadingsInterpreter;
import com.energyict.protocolimpl.pact.core.survey.LoadSurveyInterpreterImpl;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class AsciiSurvey extends LoadSurveyInterpreterImpl {

    /** Creates a new instance of MeterReadingBlocks */
    public AsciiSurvey(MeterReadingsInterpreter mri,TimeZone timeZone) {
       super(mri,timeZone);
    }


    protected void parseData(byte[] data) throws IOException {
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
