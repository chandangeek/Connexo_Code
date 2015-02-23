Ext.define('Dxp.store.ReadingTypesForTask', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Dxp.model.ReadingTypeForAddTaskGrid',
    storeId: 'ReadingTypesForTask',

    requires: [
        'Dxp.model.ReadingTypeForAddTaskGrid'
    ]
});