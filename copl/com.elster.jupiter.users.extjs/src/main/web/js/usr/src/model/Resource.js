Ext.define('Usr.model.Resource', {
    extend: 'Ext.data.Model',
    requires: [
        'Usr.model.Privilege'
    ],
    fields: [
        'applicationName',
        'componentName',
        'name',
        'qualifiedName',
        'description',
        {
            name: 'permissions',
            type: 'string',
            defaultValue: ''
        },
        {
            name: 'selected',
            type: 'int',
            defaultValue: 0
        }
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Usr.model.Privilege',
            associationKey: 'privileges',
            name: 'privileges'
        }
    ],
    idProperty: 'qualifiedName',
    proxy: {
        type: 'rest',
        url: '/api/usr/resources',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'resources'
        }
    }
});