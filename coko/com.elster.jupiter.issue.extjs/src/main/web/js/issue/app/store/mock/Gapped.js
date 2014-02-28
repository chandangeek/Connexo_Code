Ext.define('Mtr.store.mock.Gapped', {
    extend: 'Ext.data.Store',
    autoLoad: true,
    model: 'Mtr.model.mock.Irregular',
    proxy: {
        type: 'ajax',
        url: './resources/data/gapped.json',
        reader: {
            type: 'json',
            root: 'rows'
        }
    }
});