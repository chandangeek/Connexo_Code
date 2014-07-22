Ext.define('Mdc.store.DeviceDataValidationRulesSet', {
    extend: 'Ext.data.Store',
    storeId: 'DeviceDataValidationRulesSet',
    requires: ['Mdc.model.DeviceDataValidationRulesSet'],
    model: 'Mdc.model.DeviceDataValidationRulesSet',
    pageSize: 10,
    autoSync: true
});