Ext.define('Cfg.store.ReadingTypesForRule', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Cfg.model.ReadingTypeForGrid',
    storeId: 'ReadingTypesForRule',
    requires: [
        'Cfg.model.ReadingTypeForGrid'
    ]
});

