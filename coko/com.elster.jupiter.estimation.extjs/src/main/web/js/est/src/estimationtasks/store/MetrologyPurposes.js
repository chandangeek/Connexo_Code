Ext.define('Est.estimationtasks.store.MetrologyPurposes', {
    extend: 'Ext.data.Store',
    requires: ['Est.estimationtasks.model.MetrologyPurpose'],
    model: 'Est.estimationtasks.model.MetrologyPurpose',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/est/field/purposes',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'metrologyPurposes'
        }
    }
});
