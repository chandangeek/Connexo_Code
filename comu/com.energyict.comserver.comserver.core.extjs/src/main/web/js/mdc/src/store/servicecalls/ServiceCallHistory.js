Ext.define('Mdc.store.servicecalls.ServiceCallHistory', {
    extend: 'Ext.data.Store',
    model: 'Scs.model.ServiceCall',
    autoLoad: false,
    proxy: {
        type: 'rest',
        //urlTpl: '',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'serviceCalls'
        },

        setUrl: function (mRID) {
            this.url = '/api/ddr/devices/' + encodeURIComponent(mRID) + '/servicecallhistory';
        }
    }
});
