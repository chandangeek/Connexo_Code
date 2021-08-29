package com.energyict.protocolimplv2.dlms.idis.iskra.mx382;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.events.IDISLogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.dlms.idis.am500.profiledata.IDISProfileDataReader;
import com.energyict.protocolimplv2.dlms.idis.am500.registers.IDISStoredValues;
import com.energyict.protocolimplv2.dlms.idis.iskra.mx382.events.Mx382LogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.iskra.mx382.messages.Mx382Messaging;
import com.energyict.protocolimplv2.dlms.idis.iskra.mx382.profiledata.Mx382ProfileDataReader;
import com.energyict.protocolimplv2.edp.EDPDlmsSession;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cisac on 1/14/2016.
 */
public class Mx382 extends AM130 {

    private HHUSignOnV2 hhuSignOn;

    public Mx382(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }
    
    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());

        if (ComChannelType.SerialComChannel.is(comChannel) || ComChannelType.OpticalComChannel.is(comChannel)) {
            hhuSignOn = getHHUSignOn((SerialPortComChannel) comChannel);
        }

        initDlmsSession(comChannel);
    }

    protected void initDlmsSession(ComChannel comChannel) {
        readFrameCounter( comChannel, (int) getDlmsSessionProperties().getTimeout() );
        if(getDlmsSessionProperties().getConnectionMode().equals("HDLC")) {
            setDlmsSession(new EDPDlmsSession(comChannel, getDlmsSessionProperties()));
        } else {
            setDlmsSession( new DlmsSession( comChannel, getDlmsSessionProperties(), hhuSignOn, offlineDevice.getSerialNumber() ) );
        }
    }

    private HHUSignOnV2 getHHUSignOn(SerialPortComChannel serialPortComChannel) {
        HHUSignOnV2 hhuSignOn = new IEC1107HHUSignOn(serialPortComChannel, getDlmsSessionProperties());
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(false);
        return hhuSignOn;
    }

    @Override
    public String getProtocolDescription() {
        return "Iskraemeco Mx382 DLMS (IDIS P2)";
    }

    @Override
    public String getVersion(){
        return "$Date: 2021-07-07 15:30:00 +0300 (Wed, 07 Jul 2021)$";
    }

    @Override
    public IDISStoredValues getStoredValues() {
        if (storedValues == null) {
            storedValues = new Mx382StoredValues(this);
        }
        return storedValues;
    }

    @Override
    protected AM130RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new Mx382RegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return registerFactory;
    }

    protected IDISLogBookFactory getIDISLogBookFactory() {
        if (idisLogBookFactory == null) {
            idisLogBookFactory = new Mx382LogBookFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return idisLogBookFactory;
    }

    @Override
    public IDISProfileDataReader getIDISProfileDataReader() {
        if (idisProfileDataReader == null) {
            idisProfileDataReader = new Mx382ProfileDataReader(this, this.getCollectedDataFactory(), this.getIssueFactory(), getDlmsSessionProperties().getLimitMaxNrOfDays());
        }
        return idisProfileDataReader;
    }

    @Override
    protected IDISMessaging getIDISMessaging() {
        if (idisMessaging == null) {
            idisMessaging = new Mx382Messaging(this, this.getCollectedDataFactory(), this.getIssueFactory(), this.getPropertySpecService(), this.getNlsService(), this.getConverter(), this.getCalendarExtractor(), this.getMessageFileExtractor(), this.getKeyAccessorTypeExtractor());
        }
        return idisMessaging;
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList(
                new SioOpticalConnectionType(), new RxTxOpticalConnectionType(), new OutboundTcpIpConnectionType()
        );
    }

    @Override
    public boolean useDsmr4SelectiveAccessFormat() {
        return false;
    }

}
