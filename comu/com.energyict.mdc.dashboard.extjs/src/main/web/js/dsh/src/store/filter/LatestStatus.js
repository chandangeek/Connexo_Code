Ext.define('Dsh.store.filter.LatestStatus', {
    extend: 'Ext.data.Store',
    fields: ['localizedValue', 'successIndicator'],
    autoLoad: true,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/connectiontasksuccessindicators',
        reader: {
            type: 'json',
            root: 'successIndicators'
        }
    }
});

