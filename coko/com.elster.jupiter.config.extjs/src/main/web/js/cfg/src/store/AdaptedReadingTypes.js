Ext.define('Cfg.store.AdaptedReadingTypes', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.ReadingTypeForGrid',
    storeId: 'AdaptedReadingTypes',
    requires: [
        'Cfg.model.ReadingTypeForGrid'
    ],
    autoLoad: false
});