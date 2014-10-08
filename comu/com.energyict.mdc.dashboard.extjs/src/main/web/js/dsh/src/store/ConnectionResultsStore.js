Ext.define('Dsh.store.ConnectionResultsStore', {
    extend: 'Ext.data.Store',
    model: 'Dsh.model.ConnectionResults',
    autoLoad: false,

    proxy: {
        type: 'ajax',
        url: '/api/dsr/connectionheatmap',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'heatMap'
        }
    }
});