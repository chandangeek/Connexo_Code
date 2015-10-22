Ext.define('Mdc.model.ComPort', {
    extend: 'Uni.model.ParentVersion',
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
        {name: 'type', type: 'string'}
    ]
});
