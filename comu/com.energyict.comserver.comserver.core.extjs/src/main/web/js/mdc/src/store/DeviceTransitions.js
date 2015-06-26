Ext.define('Mdc.store.DeviceTransitions', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceTransition'
    ],
    model: 'Mdc.model.DeviceTransition',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/transitions',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'transitions'
        },
        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', mRID);
        }
    }
});