Ext.define('Sct.store.ServiceCallTypes', {
    extend: 'Ext.data.Store',
    //model: 'Sct.model.ServiceCallType',
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
        {type: 'SAP', versionName: '15', status: 'active', loglevel: 'Warning', lifecycle: 'default', version: '204', id: '2'},
        {type: 'SAP', versionName: '17', status: 'active', loglevel: 'Warning', lifecycle: 'default', version: '204', id: '3'},
        {type: 'SAP', versionName: '18', status: 'active', loglevel: 'Warning', lifecycle: 'default', version: '204', id: '4'},
        {type: 'SAP', versionName: '19', status: 'active', loglevel: 'Warning', lifecycle: 'default', version: '204', id: '5'},
        {type: 'SAP', versionName: '20', status: 'active', loglevel: 'Warning', lifecycle: 'default', version: '204', id: '6'},
        {type: 'SAP', versionName: '21', status: 'active', loglevel: 'Warning', lifecycle: 'default', version: '204', id: '7'},
        {type: 'SAP', versionName: '22', status: 'active', loglevel: 'Warning', lifecycle: 'default', version: '204', id: '8'},
        {type: 'SAP', versionName: '23', status: 'active', loglevel: 'Warning', lifecycle: 'default', version: '204', id: '9'},
        {type: 'SAP', versionName: '24', status: 'active', loglevel: 'Warning', lifecycle: 'default', version: '204', id: '10'},
        {type: 'SAP', versionName: '25', status: 'active', loglevel: 'Warning', lifecycle: 'default', version: '204', id: '11'},
        {type: 'SAP', versionName: '26', status: 'active', loglevel: 'Warning', lifecycle: 'default', version: '204', id: '12'},
        {type: 'SAP', versionName: '27', status: 'active', loglevel: 'Warning', lifecycle: 'default', version: '204', id: '13'},
        {type: 'SAP', versionName: '28', status: 'active', loglevel: 'Warning', lifecycle: 'default', version: '204', id: '14'},
        {type: 'SAP', versionName: '29', status: 'active', loglevel: 'Warning', lifecycle: 'default', version: '204', id: '15'},
        {type: 'SAP', versionName: '30', status: 'active', loglevel: 'Warning', lifecycle: 'default', version: '204', id: '16'},
        {type: 'SAP', versionName: '31', status: 'active', loglevel: 'Warning', lifecycle: 'default', version: '204', id: '17'}

    ]
});
