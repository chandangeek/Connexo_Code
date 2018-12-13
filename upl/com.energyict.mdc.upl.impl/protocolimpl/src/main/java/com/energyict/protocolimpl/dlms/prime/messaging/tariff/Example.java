package com.energyict.protocolimpl.dlms.prime.messaging.tariff;

import com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml.*;

import javax.xml.bind.*;
import java.io.FileNotFoundException;

/**
 * Copyrights EnergyICT
 * Date: 9/6/12
 * Time: 10:55 AM
 */
public class Example {

    public static void main(String[] args) throws JAXBException, FileNotFoundException {
        makeXml();
        makeS23Object();
    }

    private static S23 makeS23Object() throws JAXBException {
        JAXBContext contextObj = JAXBContext.newInstance(S23.class);
        Unmarshaller unmarshaller = contextObj.createUnmarshaller();

        return (S23) unmarshaller.unmarshal(Example.class.getResourceAsStream("example3.xml"));
    }

    private static void makeXml() throws JAXBException {
        JAXBContext contextObj = JAXBContext.newInstance(S23.class);

        Marshaller marshallerObj = contextObj.createMarshaller();
        marshallerObj.setProperty(Marshaller.JAXB_FRAGMENT, true);

        S23 s23 = new S23();
        s23.setFh("test");

        final ActiveCalendars activeCalendars = new ActiveCalendars();
        for (int i = 1; i <= 3; i++) {
            final Contract contract = new Contract();
            contract.setActDate("DUMMY_DATE");
            contract.setCalendarName("Calendar[" + i + "]");
            contract.setCalendarType(i);
            activeCalendars.getContract().add(contract);
        }

        s23.setActiveCalendars(activeCalendars);
        marshallerObj.marshal(s23, System.out);

        System.out.println();

    }

}
