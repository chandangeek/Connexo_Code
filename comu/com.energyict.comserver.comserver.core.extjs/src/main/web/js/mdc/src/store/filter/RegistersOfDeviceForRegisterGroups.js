Ext.define('Mdc.store.filter.RegistersOfDeviceForRegisterGroups', {
    extend: 'Ext.data.Store',
    fields: ['id', 'name'],
    sorters: {
        property: 'name',
        direction: 'ASC'
    },
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{0}/registers/registersforgroups',

        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'registers'
        },

        setUrl: function (mRID) {
            this.url = Ext.String.format(this.urlTpl, encodeURIComponent(mRID));
        }
    }
});
