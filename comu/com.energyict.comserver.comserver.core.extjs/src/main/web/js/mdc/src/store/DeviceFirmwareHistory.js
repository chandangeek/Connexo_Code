Ext.define('Mdc.store.DeviceFirmwareHistory', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.DeviceFirmwareHistory',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mrid}/history/firmwarechanges',
        reader: {
            type: 'json',
            root: 'deviceFirmwareHistoryInfos'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{mrid}', params.mRID);
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});
