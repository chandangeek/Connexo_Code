package com.energyict.genericprotocolimpl.ace4000.objects;

import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterEvent;
import org.apache.axis.encoding.Base64;
import org.w3c.dom.Element;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 30/08/11
 * Time: 17:38
 */
public class PowerFailLog extends AbstractActarisObject {

    public PowerFailLog(ObjectFactory of) {
        super(of);
    }

    @Override
    protected void parse(Element mdElement) {

        byte[] decoded = Base64.decode(mdElement.getTextContent());

        int offset = 0;
        while ((offset + 8) <= decoded.length) {

            Date date = getObjectFactory().convertMeterDateToSystemDate(getNumberFromB64(decoded, offset, 4));
            offset += 4;
            int duration = getNumberFromB64(decoded, offset, 4);
            offset += 4;

            getObjectFactory().getMeterEvents().add(new MeterEvent(date, MeterEvent.POWERDOWN, "Duration: " + duration + " seconds"));
        }
    }

    @Override
    protected String prepareXML() throws InvalidPropertyException {
        return "";  //We don't request this log
    }
}