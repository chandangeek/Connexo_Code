package com.energyict.protocolimpl.din19244.poreg2.request.register;

/**
 * Copyrights EnergyICT
 * Date: 9-mei-2011
 * Time: 9:42:35
 */
public class BankConfiguration {

    private int resultType;
    private int resultRenewal;
    private int tariffType;
    private int tariffIndex;
    private int resultLevel;
    private int bankId;
    private int[] channels = new int[8];

    public BankConfiguration(int bankId, int resultRenewal, int[] channels, int resultLevel, int resultType, int tariffIndex, int tariffType) {
        this.channels = channels;
        this.resultRenewal = resultRenewal;
        this.resultLevel = resultLevel;
        this.resultType = resultType;
        this.tariffIndex = tariffIndex;
        this.tariffType = tariffType;
        this.bankId = bankId;
    }

    public int getResultRenewal() {
        return resultRenewal;
    }

    public int[] getChannels() {
        return channels;
    }

    public int getResultLevel() {
        return resultLevel;
    }

    public int getResultType() {
        return resultType;
    }

    public int getTariffIndex() {
        return tariffIndex;
    }

    public int getTariffType() {
        return tariffType;
    }

    public boolean isTariff() {
        return (tariffType == 1);
    }

    public boolean isTotal() {
        return (tariffType == 0);
    }

    public int getBankId() {
        return bankId;
    }

    public boolean isTariffRate(int tariffRate) {
        if (tariffRate == 0) {
            return isTotal();
        }
        return isTariff() && (tariffIndex == (tariffRate - 1));
    }

    public boolean isResultLevel(int level) {
        return getResultLevel() == level;
    }

    public boolean isResultRenewal(int dField) {
        switch (resultRenewal) {
            case 2:
                return (dField == 8);
            case 3:
                return (dField == 6);
            default:
                return false;
        }
    }
}
