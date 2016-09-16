Ext.define('Usr.store.Resources', {
    extend: 'Ext.data.Store',
    requires: [
        'Usr.model.Resource'
    ],
    pageSize: 1000,
    model: 'Usr.model.Resource',
    groupField: 'componentName',
    remoteSort: false,
    sorters: {
        property: 'qualifiedName',
        direction: 'ASC'
    }
});