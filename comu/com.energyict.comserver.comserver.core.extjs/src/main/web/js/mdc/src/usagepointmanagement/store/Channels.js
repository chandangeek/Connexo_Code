Ext.define('Mdc.usagepointmanagement.store.MeterActivations', {
    extend: 'Ext.data.Store',
    model: 'Mdc.usagepointmanagement.model.Channel',
    proxy: {
        type: 'rest',
        urlTpl: '/api/mtr/usagepoints/{mRID}/channels',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'channels'
        },
        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', mRID);
        }
    }
});