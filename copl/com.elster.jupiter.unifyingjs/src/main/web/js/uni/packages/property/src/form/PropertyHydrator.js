Ext.define('Uni.property.form.PropertyHydrator', {
    extract: function(record) {
        return record.getData(true);
    },

    hydrate: function(data, record) {
        var values = data;
        if (typeof record === 'undefined' || !record.properties()) {
            return false;
        }
        record.properties().each(function (property) {
            if (property.get('isInheritedOrDefaultValue') === true) {
                if (property.get('required') === true && property.get('hasDefaultValue')) {
                    var value = values[property.get('key')] || null;
                    propertyValue = Ext.create('Uni.property.model.PropertyValue');
                    property.setPropertyValue(propertyValue);
                    propertyValue.set('value', value);
                } else {
                    property.setPropertyValue(null);
                }
            } else {
                var value = values[property.get('key')] || null;
                if (!property.raw['propertyValueInfo']) {
                    propertyValue = Ext.create('Uni.property.model.PropertyValue');
                    property.setPropertyValue(propertyValue);
                }

                var propertyValue = property.getPropertyValue();
                propertyValue.set('value', value);
            }
        });
    }
});