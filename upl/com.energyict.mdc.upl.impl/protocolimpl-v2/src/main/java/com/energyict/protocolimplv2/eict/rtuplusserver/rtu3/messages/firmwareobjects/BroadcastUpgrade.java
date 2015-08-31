package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.firmwareobjects;

import com.energyict.cpo.ObjectMapperFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.InvokeIdAndPriority;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.tasks.ConnectionTypeImpl;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540Properties;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.RTU3Messaging;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.properties.RTU3ConfigurationSupport;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.properties.RTU3Properties;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/08/2015 - 17:23
 */
public class BroadcastUpgrade {

    private static final ObisCode AM540_BROADCAST_FRAMECOUNTER_OBISCODE = ObisCode.fromString("0.0.43.1.1.255");
    private final RTU3Messaging rtu3Messaging;

    public BroadcastUpgrade(RTU3Messaging rtu3Messaging) {
        this.rtu3Messaging = rtu3Messaging;
    }

    //TODO fully test parsing & format, use NTA sim & eiserver
    public CollectedMessage broadcastFirmware(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {

        final String serializedDeviceInfos = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.broadcastDevicesGroupAttributeName).getDeviceMessageAttributeValue();

        final byte[] image = new Base64EncoderDecoder().decode(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.firmwareUpdateUserFileAttributeName).getDeviceMessageAttributeValue());
        final String imageIdentifier = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.firmwareUpdateImageIdentifierAttributeName).getDeviceMessageAttributeValue();

        final int broadcastLogicalDeviceId = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.broadcastLogicalDeviceIdAttributeName).getDeviceMessageAttributeValue());
        final int initialTimeBetweenBlocksInMilliSeconds = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.broadcastInitialTimeBetweenBlocksAttributeName).getDeviceMessageAttributeValue());
        final int numberOfBlocksPerCycle = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.broadcastNumberOfBlocksInCycleAttributeName).getDeviceMessageAttributeValue());
        final int broadcastGroupId = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.broadcastGroupIdAttributeName).getDeviceMessageAttributeValue());

        final BigDecimal broadcastClientMacAddress = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.broadcastClientMacAddressAttributeName).getDeviceMessageAttributeValue());
        final String broadcastEncryptionHexKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.broadcastEncryptionKeyAttributeName).getDeviceMessageAttributeValue();
        final String broadcastAuthenticationHexKey = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.broadcastAuthenticationKeyAttributeName).getDeviceMessageAttributeValue();
        final int encryptionLevel = DlmsEncryptionLevelMessageValues.getValueFor(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.encryptionLevelAttributeName).getDeviceMessageAttributeValue());

        DeviceInfo[] deviceInfos;
        try {
            final JSONArray jsonObject = new JSONArray(serializedDeviceInfos);
            deviceInfos = ObjectMapperFactory.getObjectMapper().readValue(new StringReader(jsonObject.toString()), DeviceInfo[].class);
        } catch (JSONException | IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setDeviceProtocolInformation(e.toString());
            collectedMessage.setFailureInformation(ResultType.InCompatible, rtu3Messaging.createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }

        List<Long> frameCounters = new ArrayList<>();
        int broadcastBlockSize = Integer.MAX_VALUE;

        //For every slave meter: create a unicast session, set the transfer status, initiate the transfer, read the block size & the FC
        for (DeviceInfo slaveDeviceInfo : deviceInfos) {

            DlmsSession unicastDlmsSession = createUnicastSessionToSlave(slaveDeviceInfo);

            //TODO how to handle timeout?
            try {
                unicastDlmsSession.connect();

                //TODO robust
                // TODO error handling ?? timeout & application errors?
                final ImageTransfer imageTransfer = unicastDlmsSession.getCosemObjectFactory().getImageTransfer();
                imageTransfer.writeImageTransferEnabledState(true);
                if (imageTransfer.getImageTransferEnabledState().getState()) {

                    final int meterBlockSize = imageTransfer.readImageBlockSize().intValue();
                    //Use the smallest block size
                    if (meterBlockSize < broadcastBlockSize) {
                        broadcastBlockSize = meterBlockSize;
                    }

                    Structure imageInitiateStructure = new Structure();
                    imageInitiateStructure.addDataType(OctetString.fromString(imageIdentifier));
                    imageInitiateStructure.addDataType(new Unsigned32(image.length));
                    imageTransfer.imageTransferInitiate(imageInitiateStructure);

                    //Note: this reads out a specific broadcast FC and only works for the AM540 for now!
                    final Long frameCounter = unicastDlmsSession.getCosemObjectFactory().getData(AM540_BROADCAST_FRAMECOUNTER_OBISCODE).getValueAttr().longValue();
                    frameCounters.add(frameCounter);
                } else {
                    //TODO more logging: 'failed during initiation phase blabla'
                    throw new ProtocolException("Could not perform the upgrade because meter '" + unicastDlmsSession.getProperties().getSerialNumber() + "' does not allow it.");
                }
            } finally {
                silentRelease(unicastDlmsSession);
            }
        }

        //Now associate to the 'broadcast' logical device. It has the exact same security keys as the management logical device in the Beacon. (only the SERVER_UPPER_MAC_ADDRESS is different)
        final DlmsSessionProperties managementProperties = rtu3Messaging.getProtocol().getDlmsSessionProperties();
        RTU3Properties broadcastLogicalDeviceProperties = new RTU3Properties();
        broadcastLogicalDeviceProperties.addProperties(managementProperties.getProperties());
        broadcastLogicalDeviceProperties.addProperties(managementProperties.getSecurityPropertySet().getSecurityProperties());
        broadcastLogicalDeviceProperties.setSecurityPropertySet(managementProperties.getSecurityPropertySet());
        broadcastLogicalDeviceProperties.getProperties().setProperty(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, broadcastLogicalDeviceId);

        final DlmsSession broadcastLogicalDeviceDlmsSession = new DlmsSession(rtu3Messaging.getProtocol().getDlmsSession().getComChannel(), broadcastLogicalDeviceProperties);
        broadcastLogicalDeviceDlmsSession.connect();

        //TODO disconnect after cycle?

        //Find the highest FC and use that to broadcast :o
        long highestFrameCounter = 0;
        for (Long frameCounter : frameCounters) {
            if (frameCounter != null && frameCounter > highestFrameCounter) {
                highestFrameCounter = frameCounter;
            }
        }

        //Create an unconfirmed dlms session to an AM540 device, to generate the action requests (APDU) for the block transfer.
        final AM540Properties blockTransferProperties = new AM540Properties();
        blockTransferProperties.getProperties().setProperty(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID, BigDecimal.ONE);
        blockTransferProperties.getProperties().setProperty(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID, BigDecimal.ONE);
        blockTransferProperties.getProperties().setProperty(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, BigDecimal.ONE);
        blockTransferProperties.getProperties().setProperty(MeterProtocol.NODEID, BigDecimal.ONE);
        blockTransferProperties.getProperties().setProperty(RTU3ConfigurationSupport.READCACHE_PROPERTY, false);

        //Note that all meters will have the same AK and broadcast EK. (this is defined in the IDIS P2 spec)
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString(), broadcastClientMacAddress);
        securityProperties.setProperty(SecurityPropertySpecName.AUTHENTICATION_KEY.toString(), broadcastAuthenticationHexKey);
        securityProperties.setProperty(SecurityPropertySpecName.ENCRYPTION_KEY.toString(), broadcastEncryptionHexKey);
        final DeviceProtocolSecurityPropertySetImpl securityPropertySet = new DeviceProtocolSecurityPropertySetImpl(0, encryptionLevel, securityProperties);//Auth level 0 does not matter, since there's no association created (pre-established)
        blockTransferProperties.setSecurityPropertySet(securityPropertySet);
        blockTransferProperties.addProperties(securityPropertySet.getSecurityProperties());
        blockTransferProperties.getSecurityProvider().setInitialFrameCounter(highestFrameCounter + 1);

        final LatchComChannel latchComChannel = new LatchComChannel();
        latchComChannel.addProperties(ConnectionTypeImpl.createTypeProperty(ComChannelType.SocketComChannel));
        final DlmsSession apduGeneratingDlmsSession = new DlmsSession(latchComChannel, blockTransferProperties);
        apduGeneratingDlmsSession.assumeConnected(blockTransferProperties.getMaxRecPDUSize(), blockTransferProperties.getConformanceBlock());

        try {
            //Make sure the frames are unsolicited, we don't expect a response!
            apduGeneratingDlmsSession.getDlmsV2Connection().getInvokeIdAndPriorityHandler().getCurrentInvokeIdAndPriorityObject().setServiceClass(InvokeIdAndPriority.ServiceClass.UNCONFIRMED);
        } catch (DLMSConnectionException e) {
            throw new ProtocolException(e);
        }

        final ImageTransfer apduGeneratingImageTransfer = apduGeneratingDlmsSession.getCosemObjectFactory().getImageTransfer();

        int numberOfBlocks = (image.length / broadcastBlockSize) + (((image.length % broadcastBlockSize) == 0) ? 0 : 1);

        //Now send the blocks to the broadcast client of the Beacon.
        for (int blockIndex = 0; blockIndex < numberOfBlocks; blockIndex++) {
            Structure blockStructure = createBlockStructure(broadcastBlockSize, image, numberOfBlocks, blockIndex);

            //'Send' the block. This doesn't actually send anything, it just creates the proper APDU.
            apduGeneratingImageTransfer.imageBlockTransfer(blockStructure);
            final byte[] apdu = latchComChannel.getLastRequest();

            //Now send the block frame to the multicast object, who will forward it to all connected meters
            final Structure broadcastStructure = new Structure();
            broadcastStructure.addDataType(new Unsigned16(broadcastGroupId));
            broadcastStructure.addDataType(new OctetString(apdu));
            broadcastLogicalDeviceDlmsSession.getCosemObjectFactory().getMulticastIC().sendMulticastPacket(broadcastStructure);

            //Wait a bit before sending the next block
            try {
                Thread.sleep(initialTimeBetweenBlocksInMilliSeconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MdcManager.getComServerExceptionFactory().communicationInterruptedException(e);
            }

            //Do a verification round before sending the next X blocks, and also at the very end of the block transfer
            if ((blockIndex > 0 && ((blockIndex % numberOfBlocksPerCycle) == 0)) || (blockIndex == (numberOfBlocks - 1))) {

                //Stop the association to the broadcast logical device while doing this verification round
                silentRelease(broadcastLogicalDeviceDlmsSession);

                //Create a unicast session to every slave device
                for (DeviceInfo slaveDeviceInfo : deviceInfos) {

                    final DlmsSession unicastDlmsSession = createUnicastSessionToSlave(slaveDeviceInfo);

                    //TODO how to handle timeout?
                    try {
                        unicastDlmsSession.connect();

                        //TODO robust
                        // TODO error handling ?? timeout & application errors?
                        final ImageTransfer imageTransfer = unicastDlmsSession.getCosemObjectFactory().getImageTransfer();

                        final BitString transferredBlocks = imageTransfer.getImageTransferBlocksStatus();
                        for (int checkIndex = 0; checkIndex < blockIndex; checkIndex++) {
                            if (!transferredBlocks.asBitSet().get(checkIndex)) {

                                Structure missingBlockStructure = createBlockStructure(broadcastBlockSize, image, numberOfBlocks, checkIndex);

                                //Resend missing block
                                imageTransfer.imageBlockTransfer(missingBlockStructure);
                            }
                        }
                    } finally {
                        silentRelease(unicastDlmsSession);
                    }
                }

                //Now connect again to the broadcast logical device, to continue the broadcast (unless all blocks were already sent)
                if (!(blockIndex == (numberOfBlocks - 1))) {
                    broadcastLogicalDeviceDlmsSession.connect();
                }
            }
        }

        //All blocks were successfully sent. Verification & activation will happen in a meter message.

        return collectedMessage;
    }

    private Structure createBlockStructure(int broadcastBlockSize, byte[] image, int numberOfBlocks, int blockIndex) {
        byte[] blockData;
        if (blockIndex < numberOfBlocks - 1) {
            blockData = new byte[broadcastBlockSize];
            System.arraycopy(image, blockIndex * broadcastBlockSize, blockData, 0, broadcastBlockSize);
        } else {
            long blockSize = image.length - (blockIndex * broadcastBlockSize);
            blockData = new byte[(int) blockSize];
            System.arraycopy(image, (int) (blockIndex * broadcastBlockSize), blockData, 0, (int) blockSize);
        }
        Structure blockStructure = new Structure();
        blockStructure.addDataType(new Unsigned32(blockIndex));
        blockStructure.addDataType(OctetString.fromByteArray(blockData));
        return blockStructure;
    }

    private DlmsSession createUnicastSessionToSlave(DeviceInfo slaveDeviceInfo) {
        final AM540Properties am540Properties = new AM540Properties();
        am540Properties.addProperties(slaveDeviceInfo.getGeneralProperties());
        am540Properties.addProperties(slaveDeviceInfo.getDialectProperties());
        final DeviceProtocolSecurityPropertySetImpl securityPropertySet = new DeviceProtocolSecurityPropertySetImpl(slaveDeviceInfo.getSecurityProperties());
        am540Properties.addProperties(securityPropertySet.getSecurityProperties());
        am540Properties.setSecurityPropertySet(securityPropertySet);

        return new DlmsSession(rtu3Messaging.getProtocol().getDlmsSession().getComChannel(), am540Properties);
    }

    private void silentRelease(DlmsSession dlmsSession) {
        try {
            dlmsSession.disconnect();
        } catch (Throwable e) {
            ; //Move on, not interested in release failures
        }
    }

}
