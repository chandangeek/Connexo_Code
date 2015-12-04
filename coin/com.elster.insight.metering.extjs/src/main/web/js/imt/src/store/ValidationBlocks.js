Ext.define('Imt.store.ValidationBlocks', {
    extend: 'Ext.data.Store',
    model: 'Imt.model.ValidationBlock',
    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{mRID}/channels/{channelId}/datavalidationissues/{issueId}/validationblocks',
        timeout: 240000,
        pageParam: false,
        startParam: false,
        limitParam: false,

        reader: {
            type: 'json',
            totalProperty: 'total',
            root: 'validationBlocks',
            idProperty: 'startTime'
        },

        setUrl: function (mRID, channelId, issueId) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{channelId}', channelId).replace('{issueId}', issueId);
        }
    }
});
