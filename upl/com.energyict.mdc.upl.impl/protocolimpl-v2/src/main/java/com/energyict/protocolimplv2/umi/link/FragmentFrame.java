package com.energyict.protocolimplv2.umi.link;

import com.energyict.protocolimplv2.umi.util.IData;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class FragmentFrame implements IData {

    private FragmentHeaderData fragmentHeaderData;
    private IData payload;

    public FragmentFrame(FragmentHeaderData fragmentHeaderData, IData payload) {
       this.fragmentHeaderData = fragmentHeaderData;
       this.payload = payload;
    }

    public FragmentFrame(byte[] rawData) {
        try {
            int fromIndex = 0;
            int toIndex = FragmentHeaderData.SIZE;

            byte[] rawHeaderPayloadData = Arrays.copyOfRange(rawData, fromIndex, toIndex);
            this.fragmentHeaderData = new FragmentHeaderData(rawHeaderPayloadData);

            fromIndex = toIndex;
            toIndex = rawData.length;
            byte[] rawPayload = Arrays.copyOfRange(rawData, fromIndex, toIndex);
            this.payload = new LittleEndianData(rawPayload);


        } catch (Exception e) {
            throw new java.security.InvalidParameterException(
                    "Invalid raw data size=" + rawData.length + ", unable to deserialize fragment frame from raw data."
            );
        }
    }

    public FragmentHeaderData getFragmentHeaderData() {
        return fragmentHeaderData;
    }

    public IData getPayload() {
        return payload;
    }

    @Override
    public byte[] getRaw() {
        int length = getLength();
        ByteBuffer buffer = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(fragmentHeaderData.getRaw()).put(payload.getRaw());
        return buffer.array();
    }

    @Override
    public int getLength() {
        return fragmentHeaderData.getLength() + payload.getLength();
    }
}
