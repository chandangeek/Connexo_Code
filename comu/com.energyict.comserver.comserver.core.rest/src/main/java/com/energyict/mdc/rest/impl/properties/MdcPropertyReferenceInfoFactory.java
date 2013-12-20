package com.energyict.mdc.rest.impl.properties;

import com.energyict.mdc.common.HexString;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.rest.impl.CodeTableResource;
import com.energyict.mdc.rest.impl.CodeTableInfo;
import com.energyict.mdc.rest.impl.TimeZoneInUseInfo;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.TimeZoneInUse;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Provides functionality for property 'Reference' objects
 * <p/>
 * Copyrights EnergyICT
 * Date: 20/11/13
 * Time: 10:07
 */
public class MdcPropertyReferenceInfoFactory {

    /**
     * Creates an 'xxxInfo' object from a given BusinessObject
     * @param property the BusinessObject
     * @return the Info version of the BusinessObject
     */
    public static Object asInfoObject(Object property) {
        Object info = property;
        if (property != null) {
            if (TimeZoneInUse.class.isAssignableFrom(property.getClass())) {
                info = new TimeZoneInUseInfo((TimeZoneInUse) property);
            } else if (Code.class.isAssignableFrom(property.getClass())) {
                info = new CodeTableInfo((Code) property);
            }
        }
        return info;
    }

    /**
     * Converts the given PropertySpec to a proper SimplePropertyType. If no match is found, then the given simplePropertyType will be returned.
     * </br>
     * <i>NOTE:</i> add conversion where necessary.
     *
     * @param propertySpec the propertySpec to deduce a propertyType from
     * @param simplePropertyType the simplePropertyType to use if we could not convert it
     * @return the converted simplePropertyType
     */
    static SimplePropertyType getReferencedSimplePropertyType(PropertySpec propertySpec, SimplePropertyType simplePropertyType) {
        if (HexString.class.isAssignableFrom(propertySpec.getValueFactory().getValueType())) {
            return SimplePropertyType.TEXT;
        } else if (TimeZoneInUse.class.isAssignableFrom(propertySpec.getValueFactory().getValueType())) {
            return SimplePropertyType.TIMEZONEINUSE;
        } else if (Code.class.isAssignableFrom(propertySpec.getValueFactory().getValueType())) {
            return SimplePropertyType.CODETABLE;
        } else {
            return simplePropertyType;
        }
    }

    /**
     * Creates a proper URI to fetch the <i>full</i> list of the BusinessObjects of the given class
     *
     * @param uriInfo           the URI info which was used for the REST call
     * @param propertyClassType the classTypeName of the object
     * @return the uri to fetch the list of objects
     */
    public static URI getReferenceUriFor(UriInfo uriInfo, Class propertyClassType) {
        URI uri = null;
        if (TimeZoneInUse.class.isAssignableFrom(propertyClassType)) {
            // The TimeZoneInUse values are provided as possibleValues
        } else if (Code.class.isAssignableFrom(propertyClassType)) {
            uri = uriInfo.getBaseUriBuilder().path(CodeTableResource.class).path("/").build();
        }
        return uri;
    }
}
