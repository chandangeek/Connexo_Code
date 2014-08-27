Ext.define('Uni.model.Privilege', {
    extend: 'Ext.data.Model',
    fields: [
        'name'
    ],
    idProperty: 'name',
    proxy: {
        type: 'rest',
        url: '/api/usr/users/privileges',
        reader: {
            type: 'json',
            root: 'privileges'
        }
    }
});

