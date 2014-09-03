Ext.define('Dsh.model.DateTimeRange', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'from', type: 'datetime', dateFormat: 'Y-m-d H:i:s' },
        { name: 'to', type: 'datetime', dateFormat: 'Y-m-d H:i:s' }
    ]
});