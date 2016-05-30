Ext.define('Imt.purpose.store.Outputs', {
    extend: 'Ext.data.Store',
    model: 'Imt.purpose.model.Output',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{mRID}/purposes/{purposeId}/outputs',
        reader: {
            type: 'json',
            root: 'outputs'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});