package com.energyict.mdc.protocol.api.device;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.LocalizableEnum;
import com.energyict.mdc.common.Quantity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
            DeviceMultiplier multiplier = channel.getDevice().getDeviceMultiplier(date);
            if (multiplier != null) {
                return rawValue.multiply(multiplier.getMultiplier());
            }
            else {
                return rawValue;
            }
        }
    },
    CONFIGURED_ON_OBJECT(2, "multiplierMode.configuredOnObject") {
        @Override
        public Quantity applyTo(Quantity rawValue, Date date, Channel channel) {
            BigDecimal multiplier = channel.getMultiplier();
            if (multiplier != null) {
                return rawValue.multiply(multiplier);
            }
            else {
                return rawValue;
            }
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
        return Environment.DEFAULT.get().getTranslation(getNameKey());
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

    public static List<MultiplierMode> getAll() {
        List<MultiplierMode> allUsageTypes = new ArrayList<>();
        allUsageTypes.add(NONE);
        allUsageTypes.add(VERSIONED);
        allUsageTypes.add(CONFIGURED_ON_OBJECT);
        return allUsageTypes;
    }

    public abstract Quantity applyTo(Quantity rawValue, Date date, Channel channel);

}