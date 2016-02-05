package com.elster.jupiter.cps.rest;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.ViewPrivilege;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomPropertySetInfo {
    public static final BiFunction<String, Object, ?> RAW_VALUES_IDENTITY = (key, raw) -> raw;

    public long id;
    public String customPropertySetId;
    public String name;
    public String domainName;
    public boolean isActive;
    public boolean isRequired;
    public boolean isVersioned;
    public boolean isEditable;
    public Set<ViewPrivilege> viewPrivileges;
    public Set<EditPrivilege> editPrivileges;
    public Set<ViewPrivilege> defaultViewPrivileges;
    public Set<EditPrivilege> defaultEditPrivileges;
    public List<CustomPropertySetAttributeInfo> properties;

    @JsonIgnore
    public CustomPropertySetValues getCustomPropertySetValues(BiFunction<String, Object, ?> rawValueConverter) {
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        if (properties != null) {
            for (CustomPropertySetAttributeInfo property : properties) {
                if (property.propertyValueInfo != null && property.propertyValueInfo.value != null) {
                    values.setProperty(property.key, rawValueConverter.apply(property.key, property.propertyValueInfo.value));
                }
            }
        }
        return values;
    }
}