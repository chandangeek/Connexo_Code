Ext.define('Mdc.store.DevicesOfDeviceGroupWithoutPaging', {
    extend: 'Mdc.store.DevicesOfDeviceGroup',
    storeId: 'DevicesOfDeviceGroupWithoutPaging',

    proxy: {
        type: 'rest',
        urlTpl: '../../api/ddr/devicegroups/{id}/devices',
        reader: {
            type: 'json',
            root: 'devices'
        },
        timeout: 300000,
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function (deviceGroupId) {
            this.url = this.urlTpl.replace('{id}', deviceGroupId);
        }
    }
});