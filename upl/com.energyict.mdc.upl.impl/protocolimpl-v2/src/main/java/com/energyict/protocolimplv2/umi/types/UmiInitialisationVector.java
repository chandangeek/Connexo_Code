package com.energyict.protocolimplv2.umi.types;

import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class UmiInitialisationVector extends LittleEndianData {
    public static int SIZE = 12;
    public static int COUNTER_SIZE = 4;

    private int[] counter;

    private UmiId commanderId;

    public UmiInitialisationVector(UmiId commanderId) {
        super(SIZE);
        this.counter = new int[COUNTER_SIZE];
        this.commanderId = commanderId;
    }

    public UmiInitialisationVector(byte[] raw) {
        super(raw, SIZE, false);
        counter = new int[COUNTER_SIZE];
        for (int index = 0; index < counter.length; index++) {
            counter[index] = getRawBuffer().get();
        }
        byte[] commanderIdRaw = new byte[UmiId.SIZE];
        getRawBuffer().get(commanderIdRaw);
        commanderId = new UmiId(commanderIdRaw, true);
    }

    @Override
    public byte[] getRaw() {
        getRawBuffer().position(0);
        for (int counterByte : counter) {
            getRawBuffer().put((byte)counterByte);
        }
        getRawBuffer().put(commanderId.getRaw());
        return super.getRaw();
    }

    public int[] getCounter() {
        return counter;
    }

    public long getCounterAsNumber() {
        ByteBuffer temp = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        for (int index = 0; index < counter.length; index++) {
            temp.put((byte)counter[index]);
        }
        temp.position(0);
        return temp.getLong();
    }

    public UmiId getCommanderId() {
        return commanderId;
    }

    public boolean increment() {
        int index = 0;
        for (; index < counter.length; index++) {
            if (counter[index] == 0xff) {
                counter[index] = 0;
                continue;
            }
            counter[index]++;
            break;
        }
        if (index >= COUNTER_SIZE) {
            return false;
        }
        return true;
    }
}
