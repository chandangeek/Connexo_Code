package com.energyict.protocolimplv2.umi.ei4.events;

import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.util.ArrayList;
import java.util.List;

public class EI4UmiEventTable extends LittleEndianData {
    private List<EI4UmiwanEvent> events;

    /**
     * Constructor for testing purposes.
     */
    public EI4UmiEventTable(List<EI4UmiwanEvent> events) {
        super(events.size() * EI4UmiwanEvent.SIZE);
        this.events = events;
        for (EI4UmiwanEvent event : events) {
            getRawBuffer().put(event.getRaw());
        }
    }

    public EI4UmiEventTable(int size) {
        super(size);
        this.events = new ArrayList<>();
    }

    public EI4UmiEventTable(byte[] raw) {
        super(raw, EI4UmiwanEvent.SIZE, true);
        events = new ArrayList<>();
        byte[] rawEvent = new byte[EI4UmiwanEvent.SIZE];

        while (getRawBuffer().position() < getRawBuffer().capacity()) {
            getRawBuffer().get(rawEvent);
            events.add(new EI4UmiwanEvent(rawEvent));
        }
    }

    public void addEvent(EI4UmiwanEvent event) {
        this.events.add(event);
    }

    public List<EI4UmiwanEvent> getEvents() {
        return events;
    }

    public int getSize() {
        return events.size();
    }
}
