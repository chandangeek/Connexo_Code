Ext.define('Mdc.store.SelectedRegisterTypesForLoadProfileType', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterType'
    ],
    pageSize: 100,
    model: 'Mdc.model.RegisterType',
    storeId: 'SelectedRegisterTypesForLoadProfileType'
});