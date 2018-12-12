package com.elster.protocolimpl.dlms;

import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;

/**
 * User: heuckeg
 * Date: 15.07.11
 * Time: 09:31
 *
 * Interface to retrieve a SimpleCosemObjectManager
 */
public interface HasSimpleObjectManager {

    public SimpleCosemObjectManager getObjectManager();
}
