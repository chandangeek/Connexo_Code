package test.com.energyict.protocolimplV2.ace4000.objects;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import org.w3c.dom.Element;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 30/08/11
 * Time: 17:38
 */
public class PowerFailLog extends AbstractActarisObject {

    private MeterEvent meterEvent;

    public PowerFailLog(ObjectFactory of) {
        super(of);
    }

    public MeterEvent getMeterEvent() {
        return meterEvent;
    }

    @Override
    protected void parse(Element mdElement) {

        byte[] decoded = new Base64EncoderDecoder().decode(mdElement.getTextContent());

        int offset = 0;
        while ((offset + 8) <= decoded.length) {

            Date date = getObjectFactory().convertMeterDateToSystemDate(getNumberFromB64(decoded, offset, 4));
            offset += 4;
            int duration = getNumberFromB64(decoded, offset, 4);
            offset += 4;

            meterEvent = new MeterEvent(date, MeterEvent.POWERDOWN, "Duration: " + duration + " seconds");
        }
    }

    @Override
    protected String prepareXML() {
        return "";  //We don't request this log
    }
}