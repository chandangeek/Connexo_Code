Ext.define('Dsh.store.CommunicationResultsStore', {
    extend: 'Ext.data.Store',
    model: 'Dsh.model.ConnectionResults',
    autoLoad: false,

    proxy: {
        type: 'ajax',
        url: '/api/dsr/communicationheatmap',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'heatMap'
        }
    }
});