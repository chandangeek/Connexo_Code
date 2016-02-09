Ext.define('Sct.store.ServiceCallTypes', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    /*proxy: {
        type: 'rest',
        url: '/api/scs/servicecalltypes',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'sct'
        }
    }*/

    fields: [
        {name: 'type'},
        {name: 'versionName'},
        {name: 'status'},
        {name: 'loglevel'},
        {name: 'lifecycle'},
        {name: 'version', type: 'int'},
        {name: 'id', type: 'int'}
    ],
    data: [
        {type: 'SAP', versionName: '15', status: 'active', loglevel: 'Warning', lifecycle: 'default', version: '204', id: '2'}
    ]
});
