Ext.define('Usr.store.Privileges', {
    extend: 'Ext.data.Store',
    requires: [
        'Usr.model.Privilege'
    ],
    model: 'Usr.model.Privilege',
    groupField: 'componentName',
    remoteSort: true,
    sorters: {
        property: 'name',
        direction: 'ASC'
    }
});