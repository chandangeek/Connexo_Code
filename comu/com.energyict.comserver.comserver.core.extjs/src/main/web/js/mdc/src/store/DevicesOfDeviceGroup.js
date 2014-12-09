Ext.define('Mdc.store.DevicesOfDeviceGroup', {

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
        }
    }
});
