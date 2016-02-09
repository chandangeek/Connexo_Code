Ext.define('Isu.store.DeviceGroups', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.DeviceGroup',
    autoLoad: false,

    proxy: {
        type: 'rest',
        pageParam: false,
        startParam: false,
        limitParam: false,
        url: '/api/isu/devicegroups',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
