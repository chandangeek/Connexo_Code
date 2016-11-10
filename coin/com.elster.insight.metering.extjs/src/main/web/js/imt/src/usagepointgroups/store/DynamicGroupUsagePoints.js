Ext.define('Imt.usagepointgroups.store.DynamicGroupUsagePoints', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.search.Result',
    remoteFilter: true,
    proxy: {        
        type: 'ajax',
        reader: {
            type: 'json',
            root: 'searchResults'
        }
    }
});