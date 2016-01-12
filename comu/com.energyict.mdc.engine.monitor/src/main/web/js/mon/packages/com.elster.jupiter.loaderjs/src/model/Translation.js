/**
 * @class Ldr.model.Translation
 */
Ext.define('Ldr.model.Translation', {
    extend: 'Ext.data.Model',
    fields: [
        'cmp',
        'key',
        'value'
    ],
    idProperty: 'key'
});