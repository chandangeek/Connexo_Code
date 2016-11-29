Ext.define('Imt.usagepointgroups.store.StaticGroupUsagePoints', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.search.Result',
    remoteFilter: true,
    pageSize: 200,
    buffered: true,
    proxy: {        
        type: 'ajax',
        reader: {
            type: 'json',
            root: 'searchResults'
        }
    }
});