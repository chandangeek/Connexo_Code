package com.energyict.protocolimplv2.umi.packet;

import com.energyict.protocolimplv2.umi.util.Limits;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Optional;

public enum AppPacketType {
    /** Protocol Support */
    NO_OPERATION(0x00),
    NO_OPERATION_RESPONSE(0x01, false),

    COMPLETE(0x02),
    WAIT(0x03, false),
    ERROR(0xFF, false),

    /** Event Service */
    EVENT_SUBSCRIBE(0x20),
    EVENT_SUBSCRIBE_RESPONSE(0x21, false),

    EVENT_PUBLISH(0x22),
    EVENT_PUBLISH_RESPONSE(0x23, false),

    /** Object Service */
    READ_OBJECT(0x30),
    READ_OBJECT_RESPONSE(0x31, false),

    READ_OBJECT_PART(0x32),
    READ_OBJECT_PART_RESPONSE(0x33, false),

    WRITE_OBJECT(0x34),
    WRITE_OBJECT_RESPONSE(0x35, false),

    WRITE_OBJECT_PART(0x36),
    WRITE_OBJECT_PART_RESPONSE(0x37, false),

    SET_OBJECT_READ_ACCESS(0x3A),
    SET_OBJECT_READ_ACCESS_RESPONSE(0x3B, false),

    SET_OBJECT_WRITE_ACCESS(0x3C),
    SET_OBJECT_WRITE_ACCESS_RESPONSE(0x3D, false),

    GET_OBJECT_ACCESS(0x3E),
    GET_OBJECT_ACCESS_RESPONSE(0x3E, false),

    /** Image Service */
    IMAGE_START(0x50),
    IMAGE_START_RESPONSE(0x51, false),

    IMAGE_DATA(0x52),
    IMAGE_DATA_RESPONSE(0x53, false),

    IMAGE_END(0x54),
    IMAGE_END_RESPONSE(0x55, false),

    /** Tunnel Service */
    TUNNEL_DATA(0x60),
    TUNNEL_DATA_RESPONSE(0x61, false),

    TUNNEL_UMI(0x62),
    TUNNEL_UMI_RESPONSE(0x63, false),

    /** Security Service */
    S1_AUTH_REQUEST(0x90),
    S1_AUTH_REQUEST_RESPONSE(0x91, false),

    S1_AUTH_CHALLENGE(0x92),
    S1_AUTH_CHALLENGE_RESPONSE(0x93, false),

    S1_AUTH_REPLY(0x94),
    S1_AUTH_REPLY_RESPONSE(0x95, false),

    S1_AUTH_LOGOUT(0x96),
    S1_AUTH_LOGOUT_RESPONSE(0x97, false),

    S2_START_SESSION(0xA0),
    S2_START_SESSION_RESPONSE(0xA1, false),

    S2_END_SESSION(0xA2),
    S2_END_SESSION_RESPONSE(0xA3, false),

    S2_GET_CERTIFICATE(0xA4),
    S2_GET_CERTIFICATE_RESPONSE(0xA5, false),

    S2_ADD_CERTIFICATE(0XA6),
    S2_ADD_CERTIFICATE_RESPONSE(0XA7, false),

    S2_GENERATE_KEYPAIR(0xA8),
    S2_GENERATE_KEYPAIR_RESPONSE(0xA9, false),

    S2_GET_CSR(0xAA),
    S2_GET_CSR_RESPONSE(0xAB, false),

    S2_INSTALL_OWN_CERTIFICATE_CHAIN(0xAC),
    S2_INSTALL_OWN_CERTIFICATE_CHAIN_RESPONSE(0xAD, false),

    S2_REVOKE_CERTIFICATE(0xB0),
    S2_REVOKE_CERTIFICATE_RESPONSE(0xB1, false),

    S2_CHECK_REVOCATION(0xB2),
    S2_CHECK_REVOCATION_RESPONSE(0xB3, false),

    S2_REVOCATION_ANSWER(0xB4),
    S2_REVOCATION_ANSWER_RESPONSE(0xB5, false),

    S2_RELINQUISH_RIGHTS(0xB6),
    S2_RELINQUISH_RIGHTS_RESPONSE(0xB7, false);

    private final int id;
    private final boolean isCmd;

    AppPacketType(final int id) {
        this(id, true);
    }

    AppPacketType(final int id, boolean isCmd) {
        if (id < Limits.MIN_UNSIGNED || id > Limits.MAX_UNSIGNED_BYTE)
            throw new InvalidParameterException("expected value in range [" +
                    Limits.MIN_UNSIGNED + "," + Limits.MAX_UNSIGNED_BYTE + "]");
        this.id = id;
        this.isCmd = isCmd;
    }

    public int getId() {
        return id;
    }

    public boolean isCmd() {
        return isCmd;
    }

    public static AppPacketType fromId(int id) {
        Optional<AppPacketType> appPacketTypeOptional = Arrays.stream(AppPacketType.values())
                .filter(type -> (byte)type.getId() == (byte)id)
                .findFirst();
        if (appPacketTypeOptional.isPresent()) {
            return appPacketTypeOptional.get();
        }
        throw new InvalidParameterException("Unsupported application packet type: " + String.format("0x%02X", (byte)id));

        /*return Arrays.stream(AppPacketType.values())
                .filter(type -> (byte)type.getId() == (byte)id)
                .findFirst()
                .orElseThrow(() -> new InvalidParameterException("Unsupported application packet type: " + String.format("0x%02X", id)));*/
    }
}
