package com.elster.jupiter.prepayment.impl;

import com.elster.jupiter.util.units.Unit;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Created by bvn on 9/18/15.
 */
public class ContactorInfo {

    public BreakerStatus status;
    public Instant activationDate;
    public LoadLimit loadLimit;
    public Integer loadTolerance;
    public String callback;

    @Override
    public String toString() {
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("ContactorInfo{");
        msgBuilder.append("status: ").append(status.getDescription());
        if (activationDate != null) {
            msgBuilder.append(", activationDate: ").append(activationDate);
        }
        if (loadLimit != null) {
            msgBuilder.append(", loadLimit: ").append(loadLimit.limit).append(" ").append(loadLimit.unit);
        }
        if (loadTolerance != null) {
            msgBuilder.append(", loadTolerance: ").append(loadTolerance);
        }
        msgBuilder.append(", callBack: ").append(callback);
        msgBuilder.append("}");
        return msgBuilder.toString();
    }

    public class LoadLimit {
        public BigDecimal limit;
        public String unit;

        public BigDecimal getLimit() {
            return limit;
        }

        /**
         * Get the corresponding {@link Unit} for the given unitCode
         *
         * @return An Optional containing the Unit
         */
        public Optional<Unit> getUnit() {
            return Optional.ofNullable(unit != null ? Unit.get(unit) : null);
        }

        /**
         * @return true in case the load limit should be disabled, false otherwise
         */
        public boolean shouldDisableLoadLimit() {
            return limit.equals(BigDecimal.ZERO);
        }
    }
}