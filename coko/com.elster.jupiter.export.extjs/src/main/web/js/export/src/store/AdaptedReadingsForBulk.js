Ext.define('Dxp.store.AdaptedReadingsForBulk', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.ReadingTypeForAddTaskGrid',
    storeId: 'AdaptedReadingsForBulk',
    requires: [
        'Dxp.model.ReadingTypeForAddTaskGrid'
    ],
    autoLoad: false
});