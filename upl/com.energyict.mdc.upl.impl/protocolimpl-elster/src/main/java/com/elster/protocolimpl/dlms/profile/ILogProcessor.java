package com.elster.protocolimpl.dlms.profile;

import com.elster.dlms.cosem.simpleobjectmodel.SimpleProfileObject;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * User: heuckeg
 * Date: 04.09.12
 * Time: 14:43
 * To change this template use File | Settings | File Templates.
 */
public interface ILogProcessor
{
    public void prepare(SimpleProfileObject profileObject, Object ArchiveStructure) throws IOException;
    public List<MeterEvent> getMeterEvents(final Date from, final Date to) throws IOException;
}
