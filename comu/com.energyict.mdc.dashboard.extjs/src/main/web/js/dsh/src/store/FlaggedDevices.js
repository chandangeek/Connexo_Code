Ext.define('Dsh.store.FlaggedDevices', {
    extend: 'Ext.data.Store',
    storeId: 'FlaggedDevices',
    requires: ['Dsh.model.FlaggedDevice'],
    model: 'Dsh.model.FlaggedDevice',
    autoLoad: false,

    proxy: {
        type: 'ajax',
        url: '/api/dsr/mylabeleddevices?category=mdc.label.category.favorites',
        reader: {
            type: 'json',
            root: 'myLabeledDevices'
        }
    }
});