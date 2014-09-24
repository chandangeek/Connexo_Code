Ext.define('Mdc.model.ExecutionLevel', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id',type:'string',useNull:true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'userRoles'}
    ],
    requires: [
        'Mdc.model.UserRole'
    ],
    associations: [
    {name: 'userRoles', type: 'hasMany', model: 'Mdc.model.UserRole', associationKey: 'userRoles',
        getTypeDiscriminator: function (node) {
            return 'Mdc.model.UserRole';
        }
    }
]
});
