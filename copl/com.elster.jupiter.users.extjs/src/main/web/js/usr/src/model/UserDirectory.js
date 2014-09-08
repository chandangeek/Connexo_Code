Ext.define('Usr.model.UserDirectory', {
    extend: 'Ext.data.Model',
    fields: [
        'domain',
        'isDefault',
        'manageGroupsInternal'
    ],
    idProperty: 'domain',
    proxy: {
        type: 'rest',
        url: '/api/usr/domains',
        reader: {
            type: 'json',
            root: 'domains'
        }
    }
});