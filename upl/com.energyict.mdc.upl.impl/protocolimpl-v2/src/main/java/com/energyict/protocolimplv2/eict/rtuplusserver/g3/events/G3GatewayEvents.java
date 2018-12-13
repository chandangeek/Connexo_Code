package com.energyict.protocolimplv2.eict.rtuplusserver.g3.events;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 11/06/2014 - 16:29
 */
public class G3GatewayEvents {

    private final DlmsSession dlmsSession;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public G3GatewayEvents(DlmsSession dlmsSession, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.dlmsSession = dlmsSession;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
    }

    @SuppressWarnings("unchecked")
    public List<CollectedLogBook> readEvents(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBook : logBooks) {
            CollectedLogBook collectedLogBook = this.collectedDataFactory.createCollectedLogBook(logBook.getLogBookIdentifier());

            try {
                Array eventArray = this.readLogbookBuffer(logBook);
                List<MeterEvent> meterEvents = new ArrayList<>();

                for (AbstractDataType abstractEventData : eventArray.getAllDataTypes()) {
                    BasicEvent basicEvent = getBasicEvent(abstractEventData);

                    if (basicEvent != null) {
                        meterEvents.add(basicEvent.getMeterEvent());
                    }
                }

                collectedLogBook.setCollectedMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents));
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isAuthorizationProblem(e)) {
                    collectedLogBook.setFailureInformation(ResultType.ConfigurationError, this.issueFactory.createWarning(logBook, "logBookXissue", logBook.getLogBookObisCode(), e.getMessage()));
                } else if (DLMSIOExceptionHandler.isUnexpectedResponse(e, dlmsSession.getProperties().getRetries() + 1)) {
                    collectedLogBook.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(logBook, "logBookXnotsupported", logBook.getLogBookObisCode().toString()));
                }
            } catch (IndexOutOfBoundsException e) {
                collectedLogBook.setFailureInformation(ResultType.InCompatible, this.issueFactory.createProblem(logBook, "logBookXissue", logBook.getLogBookObisCode().toString(), e.toString()));
            }

            result.add(collectedLogBook);
        }
        return result;
    }

    /**
     * Reads the logbook buffer and returns events that were added since the lastLogbook date.
     *
     * @return An {@link Array} of {@link Structure}s.
     */
    private Array readLogbookBuffer(final LogBookReader reader) throws IOException {
        final Calendar from = Calendar.getInstance(this.dlmsSession.getTimeZone());
        final Calendar to = Calendar.getInstance(this.dlmsSession.getTimeZone());

        from.setTime(reader.getLastLogBook());

        final ProfileGeneric eventLogProfile = this.dlmsSession.getCosemObjectFactory().getProfileGeneric(reader.getLogBookObisCode());

        if (eventLogProfile != null) {
            final byte[] bufferData = eventLogProfile.getBufferData(from, to);

            if (bufferData != null) {
                final AbstractDataType abstractDataType = AXDRDecoder.decode(bufferData);

                if (abstractDataType != null) {
                    return abstractDataType.getArray();
                }
            }
        }

        // Assume the logbook is not available.
        throw new DataAccessResultException(DataAccessResult.OBJECT_UNAVAILABLE.getId());
    }

    private BasicEvent getBasicEvent(AbstractDataType abstractEventData) throws IOException {
        if (abstractEventData.isStructure()) {
            Structure structure = abstractEventData.getStructure();
            return structure != null ? new BasicEvent(structure, dlmsSession.getTimeZone()) : null;
        } else {
            dlmsSession.getLogger().severe("Expected Array of Structures but one entry was a [" + abstractEventData.getClass().getName() + "]");
        }
        return null;
    }

    private static class BasicEvent extends Structure {

        private static final int DATE_TIME_INDEX = 0;
        private static final int EIS_CODE_INDEX = 1;
        private static final int PROTOCOL_CODE_INDEX = 2;
        private static final int DESCRIPTION_INDEX = 3;

        private final TimeZone timeZone;

        private BasicEvent(Structure eventStructure, TimeZone timeZone) throws IOException {
            super(eventStructure.getBEREncodedByteArray(), 0, 0);
            this.timeZone = timeZone;
        }

        public final MeterEvent getMeterEvent() throws IOException {
            return new MeterEvent(getEventTime(), getEisCode(), getProtocolCode(), getDescription());
        }

        private Date getEventTime() throws IOException {
            OctetString eventDateString = getDataType(DATE_TIME_INDEX).getOctetString();
            AXDRDateTime timeStamp = new AXDRDateTime(eventDateString.getBEREncodedByteArray(), 0, timeZone);
            return timeStamp.getValue().getTime();
        }

        private int getEisCode() {
            return getDataType(EIS_CODE_INDEX).intValue();
        }

        private int getProtocolCode() {
            return getDataType(PROTOCOL_CODE_INDEX).intValue();
        }

        private String getDescription() {
            return getDataType(DESCRIPTION_INDEX).getOctetString().stringValue();
        }
    }
}