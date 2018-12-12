/*
 * BinarySurvey.java
 *
 * Created on 11 maart 2004, 13:58
 */

package com.energyict.protocolimpl.pact.core.survey.binary;

import java.io.*;
import java.util.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.pact.core.survey.*;
import com.energyict.protocolimpl.pact.core.meterreading.*;

/**
 *
 * @author  Koen
 */
public class BinarySurvey extends LoadSurveyInterpreterImpl {
    
    /** Creates a new instance of MeterReadingBlocks */
    public BinarySurvey(MeterReadingsInterpreter mri,TimeZone timeZone) {
       super(mri,timeZone);
    }
    

    public void parseData(byte[] data) throws IOException {
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
