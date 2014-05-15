Ext.define('Mdc.store.AvailableRegisterTypesForRegisterGroup', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterType'
    ],
    model: 'Mdc.model.RegisterType',
    storeId: 'AvailableRegisterTypesForRegisterGroup',
    // TODO: uncomment this to activate infinite scrolling when JP-2844 is fixed
    //buffered: true,
    //leadingBufferZone: 20,
    //trailingBufferZone: 20,
    pageSize: 500,
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