Ext.define('Mtr.store.mock.Browsers', {
    extend: 'Ext.data.Store',
    autoLoad: true,
    model: 'Mtr.model.mock.Browser',
    proxy: {
        type: 'ajax',
        url: './resources/data/browsers.json',
        reader: {
            type: 'json',
            root: 'rows'
        }
    }
});