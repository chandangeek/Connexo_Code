/**
 * @class Ldr.model.Preference
 */
Ext.define('Ldr.model.Preference', {
    extend: 'Ext.data.Model',
    fields: [
        'key',
        'value'
    ],
    idProperty: 'key'
});