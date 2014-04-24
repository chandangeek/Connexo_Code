package com.energyict.mdc.pluggable.rest.impl.properties;

import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TimeOfDay;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.pluggable.rest.impl.CodeTableInfo;
import com.energyict.mdc.pluggable.rest.impl.CodeTableResource;
import com.energyict.mdc.pluggable.rest.impl.LoadProfileTypeInfo;
import com.energyict.mdc.pluggable.rest.impl.LoadProfileTypeResource;
import com.energyict.mdc.pluggable.rest.impl.TimeZoneInUseInfo;
import com.energyict.mdc.pluggable.rest.impl.UserFileReferenceInfo;
import com.energyict.mdc.pluggable.rest.impl.UserFileReferenceResource;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.timezones.TimeZoneInUse;

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
     *
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
            } else if (UserFile.class.isAssignableFrom(property.getClass())) {
                info = new UserFileReferenceInfo((UserFile) property);
            } else if (LoadProfileType.class.isAssignableFrom(property.getClass())) {
                info = new LoadProfileTypeInfo((LoadProfileType) property);
            } else if (Password.class.isAssignableFrom(property.getClass())) {
                info = ((Password) property).getValue();
            } else if (HexString.class.isAssignableFrom(property.getClass())) {
                info = property.toString();
            } else if (TimeOfDay.class.isAssignableFrom(property.getClass())) {
                info = ((TimeOfDay) property).getSeconds();
            } else if (TimeDuration.class.isAssignableFrom(property.getClass())) {
                info = new TimeDurationInfo((TimeDuration) property);
            }
        }
        return info;
    }

    /**
     * Converts the given PropertySpec to a proper SimplePropertyType. If no match is found, then the given simplePropertyType will be returned.
     * </br>
     * <i>NOTE:</i> add conversion where necessary.
     *
     * @param propertySpec       the propertySpec to deduce a propertyType from
     * @param simplePropertyType the simplePropertyType to use if we could not convert it
     * @return the converted simplePropertyType
     */
    public static SimplePropertyType getReferencedSimplePropertyType(PropertySpec propertySpec, SimplePropertyType simplePropertyType) {
        if (HexString.class.isAssignableFrom(propertySpec.getValueFactory().getValueType())) {
            return SimplePropertyType.TEXT;
        } else if (TimeZoneInUse.class.isAssignableFrom(propertySpec.getValueFactory().getValueType())) {
            return SimplePropertyType.TIMEZONEINUSE;
        } else if (Code.class.isAssignableFrom(propertySpec.getValueFactory().getValueType())) {
            return SimplePropertyType.CODETABLE;
        } else if (UserFile.class.isAssignableFrom(propertySpec.getValueFactory().getValueType())) {
            return SimplePropertyType.USERFILEREFERENCE;
        } else if (LoadProfileType.class.isAssignableFrom(propertySpec.getValueFactory().getValueType())) {
            return SimplePropertyType.LOADPROFILETYPE;
        } else if (TimeDuration.class.isAssignableFrom(propertySpec.getValueFactory().getValueType())) {
            return SimplePropertyType.TIMEDURATION;
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
        } else if (UserFile.class.isAssignableFrom(propertyClassType)) {
            uri = uriInfo.getBaseUriBuilder().path(UserFileReferenceResource.class).path("/").build();
        } else if (LoadProfileType.class.isAssignableFrom(propertyClassType)) {
            uri = uriInfo.getBaseUriBuilder().path(LoadProfileTypeResource.class).path("/").build();
        }
        return uri;
    }
}
