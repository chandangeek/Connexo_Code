package com.energyict.protocolimplv2.umi.link;

import com.energyict.protocolimplv2.umi.util.Limits;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

public class FragmentHeaderData extends LittleEndianData {
    public static final byte SIZE = 0x4;
    public static final byte VERSION = 0x0;

    private short typeVersion;   // 1 byte [t t t t v v v v] version = 0
    private byte reserved;      // 1 byte [always 0x00]
    private int data;           // 2 byte

    public FragmentHeaderData(FragmentType type) {
        this(type, 0x00);
    }
    public FragmentHeaderData (FragmentType type, int data) {
        super(SIZE);
        typeVersion = (short) ((type.getId() << 4) | (VERSION & 0xF));
        reserved = 0x00;
        switch (type) {
            // data represents the full length of the un-fragmented package
            case FIRST:
               this.data = (data == Limits.MAX_UNSIGNED_SHORT + 1) ?  0x0000 : data ;
               break;
            case ERROR:
                // error code
                this.data = data;
                break;
            default:
                this.data = 0x00;
                break;
        }

        getRawBuffer().put((byte)typeVersion).put(reserved).putShort((short)data);
    }

    public FragmentHeaderData(byte[] rawData) {
        super(rawData.clone());
        if (rawData.length != SIZE)
            throw new java.security.InvalidParameterException(
                    "Invalid raw fragmentation header size = " + rawData.length + ", required size = " + SIZE
            );
        this.typeVersion = getRawBuffer().get();
        this.reserved = getRawBuffer().get();
        this.data = getRawBuffer().getShort();
    }

    public FragmentType getFragmentType() {
        return FragmentType.fromId((byte)(this.typeVersion >> 4) & 0x0F);
    }

    public int getData() {
        return this.data;
    }

}
