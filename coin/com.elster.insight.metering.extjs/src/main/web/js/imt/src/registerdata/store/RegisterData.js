Ext.define('Imt.registerdata.store.RegisterData', {
    extend: 'Ext.data.Store',
    requires: [
               'Imt.registerdata2.model.RegisterData',
               'Imt.registerdata2.store.RegisterDataDurations'
    ],
    model: 'Imt.registerdata.model.RegisterData',
    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{mRID}/registers/{registerId}/data',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'data'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(params.mRID)).replace('{registerId}', params.registerId);
        },
//        pageParam: undefined,
//        startParam: undefined,
//        limitParam: undefined
    }
});

