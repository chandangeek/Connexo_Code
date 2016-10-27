package com.energyict.cbo;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Provides mapping services for the JAXB marshalling mechanism
 * for the {@link com.energyict.cbo.Quantity} component.
 *
 * @author sva
 * @since 30/04/2014 - 17:14
 */
public class QuantityXmlMarshallAdapter extends XmlAdapter<QuantityXmlAdaptation, Quantity> {

    @Override
    public Quantity unmarshal (QuantityXmlAdaptation v) throws Exception {
        return new Quantity(v.amount, v.unit);
    }

    @Override
    public QuantityXmlAdaptation marshal (Quantity v) throws Exception {
        return new QuantityXmlAdaptation(v);
    }
}