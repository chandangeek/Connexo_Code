Ext.define('Mtr.store.mock.Stocks', {
    extend: 'Ext.data.Store',
    autoLoad: true,
    model: 'Mtr.model.mock.Stock',
    proxy: {
        type: 'ajax',
        url: './resources/data/stocks.json',
        reader: {
            type: 'json',
            root: 'rows'
        }
    }
});