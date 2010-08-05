package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.common.AbstractGenericProtocol;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.CommunicationScheduler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 5-aug-2010
 * Time: 11:12:17
 */
public class MTU155 extends AbstractGenericProtocol {

    public void execute(CommunicationScheduler communicationScheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getVersion() {
        return "$Revision$";
    }

    public void addProperties(Properties properties) {
        setProperties(properties);
    }

    public List getRequiredKeys() {
        List<String> requiredKeys = new ArrayList<String>();
        return requiredKeys;
    }

    public List getOptionalKeys() {
        List<String> optionalKeys = new ArrayList<String>();
        return optionalKeys;
    }

}
