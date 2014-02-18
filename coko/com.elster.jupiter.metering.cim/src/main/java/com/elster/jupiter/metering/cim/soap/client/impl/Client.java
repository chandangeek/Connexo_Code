package com.elster.jupiter.metering.cim.soap.client.impl;

import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.PayloadType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.sendmeterreadings.FaultMessage;
import ch.iec.tc57._2011.sendmeterreadings.MeterReadingsPort;
import ch.iec.tc57._2011.sendmeterreadings.SendMeterReadings;
import org.apache.cxf.jaxws.spi.ProviderImpl;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import java.net.MalformedURLException;
import java.net.URL;

public class Client {

    public void execute() {
        // trick javax.xml.ws to find cxf provider
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(ProviderImpl.class.getClassLoader());

            QName SERVICE_NAME =
                    new QName("http://iec.ch/TC57/2011/SendMeterReadings", "SendMeterReadings");
            SendMeterReadings sendMeterReadings = new SendMeterReadings(new URL("http://localhost:8095/?wsdl"), SERVICE_NAME);
            MeterReadingsPort meterReadingsPort = sendMeterReadings.getMeterReadingsPort();
            try {
                Holder<HeaderType> headerTypeHolder = new Holder<>(new HeaderType());
                meterReadingsPort.createdMeterReadings(headerTypeHolder,
                        null, new Holder<>(new ReplyType()), new Holder<>(new PayloadType()));
                System.out.println(headerTypeHolder.value.getNoun());
            } catch (FaultMessage faultMessage) {
                faultMessage.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

}
