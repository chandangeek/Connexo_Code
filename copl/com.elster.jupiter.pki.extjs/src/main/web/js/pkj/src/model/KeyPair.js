Ext.define('Pkj.model.KeyPair', {
    extend: 'Uni.model.Version',
    fields: [
        'alias',
        {name: 'hasPublicKey', type: 'boolean'},
        {name: 'hasPrivateKey', type: 'boolean'},
        'keyEncryptionMethod',
        'keyType'
    ],
    proxy: {
        type: 'rest',
        url: '/api/pir/keypairs',
        reader: {
            type: 'json'
        }
    }
});