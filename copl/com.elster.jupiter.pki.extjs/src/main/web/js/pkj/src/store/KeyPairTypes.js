Ext.define('Pkj.store.KeyPairTypes', {
    extend: 'Ext.data.Store',
    model: 'Pkj.model.CertificateType',

    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/pir/keytypes/asymmetric',
        reader: {
            type: 'json',
            root: 'keyTypes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
