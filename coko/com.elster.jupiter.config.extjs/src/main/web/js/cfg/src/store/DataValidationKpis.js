Ext.define('Cfg.store.DataValidationKpis', {
    extend: 'Ext.data.Store',
    requires: [
        'Cfg.model.DataValidationKpi'
    ],
    model: 'Cfg.model.DataValidationKpi',
    storeId: 'DataValidationKpis',
    proxy: {
        type: 'rest',
        url: '/api/val/kpis',
        reader: {
            type: 'json',
            root: 'kpis'
        }
    }
});
