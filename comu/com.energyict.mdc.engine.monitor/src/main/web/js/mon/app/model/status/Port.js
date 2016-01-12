Ext.define('CSMonitor.model.status.Port', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        {
            name: 'name',
            sortType: 'asUCText' // To make the sorting case-insensitive
        },
        'description', 'inbound', 'active', 'responsive', 'lastSeen', 'threads'
    ]
});