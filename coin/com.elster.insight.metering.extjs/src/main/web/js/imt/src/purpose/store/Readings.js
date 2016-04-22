Ext.define('Imt.purpose.store.Readings', {
    extend: 'Ext.data.Store',
    model: 'Imt.purpose.model.Reading',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{mRID}/purposes/{purposeId}/outputs/{outputId}/data',
        reader: {
            type: 'json',
            root: 'data'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});