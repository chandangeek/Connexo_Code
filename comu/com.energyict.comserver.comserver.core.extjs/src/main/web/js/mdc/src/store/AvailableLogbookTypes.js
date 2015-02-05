Ext.define('Mdc.store.AvailableLogbookTypes', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest',
        'Mdc.model.LogbookTypeOfDeviceType'
    ],
    model: 'Mdc.model.LogbookTypeOfDeviceType',
    storeId: 'AvailableLogbookTypes',
    pageSize: 10,
    buffered: true,
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/logbooktypes',
        reader: {
            type: 'json',
            root: 'logbookTypes'
        }
    }
});