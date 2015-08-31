package com.elster.jupiter.metering.cim.impl;

import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.schema.message.CreatedMeterReadings;
import com.elster.jupiter.metering.cim.Sender;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CompositeSender implements Sender {

    private final List<Sender> senders;

    public CompositeSender(List<? extends Sender> senders) {
        this.senders = new CopyOnWriteArrayList<>(senders);
    }

    public void addSender(Sender sender) {
        this.senders.add(sender);
    }

    public void removeSender(Sender sender) {
        this.senders.remove(sender);
    }

    @Override
    public void send(CreatedMeterReadings createdMeterReadings, MeterReadings meterReadings) {
        for (Sender sender : senders) {
            sender.send(createdMeterReadings, meterReadings);
        }
    }
}
