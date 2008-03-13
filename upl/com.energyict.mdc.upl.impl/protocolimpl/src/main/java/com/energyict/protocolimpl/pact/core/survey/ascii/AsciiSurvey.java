/*
 * AsciiSurvey.java
 *
 * Created on 11 maart 2004, 13:59
 */

package com.energyict.protocolimpl.pact.core.survey.ascii;

import java.io.*;
import java.util.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.pact.core.survey.*;
import com.energyict.protocolimpl.pact.core.meterreading.*;
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
