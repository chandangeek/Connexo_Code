package com.elster.jupiter.metering.cim.impl;

import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.schema.message.CreatedMeterReadings;
import com.elster.jupiter.metering.cim.OutputStreamClosure;
import com.elster.jupiter.metering.cim.OutputStreamProvider;
import com.elster.jupiter.metering.cim.Sender;
import com.elster.jupiter.metering.cim.UnderlyingXmlException;

import javax.xml.bind.JAXBException;
import java.io.OutputStream;

public class SenderImpl implements Sender {

    private final Marshaller marshaller;
    private final OutputStreamProvider outputStreamProvider;

    public SenderImpl(Marshaller marshaller, OutputStreamProvider outputStreamProvider) {
        this.marshaller = marshaller;
        this.outputStreamProvider = outputStreamProvider;
    }

    @Override
    public void send(final CreatedMeterReadings createdMeterReadings, final MeterReadings meterReadings) {
        try {
            marshaller.addPayload(createdMeterReadings, meterReadings);
            outputStreamProvider.writeTo(new OutputStreamClosure() {
                @Override
                public void using(OutputStream out) {
                    marshaller.marshal(createdMeterReadings, out);
                }
            });
        } catch (JAXBException e) {
            throw new UnderlyingXmlException(e);
        }

    }
}
