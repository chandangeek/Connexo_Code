Ext.define('Usr.model.User', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'authenticationName',
        'description',
        'version',
        'domain',
        'language',
        'createdOn',
        'modifiedOn'
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Usr.model.Group',
            associationKey: 'groups',
            name: 'groups'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/usr/users',
        reader: {
            type: 'json',
            root: 'users'
        }
    }
});