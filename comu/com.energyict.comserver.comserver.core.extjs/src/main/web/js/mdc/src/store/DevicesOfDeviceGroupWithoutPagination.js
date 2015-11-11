Ext.define('Mdc.store.DevicesOfDeviceGroupWithoutPagination', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.Device'
    ],

    model: 'Mdc.model.Device',
    storeId: 'DevicesOfDeviceGroup',

    proxy: {
        type: 'rest',
        url: '../../api/ddr/devicegroups/{id}/devices',
        reader: {
            type: 'json',
            root: 'devices'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});