Ext.define('Uni.property.form.PropertyHydrator', {
    extract: function (record) {
        return record.getData(true);
    },
    hydrate: function (data, record) {
        var values = data;
        if (typeof record === 'undefined' || !record.properties()) {
            return false;
        }
        record.beginEdit();
        record.properties().each(function (property) {
            var value,
                propertyValue;

            if (property.get('isInheritedOrDefaultValue') === true && property.get('hasDefaultValue') === true) {
                value = values[property.get('key')];
                propertyValue = Ext.create('Uni.property.model.PropertyValue');
                property.setPropertyValue(propertyValue);
                propertyValue.set('value', value);
                propertyValue.set('defaultValue', property.get('default'));
            } else {
                value = values[property.get('key')];
                if (!property.raw['propertyValueInfo']) {
                    propertyValue = Ext.create('Uni.property.model.PropertyValue');
                    propertyValue.set('value', value);
                    property.setPropertyValue(propertyValue);
                }
                propertyValue = property.getPropertyValue();
                propertyValue.set('value', value);
                propertyValue.set('propertyHasValue', property.get('hasValue'));
            }
        });
        record.endEdit();
    }
});