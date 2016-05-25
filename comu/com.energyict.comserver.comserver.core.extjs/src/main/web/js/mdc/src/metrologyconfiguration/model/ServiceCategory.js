Ext.define('Mdc.metrologyconfiguration.model.ServiceCategory', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', mapping: 'name'},
        {name: 'name', mapping: 'displayName'}
    ]
});