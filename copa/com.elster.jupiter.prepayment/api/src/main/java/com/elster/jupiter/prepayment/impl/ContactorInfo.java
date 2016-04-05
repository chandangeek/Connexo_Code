package com.elster.jupiter.prepayment.impl;

import com.energyict.mdc.common.Unit;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by bvn on 9/18/15.
 */
public class ContactorInfo {

    public BreakerStatus status;
    @XmlJavaTypeAdapter(JsonInstantAdapter.class)
    public Instant activationDate;
    public LoadLimit loadLimit;
    public Integer loadTolerance;
    public Integer[] tariffs;
    public String readingType;
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
        if (tariffs != null) {
            msgBuilder.append(", tariffs: ").append(Arrays.toString(tariffs));
        }
        if (readingType != null) {
            msgBuilder.append(", readingType: ").append(readingType);
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