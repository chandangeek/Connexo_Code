Ext.define('Mdc.store.SelectedMeasurementTypesForLoadProfileType', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterType'
    ],
    model: 'Mdc.model.RegisterType',
    storeId: 'SelectedMeasurementTypesForLoadProfileType'
});