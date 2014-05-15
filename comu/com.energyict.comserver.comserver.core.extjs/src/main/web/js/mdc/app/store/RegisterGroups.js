Ext.define('Mdc.store.RegisterGroups', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterGroup'
    ],
    model: 'Mdc.model.RegisterGroup',
    storeId: 'RegisterGroups',
    pageSize: 10,
    proxy: {
        type: 'rest',
        url: '../../api/dtc/registergroups',
        reader: {
            type: 'json',
            root: 'registerGroups'
        }
    }
});
