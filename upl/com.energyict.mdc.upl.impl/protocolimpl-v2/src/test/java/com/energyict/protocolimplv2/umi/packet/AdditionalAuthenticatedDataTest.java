package com.energyict.protocolimplv2.umi.packet;

import com.energyict.protocolimplv2.umi.security.SecurityScheme;
import com.energyict.protocolimplv2.umi.types.UmiId;
import org.junit.Test;

import java.security.InvalidParameterException;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class AdditionalAuthenticatedDataTest {
    static public AdditionalAuthenticatedData createTestAAD() {
        byte encOptions = 10;
        byte sourceOptions = 11;
        byte destinationOptions = 12;
        SecurityScheme encryptionScheme = SecurityScheme.NO_SECURITY;
        UmiId destUmiId = new UmiId("123");
        UmiId sourceUmiId = new UmiId("456");
        AdditionalAuthenticatedData aad = new AdditionalAuthenticatedData.Builder()
                .encryptionScheme(encryptionScheme)
                .encryptionOptions(encOptions)
                .sourceOptions(sourceOptions)
                .destinationOptions(destinationOptions)
                .sourceUmiId(sourceUmiId)
                .destinationUmiId(destUmiId).build();
        return aad;
    }

    @Test
    public void createAADAndCheckFields() {
        byte encOptions = 10;
        byte sourceOptions = 11;
        byte destinationOptions = 12;
        SecurityScheme encryptionScheme = SecurityScheme.NO_SECURITY;
        UmiId destUmiId = new UmiId("123");
        UmiId sourceUmiId = new UmiId("456");
        AdditionalAuthenticatedData aad = new AdditionalAuthenticatedData.Builder()
                .encryptionScheme(encryptionScheme)
                .encryptionOptions(encOptions)
                .sourceOptions(sourceOptions)
                .destinationOptions(destinationOptions)
                .sourceUmiId(sourceUmiId)
                .destinationUmiId(destUmiId).build();
        assertEquals(encOptions, aad.getEncryptionOptions());
        assertEquals(sourceOptions, aad.getSourceOptions());
        assertEquals(destinationOptions, aad.getDestinationOptions());
        assertEquals(encryptionScheme, aad.getEncryptionScheme());
        assertEquals(destUmiId, aad.getDestinationUmiId());
        assertEquals(sourceUmiId, aad.getSourceUmiId());
    }

    @Test
    public void recreateAADFromRawData() {
        AdditionalAuthenticatedData aad = createTestAAD();
        AdditionalAuthenticatedData aad2 = new AdditionalAuthenticatedData(aad.getRaw());
        assertEquals(aad, aad2);

        assertArrayEquals(aad.getRaw(), aad2.getRaw());
    }

    @Test
    public void setEncryptionFieldsAndCheckEquality() {
        byte encOptions = 10;
        byte sourceOptions = 11;
        byte destinationOptions = 12;
        SecurityScheme encryptionScheme = SecurityScheme.SYMMETRIC;
        UmiId destUmiId = new UmiId("123");
        UmiId sourceUmiId = new UmiId("456");
        AdditionalAuthenticatedData aad = new AdditionalAuthenticatedData.Builder()
                .sourceOptions(sourceOptions)
                .destinationOptions(destinationOptions)
                .sourceUmiId(sourceUmiId)
                .destinationUmiId(destUmiId).build();

        AdditionalAuthenticatedData aad2 = new AdditionalAuthenticatedData(aad.getRaw());
        assertEquals(aad, aad2);

        aad.setEncryptionScheme(encryptionScheme);
        aad.setEncryptionOptions(encOptions);
        assertFalse(aad.equals(aad2));

        aad2.setEncryptionScheme(encryptionScheme);
        aad2.setEncryptionOptions(encOptions);
        assertEquals(aad, aad2);
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInitFromRawData() {
        new AdditionalAuthenticatedData(new byte[1]);
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInitFromBigRawData() {
        new AdditionalAuthenticatedData(new byte[AdditionalAuthenticatedData.SIZE + 1]);
    }
}
