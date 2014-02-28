Ext.define('Mtr.model.Group', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'name',
        'version'
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Mtr.model.Privilege',
            associationKey: 'privileges',
            name: 'privileges',
            reader: {
                type: 'array'
            }
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