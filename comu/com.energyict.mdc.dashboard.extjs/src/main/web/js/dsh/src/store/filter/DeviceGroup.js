Ext.define('Dsh.store.filter.DeviceGroup', {
    extend: 'Ext.data.Store',
    requires: ['Dsh.model.DeviceGroup'],
    model: 'Dsh.model.DeviceGroup',

    proxy: {
        type: 'rest',
        url: '/api/ddr/devicegroups',
        pageParam: undefined,
        limitParam: undefined,
        startParam: undefined,

        reader: {
            type: 'json',
            root: 'devicegroups'
        }
    }
});

