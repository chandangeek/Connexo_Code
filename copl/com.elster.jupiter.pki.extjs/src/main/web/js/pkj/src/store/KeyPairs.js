Ext.define('Pkj.store.KeyPairs', {
    extend: 'Ext.data.Store',
    model: 'Pkj.model.KeyPair',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/pir/keypairs',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'keypairs'
        }
    }
});