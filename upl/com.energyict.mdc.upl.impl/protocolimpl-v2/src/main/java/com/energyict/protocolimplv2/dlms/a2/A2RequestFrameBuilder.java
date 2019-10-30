package com.energyict.protocolimplv2.dlms.a2;

import com.energyict.dlms.HDLC2Connection;
import com.energyict.dlms.protocolimplv2.CommunicationSessionProperties;
import com.energyict.protocolimpl.utils.ArrayTools;
import org.apache.commons.lang3.ArrayUtils;

/**
 * @author H236365
 * Helper class to construct the frames for A2HHUHDLCConnection and A2HHUSignOn
 */
public class A2RequestFrameBuilder extends HDLC2Connection {
    private int clientMacAddress;
    private long frameCount;

    public A2RequestFrameBuilder(CommunicationSessionProperties properties) {
        super(properties);
        this.frameCount = 0L;
        setProtocolParams();
        this.clientMacAddress = properties.getClientMacAddress();
    }

    public void setClientMacAddress(int clientMacAddress) {
        this.clientMacAddress = clientMacAddress;
    }

    public byte[] buildRequestFrame(byte[] request) {
        byte[] header = constructHeader(request.length);
        byte[] frame = constructFrameToSend(header, request);
        return frame;
    }

    private byte[] constructFrameToSend(byte[] header, byte[] request) {
        Byte[] head = ArrayUtils.toObject(header);
        Byte[] req = ArrayUtils.toObject(request);
        Byte[] fsc = new Byte[]{0, 0}; // create 2 bytes for the fsc
        Byte[] result = ArrayTools.<Byte>concatenateArrays(head, req, fsc);
        byte[] frame = ArrayUtils.toPrimitive(result);
        writeCRC(frame, frame.length);
        return frame;
    }

    private byte[] constructHeader(int requestLength) {
        Byte[] frameFormat = ArrayUtils.toObject(calculateFrameFormat(requestLength));
        Byte[] destination = ArrayUtils.toObject(getDestination());// use 1 byte addressing type
        Byte[] source = ArrayUtils.toObject(getSource());//use 1 byte addressing type
        Byte[] control = ArrayUtils.toObject(calculateControl());
        Byte[] hsc = new Byte[]{0, 0}; // create 2 bytes for the hsc
        Byte[] result = ArrayTools.<Byte>concatenateArrays(frameFormat, destination, source, control, hsc);
        byte[] header = ArrayUtils.toPrimitive(result);
        writeCRC(header, protocolParameters[headerSize]);
        return header;
    }

    private byte[] calculateFrameFormat(int requestLength) {
        // frame format is two bytes comprised of three subfields:Format type (always 1010b), Segmentation (not used in A2), Frame length: the left-most 11 bits.
        int length = requestLength + 9;// frame length = frameformat 2 + destination 1 + source 1+ control 1 + hsc 2 + fsc 2 + requestLength (flags not counted)
        byte[] bytes = new byte[]{
                (byte) ((length >>> 8) | 0xA0),
                (byte) (length),
        };
        return bytes;
    }

    private byte[] getSource() {
        return getAddressBytes(clientMacAddress);
    }

    private byte[] getDestination() {
        return getAddressBytes(1); // destination is always 1
    }

    private byte[] getAddressBytes(int source) {
        return new byte[]{(byte) (source << 1 | 0x01)};
    }

    private byte[] calculateControl() {
        byte high = (byte) ((((byte) frameCount) << 5 | 0x10));
        byte low = (byte) (((byte) frameCount) << 1);
        byte contr = (byte) (high | low);
        frameCount++;
        return new byte[]{contr};
    }

    public byte[] buildSNMRFrame(byte[] snrm) {
        byte[] header = constructSNMRHeader(snrm.length);
        byte[] frame = constructFrameToSend(header, snrm);
        frameCount = 0; // reset frameCounter when SNMR is send
        return frame;
    }

    private byte[] constructSNMRHeader(int length) {
        Byte[] frameFormat = ArrayUtils.toObject(calculateFrameFormat(length));
        Byte[] destination = ArrayUtils.toObject(getDestination());// use 1 byte addressing type
        Byte[] source = ArrayUtils.toObject(getSource());//use 1 byte addressing type
        Byte[] control = new Byte[]{(byte) 0x93};
        Byte[] hsc = new Byte[]{0, 0}; // create 2 bytes for the hsc
        Byte[] result = ArrayTools.<Byte>concatenateArrays(frameFormat, destination, source, control, hsc);
        byte[] header = ArrayUtils.toPrimitive(result);
        writeCRC(header, protocolParameters[headerSize]);
        return header;
    }

}
