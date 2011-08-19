package com.energyict.genericprotocolimpl.ace4000.objects;

import com.energyict.cbo.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.RegisterValue;
import org.apache.axis.encoding.Base64;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * @author khe
 */
public class MaxDemandRegister extends AbstractActarisObject {

    public MaxDemandRegister(ObjectFactory of) {
        super(of);
    }

    private int peakNumber = 1;
    private MeterReadingData mdr = new MeterReadingData();

    protected String prepareXML() {
        return "";             //No request exists for md registers
    }

    protected void parse(Element mdElement) throws IOException {
        if (peakNumber > 3) {
            getObjectFactory().log(Level.WARNING, "Cannot map more than 3 demand maxima to obis codes, received values aren't stored");
        } else {
            parseDemandRegisters(mdElement.getTextContent());
        }
        peakNumber++;
    }

    private void parseDemandRegisters(String data) throws IOException {
        int offset = 0;
        byte[] decoded = Base64.decode(data);
        List<Date> timeStamps = new ArrayList<Date>();
        List<Long> maxDemands = new ArrayList<Long>();

        for (int i = 0; i < 5; i++) {
            long maxDemand = (long) (getNumberFromB64(decoded, offset, 4));
            maxDemands.add(maxDemand);
            offset += 4;

            long timeStamp = (long) (getNumberFromB64(decoded, offset, 4));
            timeStamps.add(getObjectFactory().convertMeterDateToSystemDate(timeStamp));
            offset += 4;
        }

        int nrOfRates = decoded[offset] & 0xFF;
        offset++;

        int activeOrReactive = decoded[offset] & 0xFF;
        offset++;

        int rate = 0;

        for (int i = 0; i < 5; i++) {
            ObisCode obisCode = ObisCode.fromString("1.0." + getCField(activeOrReactive) + "." + getDField() + "." + String.valueOf(rate) + ".255");
            Unit unit = activeOrReactive == 0 ? Unit.get(BaseUnit.WATT) : Unit.get(BaseUnit.VOLTAMPEREREACTIVE);
            RegisterValue value = new RegisterValue(obisCode, new Quantity(maxDemands.get(i), unit), new Date(), timeStamps.get(i));
            if (value.getToTime().getTime() != 0) {
                mdr.add(value);
            }
            rate++;
        }
    }

    private String getCField(int activeOrReactive) {
        return String.valueOf(activeOrReactive == 0 ? 1 : 3);
    }

    private String getDField() {
        return String.valueOf(6 + ((peakNumber - 1) * 10));
    }

    public MeterReadingData getMdr() {
        return mdr;
    }
}