package com.energyict.genericprotocolimpl.elster.ctr;

/**
 * Copyrights EnergyICT
 * Date: 13-okt-2010
 * Time: 10:20:17
 */
public class ObisCodeMapper {

    private final GprsRequestFactory requestFactory;

    public ObisCodeMapper(GprsRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

    public GprsRequestFactory getRequestFactory() {
        return requestFactory;
    }
}
