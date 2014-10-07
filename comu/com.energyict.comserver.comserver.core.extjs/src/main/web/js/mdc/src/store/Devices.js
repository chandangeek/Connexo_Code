Ext.define('Mdc.store.Devices', {
    extend: 'Uni.data.store.Filterable',

    requires: [
        'Mdc.model.Device'
    ],

    model: 'Mdc.model.Device',
    storeId: 'Devices',

    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices',
        reader: {
            type: 'json',
            root: 'devices'
        }
    }//,

    /*setFilterModel: function (model) {
        var data = model.getData();
        var storeProxy = this.getProxy();

        if (!Ext.isEmpty(data.serialNumber)) {
            storeProxy.setExtraParam('serialNumber', data.serialNumber);
        }
        if (!Ext.isEmpty(data.mRID)) {
            storeProxy.setExtraParam('mRID', data.mRID);
        }
    }*/
});
