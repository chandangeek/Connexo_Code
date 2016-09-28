Ext.define('Mdc.store.ValidationBlocks', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.ValidationBlock',
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/channels/{channelId}/datavalidationissues/{issueId}/validationblocks',
        timeout: 240000,
        pageParam: false,
        startParam: false,
        limitParam: false,

        reader: {
            type: 'json',
            totalProperty: 'total',
            root: 'validationBlocks',
            idProperty: 'startTime'
        }
    }
});
