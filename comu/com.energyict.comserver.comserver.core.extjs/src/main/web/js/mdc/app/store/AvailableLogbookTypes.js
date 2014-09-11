Ext.define('Mdc.store.AvailableLogbookTypes', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest',
        'Mdc.model.LogbookTypeOfDeviceType'
    ],
    model: 'Mdc.model.LogbookTypeOfDeviceType',
    storeId: 'AvailableLogbookTypes',
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/logbooktypes',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'logbookTypes'
        }
    }
});