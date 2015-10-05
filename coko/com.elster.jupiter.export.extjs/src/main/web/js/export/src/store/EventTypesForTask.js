Ext.define('Dxp.store.EventTypesForTask', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Dxp.model.EventTypeForAddTaskGrid',
    storeId: 'EventTypesForTask',

    requires: [
        'Dxp.model.EventTypeForAddTaskGrid'
    ]
});
