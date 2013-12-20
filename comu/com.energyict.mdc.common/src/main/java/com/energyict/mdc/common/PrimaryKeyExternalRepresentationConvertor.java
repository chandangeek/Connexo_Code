package com.energyict.mdc.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;

public class PrimaryKeyExternalRepresentationConvertor {

    private static final byte INT32 = 1;
    private static final byte INT64 = 2;
    private static final byte DATE = 3;
    private static final byte INT32ARRAY = 4;
    private static final byte INT64ARRAY = 5;
    private static final byte OBJECTARRAY = 6;
    private static final byte STRING = 7;
    private static final byte SERIALIZEDOBJECT = 8;

    private static final String STRINGENCODING = "UTF-8";

    /**
     * Converts the given object to bytes. Conversion of the types that are mostly used are optimized.
     *
     * @param in The object to be converted.
     * @return The converted object.
     */
    public static final byte[] toBytes(final Object in) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(byteStream);
            write(in, stream);
            stream.close();
            return byteStream.toByteArray();
        } catch (IOException ex) {
            throw new ApplicationException(ex);
        }
    }

    private static void write(Object in, DataOutputStream stream) throws IOException {
        if (in instanceof Integer) {
            writeInt((Integer) in, stream);
        } else if (in instanceof Long) {
            writeLong((Long) in, stream);
        } else if (in instanceof Date) {
            writeDate((Date) in, stream);
        } else if (in instanceof String) {
            writeString((String) in, stream);
        } else if (in instanceof int[]) {
            writeIntArray((int[]) in, stream);
        } else if (in instanceof long[]) {
            writeLongArray((long[]) in, stream);
        } else if (in instanceof Object[]) {
            writeObjectArray((Object[]) in, stream);
        } else {
            writeObject(in, stream);
        }
    }

    private static void writeInt(Integer in, DataOutputStream stream) throws IOException {
        stream.write(INT32);
        stream.writeInt(in.intValue());
    }

    private static void writeLong(Long in, DataOutputStream stream) throws IOException {
        stream.write(INT64);
        stream.writeLong(in.longValue());
    }

    private static void writeDate(Date in, DataOutputStream stream) throws IOException {
        stream.write(DATE);
        stream.writeLong(in.getTime());
    }

    private static void writeString(String in, DataOutputStream stream) throws IOException {
        stream.write(STRING);
        byte[] bytes = in.getBytes(STRINGENCODING);

        assert bytes.length < Short.MAX_VALUE;

        stream.writeShort((short) bytes.length);
        stream.write(bytes);
    }

    private static void writeIntArray(int[] in, DataOutputStream stream) throws IOException {
        assert in.length < 256;

        stream.write(INT32ARRAY);
        stream.write(in.length);
        for (int i = 0; i < in.length; i++) {
            stream.writeInt(in[i]);
        }
    }

    private static void writeLongArray(long[] in, DataOutputStream stream) throws IOException {
        assert in.length < 256;

        stream.write(INT64ARRAY);
        stream.write(in.length);
        for (int i = 0; i < in.length; i++) {
            stream.writeLong(in[i]);
        }
    }

    private static void writeObjectArray(Object[] in, DataOutputStream stream) throws IOException {
        assert in.length < 256;

        stream.write(OBJECTARRAY);
        stream.write(in.length);
        for (int i = 0; i < in.length; i++) {
            write(in[i], stream);
        }
    }

    private static void writeObject(Object in, DataOutputStream stream) throws IOException {
        stream.write(SERIALIZEDOBJECT);
        byte[] bytes = serialize(in);

        assert bytes.length <= Short.MAX_VALUE;

        stream.writeShort((short) bytes.length);
        stream.write(bytes);
    }

    private static final byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        ObjectOutputStream objOutStream = new ObjectOutputStream(byteOutStream);
        objOutStream.writeObject(obj);
        return byteOutStream.toByteArray();
    }

    public static final Serializable fromBytes(byte[] bytes) {
        try {
            DataInputStream stream = new DataInputStream(new ByteArrayInputStream(bytes));
            return readObject(stream);
        } catch (IOException ex) {
            throw new ApplicationException(ex);
        }
    }

    private static Serializable readObject(DataInputStream stream) throws IOException {
        switch (stream.read()) {
            case INT32:
                return Integer.valueOf(stream.readInt());
            case INT64:
                return Long.valueOf(stream.readLong());
            case DATE:
                return new Date(stream.readLong());
            case STRING:
                int length = stream.readShort();
                if (length < 0) {
                    throw new ApplicationException("Invalid length: " + length);
                }
                byte[] bytes = new byte[length];
                int readLength = stream.read(bytes);
                if (readLength != length) {
                    throw new ApplicationException("Inconsistent length: got " + readLength + " expected " + length);
                }
                return new String(bytes, STRINGENCODING);
            case INT32ARRAY:
                int[] intArray = new int[stream.read()];
                for (int i = 0; i < intArray.length; i++) {
                    intArray[i] = stream.readInt();
                }
                return intArray;
            case INT64ARRAY:
                long[] longArray = new long[stream.read()];
                for (int i = 0; i < longArray.length; i++) {
                    longArray[i] = stream.readLong();
                }
                return longArray;
            case OBJECTARRAY:
                Object[] objectArray = new Object[stream.read()];
                for (int i = 0; i < objectArray.length; i++) {
                    objectArray[i] = readObject(stream);
                }
                return objectArray;
            case SERIALIZEDOBJECT:
                return readSerializedObject(stream);
            default:
                throw new ApplicationException("Invalid argument " + stream);
        }
    }

    private static Serializable readSerializedObject(DataInputStream in) throws IOException {
        int length = in.readShort();
        if (length < 0) {
            throw new ApplicationException("Invalid length: " + length);
        }
        byte[] bytes = new byte[length];
        int readLength = in.read(bytes);
        if (readLength != length) {
            throw new ApplicationException("Inconsistent length: got " + readLength + " expected " + length);
        }
        try {
            ByteArrayInputStream byteInStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objInStream = new ObjectInputStream(byteInStream);
            return (Serializable) objInStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new ApplicationException(e);
        }
    }

    // performance optimization for id objects

    public static byte[] intToBytes(int id) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[5]);
        buffer.put(INT32);
        buffer.putInt(id);
        return buffer.array();
    }

    public static int intFromBytes(byte[] bytes) {
        assert bytes.length == 5 && bytes[0] == INT32;

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getInt(1);
    }

    // to support equals and hashcode

    public static boolean equals(Object key1, Object key2) {
        assert key1.getClass().equals(key2.getClass());

        if (key1 instanceof int[]) {
            return Arrays.equals((int[]) key1, (int[]) key2);
        } else if (key1 instanceof long[]) {
            return Arrays.equals((long[]) key1, (long[]) key2);
        } else if (key1 instanceof Object[]) {
            return Arrays.equals((Object[]) key1, (Object[]) key2);
        } else {
            return key1.equals(key2);
        }
    }

    public static int hashCode(Object key) {
        if (key instanceof int[]) {
            return Arrays.hashCode((int[]) key);
        } else if (key instanceof long[]) {
            return Arrays.hashCode((long[]) key);
        } else if (key instanceof Object[]) {
            return Arrays.hashCode((Object[]) key);
        } else {
            return key.hashCode();
        }
    }

}