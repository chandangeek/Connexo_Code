package com.elster.protocolimpl.dlms.profile.entrymgmt;

import com.elster.dlms.types.basic.ObisCode;
import com.elster.protocolimpl.dlms.profile.api.IArchiveLineChecker;

/**
 * User: heuckeg
 * Date: 15.04.13
 * Time: 11:35
 */
public class CheckingArchiveEntry extends AbstractArchiveEntry
{
    private final String name;
    private final IArchiveLineChecker checker;

    public CheckingArchiveEntry(final ObisCode obisCode, final int attribute, final String name, IArchiveLineChecker checker)
    {
        super(obisCode, attribute);
        this.name = name;
        this.checker = checker;
    }

    public void prepareChecker(CapturedObjects archiveObjects)
    {
        checker.prepareChecker(this, archiveObjects);
    }

    public IArchiveLineChecker.CheckResult check(Object[] archiveLine)
    {
        return checker.check(archiveLine);
    }

    @Override
    public String toString()
    {
        return name + "=" + super.toString();
    }

}
