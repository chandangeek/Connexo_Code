Ext.define('Dsh.store.ConnectionResultsStore', {
    extend: 'Ext.data.Store',
    model: 'Dsh.model.ConnectionResults',
    proxy: {
        type: 'rest',
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