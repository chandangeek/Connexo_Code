package com.elster.jupiter.metering.cim.soap.client.impl;

import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.schema.message.CreatedMeterReadings;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.sendmeterreadings.FaultMessage;
import ch.iec.tc57._2011.sendmeterreadings.MeterReadingsPort;
import ch.iec.tc57._2011.sendmeterreadings.SendMeterReadings;
import com.elster.jupiter.metering.cim.CimMessageHandlerFactory;
import com.elster.jupiter.metering.cim.Sender;
import com.elster.jupiter.metering.cim.UnderlyingXmlException;
import com.elster.jupiter.metering.cim.impl.Marshaller;
import com.elster.jupiter.soap.whiteboard.SoapProviderSupportFactory;
import com.elster.jupiter.util.osgi.ContextClassLoaderResource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import java.net.MalformedURLException;
import java.net.URL;

@Component(name = "com.elster.jupiter.metering.cim.soap.client", service = Sender.class, immediate = true)
public class SoapSender implements Sender {
    private final Marshaller marshaller = new Marshaller();
    private volatile CimMessageHandlerFactory factory;
    private volatile SoapProviderSupportFactory soapProviderSupportFactory;

    @Activate
    public void activate() {
    }

    @Deactivate
    public void deactivate() {
        factory.removeSender(this);
    }

    @Reference
    public void setFactory(CimMessageHandlerFactory factory) {
        this.factory = factory;
        factory.addSender(this);
    }

    @Reference
    public void setSoapProviderSupportFactory(SoapProviderSupportFactory soapProviderSupportFactory) {
        this.soapProviderSupportFactory = soapProviderSupportFactory;
    }

    @Override
    public void send(CreatedMeterReadings createdMeterReadings, MeterReadings meterReadings) {
        try (ContextClassLoaderResource support = soapProviderSupportFactory.create()) {
            marshaller.addPayload(createdMeterReadings, meterReadings);

            QName SERVICE_NAME =
                    new QName("http://iec.ch/TC57/2011/SendMeterReadings", "SendMeterReadings");
            SendMeterReadings sendMeterReadings = new SendMeterReadings(new URL("http://localhost:8095/?wsdl"), SERVICE_NAME);
            MeterReadingsPort meterReadingsPort = sendMeterReadings.getMeterReadingsPort();
            try {
                meterReadingsPort.createdMeterReadings(new Holder<>(createdMeterReadings.getHeader()),
                        null, new Holder<>(new ReplyType()), new Holder<>(createdMeterReadings.getPayload()));
            } catch (FaultMessage faultMessage) {
                faultMessage.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            throw new UnderlyingXmlException(e);
        }
    }
}
