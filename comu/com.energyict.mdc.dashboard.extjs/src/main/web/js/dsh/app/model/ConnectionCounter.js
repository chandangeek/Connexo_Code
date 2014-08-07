Ext.define('Dsh.model.ConnectionCounter', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'count', type: 'int' },
        { name: 'alias', type: 'string' },
        { name: 'displayName', type: 'string' }
    ]
});