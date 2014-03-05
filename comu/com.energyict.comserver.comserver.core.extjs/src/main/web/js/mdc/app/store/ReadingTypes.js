Ext.define('Mdc.store.ReadingTypes', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ReadingType'
    ],
    model: 'Mdc.model.ReadingType',
    storeId: 'ReadingTypes',
    proxy: {
        type: 'rest',
        url: '../../api/dtc/readingtypes',
        reader: {
            type: 'json'
        }
    }
});
