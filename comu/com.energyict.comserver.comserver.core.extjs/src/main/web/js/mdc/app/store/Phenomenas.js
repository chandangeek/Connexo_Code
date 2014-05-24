Ext.define('Mdc.store.Phenomenas', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.UnitOfMeasure'
    ],

    model: 'Mdc.model.UnitOfMeasure',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/mds/phenomena',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});