package com.energyict.protocolimplv2.dlms.common.obis.readers.logbook;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.AbstractObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.Matcher;
import com.energyict.protocolimplv2.dlms.common.obis.readers.logbook.mapper.EventMapper;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class GenericLogBookReader extends AbstractObisReader<CollectedLogBook, com.energyict.protocol.LogBookReader, ObisCode> implements LogBookReader<ObisCode> {

    private EventMapper mapper;
    private final CollectedLogBookBuilder collectedLogBookBuilder;

    public GenericLogBookReader(EventMapper mapper, Matcher<ObisCode> matcher, CollectedLogBookBuilder collectedLogBookBuilder) {
        super(matcher);
        this.mapper = mapper;
        this.collectedLogBookBuilder = collectedLogBookBuilder;
    }

    @Override
    public CollectedLogBook read(AbstractDlmsProtocol dlmsProtocol, com.energyict.protocol.LogBookReader lbr) {
        try {
            ProfileGeneric profileGeneric = dlmsProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(lbr.getLogBookObisCode());
            CollectedLogBook logBook = collectedLogBookBuilder.createLogBook(lbr.getLogBookIdentifier());

            if (profileGeneric != null) {
                Calendar fromDate = getCalendar(dlmsProtocol);
                fromDate.setTime(lbr.getLastLogBook());

                DataContainer dataContainer = profileGeneric.getBuffer(fromDate, getCalendar(dlmsProtocol));
                List<MeterProtocolEvent> meterProtocolEvents = mapper.map(dataContainer);
                logBook.setCollectedMeterEvents(meterProtocolEvents);
            } else {
                return collectedLogBookBuilder.createLogBook(lbr, ResultType.NotSupported, "Null received when reading generic profile");
            }
            return logBook;
        } catch (IOException e) {
            return collectedLogBookBuilder.createLogBook(lbr, ResultType.NotSupported, "Not supported");
        }

    }

    private Calendar getCalendar(AbstractDlmsProtocol dlmsProtocol) {
        return ProtocolUtils.getCalendar(dlmsProtocol.getTimeZone());
    }

}
