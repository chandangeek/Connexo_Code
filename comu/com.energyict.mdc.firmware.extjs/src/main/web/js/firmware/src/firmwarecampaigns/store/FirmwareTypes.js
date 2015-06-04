Ext.define('Fwc.firmwarecampaigns.store.FirmwareTypes', {
    extend: 'Ext.data.Store',
    model: 'Fwc.model.FirmwareType',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/fwc/field/firmwareTypes',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'firmwareTypes'
        }
    }
});