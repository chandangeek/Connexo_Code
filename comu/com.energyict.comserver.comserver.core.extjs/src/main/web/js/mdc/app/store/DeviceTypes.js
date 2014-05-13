Ext.define('Mdc.store.DeviceTypes',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceType'
    ],
    model: 'Mdc.model.DeviceType',
    storeId: 'DeviceTypes',
    pageSize: 10,
    /*sorters: [{
        property: 'name',
        direction: 'ASC'
    }],  */
    remoteSort: true,
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes',
        reader: {
            type: 'json',
            root: 'deviceTypes'
        }
    }
});
