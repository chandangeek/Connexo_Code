Ext.define('Imt.servicecategories.model.ServiceCategory', {
    extend: 'Ext.data.Model',
    fields: ['name', 'displayName', 'meterRoles'],
    idProperty: 'name'
});