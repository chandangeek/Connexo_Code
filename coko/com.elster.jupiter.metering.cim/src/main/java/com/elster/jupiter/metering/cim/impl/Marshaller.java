package com.elster.jupiter.metering.cim.impl;

import ch.iec.tc57._2011.meterreadings.MeterReadings;
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

    private javax.xml.bind.Marshaller payloadMarshaller = getMarshallerForContext("ch.iec.tc57._2011.meterreadings");
    private javax.xml.bind.Marshaller messageMarshaller = getMarshallerForContext("ch.iec.tc57._2011.schema.message");

    public void marshal(CreatedMeterReadings createdMeterReadings, OutputStream out) {
        try {
            messageMarshaller.marshal(createdMeterReadings, out);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }

    }

    public void addPayload(CreatedMeterReadings createdMeterReadings, MeterReadings meterReadings) throws JAXBException {
        DOMResult result = new DOMResult();
        payloadMarshaller.marshal(new JAXBElement<>(new QName("http://iec.ch/TC57/2011/MeterReadings#", "MeterReadings"), MeterReadings.class, meterReadings), result);
        Element payloadElement = ((Document) result.getNode()).getDocumentElement();
        createdMeterReadings.getPayload().getAny().add(payloadElement);
    }


    private javax.xml.bind.Marshaller getMarshallerForContext(String contextPath) {
        try {
            JAXBContext jc = JAXBContext.newInstance(contextPath, Marshaller.class.getClassLoader());
            javax.xml.bind.Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
            return marshaller;
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

}
