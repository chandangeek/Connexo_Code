Ext.define('Mdc.model.ComPort', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'name',
        'comPortType',
        'description',
        {name:'modificationDate',type: 'date',dateFormat:'time'},
        'numberOfSimultaneousConnections',
        'active',
        'bound',
        'comserver_id',
    ],
    proxy: {
        type: 'rest',
        url: '../../api/mdc/comports',
        reader: {
            type: 'json'
        }
    }
});
