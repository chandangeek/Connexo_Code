Ext.define('Mdc.store.DeviceConfigMessages', {
    extend: 'Ext.data.Store',
    requires: [ 'Mdc.model.MessageCategory' ],
    model: 'Mdc.model.MessageCategory',
    storeId: 'DeviceConfigMessages',
    sorters: [
        {
            property: 'name',
            direction: 'ASC'
        }
    ]
});