Ext.define('Mdc.store.TimeOfUses',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Mdc.model.field.TimeOfUse',
    storeId: 'timeOfUses',

    proxy: {
        type: 'rest',
        url: '../../api/dtc/field/timeOfUse',
        reader: {
            type: 'json',
            root: 'timeOfUse'
        }
    }
});