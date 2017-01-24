package com.energyict.protocolimpl.powermeasurement.ion;

class Packet {

    /** flag indicating password size is 0 bytes, for setPasswordSize() */
    public static int PASSWORD_0_BYTES = 0x00;
    /** flag indicating password size is 2 bytes, for setPasswordSize() */
    public static int PASSWORD_2_BYTES = 0x01;
    /** flag indicating password size is 8 bytes, for setPasswordSize() */
    public static int PASSWORD_8_BYTES = 0x02;
    /** flag indicating password is reserved, for setPasswordSize() */
    public static int PASSWORD_RESERVED = 0x03;

    int length;
    int msgId;
    int descriptor;
    int source;
    int destination;
    int service;
    int msgType;
    Authentication authentication;
    ByteArray data;

    Packet() { }

    public static Packet parse(Assembly assembly) {
        Packet packet = new Packet();

        packet.length = assembly.unsignedIntValue(2);
        packet.msgId = assembly.unsignedIntValue(2);
        packet.descriptor = assembly.unsignedIntValue(1);

        int pwdSize = 0;
        int bits = (packet.descriptor & 0x06)>>1;

        if( bits == PASSWORD_0_BYTES )
            pwdSize = 0;
        if( bits == PASSWORD_2_BYTES )
            pwdSize = 2;
        if( bits == PASSWORD_8_BYTES )
            pwdSize = 8;

        assembly.getBytes(2); // ignore first 2 bytes
        packet.source = assembly.unsignedIntValue(2);
        assembly.getBytes(2);
        packet.destination = assembly.unsignedIntValue(2);

        // up to now there are _no_ ion devices with pwd
        if( pwdSize > 0 )
            assembly.getBytes(pwdSize).getBytes();

        packet.service = assembly.unsignedIntValue(1);
        packet.msgType = assembly.unsignedIntValue(1);

        packet.data = assembly.getBytes( packet.length - pwdSize - 15 );

        return packet;

    }

    Packet setIsTimeSetMessage(boolean flag) {
        if( flag == true )
            descriptor = descriptor | 0x08;
        else
            descriptor = descriptor & ~0x08;
        return this;
    }

    Packet setPasswordSize(int size) {
        descriptor = descriptor | ( size << 1 );
        return this;
    }

    Packet setIsResponse(boolean flag) {
        if (flag == true) // if response, bit 0 is 0
            descriptor = descriptor & ~0x01;
        else             // if request, bit 0 is 1
            descriptor = descriptor | 0x01;
        return this;
    }

    int getDestination() {
        return destination;
    }

    Packet setDestination(int destination) {
        this.destination = destination;
        return this;
    }

    int getLength() {
        return length;
    }

    Packet setLength(int length) {
        this.length = length;
        return this;
    }

    int getMsgId() {
        return msgId;
    }

    Packet setMsgId(int msgId) {
        this.msgId = msgId;
        return this;
    }

    int getMsgType() {
        return msgType;
    }

    Packet setMsgType(int msgType) {
        this.msgType = msgType;
        return this;
    }

    int getService() {
        return service;
    }

    Packet setService(int service) {
        this.service = service;
        return this;
    }

    int getSource() {
        return source;
    }

    Packet setSource(int source) {
        this.source = source;
        return this;
    }

    Packet setAuthentication( Authentication authentication ){
        this.authentication = authentication;
        if( authentication != null )
            setPasswordSize( PASSWORD_8_BYTES );

        return this;
    }

    Packet setData(ByteArray data) {
        this.data = data;
        length = 15 + data.size();
        if( authentication != null )
            length = length + 8;
        return this;
    }

    ByteArray toByteArray() {
        ByteArray rslt = new ByteArray();

        rslt.add(
            new byte[] {
                (byte) (length >> 8),
                (byte) (length & 0x00ff),
                (byte) (msgId >> 8),
                (byte) (msgId & 0x00ff),
                (byte) descriptor,

                (byte) 0x95, 0x64,
                (byte) 0x00, 0x01,
                /*(byte) 0x80, 0x00,
                (byte) (source >> 8),
                (byte) (source & 0x00ff),

                (byte) 0x80, 0x00,
                */
                (byte) 0x95, 0x01,
                (byte) (destination >> 8),
                (byte) (destination & 0x00ff)

            });

        if( authentication != null )
            rslt.add( authentication.toByteArray() );

        rslt.add(
            new byte[] {
                (byte) service,
                (byte) msgType } );

        rslt.add(data);
        return rslt;

    }

    public ByteArray getData() {
        return data;
    }

}
