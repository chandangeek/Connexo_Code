Ext.define('Imt.purpose.store.Readings', {
    extend: 'Ext.data.Store',
    model: 'Imt.purpose.model.Reading',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{mRID}/purposes/{purposeId}/outputs/{outputId}/channelData',
        reader: {
            type: 'json',
            root: 'channelData'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});