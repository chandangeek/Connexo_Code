package com.energyict.protocolimpl.ansi.c12;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocolimpl.ansi.c12.EAXPrime.EAXPrimeEncoder;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class C1222Layer extends C12Layer2 {

    private C1222Buffer c1222Buffer = null;
    private int c1222Position = -1;
    private EAXPrimeEncoder eaxPrimeEncoder;

    public enum SecurityModeEnum {
        SecurityClearText,
        SecurityClearTextWithAuthentication,
        SecurityCipherTextWithAuthentication;
    }

    public enum ResponseControlEnum {
        ResponseControlAlways,
        ResponseControlOnException,
        ResponseControlNever;
    }

    public enum SecurityExtensionEnum {
        ExtensionNo,
        ExtensionSeed,
        ExtensionPassword;
    }

    public C1222Layer(InputStream inputStream,
                      OutputStream outputStream,
                      int timeout,
                      int maxRetries,
                      long forcedDelay,
                      int echoCancelling,
                      HalfDuplexController halfDuplexController, Logger logger, boolean validateControlToggleBit) throws ConnectionException {
        super(inputStream, outputStream, timeout, maxRetries, forcedDelay, echoCancelling, halfDuplexController, logger, validateControlToggleBit);
        this.timeout = timeout;
        this.maxRetries = maxRetries;
    }

    public void setC1222Buffer(C1222Buffer c1222Buffer) {
        this.c1222Buffer = c1222Buffer;
    }

    protected void buildPacket(RequestData requestData) {
        c1222Buffer.getCommand().reset();
        try {
            if (requestData.getCode() == -1) {
                c1222Buffer.getCommand().write(requestData.getData());
            } else {
                c1222Buffer.getCommand().write(encodeInteger(requestData.getAssembledData().length));
                c1222Buffer.getCommand().write(requestData.getAssembledData());
            }

            buildC1222Wrapper(c1222Buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] data = c1222Buffer.getResult().toByteArray();
        packet = new byte[data.length];
        System.arraycopy(data, 0, packet, 0, data.length);
        buildControl(false);
        buildSequence();
        //int crc = CRCGenerator.calcHDLCCRC(packet, packet.length-2);
        previousControl = getControl();
        previousSequence = getSequence();
        previousPacket = packet;
    }

    protected ResponseData receiveResponseData() throws IOException {
        long protocolTimeout = System.currentTimeMillis() + TIMEOUT;
        long interFrameTimeout = System.currentTimeMillis() + timeout;

        ResponseData responseData = new ResponseData();
        int currentByte = 0;
        int previousByte = 0;
        int c1222Size = -1;
        int fieldId = -1;
        int fieldSize = -1;
        int fieldPosition = -1;
        ByteArrayOutputStream fieldValue = new ByteArrayOutputStream();
        ByteArrayOutputStream completeResponse = new ByteArrayOutputStream();

        copyEchoBuffer();
        C1222ResponseParms responseParms = c1222Buffer.getResponseParms();
        while (true) {
            if ((currentByte = readIn()) != -1) {
                completeResponse.write(currentByte);
                if (currentByte == 0x60 && c1222Size == -1)  // Read past first byte byte of message
                {
                } else if (previousByte == 0x60 && c1222Size == -1) // Store size of C1222 message. 2nd byte of message
                {
                    c1222Size = extractSize(currentByte);
                    c1222Position = 0;
                } else {
                    c1222Position++;
                    if (fieldId == -1)  // If we're not reading a field value, look for a control byte
                    {
                        if (isControlByte(previousByte)) // Control byte found in previous byte, start reading new field
                        {
                            fieldId = previousByte;
                            fieldPosition = 0;
                            fieldSize = extractSize(currentByte);
                            fieldValue.reset();
                        }
                    } else // Append byte to field
                    {
                        fieldValue.write(currentByte);
                        fieldPosition++;
                        if (fieldPosition == fieldSize) // If is last byte for field, close the field
                        {
                            responseParms.assignFieldValue(fieldId, fieldValue);
                            fieldId = -1;
                            fieldSize = -1;
                            fieldPosition = -1;
                            previousByte = 0;
                            currentByte = 0;
                        }
                    }
                    if (c1222Position == c1222Size) // Done reading
                    {
                        break;
                    }
                }

                previousByte = currentByte;
            }
            if (((long) (System.currentTimeMillis() - protocolTimeout)) > 0) {
                throw new ProtocolConnectionException("receiveFrame() response timeout error", TIMEOUT_ERROR);
            }
            if (((long) (System.currentTimeMillis() - interFrameTimeout)) > 0) {
                throw new ProtocolConnectionException("receiveFrame() interframe timeout error", TIMEOUT_ERROR);
            }
        }
        c1222Buffer.setApInvocationId(responseParms.getCallingApInvocationId());

        int securityMode = responseParms.getSecurityMode();
        if (securityMode == 0) {
            // 1. Return the unencrypted response data
             responseData.setData(responseParms.getUserInformation());
        } else if (securityMode == 1) { // ToDO: SecurityMode 1 needs to be tested out!
            byte[] receivedMac = responseParms.getMac();
            byte[] receivedCipherText =null;
            byte[] responseClearText = buildResponseCanonifiedCleartext(responseParms);

            // 1. Verify the authentication fo the response is correct (verify MAC)
            getEAXPrimeEncoder().decrypt(responseClearText, receivedCipherText, receivedMac);

            // 2. Return the UserInformation data - only if MAC verification passes (else an error is thrown, so we do not come here)
            responseData.setData(ProtocolTools.getSubArray(responseParms.getUserInformation(), 1, responseParms.getUserInformation().length));
        } else if (securityMode == 2) {
            byte[] receivedMac = responseParms.getMac();
            byte[] receivedCipherText = responseParms.getUserInformation();
            byte[] responseClearText = buildResponseCanonifiedCleartext(responseParms);

            // 1. Verify the authentication fo the response is correct (verify MAC)
            getEAXPrimeEncoder().decrypt(responseClearText, receivedCipherText, receivedMac);

            // 2. Decrypt the encrypted response data and update the responseData with it
            byte[] plainText = getEAXPrimeEncoder().getPlainText();

            // PlainText starts with the encoded length - this should be stripped off.
            if (plainText[0] == (byte) 0x82) {  // 3 byte-encoded length
                responseData.setData(ProtocolTools.getSubArray(plainText, 3, plainText.length));
            } else if (plainText[0] == (byte) 0x81) {   // 2 byte-encoded length
                responseData.setData(ProtocolTools.getSubArray(plainText, 2, plainText.length));
            } else {    // 1 byte-encoded length
                responseData.setData(ProtocolTools.getSubArray(plainText, 1, plainText.length));
            }
        }

        c1222Buffer.reset();

        if (responseData.getData() == null) {
            throw new ApplicationException("Invalid message format. User Information is null.");
        }

        return responseData;
    }

    private boolean isControlByte(int theByte) {
        boolean result = false;

        if (theByte == 0xA1 ||
                theByte == 0xA2 ||
                theByte == 0xA4 ||
                theByte == 0xA6 ||
                theByte == 0xA7 ||
                theByte == 0xA8 ||
                theByte == 0xAC ||
                theByte == 0xBE) {
            result = true;
        }
        return result;
    }

    private int extractSize(int currentByte) throws NestedIOException, IOException {
        int result = currentByte;
        if ((currentByte & 0x80) != 0) // If high bit is set
        {
            result &= 0x7F; // Turn off high bit to get the number of bytes to read
            int pos = result;
            int tempValue = 0;
            result = 0;
            for (int i = 1; i <= pos; i++) // Read bytes that makes up the size
            {
                if ((tempValue = readIn()) != -1) {
                    c1222Position++;
                    result = 256 * result + tempValue;
                }
            }
        }
        return result;
    }

    private void buildC1222Wrapper(C1222Buffer c1222Buffer) throws IOException, ApplicationException, IllegalStateException {
        buildHeader(c1222Buffer);
        buildUserInformation(c1222Buffer);

        if (c1222Buffer.getSecurityMode() == SecurityModeEnum.SecurityClearText) {
            c1222Buffer.getUserInformation().write(c1222Buffer.getEpsemControl());
            c1222Buffer.getUserInformation().write(c1222Buffer.getCommand().toByteArray());
        } else {
            buildCanonifiedCleartext(c1222Buffer);
            if (c1222Buffer.getSecurityMode() == SecurityModeEnum.SecurityClearTextWithAuthentication) {
                buildAuthentication(c1222Buffer);
            } else if (c1222Buffer.getSecurityMode() == SecurityModeEnum.SecurityCipherTextWithAuthentication) {
                buildEncryption(c1222Buffer);
            }
        }

        c1222Buffer.getAcse().write(c1222Buffer.getUserInformation().toByteArray());
        c1222Buffer.getResult().write(0x60);
        encodeAndAppendInteger(c1222Buffer.getResult(), c1222Buffer.getAcse().size());
        c1222Buffer.getResult().write(c1222Buffer.getAcse().toByteArray());
    }

    private void buildHeader(C1222Buffer c1222Buffer) throws IOException {
        byte epsemControl = (byte) 0x80;

        appendUid(c1222Buffer.getAcse(), 0xA1, c1222Buffer.getRequestParms().getApplicationContext());
        appendUid(c1222Buffer.getAcse(), 0xA2, c1222Buffer.getCalledApTitle());
        appendUid(c1222Buffer.getAcse(), 0xA6, c1222Buffer.getRequestParms().getCallingApTitle());
        if (c1222Buffer.getRequestParms().getAeQualifier() != -1) {
            appendInteger(c1222Buffer.getAcse(), 0xA7, c1222Buffer.getRequestParms().getAeQualifier());
        }
        appendInteger(c1222Buffer.getAcse(), 0xA8, (int) c1222Buffer.getApInvocationId());
        c1222Buffer.setUiExtra((c1222Buffer.getSecurityMode() == SecurityModeEnum.SecurityClearText) ? 1 : 5);
        c1222Buffer.setEpsemSize((c1222Buffer.getCommand().size() + c1222Buffer.getUiExtra()));

        epsemControl |= (byte) c1222Buffer.getResponseControl().ordinal();

        if (c1222Buffer.getRequestParms().getEdClass() != null && c1222Buffer.getRequestParms().getEdClass().length() > 0) {
            epsemControl |= 0x10;    // Set bit 4
            c1222Buffer.setUiExtra(c1222Buffer.getUiExtra() + 4);
        }

        c1222Buffer.setEpsemControl(epsemControl);
    }

    private void buildUserInformation(C1222Buffer c1222Buffer) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] uiEncodedLength;
        byte[] uiExternalEncodedLength;
        byte[] uiLength;

        uiEncodedLength = encodeInteger(c1222Buffer.getEpsemSize());
        uiExternalEncodedLength = encodeInteger(uiEncodedLength.length + c1222Buffer.getEpsemSize() + 1);
        uiLength = encodeInteger(uiExternalEncodedLength.length + uiEncodedLength.length + c1222Buffer.getEpsemSize() + 2);

        result.write((byte) 0xBE);
        result.write(uiLength);
        result.write((byte) 0x28);
        result.write(uiExternalEncodedLength);
        result.write((byte) 0x81);
        result.write(uiEncodedLength);

        if (c1222Buffer.getSecurityMode() == SecurityModeEnum.SecurityClearText) {
            if (c1222Buffer.getRequestParms().getEdClass() != null && c1222Buffer.getRequestParms().getEdClass().length() > 8) {    // Length = 4 bytes
                throw new ApplicationException("ED Class Too Long");
            }
            if (c1222Buffer.getRequestParms().getEdClass() != null && c1222Buffer.getRequestParms().getEdClass().length() > 0) {
                result.write(ProtocolTools.getBytesFromHexString(c1222Buffer.getRequestParms().getEdClass(), ""));
            }
        }

        c1222Buffer.getUserInformation().write(result.toByteArray());
    }

    private void buildResponseUserInformation(ByteArrayOutputStream byteArrayList, C1222ResponseParms responseParms) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] uiEncodedLength;
        byte[] uiExternalEncodedLength;
        byte[] uiLength;

        responseParms.setUiExtra((responseParms.getSecurityMode() == 0) ? 1 : 5);   // 1 byte Epsem control (+ 4 bytes MAC)
        responseParms.setEpsemSize((responseParms.getUserInformation().length + responseParms.getUiExtra()));

        uiEncodedLength = encodeInteger(responseParms.getEpsemSize());
        uiExternalEncodedLength = encodeInteger(uiEncodedLength.length + responseParms.getEpsemSize() + 1);
        uiLength = encodeInteger(uiExternalEncodedLength.length + uiEncodedLength.length + responseParms.getEpsemSize() + 2);

        result.write((byte) 0xBE);
        result.write(uiLength);
        result.write((byte) 0x28);
        result.write(uiExternalEncodedLength);
        result.write((byte) 0x81);
        result.write(uiEncodedLength);
        byteArrayList.write(result.toByteArray());
    }

    private void buildCanonifiedCleartext(C1222Buffer c1222Buffer) throws IOException {
        byte epsemControl = c1222Buffer.getEpsemControl();

        epsemControl |= (c1222Buffer.getSecurityMode().ordinal() << 2);
        c1222Buffer.setEpsemControl(epsemControl);
        c1222Buffer.getUserInformation().write(c1222Buffer.getEpsemControl());

        appendUid(c1222Buffer.getCanonifiedCleartext(), 0xA1, c1222Buffer.getRequestParms().getApplicationContext());
        appendUid(c1222Buffer.getCanonifiedCleartext(), 0xA2, c1222Buffer.getCalledApTitle());
        if (c1222Buffer.getRequestParms().getAeQualifier() != -1) {
            appendInteger(c1222Buffer.getCanonifiedCleartext(), 0xA7, c1222Buffer.getRequestParms().getAeQualifier());
        }
        appendInteger(c1222Buffer.getCanonifiedCleartext(), 0xA8, (int) c1222Buffer.getApInvocationId());

        if (!c1222Buffer.isSecurityKeyIdAndInitializationVectorWereSent()) {
            c1222Buffer.setSecurityKeyIdAndInitializationVectorWereSent(true);
            buildCallingInvocation(c1222Buffer.getAcse(), c1222Buffer);
            buildCallingInvocation(c1222Buffer.getCanonifiedCleartext(), c1222Buffer);
        }

        c1222Buffer.getCanonifiedCleartext().write(c1222Buffer.getUserInformation().toByteArray());

        appendUid(c1222Buffer.getCanonifiedCleartext(), 0xA6, c1222Buffer.getRequestParms().getCallingApTitle());

        c1222Buffer.getCanonifiedCleartext().write((byte) c1222Buffer.getSecurityKeyId());

        byte[] tempBytes = longToByteArrayLittleEndian(c1222Buffer.getInitializationVector());
        c1222Buffer.getCanonifiedCleartext().write(tempBytes[0]);
        c1222Buffer.getCanonifiedCleartext().write(tempBytes[1]);
        c1222Buffer.getCanonifiedCleartext().write(tempBytes[2]);
        c1222Buffer.getCanonifiedCleartext().write(tempBytes[3]);
    }

    private byte[] buildResponseCanonifiedCleartext(C1222ResponseParms responseParms) throws IOException {
        ByteArrayOutputStream canonifiedCleartext = new ByteArrayOutputStream();

        appendUid(canonifiedCleartext, 0xA1, responseParms.getApplicationContext());
        appendUid(canonifiedCleartext, 0xA2, responseParms.getCalledApTitle());
        if (responseParms.getCalledApInvocationId() != -1) {
            appendInteger(canonifiedCleartext, 0xA4, (int) responseParms.getCalledApInvocationId());
        }
        if (responseParms.getCallingAeQualifier() != -1) {
            appendInteger(canonifiedCleartext, 0xA7, (int) responseParms.getCallingAeQualifier());
        }
        appendInteger(canonifiedCleartext, 0xA8, (int) responseParms.getCallingApInvocationId());

        if (!responseParms.isSecurityKeyIdAndInitializationVectorWereSent()) {
            responseParms.setSecurityKeyIdAndInitializationVectorWereSent(true);
            buildResponseCallingInvocation(canonifiedCleartext, responseParms);
        }

        buildResponseUserInformation(canonifiedCleartext, responseParms);
        canonifiedCleartext.write(responseParms.getEpsemControl());

        appendUid(canonifiedCleartext, 0xA6, responseParms.getCallingApTitle());

        canonifiedCleartext.write((byte) responseParms.getSecurityKeyId());

        byte[] tempBytes = longToByteArrayLittleEndian(responseParms.getInitializationVector());
        canonifiedCleartext.write(tempBytes[0]);
        canonifiedCleartext.write(tempBytes[1]);
        canonifiedCleartext.write(tempBytes[2]);
        canonifiedCleartext.write(tempBytes[3]);

        return canonifiedCleartext.toByteArray();
    }



    private void buildEncryption(C1222Buffer c1222Buffer) throws ApplicationException, IOException {
        byte[] clearText = c1222Buffer.getCanonifiedCleartext().toByteArray();
        byte[] plainText = c1222Buffer.getCommand().toByteArray();

        getEAXPrimeEncoder().encrypt(clearText, plainText);
        byte[] cipherText = getEAXPrimeEncoder().getCipherText();
        byte[] mac = getEAXPrimeEncoder().getMac();

        c1222Buffer.setMac(mac);
        c1222Buffer.getUserInformation().write(cipherText);
        c1222Buffer.getUserInformation().write(mac);
    }

    private void buildAuthentication(C1222Buffer c1222Buffer) throws ApplicationException, IOException {    // ToDO: SecurityMode 1 needs to be tested out!
        byte[] clearText = c1222Buffer.getCanonifiedCleartext().toByteArray();
        byte[] plainText = null;

        getEAXPrimeEncoder().encrypt(clearText, plainText);
        byte[] mac = getEAXPrimeEncoder().getMac();

        c1222Buffer.setMac(mac);
        c1222Buffer.getUserInformation().write(c1222Buffer.getCommand().toByteArray());
        c1222Buffer.getUserInformation().write(mac);
    }

    private void buildCallingInvocation(ByteArrayOutputStream byteArrayList, C1222Buffer c1222Buffer) throws IOException {
        if (c1222Buffer.getSecurityKeyId() > 256) {
            throw new ApplicationException("securityKeyId too large");
        }

        byte[] initializationVector = longToByteArrayLittleEndian(c1222Buffer.getInitializationVector());
        byte[] result = new byte[17];

        result[0] = (byte) 0xAC;
        result[1] = (byte) 0x0F;
        result[2] = (byte) 0xA2;
        result[3] = (byte) 0x0D;
        result[4] = (byte) 0xA0;
        result[5] = (byte) 0x0B;
        result[6] = (byte) 0xA1;
        result[7] = (byte) 0x09;
        result[8] = (byte) 0x80;
        result[9] = (byte) 0x01;
        result[10] = (byte) c1222Buffer.getSecurityKeyId();
        result[11] = (byte) 0x81;
        result[12] = (byte) 0x04;
        result[13] = initializationVector[0];
        result[14] = initializationVector[1];
        result[15] = initializationVector[2];
        result[16] = initializationVector[3];
        byteArrayList.write(result);
    }

    private void buildResponseCallingInvocation(ByteArrayOutputStream byteArrayList, C1222ResponseParms responseParms) throws IOException {
        byte[] initializationVector = longToByteArrayLittleEndian(responseParms.getInitializationVector());
        byte[] result = new byte[17];

        result[0] = (byte) 0xAC;
        result[1] = (byte) 0x0F;
        result[2] = (byte) 0xA2;
        result[3] = (byte) 0x0D;
        result[4] = (byte) 0xA0;
        result[5] = (byte) 0x0B;
        result[6] = (byte) 0xA1;
        result[7] = (byte) 0x09;
        result[8] = (byte) 0x80;
        result[9] = (byte) 0x01;
        result[10] = (byte) responseParms.getSecurityKeyId();
        result[11] = (byte) 0x81;
        result[12] = (byte) 0x04;
        result[13] = initializationVector[0];
        result[14] = initializationVector[1];
        result[15] = initializationVector[2];
        result[16] = initializationVector[3];
        byteArrayList.write(result);
    }

    private int getRandomInteger(int min, int max, int valueToExclude) {
        int result = 0;
        SecureRandom random = new SecureRandom();
        do {
            result = random.nextInt();
        } while (result < min || result > max || valueToExclude == result);
        return result;
    }

    private void appendInteger(ByteArrayOutputStream byteArrayList, int elementCode, int value) {
        if (value != -1) {
            byteArrayList.write((byte) elementCode);
            if (value <= 0xFF) {
                byteArrayList.write((byte) 3);
                byteArrayList.write((byte) 0x02); // integer
                byteArrayList.write((byte) 1);
                byteArrayList.write((byte) value);
            } else if (value <= 0xFFFF) {
                byteArrayList.write((byte) 4);
                byteArrayList.write((byte) 0x02); // integer
                byteArrayList.write((byte) 2);
                byteArrayList.write((byte) (value >> 8)); // most significant byte first
                byteArrayList.write((byte) value);
            } else if (value <= 0xFFFFFF) {
                byteArrayList.write((byte) 5);
                byteArrayList.write((byte) 0x02); // integer
                byteArrayList.write((byte) 3);
                byteArrayList.write((byte) (value >> 16)); // most significant byte first
                byteArrayList.write((byte) (value >> 8));
                byteArrayList.write((byte) value);
            } else {
                byteArrayList.write((byte) 6);
                byteArrayList.write((byte) 0x02); // integer
                byteArrayList.write((byte) 4);
                byteArrayList.write((byte) (value >> 24)); // most significant byte first
                byteArrayList.write((byte) (value >> 16));
                byteArrayList.write((byte) (value >> 8));
                byteArrayList.write((byte) value);
            }
        }
    }

    private void appendUid(ByteArrayOutputStream byteArrayList, int elementCode, String value) throws IOException {
        if (value != null && value.length() > 0) {
            byte[] encodedId = encodeUid(value);
            byteArrayList.write((byte) elementCode);
            byteArrayList.write(encodeInteger(encodedId.length + 2));
            byteArrayList.write(value.startsWith(".") ? (byte) 0x80 : (byte) 0x06); // 0x80: Relative encoding <-> 0x06 Universal Absolute encoding
            byteArrayList.write(encodeInteger(encodedId.length));
            byteArrayList.write(encodedId);
        }
    }

    private void encodeAndAppendUid(ByteArrayOutputStream byteArrayList, String valueToEncode) {
        boolean firstPass = true;

        if (valueToEncode == null || valueToEncode.length() < 2) {
            throw new ApplicationException("Is null or Invalid Length");
        }

        StringTokenizer st = new StringTokenizer(valueToEncode, ".");
        while (st.hasMoreTokens()) {
            String value = st.nextToken();
            if (firstPass) {
                firstPass = false;
                if (!valueToEncode.startsWith("."))  // start with . means value is relative.
                {
                    if (!st.hasMoreTokens()) {
                        throw new ApplicationException("Invalid Value");
                    }
                    byteArrayList.write((byte) (Integer.parseInt(value) * 40 + Integer.parseInt(st.nextToken())));
                    if (!st.hasMoreTokens()) {
                        throw new ApplicationException("Invalid Value");
                    }
                    value = st.nextToken();
                }
            }

            int valueAsInt = Integer.parseInt(value);
            int mask = 0x7F << (3 * 7);
            if (mask <= valueAsInt) {
                throw new ApplicationException("Invalid Value");
            }

            int septet;
            for (int i = 3; i > 0; --i) {
                septet = mask & valueAsInt;
                mask >>= 7;
                if (septet != 0) {
                    septet >>= i * 7;
                    septet |= 0x80;
                    byteArrayList.write((byte) septet);
                }
            }
            septet = mask & valueAsInt;
            byteArrayList.write((byte) septet);
        }
    }

    private byte[] encodeUid(String valueToEncode) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        encodeAndAppendUid(result, valueToEncode);
        return result.toByteArray();
    }

    private byte[] longToByteArrayLittleEndian(long value) {
        return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array();
    }

    public static byte[] encodeInteger(int value) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        encodeAndAppendInteger(result, value);
        return result.toByteArray();
    }

    public static void encodeAndAppendInteger(ByteArrayOutputStream byteArrayList, int value) {
        if (value > 0xFFFFFF) {
            byteArrayList.write((byte) 0x84); // length of the length is four bytes, upper bit is set
            byteArrayList.write((byte) (value >> 24));
            byteArrayList.write((byte) (value >> 16));
            byteArrayList.write((byte) (value >> 8));
            byteArrayList.write((byte) (value));
        } else if (value >= 0xFFFF) {
            byteArrayList.write((byte) 0x83); // length of the length is three bytes, upper bit is set
            byteArrayList.write((byte) (value >> 16));
            byteArrayList.write((byte) (value >> 8));
            byteArrayList.write((byte) (value));
        } else if (value >= 0xFF) {
            byteArrayList.write((byte) 0x82); // length of the length is two bytes, upper bit is set
            byteArrayList.write((byte) (value >> 8));
            byteArrayList.write((byte) (value));
        } else if (value >= 0x7F) {
            byteArrayList.write((byte) 0x81); // length of the length is two bytes, upper bit is set
            byteArrayList.write((byte) (value));
        } else {
            byteArrayList.write((byte) (value)); // no length is given, size is as it is, upper bit is clear
        }
    }

    public EAXPrimeEncoder getEAXPrimeEncoder() {
        if (eaxPrimeEncoder == null) {
            eaxPrimeEncoder = new EAXPrimeEncoder(ProtocolTools.getBytesFromHexString(c1222Buffer.getSecurityKey(), ""));
        }
        return eaxPrimeEncoder;
    }
}