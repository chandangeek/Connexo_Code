/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mail.impl;

import com.elster.jupiter.mail.MailAddress;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Objects;

public final class MailAddressImpl implements MailAddress {

    private final Address internetAddress;

    private MailAddressImpl(Address internetAddress) {
        this.internetAddress = internetAddress;
    }

    public static MailAddress of(String mailAddress) throws AddressException {
        return new MailAddressImpl(new InternetAddress(mailAddress));
    }

    @Override
    public Address asAddress() {
        return internetAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MailAddressImpl that = (MailAddressImpl) o;
        return Objects.equals(internetAddress, that.internetAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(internetAddress);
    }

    @Override
    public String toString() {
        return internetAddress.toString();
    }

}