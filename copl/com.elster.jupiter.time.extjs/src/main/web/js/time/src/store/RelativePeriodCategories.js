Ext.define('Tme.store.RelativePeriodCategories', {
    extend: 'Ext.data.Store',
    fields: ['id', 'name'],
    storeId: 'relativePeriodCategories',
    model: 'Tme.model.RelativePeriod',
    proxy: {
        type: 'rest',
        url: '../../api/tmr/relativeperiods/categories',
        reader: {
            type: 'json',
            root: 'units'
        }
    }
});
