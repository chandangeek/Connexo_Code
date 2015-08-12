Ext.define('Ldr.model.Privilege', {
    extend: 'Ext.data.Model',

    fields: [
        'name', 'applicationName'
    ],
    idProperty: 'name',

    proxy: {
        type: 'rest',
        url: '/api/usr/users/privileges',

        pageParam: undefined,
        limitParam: undefined,
        startParam: undefined,

        reader: {
            type: 'json',
            root: 'privileges'
        }
    }
});

