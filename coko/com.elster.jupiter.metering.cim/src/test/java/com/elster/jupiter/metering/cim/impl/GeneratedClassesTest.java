package com.elster.jupiter.metering.cim.impl;

import ch.iec.tc57._2011.meterreadings.Meter;
import ch.iec.tc57._2011.meterreadings.MeterReading;
import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.meterreadings.Reading;
import ch.iec.tc57._2011.schema.message.CreatedMeterReadings;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ObjectFactory;
import ch.iec.tc57._2011.schema.message.PayloadType;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMResult;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Date;

public class GeneratedClassesTest {

    private ch.iec.tc57._2011.meterreadings.ObjectFactory payloadObjectFactory = new ch.iec.tc57._2011.meterreadings.ObjectFactory();

    @Test
    public void testGenerateMeterReadingMessage() throws JAXBException {
        ByteArrayOutputStream root = new ByteArrayOutputStream();
        marshal(root);
        System.out.println(new String(root.toByteArray()));
    }

    public void marshal(OutputStream out) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance("ch.iec.tc57._2011.schema.message");
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        m.marshal(this.rootElement(), out);
    }

    private CreatedMeterReadings rootElement() {
        CreatedMeterReadings createdMeterReadings = new ObjectFactory().createCreatedMeterReadings();
        createdMeterReadings.setHeader(createHeader());
        createdMeterReadings.setPayload(createPayLoad());
        return createdMeterReadings;
    }

    private PayloadType createPayLoad() {
        PayloadType payload = new ObjectFactory().createPayloadType();
        payload.getAny().add(createElement());
        return payload;
    }

    private Element createElement() {
        try {
            MeterReadings meterReadings = payloadObjectFactory.createMeterReadings();
            meterReadings.getMeterReading().add(createMeterReading());
            JAXBContext jc = JAXBContext.newInstance("ch.iec.tc57._2011.meterreadings");
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            DOMResult result = new DOMResult();
            //m.marshal(meterReadings, result);
            m.marshal(new JAXBElement<>(new QName("http://iec.ch/TC57/2011/MeterReadings#","MeterReadings"), MeterReadings.class, meterReadings ), result);
            return (Element) ((Document) result.getNode()).getDocumentElement();

        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }


    }

    private MeterReading createMeterReading() {
        MeterReading meterReading = payloadObjectFactory.createMeterReading();
        Meter value = new Meter();
        value.setMRID("MRID");
        meterReading.setMeter(value);
        meterReading.getReadings().add(createReading());
        return meterReading;
    }

    private Reading createReading() {
        Reading reading = new ch.iec.tc57._2011.meterreadings.ObjectFactory().createReading();
        Reading.ReadingType readingType = new Reading.ReadingType();
        readingType.setRef("1.2.3");
        reading.setReadingType(readingType);
        reading.setReportedDateTime(new Date());
        reading.setValue("3.141592");
        return reading;
    }

    private HeaderType createHeader() {
        HeaderType header = new ObjectFactory().createHeaderType();
        header.setVerb("created");
        header.setNoun("MeterReadings");
        header.setRevision("");
        header.setContext("Testing");
        header.setTimestamp(new Date());
        header.setSource("Jupiter");
        header.setAsyncReplyFlag(false);
        header.setAckRequired(false);
        header.setMessageID("TTT-001");
        header.setCorrelationID("");
        header.setComment("");

        return header;
    }

}
