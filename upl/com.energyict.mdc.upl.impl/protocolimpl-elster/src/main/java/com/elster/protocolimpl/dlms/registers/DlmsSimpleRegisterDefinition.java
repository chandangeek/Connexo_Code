package com.elster.protocolimpl.dlms.registers;

/**
 * User: heuckeg
 * Date: 24.03.11
 * Time: 14:17
 */
public class DlmsSimpleRegisterDefinition
    implements IReadableRegister
{

    private final String eisCode;
    private final com.elster.dlms.types.basic.ObisCode elsCode;
    private final int attributeNo;
    private final int classId;

    private final String desc;

    public DlmsSimpleRegisterDefinition(final String eisCode, final com.elster.dlms.types.basic.ObisCode elsCode)
    {
        this(eisCode, elsCode, null, 0, 0);
    }

    public DlmsSimpleRegisterDefinition(final String eisCode, final com.elster.dlms.types.basic.ObisCode elsCode, final String description)
    {
        this(eisCode, elsCode, description, 0, 0);
    }

    public DlmsSimpleRegisterDefinition(final String eisCode, final com.elster.dlms.types.basic.ObisCode elsCode, final int classId, final int attributeNo)
    {
        this(eisCode, elsCode, null, classId, attributeNo);
    }

    public DlmsSimpleRegisterDefinition(final String eisCode, final com.elster.dlms.types.basic.ObisCode elsCode, final String description, final int classId, final int attributeNo)
    {
        this.eisCode = eisCode;
        this.elsCode = elsCode;
        this.desc = description;
        this.classId = classId;
        this.attributeNo = attributeNo;
    }

    public String getObisCode()
    {
        return eisCode;
    }

    public com.elster.dlms.types.basic.ObisCode getElsObisCode() {
        return elsCode;
    }

    public String getDescriptor()
    {
        return ((desc == null) || (desc.length() == 0)) ? eisCode : desc;
    }

    public int getAttributeNo()
    {
        return this.attributeNo;
    }

    public int getClassId()
    {
        return this.classId;
    }

    public boolean match(String obisCode)
    {
        return eisCode.equalsIgnoreCase(obisCode);
    }
}
