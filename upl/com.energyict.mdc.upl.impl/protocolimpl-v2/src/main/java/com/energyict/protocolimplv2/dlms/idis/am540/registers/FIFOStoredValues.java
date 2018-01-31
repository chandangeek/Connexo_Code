package com.energyict.protocolimplv2.dlms.idis.am540.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.CommunicationSessionProperties;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.idis.am500.profiledata.IDISProfileDataReader;
import com.energyict.protocolimplv2.dlms.idis.am500.registers.ExtendedRegisterChannelIndex;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link com.energyict.dlms.cosem.StoredValues} implementation that reads out the last entry of the profile (FIFO), and does not manipulate it by sorting.
 *
 * @author alex
 */
public final class FIFOStoredValues implements StoredValues {

	/** Read the entries per 15. */
	private static final int BLOCK_SIZE = 15;

	/** The backing profile. */
	private final ProfileGeneric backingProfile;

	/** The {@link com.energyict.dlms.protocolimplv2.CommunicationSessionProperties}. */
	private final CommunicationSessionProperties communicationSessionProperties;

	/** The {@link java.util.TimeZone}. */
	private final TimeZone timezone;

	/** The cache of billing points. */
	private final List<IntervalData> cache = new ArrayList<>();

	/** Logger to use. */
	private final Logger logger;

	/** Profile data reader. */
	private final IDISProfileDataReader profileDataReader;

	/** Also cache the units. */
	private final Map<ObisCode, Unit> cachedUnits = new HashMap<>();

	/**
	 * Create a new instance.
	 *
	 * @param 	backingProfileLogicalName	The OBIS code of the profile backing this {@link com.energyict.dlms.cosem.StoredValues}.
	 *
	 * @throws	java.io.IOException		If an error occurs.
	 */
	public FIFOStoredValues(final ObisCode backingProfileLogicalName,
                            final CosemObjectFactory objectFactory,
                            final CommunicationSessionProperties sessionProperties,
                            final IDISProfileDataReader profileDataReader,
                            final TimeZone timezone,
                            final Logger logger) throws IOException {
		this.backingProfile = objectFactory.getProfileGeneric(backingProfileLogicalName);
		this.communicationSessionProperties = sessionProperties;
		this.logger = logger;
		this.timezone = timezone;
		this.profileDataReader = profileDataReader;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final HistoricalValue getHistoricalValue(final ObisCode obisCode) throws IOException {
		final int billingPoint = obisCode.getF();
        final ObisCode baseObisCode = ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255);

        if (!this.billingPointExists(billingPoint)) {
            throw new NoSuchRegisterException("Billing point " + obisCode.getF() + " doesn't exist for obiscode " + baseObisCode + ".");
        }

        final ExtendedRegisterChannelIndex channelIndex = new ExtendedRegisterChannelIndex(baseObisCode, this.backingProfile.getCaptureObjects());

        final IntervalData intervalData = this.getInterval(billingPoint);

        if (intervalData != null) {
        	final IntervalValue intervalValue = (IntervalValue)intervalData.getIntervalValues().get(channelIndex.getValueIndex() - 1);
        	final int value = intervalValue.getNumber().intValue();

        	final Calendar billingTime = Calendar.getInstance(this.timezone);
        	billingTime.setTimeInMillis(intervalData.getEndTime().getTime());

        	// try to see if we have also event time (i.e. for extended registers)
        	Date eventTime = null;

        	if (channelIndex.getEventTimeIndex() > 0){
        		final IntervalValue eventTimeMillis = (IntervalValue) intervalData.getIntervalValues().get(channelIndex.getEventTimeIndex() - 1);

        		if (eventTimeMillis.getNumber() != null) {
        			final Calendar calendar = Calendar.getInstance(this.timezone);
        			calendar.setTimeInMillis(eventTimeMillis.getNumber().longValue());

        			eventTime = calendar.getTime();
        		}
            }

            final HistoricalRegister cosemValue = new HistoricalRegister();
            cosemValue.setQuantityValue(BigDecimal.valueOf(value), this.getUnit(baseObisCode));

            return new HistoricalValue(cosemValue, billingTime.getTime(), eventTime, 0);
        }

        return null;
	}

