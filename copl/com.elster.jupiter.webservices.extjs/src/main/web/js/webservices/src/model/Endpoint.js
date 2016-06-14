Ext.define('Wss.model.Endpoint', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'number'},
        {name: 'version', type: 'number'},
        {name: 'name', type: 'string'},
        {name: 'webServiceName', type: 'string'},
        {name: 'url', type: 'string'},
        {name: 'logLevel', type: 'auto'},
        {name: 'tracing', type: 'boolean'},
        {name: 'httpCompression', type: 'boolean'},
        {name: 'schemaValidation', type: 'boolean'},
        {name: 'active', type: 'boolean'},
        {name: 'authenticated', type: 'boolean'},
        {name: 'username', type: 'string'},
        {name: 'password', type: 'string'},
        {name: 'type', type: 'string'},
        {name: 'direction', type: 'auto'}

    ],

    proxy: {
        type: 'rest',
        url: '/api/ws/endpointconfigurations',
        reader: {
            type: 'json'
        }
    }
});