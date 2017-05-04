package com.energyict.encryption;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * OID class.
 *
 * @author alex
 */
public final class OID {

    /**
     * The components.
     */
    private final int[] components;

    /**
     * Create a new OID.
     *
     * @param components The components.
     */
    private OID(final int[] components) {
        this.components = components;
    }

    /**
     * Creates an OID based on a {@link String}.
     *
     * @param oid The OID.
     * @return The OID.
     */
    public static final OID fromString(final String oid) {
        final String[] stringComponents = Objects.requireNonNull(oid).split("\\.");
        final int[] components = new int[stringComponents.length];

        for (int i = 0; i < stringComponents.length; i++) {
            components[i] = Integer.parseInt(stringComponents[i]);
        }

        return new OID(components);
    }

    private static void reverse(byte[] array) {
        if (array != null) {
            int i = 0;

            for (int j = array.length - 1; j > i; ++i) {
                byte tmp = array[j];
                array[j] = array[i];
                array[i] = tmp;
                --j;
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    public final String toString() {
        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < this.components.length - 1; i++) {
            builder.append(this.components[i]).append("");
        }

        builder.append(components[this.components.length - 1]);

        return builder.toString();
    }

    /**
     * Encodes this OID as ASN.1.
     *
     * @return The encoded OID.
     */
    public final byte[] encodeASN1() {
        try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            // Encode the first two bytes into one byte.
            stream.write((byte) (((this.components[0] * 40) + this.components[1]) & 0xFF));

            for (int i = 2; i < this.components.length; i++) {
                final int component = components[i];

                if (component < 128) {
                    stream.write((byte) (component & 0xFF));
                } else {
                    stream.write(this.encodeMultiple(component));
                }
            }
            // Then for each byte, either encode into one or multiple bytes.
            return stream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Got an IO error closing the stream when encoding : [" + e.getMessage() + "]", e);
        }
    }

    /**
     * Encodes a value in multiple bytes.
     *
     * @param value The value to encode.
     * @return The encoded value.
     */
    private final byte[] encodeMultiple(final int value) throws IOException {
        try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            int remainder = value;
            int index = 0;

            while (remainder > 0) {
                int encoded = (byte) (remainder & 0x7F);
                remainder = remainder >> 7;

                if (index != 0) {
                    encoded |= 0x80;
                }

                stream.write(encoded);
                index++;
            }

            final byte[] encodedValue = stream.toByteArray();
            ProtocolTools.reverseByteArray(encodedValue);

            return encodedValue;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.components);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        OID other = (OID) obj;

        return Arrays.equals(components, other.components);

    }
}
