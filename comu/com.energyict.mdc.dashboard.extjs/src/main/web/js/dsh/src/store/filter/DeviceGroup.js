Ext.define('Dsh.store.filter.DeviceGroup', {
    extend: 'Ext.data.Store',
    requires: ['Dsh.model.DeviceGroup'],
    model: 'Dsh.model.DeviceGroup',

    proxy: {
        type: 'rest',
        url: '/api/ddr/devicegroups',

        reader: {
            type: 'json',
            root: 'devicegroups'
        }
    }
});

