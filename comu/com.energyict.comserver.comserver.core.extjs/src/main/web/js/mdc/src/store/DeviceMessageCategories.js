Ext.define('Mdc.store.DeviceMessageCategories', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceMessageCategory'
    ],
    model: 'Mdc.model.DeviceMessageCategory',
    url: '/api/ddr/devices/',
    categoriesPostfix: '/messagecategories',
    proxy: {
        type: 'rest',
        limitParam: false,
        pageParam: false,
        startParam: false,
        url: '/apps/mdc/fakedata/msgcategories.json',
        reader: {
            type: 'json',
            root: 'categories',
            totalProperty: 'total'
        }
    },

    setName: function (name) {
        var me = this;
        me.getProxy().url = me.url + encodeURIComponent(name) + me.categoriesPostfix
    }
});
