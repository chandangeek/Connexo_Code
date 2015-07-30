package com.energyict.protocolimplv2.dlms.g3.properties;

import com.energyict.dlms.IncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.math.BigDecimal;

import static com.energyict.dlms.common.DlmsProtocolProperties.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 9/06/2015 - 17:27
 */
public class AS330DProperties extends DlmsProperties {

    public static final String AARQ_TIMEOUT_PROPERTY = "AARQTimeout";
    public static final String AARQ_RETRIES_PROPERTY = "AARQRetries";
    public static final BigDecimal DEFAULT_MAX_REC_PDU_SIZE = new BigDecimal(512);
    public static final boolean DEFAULT_VALIDATE_INVOKE_ID = true;

    /**
     * Indicate if we're using the mirror logical device to read buffered meter data from the DC,
     * or the gateway logical device to read out data from the actual e-meter
     */
    private final boolean useMirrorLogicalDevice;

    public AS330DProperties() {
        this.useMirrorLogicalDevice = true;
    }

    @Override
    public byte[] getSystemIdentifier() {
        if (getSerialNumber() == null) {
            return new byte[6];
        }

        final String serial = ProtocolTools.addPaddingAndClip(getSerialNumber(), '0', 12, true);
        byte[] systemTitle = ProtocolTools.getBytesFromHexString(serial, "");

        return systemTitle;
    }

    public AS330DProperties(boolean useMirrorLogicalDevice) {
        this.useMirrorLogicalDevice = useMirrorLogicalDevice;
    }


    public long getAARQTimeout() {
        return getProperties().getTypedProperty(AARQ_TIMEOUT_PROPERTY, BigDecimal.ZERO).longValue();
    }

    @Override
    public boolean isUsePolling() {
        return false;   //The AS330D protocol will run embedded in the RTU3, so avoid polling on the inputstream
    }

    /**
     * Property indicating to read the cache out (useful because there's no config change state)
     */
    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(AS330DConfigurationSupport.READCACHE_PROPERTY, false);
    }

    public int getAARQRetries() {
        return getProperties().getTypedProperty(AARQ_RETRIES_PROPERTY, BigDecimal.valueOf(2)).intValue();
    }

    @Override
    public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
        byte invokeIdAndPriority = (byte) (DEFAULT_INVOKE_ID_AND_PRIORITY.intValue());
        if (getProperties().<Boolean>getTypedProperty(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID)) {
            return new IncrementalInvokeIdAndPriorityHandler(invokeIdAndPriority);
        } else {
            return new NonIncrementalInvokeIdAndPriorityHandler(invokeIdAndPriority);
        }
    }

    @Override
    public int getServerUpperMacAddress() {
        return useMirrorLogicalDevice ? getMirrorLogicalDeviceId() : getGatewayLogicalDeviceId();
    }

    private int getMirrorLogicalDeviceId() {
        return parseBigDecimalProperty(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID);
    }

    private int getGatewayLogicalDeviceId() {
        return parseBigDecimalProperty(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID);
    }

    @Override
    public int getMaxRecPDUSize() {
        return parseBigDecimalProperty(MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE);
    }
}