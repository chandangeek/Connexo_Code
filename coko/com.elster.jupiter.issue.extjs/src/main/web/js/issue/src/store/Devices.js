Ext.define('Isu.store.Devices', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.Device',
    pageSize: 50,
    autoLoad: false
});