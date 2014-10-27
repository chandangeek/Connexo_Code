Ext.define('Dxp.store.FileFormatters', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.FileFormatter',

    data: [
        {name: 'standardCsv', displayValue: 'Standard CSV Exporter'}
    ]
});
