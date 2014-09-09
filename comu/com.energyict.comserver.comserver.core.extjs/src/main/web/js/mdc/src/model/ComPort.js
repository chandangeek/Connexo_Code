Ext.define('Mdc.model.ComPort', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'name',
        'direction',
        'comServer_id',
        'comServerName',
        'comPortType',
        'description',
        'active',
        'bound',
        {name:'numberOfSimultaneousConnections',type:'int', defaultValue: 1},
        {name:'modificationDate',type: 'date',dateFormat:'time'},
        {name: 'type', type: 'string'}
    ]
});
