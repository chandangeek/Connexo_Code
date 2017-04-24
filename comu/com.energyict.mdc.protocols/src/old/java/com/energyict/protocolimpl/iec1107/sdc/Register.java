/*
 * Register.java
 *
 * Created on 27 oktober 2004, 18:15
 */

package com.energyict.protocolimpl.iec1107.sdc;

import com.energyict.cbo.Quantity;

import java.util.Date;
/**
 *
 * @author  Koen
 */
public class Register {

    private static final int ACTIVE_ENERGY_TARIFF_HV = 67;
    private static final int ACTIVE_ENERGY_TARIFF_HP = 68;
    private static final int ACTIVE_ENERGY_TARIFF_HC = 69;
    private static final int ACTIVE_ENERGY_TARIFF_HSV = 70;

    private static final int REACTIVE_ENERGY_INDUCTIVE_TARIFF_HV = 71;
    private static final int REACTIVE_ENERGY_INDUCTIVE_TARIFF_HFV = 72;
    private static final int REACTIVE_ENERGY_CAPACITIVE_TARIFF_HV = 73;
    private static final int REACTIVE_ENERGY_CAPACITIVE_TARIFF_HFV = 74;

    private static final int ACTIVE_ENERGY_MAXIMUM_DEMAND_TARIFF_HV = 75;
    private static final int ACTIVE_ENERGY_MAXIMUM_DEMAND_TARIFF_HFV = 76;

    private static final int REACTIVE_ENERGY_MD_INDUCTIVE_TARIFF_HV = 77;
    private static final int REACTIVE_ENERGY_MD_INDUCTIVE_TARIFF_HFV = 78;
    private static final int REACTIVE_ENERGY_MD_CAPACITIVE_TARIFF_HV = 79;
    private static final int REACTIVE_ENERGY_MD_CAPACITIVE_TARIFF_HFV = 80;

    private static final int ACTIVE_ENERGY_TOTAL = 81;
    private static final int REACTIVE_ENERGY_INDUCTIVE_TOTAL = 82;
    private static final int REACTIVE_ENERGY_CAPACITIVE_TOTAL = 83;

    private static final int TI_RATIO = 90;
    private static final int SERIAL_NUMBER = 99;


    int type;
    Quantity quantity;
    Date mdTimestamp;
    Date billingTimestamp;

    /** Creates a new instance of Register */
    public Register(int type, Quantity quantity, Date mdTimestamp, Date billingTimestamp) {
        this.type=type;
        this.quantity=quantity;
        this.mdTimestamp=mdTimestamp;
        this.billingTimestamp=billingTimestamp;
    }

    public Register( int type, Quantity quantity, Date mdTimeStamp){
    	this.type = type;
    	this.quantity = quantity;
    	this.mdTimestamp = mdTimeStamp;
    	this.billingTimestamp = null;
    }

    public String toString() {
        return getTypeDescription()+", "+getQuantity()+(mdTimestamp==null?"":", MD:"+mdTimestamp)+(billingTimestamp==null?"":", BP:"+billingTimestamp);
    }

    private String getTypeDescription() {
       switch(type) {

       		case ACTIVE_ENERGY_TARIFF_HV:
       			return "Active Energy tariff HV";

       		case ACTIVE_ENERGY_TARIFF_HP:
       			return "Active Energy tariff HP";

       		case ACTIVE_ENERGY_TARIFF_HC:
       			return "Active Energy tariff HC";

       		case ACTIVE_ENERGY_TARIFF_HSV:
       			return "Active Energy tariff HSV";

       		case REACTIVE_ENERGY_INDUCTIVE_TARIFF_HV:
       			return "Reactive Energy tariff HV";

       		case REACTIVE_ENERGY_INDUCTIVE_TARIFF_HFV:
       			return "Reactive Energy tariff HFV";

       		case REACTIVE_ENERGY_CAPACITIVE_TARIFF_HV:
       			return "Reactive Energy tariff HV";

       		case REACTIVE_ENERGY_CAPACITIVE_TARIFF_HFV:
       			return "Reactive Energy tariff HFV";

       		case ACTIVE_ENERGY_MAXIMUM_DEMAND_TARIFF_HV:
       			return "Active Energy maximum demand tariff HV";

       		case ACTIVE_ENERGY_MAXIMUM_DEMAND_TARIFF_HFV:
       			return "Active Energy maximun demand tariff HFV";

       		case REACTIVE_ENERGY_MD_INDUCTIVE_TARIFF_HV:
       			return "Reactive Energy MD inductive tariff HV";

       		case REACTIVE_ENERGY_MD_INDUCTIVE_TARIFF_HFV:
       			return "Reactive Energy MD inductive tariff HFV";

       		case REACTIVE_ENERGY_MD_CAPACITIVE_TARIFF_HV:
       			return "Reactive Energy MD capacitive tariff HV";

       		case REACTIVE_ENERGY_MD_CAPACITIVE_TARIFF_HFV:
       			return "Reactive Energy MD capacitive tariff HFV";

       		case ACTIVE_ENERGY_TOTAL:
       			return "Active Energy total";

       		case REACTIVE_ENERGY_INDUCTIVE_TOTAL:
       			return "Reactive Energy inductive total";

       		case REACTIVE_ENERGY_CAPACITIVE_TOTAL:
       			return "Reactive Energy capacitive total";

       		case TI_RATIO:
       			return "TI ratio";

       		case SERIAL_NUMBER:
       			return "Serialnumber";

           default:
               return "unknown registertype "+type;
       }
    }

    /**
     * Getter for property type.
     * @return Value of property type.
     */
    public int getType() {
        return type;
    }

    /**
     * Getter for property quantity.
     * @return Value of property quantity.
     */
    public Quantity getQuantity() {
        return quantity;
    }

    /**
     * Getter for property mdTimestamp.
     * @return Value of property mdTimestamp.
     */
    public java.util.Date getMdTimestamp() {
        return mdTimestamp;
    }

    /**
     * Getter for property billingTimeStamp.
     * @return Value of property billingTimeStamp.
     */
    public java.util.Date getBillingTimestamp() {
        return billingTimestamp;
    }

}
