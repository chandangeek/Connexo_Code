/**
 * @class Uni.model.Translation
 */
Ext.define('Uni.model.Translation', {
    extend: 'Ext.data.Model',
    fields: [
        'cmp',
        'key',
        'value'
    ],
    idProperty: 'key'
});