Ext.define('Mdc.usagepointmanagement.store.Channels', {
    extend: 'Ext.data.Store',
    model: 'Mdc.usagepointmanagement.model.Channel',
    proxy: {
        type: 'rest',
        urlTpl: '/api/upr/usagepoints/{mRID}/channels',
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