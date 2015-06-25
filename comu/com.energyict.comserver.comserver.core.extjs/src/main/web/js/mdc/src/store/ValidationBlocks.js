Ext.define('Mdc.store.ValidationBlocks', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.ValidationBlock',
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/channels/{channelId}/datavalidationissues/{issueId}/validationblocks',
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
