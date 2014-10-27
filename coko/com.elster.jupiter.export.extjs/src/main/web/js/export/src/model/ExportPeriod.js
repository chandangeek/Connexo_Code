Ext.define('Dxp.model.ExportPeriod', {
    extend: 'Ext.data.Model',
    fields: [
        'id', 'name'
    ],
    proxy: {
        type: 'rest',
        url: '/api/tmr/relativeperiods',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});