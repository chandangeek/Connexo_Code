Ext.define('Fwc.store.DeviceGroups', {
    extend: 'Ext.data.Store',
    model: 'Fwc.model.DeviceGroup',

    proxy: {
        type: 'rest',
        url: '/api/ddr/devicegroups',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'devicegroups'
        }
    }
});