/**
 * @class Uni.model.search.Field
 */
Ext.define('Uni.model.search.Field', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'propertyName', type: 'string'},
        {name: 'type', type: 'string'},
        {name: 'displayValue', type: 'string'}
    ]
});