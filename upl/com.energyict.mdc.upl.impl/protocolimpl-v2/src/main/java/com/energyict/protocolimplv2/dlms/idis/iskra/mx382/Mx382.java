package com.energyict.protocolimplv2.dlms.idis.iskra.mx382;

import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.events.IDISLogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.dlms.idis.am500.profiledata.IDISProfileDataReader;
import com.energyict.protocolimplv2.dlms.idis.am500.registers.IDISStoredValues;
import com.energyict.protocolimplv2.dlms.idis.iskra.mx382.events.Mx382LogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.iskra.mx382.messages.Mx382Messaging;
import com.energyict.protocolimplv2.dlms.idis.iskra.mx382.profiledata.Mx382ProfileDataReader;

/**
 * Created by cisac on 1/14/2016.
 */
public class Mx382 extends AM130{

    @Override
    public String getProtocolDescription() {
        return "Iskraemeco Mx382 DLMS (IDIS P2)";
    }

    @Override
    public String getVersion(){
        return "$Date: 2016-02-09 13:07:48 +0200 (Tue, 09 Feb 2016)$";
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
            this.registerFactory = new Mx382RegisterFactory(this);
        }
        return registerFactory;
    }

    protected IDISLogBookFactory getIDISLogBookFactory() {
        if (idisLogBookFactory == null) {
            idisLogBookFactory = new Mx382LogBookFactory(this);
        }
        return idisLogBookFactory;
    }

    @Override
    public IDISProfileDataReader getIDISProfileDataReader() {
        if (idisProfileDataReader == null) {
            idisProfileDataReader = new Mx382ProfileDataReader(this, getDlmsSessionProperties().getLimitMaxNrOfDays());
        }
        return idisProfileDataReader;
    }

    @Override
    protected IDISMessaging getIDISMessaging() {
        if (idisMessaging == null) {
            idisMessaging = new Mx382Messaging(this);
        }
        return idisMessaging;
    }

    @Override
    public boolean useDsmr4SelectiveAccessFormat() {
        return false;
    }

}