	/**
	 * Returns the {@link com.energyict.cbo.Unit} for the given {@link com.energyict.obis.ObisCode}.
	 *
	 * @param 		baseObisCode		The OBIS.
	 *
	 * @return		The corresponding {@link com.energyict.cbo.Unit}.
	 *
	 * @throws 		java.io.IOException			If an IO error occurs.
	 */
    private final Unit getUnit(final ObisCode baseObisCode) throws IOException {
    	if (!this.cachedUnits.containsKey(baseObisCode)) {
    		this.cachedUnits.putAll(this.profileDataReader.readUnits(null, Arrays.asList(baseObisCode)));
    	}

        return this.cachedUnits.get(baseObisCode);
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public final Date getBillingPointTimeDate(final int billingPoint) throws IOException {
		return this.getInterval(billingPoint).getEndTime();
	}

	/**
	 * Reads the next block of profile entries from the meter.
	 *
	 * @throws 	java.io.IOException		If an IO error occurs.
	 */
	private final void readNextBlock() throws IOException {
		final int lastBillingPointInCache = this.cache.size() - 1;
		final int firstBillingPointToRead = lastBillingPointInCache + 1;

		// For the selective access : get the indices.
		final int lastEntry = this.getProfileEntryIndex(firstBillingPointToRead);

		if (lastEntry > 0) {
			int firstEntry = lastEntry - BLOCK_SIZE;

			if (firstEntry < 1) {
				firstEntry = 1;
			}

			final DataContainer data = this.backingProfile.getBuffer(firstEntry, lastEntry, 1, 0);

			if (data != null) {
				final DataStructure root = data.getRoot();
				final List<IntervalData> intervals = new ArrayList<>();

				if (root != null && root.getNrOfElements() > 0) {
					// We'll get these in reverse order, older billing points first.
					for (int i = 0; i < root.getNrOfElements(); i++) {
						intervals.add(this.map(root.getStructure(i)));
					}
				}

				Collections.reverse(intervals);

				this.cache.addAll(intervals);
			}
		}
	}

	/**
	 * Returns the interval for the given billing point.
	 *
	 * @param 		billingPoint
	 * @return
	 */
	private final IntervalData getInterval(final int billingPoint) {
		// Make sure we have this in the cache.
		while (cache.size() <= billingPoint) {
			try {
				this.readNextBlock();
			} catch (IOException e) {
				throw DLMSIOExceptionHandler.handle(e, this.communicationSessionProperties.getRetries() + 1);
			}
		}

		return this.cache.get(billingPoint);
	}

	/**
	 * Maps the given billing point Structure to an IntervalData object.
	 *
	 * @param 		structure		The {@link com.energyict.dlms.DataStructure}.
	 *
	 * @return		The corresponding {@link com.energyict.protocol.IntervalData}.
	 */
	private final IntervalData map(final DataStructure structure) {
        final List<IntervalValue> values = new ArrayList<>();
        Calendar timestamp = Calendar.getInstance();
        
        for (int channel = 0; channel < structure.getNrOfElements(); channel++) {
            try {
            	IntervalValue value;
            	
                if (channel == 0) {
                    timestamp = structure.getOctetString(0).toCalendar(this.timezone);
                } else {
                    if (structure.isInteger(channel)) {
                        value = new IntervalValue(structure.getInteger(channel), 0, 0);
                    } else if (structure.isLong(channel)) {
                        value = new IntervalValue(structure.getLong(channel), 0, 0);
                    } else if (structure.isOctetString(channel)){
                        Calendar cal = structure.getOctetString(channel).toCalendar(this.timezone);
                        value = new IntervalValue(cal.getTimeInMillis(), 0, 0);
                    } else {
                        value = new IntervalValue(null, 0, 0);
                    }

                    values.add(value);
                }
            } catch (Exception ex) {
                this.logger.log(Level.WARNING, ex.getMessage(), ex);
                
                if (channel>0){
                    values.add(new IntervalValue(null, 0, 0));
                }
            }
        }
        
        return new IntervalData(timestamp.getTime(), 0, 0, 0, values);
	}
	
	/**
	 * So this would appear to mean : the number of entries in the backing profile.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public final int getBillingPointCounter() throws IOException {
		return this.backingProfile.getEntriesInUse();
	}
	
	/**
	 * Returns the index of the profile entry we want.
	 * 
	 * @param 		billingPointIndex		The billing point index (means F of the OBIS basically).
	 * 
	 * @return		The profile entry index, so we can do selective access.
	 */
	private final int getProfileEntryIndex(final int billingPointIndex) {
		try {
			// These are 1-based.
			return this.backingProfile.getEntriesInUse() - billingPointIndex;
		} catch (IOException e) {
			throw DLMSIOExceptionHandler.handle(e, this.communicationSessionProperties.getRetries() + 1);
		}
	}
	
	/**
	 * Returns <code>true</code> if the given billing point exists, <code>false</code> if it doesn't.
	 * 
	 * @param 		billingPointIndex		The billing point index.
	 * 
	 * @return		<code>true</code> if the given billing point exists, <code>false</code> if it doesn't.
	 */
	private final boolean billingPointExists(final int billingPointIndex) {
		try {
			return billingPointIndex >= 0 && billingPointIndex < this.backingProfile.getEntriesInUse();
		} catch (IOException e) {
			throw DLMSIOExceptionHandler.handle(e, this.communicationSessionProperties.getRetries() + 1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void retrieve() throws IOException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ProfileGeneric getProfileGeneric() throws NotInObjectListException {
		return this.backingProfile;
	}
}
