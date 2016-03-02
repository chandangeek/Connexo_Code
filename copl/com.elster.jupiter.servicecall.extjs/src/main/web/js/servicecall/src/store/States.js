Ext.define('Scs.store.States', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['displayValue', 'id'],
    proxy: {
        type: 'rest',
        url: '/api/scs/field/states',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'states'
        }
    }
});
