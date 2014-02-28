Ext.define('Mtr.model.Role', {
    extend: 'Ext.data.Model',
    fields: [
        'componentName',
        'mRID',
        'name',
        'aliasName',
        'description',
        'version'
    ],
    idProperty: 'mRID',
    proxy: {
        type: 'rest',
        url: '/api/prt/roles',
        reader: {
            type: 'json',
            root: 'roles'
        }
    }
});