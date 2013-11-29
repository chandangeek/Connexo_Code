Ext.define('Mdc.model.ComPort', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'name',
        'comServer_id',
        'comPortType',
        'description',
        'active',
        'bound',
        'comPortPool_id',
        {name:'numberOfSimultaneousConnections',type:'int'},
        {name:'modificationDate',type: 'date',dateFormat:'time'}
//        'numberOfSimultaneousConnections',
//        'bound',

    ]
//    proxy: {
//        type: 'rest',
//        url: '../../api/mdc/comports',
//        reader: {
//            type: 'json'
//        }
//    }
});
