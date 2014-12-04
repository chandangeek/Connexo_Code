package com.energyict.protocolimplv2.elster.ctr.MTU155.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Channel;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.DateAndTimeCategory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRAbstractValue;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Codice;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.EventNumber;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.TariffSchemeIdentifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 8-okt-2010
 * Time: 16:26:00
 */
public class TriggerEventsStructure extends Data<TriggerEventsStructure> {

    private static CTRObjectID[] billingRegisters = new CTRObjectID[]{new CTRObjectID("2.5.0"), new CTRObjectID("2.5.1"), new CTRObjectID("2.5.2"),
            new CTRObjectID("2.3.7"), new CTRObjectID("2.3.8"), new CTRObjectID("2.3.9")};

    private DateAndTimeCategory dateAndhourS;
    private EventNumber eventNumber;
    private Channel channel;
    private Codice codice;
    private TariffSchemeIdentifier tariffSchemeIdentifier;
    private List<AbstractCTRObject> billingValues;

    public TriggerEventsStructure(boolean longFrame) {
        super(longFrame);
    }

    @Override
    public byte[] getBytes() {
        //Parse the bytes of the billing values
        AttributeType type = new AttributeType(0x00);
        type.setHasIdentifier(false);
        type.setHasQualifier(true);
        type.setHasValueFields(true);
        byte[] valueBytes = new byte[0];
        for (AbstractCTRObject ctrObject : billingValues) {
            valueBytes = ProtocolTools.concatByteArrays(valueBytes, ctrObject.getBytes());
        }

        return padData(ProtocolTools.concatByteArrays(
                dateAndhourS.getBytes(),
                eventNumber.getBytes(),
                channel.getBytes(),
                codice.getBytes(),
                tariffSchemeIdentifier.getBytes(),
                valueBytes
        ));
    }

    /**
     * Create a CTR Structure Object representing the given byte array
     * @param rawData: a given byte array
     * @param offset: the start position in the array
     * @return the CTR Structure Object
     * @throws com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException
     */
    @Override
    public TriggerEventsStructure parse(byte[] rawData, int offset) throws CTRParsingException {
        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType(0x00);
        type.setHasValueFields(true);

        int ptr = offset;

        dateAndhourS = (DateAndTimeCategory) factory.parse(rawData, ptr, type, "8.0.1");
        ptr += dateAndhourS.getLength();

        eventNumber = new EventNumber().parse(rawData, ptr);
        ptr += eventNumber.getLength();

        channel = new Channel().parse(rawData, ptr);
        ptr += channel.getLength();

        codice = new Codice().parse(rawData, ptr);
        ptr += codice.getLength();

        tariffSchemeIdentifier = new TariffSchemeIdentifier().parse(rawData, ptr);
        ptr += tariffSchemeIdentifier.getLength();

        type.setHasIdentifier(false);
        type.setHasQualifier(true);
        type.setHasValueFields(true);
        billingValues = new ArrayList<AbstractCTRObject>();
        for (int i = 0; i < 6; i++) {
            AbstractCTRObject obj = factory.parse(rawData, ptr, type, billingRegisters[i].toString());
            billingValues.add(obj);
            ptr += obj.getLength();
        }

        return this;
    }

    private int sumLength(CTRAbstractValue<BigDecimal>[] dateAndhourS) {
        int sumLength = 0;
        for (CTRAbstractValue<BigDecimal> value : dateAndhourS) {
            sumLength += value.getValueLength();
        }
        return sumLength;
    }

    public static CTRObjectID[] getBillingRegisters() {
        return billingRegisters;
    }

    public static void setBillingRegisters(CTRObjectID[] billingRegisters) {
        TriggerEventsStructure.billingRegisters = billingRegisters;
    }

    public DateAndTimeCategory getDateAndhourS() {
        return dateAndhourS;
    }

    public void setDateAndhourS(DateAndTimeCategory dateAndhourS) {
        this.dateAndhourS = dateAndhourS;
    }

    public EventNumber getEventNumber() {
        return eventNumber;
    }

    public void setEventNumber(EventNumber eventNumber) {
        this.eventNumber = eventNumber;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Codice getCodice() {
        return codice;
    }

    public void setCodice(Codice codice) {
        this.codice = codice;
    }

    public TariffSchemeIdentifier getTariffSchemeIdentifier() {
        return tariffSchemeIdentifier;
    }

    public void setTariffSchemeIdentifier(TariffSchemeIdentifier tariffSchemeIdentifier) {
        this.tariffSchemeIdentifier = tariffSchemeIdentifier;
    }

    public List<AbstractCTRObject> getBillingValues() {
        return billingValues;
    }

    public void setBillingValues(List<AbstractCTRObject> billingValues) {
        this.billingValues = billingValues;
    }
}