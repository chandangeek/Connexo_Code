Ext.define('Imt.channeldata.store.Channel', {
    extend: 'Ext.data.Store',
    model: 'Imt.channeldata.model.Channel',
    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{mRID}/channels',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'channels'
        },
        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});