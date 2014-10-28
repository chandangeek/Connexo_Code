Ext.define('Idc.store.AssigneeTypes', {
    extend: 'Ext.data.Store',
    fields: [
        {
            name: 'id', type: 'auto'
        },
        {
            name: 'localizedValue', type: 'auto'
        }
    ],
    data: [
        {
            id: 'USER',
            localizedValue: 'User'
        },
        {
            id: 'GROUP',
            localizedValue: 'Group'
        },
        {
            id: 'ROLE',
            localizedValue: 'Role'
        }
    ]
});
