package com.energyict.cbo;

import javax.xml.bind.annotation.XmlElement;
import java.math.BigDecimal;

/**
 * Adapter class for {@link com.energyict.cbo.Quantity} to enable xml marshalling.
 *
 * @author sva
 * @since 30/04/2014 - 17:14
 */
public class QuantityXmlAdaptation {

    @XmlElement
    public BigDecimal amount;

    @XmlElement
    public Unit unit;

    public QuantityXmlAdaptation() {
        super();
    }

    public QuantityXmlAdaptation(Quantity quantity) {
        this();
        this.amount = quantity.getAmount();
        this.unit = quantity.getUnit();
    }
}