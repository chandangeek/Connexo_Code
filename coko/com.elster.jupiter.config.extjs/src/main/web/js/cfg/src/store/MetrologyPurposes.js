Ext.define('Cfg.store.MetrologyPurposes', {
    extend: 'Ext.data.Store',
    requires: [
        'Cfg.model.MetrologyContract'
    ],
    model: 'Cfg.model.MetrologyContract',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/val/field/purposes',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'metrologyPurposes'
        }
    }
});