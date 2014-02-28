Ext.define('Mtr.store.mock.Countries', {
    extend: 'Ext.data.Store',
    autoLoad: true,
    model: 'Mtr.model.mock.Country',
    proxy: {
        type: 'ajax',
        url: './resources/data/countries.json',
        reader: {
            type: 'json',
            root: 'countries'
        }
    }
});