package com.elster.jupiter.metering.cim.impl;

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
import java.util.ArrayList;
import java.util.Date;

public class GeneratedClassesTest {

    @Test
    public void testGenerateMeterReadingMessage() throws JAXBException {
        ByteArrayOutputStream root = new ByteArrayOutputStream();
        marshal(root);
        System.out.println(new String(root.toByteArray()));
    }

    public void marshal(OutputStream out) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance("com.elster.jupiter.metering.cim");
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
        payload.any = new ArrayList<>();
        payload.any.add(createElement());
        return payload;
    }

    private Element createElement() {
        try {
            MeterReadings meterReadings = new ObjectFactory().createMeterReadings();
            meterReadings.meterReading = new ArrayList<>();
            meterReadings.meterReading.add(createMeterReading());
            JAXBContext jc = JAXBContext.newInstance("com.elster.jupiter.metering.cim");
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
        MeterReading meterReading = new MeterReading();
        Meter value = new Meter();
        value.setMRID("MRID");
        meterReading.setMeter(value);
        meterReading.readings = new ArrayList<>();
        meterReading.readings.add(createReading());
        return meterReading;
    }

    private Reading createReading() {
        Reading reading = new Reading();
        Reading.ReadingType readingType = new Reading.ReadingType();
        readingType.setRef("1.2.3");
        reading.setReadingType(readingType);
        reading.setReportedDateTime(new Date());
        reading.setValue("3.141592");
        return reading;
    }

    private HeaderType createHeader() {
        HeaderType header = new HeaderType();
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
