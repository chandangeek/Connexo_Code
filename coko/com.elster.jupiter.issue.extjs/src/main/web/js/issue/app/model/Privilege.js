Ext.define('Mtr.model.Privilege', {
    extend: 'Ext.data.Model',
    fields: [
        'componentName',
        'name',
        'description'
    ],
    associations: [
        {
            type: 'belongsTo',
            model: 'Mtr.model.Group',
            name: 'privileges'
        }
    ],
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