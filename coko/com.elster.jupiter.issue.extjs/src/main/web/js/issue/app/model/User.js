Ext.define('Mtr.model.User', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'authenticationName',
        'description',
        'version'
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Mtr.model.Group',
            associationKey: 'groups',
            name: 'groups'
        },
        {
            type: 'belongsTo',
            model: 'Mtr.model.Party',
            name: 'delegates'
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