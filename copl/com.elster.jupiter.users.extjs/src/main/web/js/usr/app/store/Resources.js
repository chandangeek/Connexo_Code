Ext.define('Usr.store.Resources', {
    extend: 'Ext.data.Store',
    requires: [
        'Usr.model.Resource'
    ],
    model: 'Usr.model.Resource',
    groupField: 'componentName',
    remoteSort: true,
    sorters: {
        property: 'name',
        direction: 'ASC'
    }
});