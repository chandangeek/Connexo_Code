Ext.define('Uni.property.form.PropertyHydrator', {
    extract: function(record) {
        return record.getData(true);
    },
    falseAndZeroChecker: function(value) {
        if (value.toString() == "false") {
            return false;
        }
        if (value.toString() == "0"){
           return 0;
        }
        return value || null
    },
    hydrate: function(data, record) {
        var values = data;
        var me = this;
        if (typeof record === 'undefined' || !record.properties()) {
            return false;
        }
        record.properties().each(function (property) {
            if (property.get('isInheritedOrDefaultValue') === true) {
                if (property.get('required') === true && property.get('hasDefaultValue')) {
                    var value = me.falseAndZeroChecker(values[property.get('key')]);
                    propertyValue = Ext.create('Uni.property.model.PropertyValue');
                    property.setPropertyValue(propertyValue);
                    propertyValue.set('value', value);
                } else {
                    property.setPropertyValue(null);
                }
            } else {
                var value = me.falseAndZeroChecker(values[property.get('key')]);
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