package com.energyict.protocolimplv2.umi.signature;

import com.energyict.protocolimplv2.umi.security.SecurityScheme;
import com.energyict.protocolimplv2.umi.types.Role;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public abstract class AppPacketSignatureGeneric extends AppPacketSignatureS0 {

    private final Optional<Role> role;       // 1 byte
    private final Optional<Date> validFrom;  // 4 bytes
    private final Optional<Date> validUntil; // 4 bytes

    protected AppPacketSignatureGeneric(SecurityScheme scheme, int length) {
        this(scheme, length, Optional.empty(), Optional.empty(), Optional.empty());
    }

    protected AppPacketSignatureGeneric(SecurityScheme scheme, int length, Role role) {
        this(scheme, length, Optional.of(role), Optional.empty(), Optional.empty());
    }

    protected AppPacketSignatureGeneric(SecurityScheme scheme, int length,
                                        Optional<Role> role,
                                        Optional<Date> from,
                                        Optional<Date> until) {
        super(scheme, length);
        this.role = role;
        this.validFrom = from;
        this.validUntil = until;

        if (this.role.isPresent())
            getRawBuffer().put((byte) this.role.get().getId());
        if (this.validFrom.isPresent())
            getRawBuffer().putInt((int)(TimeUnit.MILLISECONDS.toSeconds(validFrom.get().getTime())));
        if (this.validUntil.isPresent())
            getRawBuffer().putInt((int)(TimeUnit.MILLISECONDS.toSeconds(validUntil.get().getTime())));
    }

    protected AppPacketSignatureGeneric(byte[] rawSignature, int length, boolean roleSet, boolean validPeriodSet) {
        super(rawSignature, length);
        role = roleSet ? Optional.of(Role.fromId(getRawBuffer().get())) : Optional.empty();
        if (validPeriodSet) {
            validFrom = Optional.of(new Date(TimeUnit.SECONDS.toMillis(Integer.toUnsignedLong(getRawBuffer().getInt()))));
            validUntil = Optional.of(new Date(TimeUnit.SECONDS.toMillis(Integer.toUnsignedLong(getRawBuffer().getInt()))));
        } else {
            validFrom = validUntil = Optional.empty();
        }
    }

    protected Optional<Role> getOptionalRole() {
        return role;
    }

    protected Optional<Date> getOptionalValidFrom() {
        return validFrom;
    }

    protected Optional<Date> getOptionalValidUntil() {
        return validUntil;
    }

    protected abstract byte[] getToBeSigned();
}
