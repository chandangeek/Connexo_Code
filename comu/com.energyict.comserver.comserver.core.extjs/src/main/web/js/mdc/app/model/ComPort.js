Ext.define('Mdc.model.ComPort', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'name',
        'comPortType',
        'description',
        'modificationDate',
        'numberOfSimultaneousConnections',
        'active',
        'bound',
        'comserver_id',
    ],
    proxy: {
        type: 'rest',
        url: '../../api/mdc/comports',
        reader: {
            type: 'json',
            root: 'ComPorts'
        }
    }
});
