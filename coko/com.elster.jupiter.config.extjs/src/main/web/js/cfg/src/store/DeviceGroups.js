Ext.define('Cfg.store.DeviceGroups', {
    extend: 'Ext.data.Store',
    requires: [
        'Cfg.model.DeviceGroup'
    ],
    model: 'Cfg.model.DeviceGroup',
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
