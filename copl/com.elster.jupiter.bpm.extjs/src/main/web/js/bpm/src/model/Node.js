Ext.define('Bpm.model.Node', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'type', type: 'string', useNull: true},
        {name: 'nodeType', type: 'string', useNull: true},
        {name: 'nodeName', type: 'string', useNull: true},
        {name: 'date', type: 'string', useNull: true},
        {name: 'state', type: 'int', useNull: true}
    ]
});

