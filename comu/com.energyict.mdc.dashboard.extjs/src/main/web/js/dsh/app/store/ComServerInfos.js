Ext.define('Dsh.store.ComServerInfos', {
    extend: 'Ext.data.Store',
    storeId: 'ComServerInfos',
    requires: ['Dsh.model.ComServerInfo'],
    model: 'Dsh.model.ComServerInfo',
    pageSize: 10
});