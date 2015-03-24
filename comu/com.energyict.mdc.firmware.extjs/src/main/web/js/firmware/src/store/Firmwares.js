Ext.define('Fwc.store.Firmwares', {
    extend: 'Uni.data.store.Filterable',
    requires: [
        'Fwc.model.Firmware'
    ],
    model: 'Fwc.model.Firmware',
    storeId: 'Firmwares',
    autoLoad: false,
    remoteSort: true,
    sorters: [{
        property: 'version',
        direction: 'DESC'
    }]
});
