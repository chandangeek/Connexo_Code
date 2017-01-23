/*
 * RealTimeBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum.basepages;

import com.energyict.cbo.Quantity;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class RegisterBasePage {

    private Register register;
    BasePagesFactory basePagesFactory;
    private Quantity quantity;
    private Date selfReadDate = null;

    /** Creates a new instance of RealTimeBasePage */
    public RegisterBasePage(BasePagesFactory basePagesFactory) {
        this.basePagesFactory=basePagesFactory;
    }

    public void init() throws IOException {
        if (getRegister().getObisCode().getF() == RegisterFactory.PRESENT_REGISTERS) {
            setQuantity((Quantity)basePagesFactory.getRegisterDataBasePage().getQuantities().get(getRegister().getIndex()));
        }
        else if (getRegister().getObisCode().getF() == RegisterFactory.BILLING_REGISTERS) {
            setQuantity((Quantity)basePagesFactory.getRegisterDataSelfReadBasePage().getQuantities().get(getRegister().getIndex()));
            setSelfReadDate(basePagesFactory.getRegisterDataSelfReadBasePage().getSelfReadDate());
        }
        else if (getRegister().getObisCode().getF() == RegisterFactory.LAST_SEASON_REGISTERS) {
            setQuantity((Quantity)basePagesFactory.getRegisterDataLastSeasonBasePage().getQuantities().get(getRegister().getIndex()));
        }
    }

    public Register getRegister() {
        return register;
    }

    public void setRegister(Register register) {
        this.register = register;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }

    public Date getSelfReadDate() {
        return selfReadDate;
    }

    public void setSelfReadDate(Date selfReadDate) {
        this.selfReadDate = selfReadDate;
    }


} // public class RealTimeBasePage extends AbstractBasePage
