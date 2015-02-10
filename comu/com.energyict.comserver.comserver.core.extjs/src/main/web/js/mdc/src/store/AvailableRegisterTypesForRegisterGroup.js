Ext.define('Mdc.store.AvailableRegisterTypesForRegisterGroup', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterType'
    ],
    model: 'Mdc.model.RegisterType',
    storeId: 'AvailableRegisterTypesForRegisterGroup',
    buffered: true,
    proxy: {
        type: 'rest',
        url: '../../api/dtc/registergroups/{registerGroup}/registertypes',
        reader: {
            type: 'json',
            root: 'registerTypes',
            totalProperty: 'total'
        }
    }
});