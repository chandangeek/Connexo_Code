Ext.define('Dsh.store.ConnectionResultsStore', {
    extend: 'Ext.data.Store',
    model: 'Dsh.model.ConnectionResults',
    proxy: {
//        type: 'ajax',
        type: 'rest',
        url: '/api/dsr/connectionheatmap',
//        url: '../../apps/dashboard/app/fakeData/heatMapFake.json',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'heatMap'
        }
    }
});