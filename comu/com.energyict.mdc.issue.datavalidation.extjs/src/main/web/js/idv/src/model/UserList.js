Ext.define('Idv.model.UserList', {
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
            name: 'name'
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