Ext.define('Mdc.store.servicecalls.RunningServiceCalls', {
    extend: 'Ext.data.Store',
    model: 'Scs.model.ServiceCall',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/runningservicecalls',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'serviceCalls'
        },
        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }


});
