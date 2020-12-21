package com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.channel;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GenericChannelInfoReader implements ChannelInfoReader {

    private final CollectedDataFactory collectedDataFactory;

    public GenericChannelInfoReader(CollectedDataFactory collectedDataFactory) {
        this.collectedDataFactory = collectedDataFactory;
    }

    @Override
    public CollectedLoadProfileConfiguration getChannelInfo(com.energyict.protocol.LoadProfileReader lpr, AbstractDlmsProtocol protocol) {
        try {
            CollectedLoadProfileConfiguration lpc = collectedDataFactory.createCollectedLoadProfileConfiguration(lpr.getProfileObisCode(), protocol.getOfflineDevice().getDeviceIdentifier(), lpr.getMeterSerialNumber());
            ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpr.getProfileObisCode());
            List<ChannelInfo> channelInfos = getChannelInfo(protocol, profileGeneric.getCaptureObjects(), lpr.getMeterSerialNumber());
            lpc.setSupportedByMeter(true);
            lpc.setChannelInfos(channelInfos);
            return lpc;
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
        }
    }

    private List<ChannelInfo> getChannelInfo(AbstractDlmsProtocol protocol, List<CapturedObject> capturedObjects, String serialNumber) {
        List<ObisCode> channelObisCodes = new ArrayList<>();
        for (CapturedObject capturedObject : capturedObjects) {
            channelObisCodes.add(capturedObject.getLogicalName().getObisCode());
        }
        List<ChannelInfo> channelInfos = new ArrayList<>();
        int counter = 0;
        for (ObisCode obisCode : channelObisCodes) {
            Unit unit = getUnit(protocol, obisCode);
            String newOBIS = obisCode.toString();
            ChannelInfo channelInfo = new ChannelInfo(counter, newOBIS, unit, serialNumber);
            channelInfos.add(channelInfo);
            counter++;
        }
        return channelInfos;
    }

    private Unit getUnit(AbstractDlmsProtocol protocol, ObisCode obisCode) {
        try {
            UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(protocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), obisCode);
            if (uo == null) {
                throw new IOException("Could not determine unit for obis code:" + obisCode);
            }
            DLMSAttribute dlmsAttribute = new DLMSAttribute(obisCode, RegisterAttributes.SCALER_UNIT.getAttributeNumber(), uo.getClassID());
            ComposedCosemObject cosemObject = new ComposedCosemObject(protocol.getDlmsSession(), false, dlmsAttribute);
            AbstractDataType attribute = cosemObject.getAttribute(dlmsAttribute);
            return new ScalerUnit(attribute).getEisUnit();
        } catch (IOException e) {
            protocol.journal("Could not determine unit! Reading DLMS attribute failed:" + e.getMessage());
            return Unit.get(BaseUnit.UNITLESS);
        }
    }
}
