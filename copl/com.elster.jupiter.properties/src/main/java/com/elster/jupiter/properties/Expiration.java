/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

public class Expiration {

    public enum Type {
        EXPIRED("expired") {
            long getExpirationPeriodEnd(Instant when) {
                return when.toEpochMilli();
            }
        },
        EXPIRES_1WEEK("expires_1week") {
            long getExpirationPeriodEnd(Instant when) {
                return when.plus(7, ChronoUnit.DAYS).toEpochMilli();
            }
        },
        EXPIRES_1MONTH("expires_1month") {
            long getExpirationPeriodEnd(Instant when) {
                LocalDateTime oneMonthLater = LocalDateTime.ofInstant(when, ZoneId.systemDefault()).plusMonths(1);
                return oneMonthLater.toInstant(ZoneOffset.UTC).toEpochMilli();
            }
        },
        EXPIRES_3MONTHS("expires_3months") {
            long getExpirationPeriodEnd(Instant when) {
                LocalDateTime threeMonthsLater = LocalDateTime.ofInstant(when, ZoneId.systemDefault()).plusMonths(3);
                return threeMonthsLater.toInstant(ZoneOffset.UTC).toEpochMilli();
            }
        },
        OBSOLETE("obsolete") {
            @Override
            long getExpirationPeriodEnd(Instant when) {
                throw new UnsupportedOperationException("called #getExpirationPeriodEnd for Obsolete filter");
            }
        };

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @JsonValue
        String getName() {
            return name;
        }

        @JsonIgnore
        abstract long getExpirationPeriodEnd(Instant when);

        @JsonCreator
        public static Expiration.Type fromString(String name) {
            return Stream.of(values())
                    .filter(type -> type.name.equals(name))
                    .findAny()
                    .orElse(null);
        }
    }

    private Type type;

    public Expiration(Type type) {
        this.type = type;
    }

    @JsonProperty
    public Type getType() {
        return type;
    }

    public Condition isExpired(String fieldName, Instant when) {
        return (Where.where(fieldName).isLessThanOrEqual(type.getExpirationPeriodEnd(when)));
    }

    public Condition isObsolete(String fieldName) {
        // keep in sync with com.elster.jupiter.pki.CertificateWrapperStatus.OBSOLETE#name
        return (Where.where(fieldName).isEqualToIgnoreCase("OBSOLETE"));
    }
}
