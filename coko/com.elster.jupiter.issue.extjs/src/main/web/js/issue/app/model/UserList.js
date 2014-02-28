Ext.define('Mtr.model.UserList', {
    extend: 'Ext.data.Model',

    fields: [
        {
            name: 'id',
            type: 'auto'
        },
        {
            name: 'authenticationName',
            type: 'auto'
        },
        {
            name: 'description',
            type: 'auto'
        },
        {
            name: 'version',
            type: 'auto'
        },
        {
            name: 'groups',
            type: 'auto'
        }
    ]
});