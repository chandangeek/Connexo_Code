Ext.define('Cfg.store.DeviceGroups', {
    extend: 'Ext.data.Store',
    requires: [
        'Cfg.model.DeviceGroup'
    ],
    model: 'Cfg.model.DeviceGroup',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/val/metergroups',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'devicegroups'
        }
    }
});
