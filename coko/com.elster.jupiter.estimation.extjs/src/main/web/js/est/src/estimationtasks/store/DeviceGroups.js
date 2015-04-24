Ext.define('Est.estimationtasks.store.DeviceGroups', {
    extend: 'Ext.data.Store',
    requires: ['Est.estimationtasks.model.DeviceGroup'],
    model: 'Est.estimationtasks.model.DeviceGroup',
    autoLoad: false,
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
