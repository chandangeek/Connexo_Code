Ext.define('Usr.model.Privilege', {
    extend: 'Ext.data.Model',
    fields: [
        'componentName',
        'name',
        'description',
        'selected'
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
        reader: {
            type: 'json',
            root: 'privileges'
        }
    }
});