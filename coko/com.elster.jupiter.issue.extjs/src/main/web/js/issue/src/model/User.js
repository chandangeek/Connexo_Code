Ext.define('Isu.model.User', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    fields: [
        {
            name: 'id',
            displayValue: 'ID',
            type: 'int'
        },
        {
            name: 'type',
            displayValue: 'Type',
            type: 'auto'
        },
        {
            name: 'name',
            displayValue: 'Name',
            type: 'auto'
        }
    ],

    // GET ?like="operator"
    proxy: {
        type: 'rest',
        url: '/api/isu/assignees/users',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

