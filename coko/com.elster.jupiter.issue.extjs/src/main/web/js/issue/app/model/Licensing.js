Ext.define('Isu.model.Licensing', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'application',
            type: 'string'
        },
        {
            name: 'type',
            type: 'string'
        },
        {
            name: 'description',
            type: 'string'
        },
        {
            name: 'expires',
            type: 'string'
        },
        {
            name: 'content',
            type: 'auto'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/sam/license',
        reader: {
            type: 'json',
            root: 'licensing'
        }
    }
});
