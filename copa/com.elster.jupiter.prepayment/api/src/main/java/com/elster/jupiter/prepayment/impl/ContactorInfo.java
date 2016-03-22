package com.elster.jupiter.prepayment.impl;

import com.energyict.mdc.common.Unit;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Created by bvn on 9/18/15.
 */
public class ContactorInfo {

    public Status status;
    public Instant activationDate;
    public LoadLimit loadLimit;
    public Integer loadTolerance;
    public Integer[] tariffs;
    public String readingType;
    public String callback;

    public class LoadLimit {
        public BigDecimal limit;
        public String unit;

        public BigDecimal getLimit() {
            return limit;
        }

        public boolean shouldDisableLoadLimit() {
            return limit != null && limit.equals(BigDecimal.ZERO);
        }

        /**
         * Get the corresponding {@link Unit} for the given unitCode<br/>
         *
         * @return An Optional containing the Unit
         */
        public Optional<Unit> getUnit() {
            return Optional.ofNullable(Unit.get(unit));
        }
    }
}