Ext.define('Usr.model.Privilege', {
    extend: 'Ext.data.Model',
    fields: [
        'componentName',
        'name',
        'description',
        {
            name: 'selected',
            type: 'boolean',
            defaultValue: false
        }
    ],
    /*associations: [
        {
            type: 'belongsTo',
            model: 'Usr.model.Group',
            name: 'privileges'
        }
    ],*/
    idProperty: 'name',
    proxy: {
        type: 'rest',
        url: '/api/usr/privileges',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'privileges'
        }
    }
});