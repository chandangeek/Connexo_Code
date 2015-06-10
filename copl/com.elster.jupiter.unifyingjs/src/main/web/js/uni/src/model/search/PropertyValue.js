/**
 * @class Uni.model.search.PropertyValue
 */
Ext.define('Uni.model.search.PropertyValue', {
    extend: 'Ext.data.Model',

    idProperty: 'displayValue',

    fields: [
        {
            name: 'id', type: 'auto', convert: function (v, record) {
            var result = v;
            
            if (!Ext.isDefined(result)) {
                result = record.raw.displayValue;
            }

            return result;
        }
        },
        {name: 'displayValue', type: 'string'}
    ]
});