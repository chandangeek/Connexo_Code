Ext.define('Mdc.store.NumericalRegisterData', {
    extend: 'Mdc.store.RegisterData',
    requires: [
        'Mdc.model.NumericalRegisterData'
    ],
    model: 'Mdc.model.NumericalRegisterData',
    storeId: 'NumericalRegisterData',
    autoLoad: false,
    proxy: {
        type: 'rest',
        timeout: 60000,
        url: '/api/ddr/devices/{mRID}/registers/{registerId}/data',
        reader: {
            type: 'json',
            root: 'data'
        }
    },
    setFilterModel: function (model) {
        var data = model.getData(),
            storeProxy = this.getProxy();
        storeProxy.setExtraParam('onlySuspect', data.onlySuspect);
        storeProxy.setExtraParam('onlyNonSuspect', data.onlyNonSuspect);
    }
});