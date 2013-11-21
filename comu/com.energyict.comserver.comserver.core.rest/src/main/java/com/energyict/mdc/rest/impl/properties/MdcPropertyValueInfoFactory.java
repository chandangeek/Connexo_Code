package com.energyict.mdc.rest.impl.properties;

import com.energyict.mdc.rest.impl.CodeTableResource;
import com.energyict.mdc.rest.impl.TimeZoneInUseResource;
import com.energyict.mdc.rest.impl.properties.propertycontexts.CodeTableInfo;
import com.energyict.mdc.rest.impl.properties.propertycontexts.TimeZoneInUseInfo;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.TimeZoneInUse;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Copyrights EnergyICT
 * Date: 20/11/13
 * Time: 10:07
 */
public class MdcPropertyValueInfoFactory {

    public static Object asInfo(Object property) {
        Object info = property;
        if (property != null) {
            if (TimeZoneInUse.class.isAssignableFrom(property.getClass())) {
                info = new TimeZoneInUseInfo((TimeZoneInUse) property);
            } else if(Code.class.isAssignableFrom(property.getClass())){
                info = new CodeTableInfo((Code) property);
            }
        }
        return info;
    }

    public static URI getReferenceUriFor(UriInfo uriInfo, Class propertyClassType) {
        URI uri = null;
        if (TimeZoneInUse.class.isAssignableFrom(propertyClassType)) {
            // The TimeZoneInUse values are provided as possibleValues
        } else if(Code.class.isAssignableFrom(propertyClassType)){
            uri = uriInfo.getBaseUriBuilder().path(CodeTableResource.class).path("/").build();
        }
        return uri;
    }
}
