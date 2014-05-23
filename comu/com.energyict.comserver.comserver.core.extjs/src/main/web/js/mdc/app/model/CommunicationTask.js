Ext.define('Mdc.model.CommunicationTask',{
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name:'name', type: 'string', useNull: true},
        {name:'inUse', type: 'boolean', useNull: true}
    ],
    proxy: {
        type: 'rest',
        url: '../../api/cts/comtasks',
        reader: {
            type: 'json'
        }
    }
});
