package com.energyict.mdc.engine.offline.gui.models;

import com.energyict.cbo.Quantity;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.engine.offline.core.LocalizableEnum;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.core.exception.BusinessException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 7/11/12
 * Time: 13:20
 */
public enum MultiplierMode implements LocalizableEnum {

    NONE(0, "multiplierMode.none") {
        @Override
        public Quantity applyTo(Quantity value, Date date, Channel channel) {
            return value;
        }
    },
    VERSIONED(1, "multiplierMode.versioned") {
        @Override
        public Quantity applyTo(Quantity rawValue, Date date, Channel channel) {
            Optional<BigDecimal> multiplier = channel.getDevice().getMultiplierAt(date.toInstant());
            return multiplier.isPresent() ? rawValue.multiply(multiplier.get()) : rawValue;
        }
    },
    CONFIGURED_ON_OBJECT(2, "multiplierMode.configuredOnObject") {
        @Override
        public Quantity applyTo(Quantity rawValue, Date date, Channel channel) {
            Optional<BigDecimal> multiplier = channel.getMultiplier(date.toInstant());
            return multiplier.isPresent() ? rawValue.multiply(multiplier.get()) : rawValue;
        }
    };

    private int code;
    private String nameKey;

    private MultiplierMode(int code, String nameKey) {
        this.code = code;
        this.nameKey = nameKey;
    }

    public int getCode() {
        return code;
    }

    public String getNameKey() {
        return nameKey;
    }

    public String getLocalizedName() {
        return TranslatorProvider.instance.get().getTranslator().getTranslation(getNameKey());
    }

    public static MultiplierMode fromDb(int code) throws BusinessException {
        switch (code) {
            case 0:
                return NONE;
            case 1:
                return VERSIONED;
            case 2:
                return CONFIGURED_ON_OBJECT;
            default:
                throw new BusinessException("noMultiplierModeDefinedForCodeX", "No MultiplierMode defined for code {0}", code);
        }
    }

    @Override // eg. for in Combo boxes
    public String toString() {
        return getLocalizedName();
    }

    public abstract Quantity applyTo(Quantity rawValue, Date date, Channel channel);
}
