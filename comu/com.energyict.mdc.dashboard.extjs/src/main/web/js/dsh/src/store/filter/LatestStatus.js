Ext.define('Dsh.store.filter.LatestStatus', {
    extend: 'Ext.data.Store',
    fields: ['localizedValue', 'successIndicator'],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/connectiontasksuccessindicators',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'successIndicators'
        }
    }
});

