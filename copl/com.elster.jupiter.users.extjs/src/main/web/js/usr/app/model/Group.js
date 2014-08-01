Ext.define('Usr.model.Group', {
    extend: 'Ext.data.Model',
    requires: [
        'Usr.model.Privilege'
    ],
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'name',
        'description',
        'version',
        'createdOn',
        'modifiedOn',
        'selected'
    ],
    idProperty: 'id',
    associations: [
        {
            type: 'hasMany',
            model: 'Usr.model.Privilege',
            associationKey: 'privileges',
            name: 'privileges'

        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/usr/groups',
        reader: {
            type: 'json',
            root: 'groups'
        }
    }
});