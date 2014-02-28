Ext.define('Mtr.store.mock.Irregular', {
    extend: 'Ext.data.Store',
    autoLoad: true,
    model: 'Mtr.model.mock.Irregular',
    proxy: {
        type: 'ajax',
        url: './resources/data/irregular.json',
        reader: {
            type: 'json',
            root: 'rows'
        }
    }
});