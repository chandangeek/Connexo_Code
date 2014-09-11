Ext.define('Dsh.store.CommunicationServerInfos', {
    extend: 'Ext.data.Store',
    storeId: 'CommunicationServerInfos',
    requires: ['Dsh.model.CommunicationServerInfo'],
    model: 'Dsh.model.CommunicationServerInfo',
    sorters: [{ property: 'running', direction: 'ASC' }]
});