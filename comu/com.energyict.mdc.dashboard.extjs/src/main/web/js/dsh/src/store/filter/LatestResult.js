Ext.define('Dsh.store.filter.LatestResult', {
    extend: 'Ext.data.Store',
    fields: ['localizedValue', 'successIndicator'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/comsessionsuccessindicators',
        reader: {
            type: 'json',
            root: 'successIndicators'
        }
    }
});

