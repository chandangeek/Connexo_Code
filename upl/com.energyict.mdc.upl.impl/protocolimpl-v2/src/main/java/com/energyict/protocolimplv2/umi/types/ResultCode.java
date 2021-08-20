package com.energyict.protocolimplv2.umi.types;

import com.energyict.protocolimplv2.umi.packet.AppPacketType;
import com.energyict.protocolimplv2.umi.util.Limits;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Optional;

public enum ResultCode {
    /** Application Services Result Codes according UMI-S-001 */
    OK(0, "The command succeeded and the result (if any) is in the packet payload field"),
    UNKNOWN_VERSION(1, "The device does not support the application layer version number in the command"),
    UNKNOWN_PACKET_TYPE(2, "Unknown packet type"),
    INVALID_PACKET_FORMAT(3, "The packet format is incorrect (e.g., too short or too long) for the packet type"),
    UNKNOWN_OBJECT(4, "The server does not recognise the UMI Code"),
    UNKNOWN_MEMBER(5, "The requested member is invalid for the specified object"),
    UNKNOWN_ELEMENT(6, "The requested element number is invalid for the specified object"),
    NOT_IMPLEMENTED(7, "The object does not support the method requested"),
    TOO_LONG(8, "The server cannot assemble a response because the response will not fit into a " +
            "single application packet"),
    ACCESS_DENIED(9,"The client is not allowed to access the specified object. The access control rules are " +
            "device specific"),
    UNAVAILABLE(10, "The device is currently unable to complete the application response. Try again later"),
    INVALID_LENGTH(11, "The length of the object value field is incorrect"),
    INVALID_VALUE(12, "The value is invalid for the object being written"),
    TRANSFER_RESTARTED(13, "An image transfer has started but the previous transfer did not complete"),
    IMAGE_PROCESSING_ERROR(14, "An application error occurred during an image transfer"),
    UNKNOWN_EVENT(15, "The event is not available on the Publisher"),
    APPLICATION_ERROR(16, "Application-specific error"),
    NO_SESSION(17, "An encrypted packet was received that cannot be decrypted"),
    NO_ENCRYPTION(18, "An unencrypted packet was received that should be encrypted"),
    UNKNOWN_SIGNATURE_SCHEME(19, "Command, Response or Image Signature Scheme is not recognised"),
    UNKNOWN_ENCRYPTION_SCHEME(20, "Encryption Scheme field in packet header is not recognised"),
    DEFER_ERROR(21, "A deferred response could not be completed"),
    WRONG_IMAGE_TARGET(22, "UMI device has received an inappropriate Image"),
    BAD_IMAGE(23, "Target UMI Device rejects this Image at application layer"),
    UNKNOWN_TRANSPORT_KEY_ID(24, "Target UMI Device does not have a Transport Key for this Transport Key ID"),

    /** Security Result Codes according to UMI-S-004 */
    INVALID_SIGNATURE(128, "Invalid signature"),
    AUTHENTICATION_FAILED(129, "Authentication failed. The Supplicant has Role 0"),
    NO_PUBLIC_KEY(130, "Public key unavailable"),
    NO_CERTIFICATE(131, "Certificate unavailable"),
    KEYPAIR_ALREADY_GENERATED(132, "Keypair already generated"),
    ALREADY_INSTALLED(133, "A certificate chain has already been installed for this device and the " +
            "manufacturer has not permitted certificate chain updates"),
    REVOCATION_POINT_UNAVAILABLE(134, "The receiving UMI Device knows that it will not try and " +
            "reach the requested revocation point because it has no existing (and available) addressing " +
            "scheme, route, or device to pass this message onto"),
    REVOCATION_POINT_UNKNOWN(135, "A Revocation message has arrived, but the receiver has " +
            "not seen a ‘Revocation Signing’ Certificate that allow it to trust the revocation point"),
    NO_CURRENT_RIGHTS(136, "A UMI Device has sent a Relinquish Rights message to another UMI device, but " +
            "does not appear in the key table and has no rights to relinquish"),
    UNKNOWN_ISSUER(137, "A certificate has been presented but the issuer is not recognised or is untrusted/revoked"),
    OUT_OF_REVOCATION_MEMORY(138, "A UMI Device has received an “S2 revoke certificate” command but has no room " +
            "to store it and cannot make room to store it by deleting entries. The revoked party will be removed from the " +
            "Key Table if it exists but the revocation will not be retained"),
    INVALID_CERTIFICATE_FORMAT(139, "Device finds a format error in the received Certificate"),
    INVALID_CERTIFICATE_SIGNATURE(140, "Device finds a signature error in the received Certificate"),
    INVALID_CERTIFICATE(141, "Device finds some other error in the received Certificate"),
    KEY_TABLE_FULL(142, "Key table is full");

    private final int id;
    private final String description;

    ResultCode(final int id, final String description) {
        if (id < Limits.MIN_UNSIGNED || id > Limits.MAX_UNSIGNED_BYTE)
            throw new InvalidParameterException("expected value in range [" +
                    Limits.MIN_UNSIGNED + "," + Limits.MAX_UNSIGNED_BYTE + "]");
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return this.description;
    }

    public static ResultCode[] resultCodes = ResultCode.values();

    public static ResultCode fromId(int id) {
        Optional<ResultCode> resultCodeOptional = Arrays.stream(ResultCode.values())
                .filter(code -> (byte)code.getId() == (byte)id)
                .findFirst();
        if (resultCodeOptional.isPresent()) {
            return resultCodeOptional.get();
        }
        throw new InvalidParameterException("Unsupported result code: " + String.format("0x%02X", (byte)id));
    }
}
