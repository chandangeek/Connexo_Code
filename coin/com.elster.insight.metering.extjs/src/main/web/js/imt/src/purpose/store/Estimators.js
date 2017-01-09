Ext.define('Imt.purpose.store.Estimators', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.purpose.model.Estimator'
    ],
    model: 'Imt.purpose.model.Estimator',
    proxy: {
        type: 'rest',
        url: '/api/est/estimation/estimators',
        reader: {
            type: 'json',
            root: 'estimators',
            totalProperty: 'total'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
