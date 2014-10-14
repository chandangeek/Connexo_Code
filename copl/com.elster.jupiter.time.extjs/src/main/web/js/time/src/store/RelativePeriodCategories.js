Ext.define('Mdc.store.RelativePeriodCategories',{
    extend: 'Ext.data.Store',
    autoLoad: true,
    fields: ['id','name'],
    storeId: 'relativePeriodCategories',

    proxy: {
        type: 'rest',
        url: '../../api/tmr/relativeperiods/categories',
        reader: {
            type: 'json',
            root: 'units'
        }
    }
});
