package com.energyict.cbo;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Provides mapping services for the JAXB marshalling mechanism
 * for the {@link com.energyict.cbo.Unit} component.
 *
 * @author sva
 * @since 16/04/2014 - 16:35
 */
public class UnitXmlMarshallAdapter extends XmlAdapter<UnitXmlAdaptation, Unit> {

    @Override
    public Unit unmarshal (UnitXmlAdaptation v) throws Exception {
        return Unit.get(v.code, v.scale);
    }

    @Override
    public UnitXmlAdaptation marshal (Unit v) throws Exception {
        return new UnitXmlAdaptation(v);
    }

}