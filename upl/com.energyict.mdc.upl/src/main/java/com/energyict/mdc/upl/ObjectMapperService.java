package com.energyict.mdc.upl;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Provides services for mapping objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-05 (16:18)
 */
public interface ObjectMapperService {
    ObjectMapper newJacksonMapper();
}