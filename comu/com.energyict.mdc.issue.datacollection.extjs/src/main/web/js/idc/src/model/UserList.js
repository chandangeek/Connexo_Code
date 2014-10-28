Ext.define('Idc.model.UserList', {
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
            name: 'name',
            mapping: function (data) {
                return data.authenticationName
            }
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