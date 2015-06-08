package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.cbo.LittleEndianInputStream;
import com.energyict.cbo.LittleEndianOutputStream;
import com.energyict.mdc.channels.inbound.EIWebConnectionType;
import com.energyict.mdc.meterdata.CollectedData;
import com.energyict.mdc.protocol.exceptions.CommunicationException;
import com.energyict.mdc.protocol.exceptions.DataEncryptionException;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.crypto.Cryptographer;
import com.energyict.mdc.protocol.inbound.crypto.MD5Seed;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PacketBuilder {

    public static final int MAX_CHANNELS = 32;
    public static final int VERSION_WITHOUT_STATEBITS_1 = 1;
    public static final int VERSION_WITH_STATEBITS_2 = 2;
    public static final int VERSION_32BITS_3 = 3;
    private static final int EIWEB_BULK_HEADER_LENGTH = 19;
    private static final int HEX_PARSE_RADIX = 16;
    /**
     * Bit mask that allows to check if the version value
     * specifies the config file mode by applying
     * the binary and operator (&amp;) to the version number.
     */
    private static final int CONFIG_FILE_MODE_BIT_MASK = 0x10;
    /**
     * Bit mask that allows to extract the lower nibble from an int
     * by using the binary and operator (&amp;).
     */
    private static final int LOWER_NIBBLE_BIT_MASK = 0x000F;
    /**
     * Bit mask that allows to extract the lower bits from an int
     * by using the binary and operator (&amp;).
     */
    private static final int LOWER_BYTE_BIT_MASK = 0xff;
    /**
     * Bit mask that allows to extract the lower word from an int
     * by using the binary and operator (&amp;).
     */
    private static final int LOWER_WORD_BIT_MASK = 0xFFFF;
    /**
     * Bit mask that allows to extract the lower bits from a long
     * by using the binary and operator (&amp;).
     */
    private static final long LOWER_BYTE_BIT_MASK_AS_LONG = 0x000000ffL;
    /**
     * Bit mask that will be used as a default for older firmware versions
     * did not know about the mask parameter and will set the number
     * of channels to 6.
     */
    private static final long SIX_CHANNELS_MASK = 0x0000003FL;
    private static final int BITS_IN_NIBBLE = 4;
    private static final String SEPARATOR = "; ";
    private int version;
    private long mask;
    private int contentLength;
    private String ipAddress;
    private int nrOfRecords;
    private byte[] data;
    private String seq;
    private int nrOfChannels;
    private Integer nrOfAcceptedMessages = null;
    private StringBuilder additionalInfo;

    private Cryptographer cryptographer;
    private DeviceIdentifier deviceIdentifier;
    private List<CollectedData> collectedData = new ArrayList<>();
    private Logger logger;

    public PacketBuilder(Cryptographer cryptographer) {
        this(cryptographer, Logger.getAnonymousLogger());
    }

    public PacketBuilder(Cryptographer cryptographer, Logger logger) {
        super();
        this.cryptographer = cryptographer;
        this.logger = logger;
    }

    public String getSeq() {
        return seq;
    }

    public StringBuilder getAdditionalInfo() {
        if (additionalInfo == null) {
            additionalInfo = new StringBuilder();
        }
        return additionalInfo;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public byte[] getData() {
        return data;
    }

    public int getNrOfChannels() {
        return nrOfChannels;
    }

    public long getMask() {
        return mask;
    }

    public int getNrOfRecords() {
        return nrOfRecords;
    }

    public Integer getNrOfAcceptedMessages() {
        return nrOfAcceptedMessages;
    }

    public void addCollectedData(List<CollectedData> collectedData) {
        collectedData.addAll(this.collectedData);
    }

    private byte[] hex2byteArray(String value) {
        int length = value.length() / 2;
        byte[] buffer = new byte[length];
        for (int i = 0; i < length; i++) {
            buffer[i] = Short.valueOf(value.substring(i * 2, i * 2 + 2), HEX_PARSE_RADIX).byteValue();
        }
        return buffer;
    }

    public boolean isConfigFileMode() {
        return ((getVersion() & CONFIG_FILE_MODE_BIT_MASK) == CONFIG_FILE_MODE_BIT_MASK);
    }

    private void printAttributes() {
        this.logger.finest("Version  = " + this.version);
        this.logger.finest("DeviceId = " + this.deviceIdentifier.toString());
        this.logger.finest("Records  = " + this.nrOfRecords);
        this.logger.finest("IP       = " + this.ipAddress);
        this.logger.finest("Sequence = " + this.seq);
        this.logger.finest("Mask     = " + Long.toHexString(this.mask));
        if (this.nrOfAcceptedMessages == null) {
            this.logger.finest("Xmlctr   = ?");
        } else {
            this.logger.finest("Xmlctr   = " + this.nrOfAcceptedMessages.intValue());
        }

        this.logger.finest("DataLen  = " + this.contentLength);

        StringBuilder dataStringBuilder = new StringBuilder();
        for (int i = 0; i < this.data.length; i++) {
            dataStringBuilder.append(Integer.toHexString(this.data[i] & LOWER_BYTE_BIT_MASK) + " ");
        }
        this.logger.finest(limitToVarchar2Length(dataStringBuilder));
    }

    private String limitToVarchar2Length(StringBuilder dataStringBuilder) {
        String data = dataStringBuilder.toString();
        int subStringLength = data.length() >= 4000 ? 4000 : data.length();
        return data.substring(0, subStringLength);
    }

    public void parse(String id, String seq, String utc, String code, String statebits, String mask, String value, String ip, String sn, String xmlctr)
            throws IOException {
        if (statebits == null) {
            version = VERSION_WITHOUT_STATEBITS_1;
        } else {
            version = VERSION_32BITS_3;
        } // always interprete the value as a 32 bit value

        getAdditionalInfo().append("Device ID: ").append(id).append(SEPARATOR);
        getAdditionalInfo().append("Serial number: ").append(sn).append(SEPARATOR);
        getAdditionalInfo().append("IP address: ").append(ip);

        this.parseDeviceIdentifier(id, sn);
        nrOfRecords = 1;
        ipAddress = ip;
        if (seq == null) {
            this.seq = "FFFF";
        } else {
            this.seq = seq;
        }
        this.parseMask(mask);

        // xmlctr : Number of parsed xml tags from the previous reply, not counting the CLOCKUTC and RESULT tags
        // corresponds to the number of 'accepted device messages'
        this.parseNrOfAcceptedMessages(xmlctr);
        this.parseNumberOfChannelsFromMask();
        contentLength = EIWEB_BULK_HEADER_LENGTH + nrOfChannels * 2 + 4 + 1;
        if (this.ipAddress != null) {
            this.collectedData.add(MdcManager.getCollectedDataFactory().createDeviceIpAddress(this.deviceIdentifier, this.ipAddress, EIWebConnectionType.IP_ADDRESS_PROPERTY_NAME));
        }
        this.createData(utc, code, statebits, this.parseValues(this.getDecryptedData(value)));
    }

    private void parseDeviceIdentifier(String id, String serialNumber) {
        if (id == null) {
            this.parseDeviceIdentifier(0, serialNumber);
        } else {
            try {
                this.parseDeviceIdentifier(Integer.parseInt(id), serialNumber);
            } catch (NumberFormatException e) {
                throw new CommunicationException(e, EIWebConstants.DEVICE_ID_URL_PARAMETER_NAME, id);
            }
        }
    }

    private void parseDeviceIdentifier(int id, String serialNumber) {
        if (id == 0 && serialNumber != null) {
            this.deviceIdentifier = new DeviceIdentifierBySerialNumber(serialNumber);
        } else {
            this.deviceIdentifier = new DeviceIdentifierById(id);
        }
    }

    private void parseMask(String mask) {
        // To have compatibility with older firmware versions, if mask parameter is not present,
        // set it to 6 channels, 0x0000003F. This is for old WebRTU firmware versions.
        try {
            if (mask == null) {
                this.mask = SIX_CHANNELS_MASK;
            } else {
                this.mask = Long.parseLong(mask, HEX_PARSE_RADIX);
            }
        } catch (NumberFormatException e) {
            throw new CommunicationException(e, EIWebConstants.MASK_URL_PARAMETER_NAME, mask);
        }
    }

    public void parseNrOfAcceptedMessages(String xmlctr) {
        if (xmlctr == null) {
            nrOfAcceptedMessages = null;
        } else {
            try {
                nrOfAcceptedMessages = Integer.parseInt(xmlctr);
            } catch (NumberFormatException e) {
                throw new CommunicationException(e, EIWebConstants.MESSAGE_COUNTER_URL_PARAMETER_NAME, xmlctr);
            }
        }
    }

    private void parseNumberOfChannelsFromMask() {
        int i;
        nrOfChannels = 0;
        for (i = 0; i < MAX_CHANNELS; i++) {
            if ((this.mask & (1 << i)) != 0) {
                nrOfChannels++;
            }
        }
    }

    private int[] parseValues(byte[] buffer) {
        String strValues = new String(buffer);
        int[] valuesArray = new int[nrOfChannels];
        StringTokenizer st = new StringTokenizer(strValues, ",");
        int iTokens = st.countTokens();
        if (iTokens != nrOfChannels) {
            throw new DataEncryptionException(this.getDeviceIdentifier());
        }
        try {
            for (int i = 0; i < iTokens; i++) {
                valuesArray[i] = Integer.parseInt(st.nextToken());
            }
        } catch (NumberFormatException e) {
            throw new CommunicationException(e, EIWebConstants.METER_DATA_PARAMETER_NAME, strValues);
        }
        return valuesArray;
    }

    private void createData(String utc, String code, String statebits, int[] values) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        LittleEndianOutputStream os = new LittleEndianOutputStream(bos);
        this.addUTCToDataStream(utc, os);
        this.addCodeToDataStream(code, os);
        if (version == VERSION_32BITS_3) {
            this.addStateBitsToDataStream(statebits, os);
        }
        this.addValuesToDataStream(values, os);
        data = bos.toByteArray();
    }

    private void addUTCToDataStream(String utc, LittleEndianOutputStream os) throws IOException {
        try {
            os.writeLEInt((int) Long.parseLong(utc));
        } catch (NumberFormatException e) {
            throw new CommunicationException(e, EIWebConstants.UTC_URL_PARAMETER_NAME, utc);
        }
    }

    private void addCodeToDataStream(String code, LittleEndianOutputStream os) throws IOException {
        try {
            os.writeByte(Byte.parseByte(code));
        } catch (NumberFormatException e) {
            throw new CommunicationException(e, EIWebConstants.CODE_URL_PARAMETER_NAME, code);
        }
    }

    private void addStateBitsToDataStream(String statebits, LittleEndianOutputStream os) throws IOException {
        try {
            os.writeLEShort((short) Integer.parseInt(statebits, HEX_PARSE_RADIX));
        } catch (NumberFormatException e) {
            throw new CommunicationException(e, EIWebConstants.STATE_BITS_URL_PARAMETER_NAME, statebits);
        }
    }

    private void addValuesToDataStream(int[] values, LittleEndianOutputStream os) throws IOException {
        for (int i = 0; i < values.length; i++) {
            if (version < VERSION_32BITS_3) {
                os.writeLEShort((short) values[i]);
            } else {
                os.writeLEInt(values[i]);
            }
        }
    }

    /**
     * Decrypts the specified byte buffer inline.
     *
     * @param encrypted The encrypted byte buffer that will be decrypted when complete
     */
    private void decrypt(byte[] encrypted) {
        if (getSeq().compareTo("FFFF") != 0) {
            Decryptor2 decryptor = new Decryptor2(this.buildMD5Seed());
            for (int i = 0; i < encrypted.length; i++) {
                encrypted[i] = decryptor.decrypt(encrypted[i]);
            }
        } else {
            // data send without encryption
        }
    }

    private byte[] getDecryptedData(String value) {
        if (getSeq().compareTo("FFFF") != 0) {
            byte[] buffer = this.hex2byteArray(value);
            this.decrypt(buffer);
            return buffer;
        } else {
            return value.getBytes();
        }
    }

    private byte[] getDecryptedData(LittleEndianInputStream in) throws IOException {
        byte[] buffer = new byte[contentLength - EIWEB_BULK_HEADER_LENGTH];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = in.readByte();
        }
        this.decrypt(buffer);
        return buffer;
    }

    public void parse(InputStream is, String sn) throws IOException {
        LittleEndianInputStream in = new LittleEndianInputStream(is);
        // read the header
        version = in.readByte() & LOWER_BYTE_BIT_MASK;
        int deviceId = in.readLEUnsignedShort();
        getAdditionalInfo().append("Device ID: ").append(deviceId).append(SEPARATOR);
        getAdditionalInfo().append("Serial number: ").append(sn).append(SEPARATOR);

        this.parseDeviceIdentifier(deviceId, sn);
        nrOfRecords = in.readLEUnsignedShort();

        this.parseIpAddress(in);
        getAdditionalInfo().append("IP address: ").append(ipAddress);

        this.parseSeq(in);
        mask = in.readLEUnsignedInt();
        this.parseNumberOfChannelsFromMask();
        contentLength = in.readLEInt();

        this.collectedData.add(MdcManager.getCollectedDataFactory().createDeviceIpAddress(this.deviceIdentifier, this.ipAddress, EIWebConnectionType.IP_ADDRESS_PROPERTY_NAME));

        // retrieve data
        data = this.getDecryptedData(in);

        if (this.logger.isLoggable(Level.FINE)) {
            this.printAttributes();
        }
    }

    private void parseIpAddress(LittleEndianInputStream in) throws IOException {
        ipAddress = "";
        long ip = in.readLEUnsignedInt();
        for (int t = 3; t >= 0; t--) {
            ipAddress += Integer.toString((int) ((ip >> (t * 8)) & LOWER_BYTE_BIT_MASK_AS_LONG));
            if (t == 0) {
                break;
            }
            ipAddress += ".";
        }
    }

    private void parseSeq(LittleEndianInputStream in) throws IOException {
        int iSeq = in.readLEUnsignedShort();
        if (iSeq == LOWER_WORD_BIT_MASK) {
            seq = "FFFF";
        } else {
            StringBuilder seqBuilder = new StringBuilder();
            for (int nibbleIndex = 3; nibbleIndex > -1; nibbleIndex--) {
                seqBuilder.append(this.nibbleToString(iSeq, nibbleIndex));
            }
            seq = seqBuilder.toString();
        }
    }

    private String nibbleToString(int word, int nibbleIndex) {
        return String.valueOf(word >> (nibbleIndex * BITS_IN_NIBBLE) & LOWER_NIBBLE_BIT_MASK);
    }


    public boolean isTimeCorrect(Date date) {

        // If data not encrypted, always return true!
        if ("FFFF".compareTo(getSeq()) == 0) {
            return true;
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTime(date);

        return (calendar.get(Calendar.HOUR_OF_DAY) == this.getSeqTimeHour())
                && (calendar.get(Calendar.MINUTE) == this.getSeqTimeMinute());
    }

    private int getSeqTimeHour() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.getSeq().charAt(1));
        buffer.append(this.getSeq().charAt(3));
        return Integer.parseInt(buffer.toString());
    }

    private int getSeqTimeMinute() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.getSeq().charAt(0));
        buffer.append(this.getSeq().charAt(2));
        return Integer.parseInt(buffer.toString());
    }

    private MD5Seed buildMD5Seed() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(getSeq().charAt(1));
        buffer.append(getSeq().charAt(3));
        buffer.append(getSeq().charAt(0));
        buffer.append(getSeq().charAt(2));
        return this.cryptographer.buildMD5Seed(this.deviceIdentifier, buffer.toString());
    }

    public int getVersion() {
        return version;
    }

}