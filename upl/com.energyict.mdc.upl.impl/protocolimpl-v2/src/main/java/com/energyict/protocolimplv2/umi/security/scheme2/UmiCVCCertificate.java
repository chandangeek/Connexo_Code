package com.energyict.protocolimplv2.umi.security.scheme2;

import com.energyict.protocolimplv2.umi.types.Role;
import com.energyict.protocolimplv2.umi.types.UmiId;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.cert.CertificateParsingException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class UmiCVCCertificate {
    private static final int DATE_ARRAY_SIZE = 15;
    private static final int STANDARD_CERTIFICATE_TYPE = 0x0;
    private static final int SIGNING_AUTHORITY_CERTIFICATE_TYPE = 0x1;
    private static final int CHA_FIELD_VERSION = 0x1;

    private byte[]                  encoded;
    private UmiId                   issuerUmiId;
    private byte[]                  issuerSerialNumber;
    private BouncyCastlePublicKeyEC publicKey;
    private UmiId                   subjectUmiId;
    private Date                    validFrom;
    private Date                    validUntil;
    private byte                    type;
    private UmiId                   targetUmiIdMin;
    private UmiId                   targetUmiIdMax;
    private Role                    role = Role.GUEST;
    private byte                    profileIdentifier;
    private byte                    chaFieldVersion;

    private short                   signingRoles;
    private short                   devolvableRoles;
    private byte                    flags;

    public UmiCVCCertificate(byte[] encoded) throws CertificateParsingException {
        this.encoded = encoded;
        decode();
    }

    public byte[] getEncoded() {
        return encoded;
    }

    public UmiId getIssuerUmiId() {
        return issuerUmiId;
    }

    public byte[] getIssuerSerialNumber() {
        return issuerSerialNumber;
    }

    public BouncyCastlePublicKeyEC getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = new BouncyCastlePublicKeyEC(publicKey);
    }

    public UmiId getSubjectUmiId() {
        return subjectUmiId;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public byte getType() {
        return type;
    }

    public UmiId getTargetUmiIdMin() {
        return targetUmiIdMin;
    }

    public UmiId getTargetUmiIdMax() {
        return targetUmiIdMax;
    }

    public Role getRole() {
        return role;
    }

    public byte getProfileIdentifier() {
        return profileIdentifier;
    }

    public byte getChaFieldVersion() {
        return chaFieldVersion;
    }

    public short getSigningRoles() {
        return signingRoles;
    }

    public short getDevolvableRoles() {
        return devolvableRoles;
    }

    public byte getFlags() {
        return flags;
    }

    /**
     * Creates InputStreams and starts the decoding
     * @throws Exception
     */
    private void decode() throws CertificateParsingException {
        ByteArrayInputStream bin = null;
        try {
            try {
                bin = new ByteArrayInputStream(encoded);
                DataInputStream din = new DataInputStream(bin);
                decode(din);
            } finally {
                if (bin != null) {
                    bin.close();
                }
            }
        } catch (IOException e) {
            throw new CertificateParsingException(e);
        }
    }

    /**
     * Performs the actual decoding
     * @param din
     * @throws Exception
     */
    private void decode(DataInputStream din) throws CertificateParsingException, IOException {
        int tagValue = decodeTag(din);
        CVCTagEnum tag = findTagFromValue(tagValue);

        if (tag != CVCTagEnum.CV_CERTIFICATE) {
            throw new CertificateParsingException("Expected first tag " + CVCTagEnum.CV_CERTIFICATE + " but found " + tag);
        }

        int length = decodeLength(din);
        while (din.available() > 0) {
            // First chunk to decode is the tag
            tagValue = decodeTag(din);
            tag = findTagFromValue(tagValue);

            // The second chunk to decode is the field length
            length = decodeLength(din);
            byte[] data = null;
            if (!tag.isSequence()) {
                data = new byte[length];
                din.read(data, 0, length);
            }

            switch (tag) {
                case PROFILE_IDENTIFIER: /* interchange profile */
                    this.profileIdentifier = data[0];
                    break;

                case CA_REFERENCE: /* issuer UMI ID */
                    this.issuerUmiId = new UmiId(data, false);
                    break;

                case SERIAL_NUMBER: /* issuer serial number */
                    this.issuerSerialNumber = data;
                    break;

                case PUBLIC_KEY: /* public key template */
                    decodePublicKey(din);
                    break;

                case HOLDER_REFERENCE: /* subject UMI ID */
                    this.subjectUmiId = new UmiId(data, false);
                    break;

                case UMI_EFFECTIVE_DATE: /* valid from */
                    /* 15 ascii encoded digits representing a UTC DateTime: YYYYMMDDHHMMSSZ */
                    this.validFrom = decodeDate(data);
                    break;
                case UMI_EXPIRATION_DATE: /* valid to */
                    this.validUntil = decodeDate(data);
                    break;

                case HOLDER_AUTHORIZATION: /* CHA */
                    decodeCHA(data);
                    break;

                default:
                    /* accept tag and do nothing */
                    break;
            }
        }
    }

    /**
     * Maps a tag value to a specific CVCTagEnum. Note that there
     * exists two tags with the same value (0x82)! In this case the
     * first of these (EXPONENT) will be returned.
     */
    private CVCTagEnum findTagFromValue(int tagValue) throws CertificateParsingException {
        CVCTagEnum wantedType = null;
        for (CVCTagEnum type : CVCTagEnum.values()) {
            if (type.getValue() == tagValue) {
                wantedType = type;
                break;
            }
        }
        if (wantedType != null) {
            return wantedType;
        } else {
            throw new CertificateParsingException("Unknown CVC tag value " + Integer.toHexString(tagValue));
        }
    }

    /**
     * Reads a tag value from the input stream. Encoded according to ITU-T X.690
     * @param din
     * @return
     */
    private int decodeTag(DataInputStream din) throws IOException {
        int tagValue = 0;
        int b1 = din.readUnsignedByte();
        if ((b1 & 0x1F) == 0x1F) {
            // There is another byte to read
            byte b2 = din.readByte();
            tagValue = (b1 << 8) + b2;
        } else {
            tagValue = b1;
        }
        return tagValue;
    }

    /**
     * Reads and decodes a DER-encoded length value
     * @param in
     * @return
     */
    public static int decodeLength(final DataInputStream in) throws IOException {
        int lenBytes = 1;
        int length = 0;
        final int b1 = in.read();
        if (b1 > 0x7F) {  // If the MSB is set then the number of bytes is stored here
            lenBytes = b1 & 0xF;
            if (lenBytes == 1) {
                length = in.readUnsignedByte();
            } else {
                // Assumption: lenBytes = 2 (theoretically it could be longer but hardly in a CV-certificate)
                length = in.readShort();
            }
        } else {
            // No, the MSB wasn't set so the length can be read directly from the current byte
            length = b1;
        }
        return length;
    }

    public static Date decodeDate(byte[] data) {
        final int offset = 48;

        if (data == null || data.length != DATE_ARRAY_SIZE) {
            throw new IllegalArgumentException("data argument must have length " +
                    DATE_ARRAY_SIZE +", was " + (data == null ? 0 : data.length));
        }
        int year = (data[0] - offset) * 1000 + (data[1] - offset) * 100 + (data[2] - offset) * 10 + data[3] - offset;
        int month = (data[4] - offset) * 10 + data[5] - offset - 1;
        int day = (data[6] - offset) * 10 + data[7] - offset;

        int hour = (data[8] - offset) * 10 + data[9] - offset;
        int minutes = (data[10] - offset) * 10 + data[11] - offset;
        int seconds = (data[12] - offset) * 10 + data[13] - offset;

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month, day, hour, minutes, seconds);
        return cal.getTime();
    }

    public void decodeCHA(byte[] data) {
        this.chaFieldVersion = data[0];
        this.type = data[1];
        if (this.chaFieldVersion == CHA_FIELD_VERSION && this.type == STANDARD_CERTIFICATE_TYPE) {
            // Standard certificate
            byte[] umiIdMin = Arrays.copyOfRange(data, 2, 2 + UmiId.SIZE);
            byte[] umiIdMax = Arrays.copyOfRange(data, 10, 10 + UmiId.SIZE);
            this.targetUmiIdMin = new UmiId(umiIdMin, false);
            this.targetUmiIdMax = new UmiId(umiIdMax, false);
            this.role = Role.fromId(data[18]);
            this.flags = data[19];
        }
        if (this.chaFieldVersion == CHA_FIELD_VERSION && this.type == SIGNING_AUTHORITY_CERTIFICATE_TYPE) {
            // Signing authority certificate
            byte[] umiIdMin = Arrays.copyOfRange(data, 2, 2 + UmiId.SIZE);
            byte[] umiIdMax = Arrays.copyOfRange(data, 10, 10 + UmiId.SIZE);
            this.targetUmiIdMin = new UmiId(umiIdMin, false);
            this.targetUmiIdMax = new UmiId(umiIdMax, false);
            this.signingRoles = (byte)((data[18] << 8) | data[19]);
            this.devolvableRoles = (byte)((data[20] << 8) | data[21]);
            this.flags = data[22];
        }
    }

    private void decodePublicKey(DataInputStream din) throws CertificateParsingException, IOException {
        int tagValue = decodeTag(din);
        CVCTagEnum tag = findTagFromValue(tagValue);
        if (tag == CVCTagEnum.PUBLIC_POINT_Y) {
            int length = decodeLength(din);
            byte[] data = new byte[length];
            din.read(data, 0, length);
            this.setPublicKey(data);
        } else {
            throw new CertificateParsingException("Invalid certificate public key!");
        }
    }
}
