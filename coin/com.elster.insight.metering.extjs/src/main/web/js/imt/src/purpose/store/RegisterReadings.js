Ext.define('Imt.purpose.store.RegisterReadings', {
    extend: 'Ext.data.Store',
    model: 'Imt.purpose.model.RegisterReading',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{mRID}/purposes/{purposeId}/outputs/{outputId}/registerData',
        reader: {
            type: 'json',
            root: 'registerData'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});