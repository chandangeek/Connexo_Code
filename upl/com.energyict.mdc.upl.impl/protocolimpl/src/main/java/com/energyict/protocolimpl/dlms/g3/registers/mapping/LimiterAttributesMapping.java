package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Limiter;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by H165680 on 6/16/2017.
 */
public class LimiterAttributesMapping extends RegisterMapping {
    private static final int MIN_ATTR = 4;
    private static final int MAX_ATTR = 7;

    public LimiterAttributesMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return Limiter.getDefaultObisCode().equalsIgnoreBAndEChannel(obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final Limiter limiter = getCosemObjectFactory().getLimiter();
        return parse(obisCode, readAttribute(obisCode, limiter));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, Limiter limiter) throws IOException {
        switch (obisCode.getB()) {
            case 4:
                return limiter.readThresholdNormal();
            case 6:
                return limiter.readMinOverThresholdDuration();
            case 7:
                return limiter.readMinUnderThresholdDuration();
            default:
                throw new NoSuchRegisterException("Limiter attribute [" + obisCode.getB() + "] not supported!");

        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getB()) {
            case 4:
                return new RegisterValue(obisCode, "Threshold normal: " + getNumericalValue(abstractDataType));
            case 6:
                return new RegisterValue(obisCode, "Min over threshold duration: " + abstractDataType.getUnsigned32().getValue());
            case 7:
                return new RegisterValue(obisCode, "Min under threshold duration: " + abstractDataType.getUnsigned32().getValue());
            default:
                throw new NoSuchRegisterException("Limiter attribute [" + obisCode.getB() + "] not supported!");

        }
    }

    public BigDecimal getNumericalValue(AbstractDataType abstractDataType) throws ProtocolException {
        if(abstractDataType.isNumerical()){
            return abstractDataType.toBigDecimal();
        } else {
            throw new ProtocolException("Only numerical values are supported");
        }
    }

}
