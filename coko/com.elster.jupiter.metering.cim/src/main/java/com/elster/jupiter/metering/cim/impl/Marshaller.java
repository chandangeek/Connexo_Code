package com.elster.jupiter.metering.cim.impl;

import ch.iec.tc57._2011.meterreadings_.MeterReadings;
import ch.iec.tc57._2011.schema.message.CreatedMeterReadings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMResult;
import java.io.OutputStream;

public class Marshaller {

    private javax.xml.bind.Marshaller marshaller = getMarshaller();

    void marshal(CreatedMeterReadings createdMeterReadings, OutputStream out) {
        try {
            marshaller.marshal(createdMeterReadings, out);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }

    }

    void addPayload(CreatedMeterReadings createdMeterReadings, MeterReadings meterReadings) throws JAXBException {
        DOMResult result = new DOMResult();
        getMarshaller().marshal(new JAXBElement<>(new QName("http://iec.ch/TC57/2011/MeterReadings#", "MeterReadings"), MeterReadings.class, meterReadings ), result);
        Element payloadElement = ((Document) result.getNode()).getDocumentElement();
        createdMeterReadings.getPayload().getAny().add(payloadElement);
    }


    private javax.xml.bind.Marshaller getMarshaller() {
        try {
            JAXBContext jc = JAXBContext.newInstance("com.elster.jupiter.metering.cim.impl", Marshaller.class.getClassLoader());
            javax.xml.bind.Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
            return marshaller;
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
