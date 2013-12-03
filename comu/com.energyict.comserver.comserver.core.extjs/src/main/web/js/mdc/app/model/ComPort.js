Ext.define('Mdc.model.ComPort', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'name',
        'direction',
        'comServer_id',
        'comPortType',
        'description',
        'active',
        'bound',
        'comPortPool_id',
        {name:'numberOfSimultaneousConnections',type:'int'},
        {name:'modificationDate',type: 'date',dateFormat:'time'}
    ]
});
