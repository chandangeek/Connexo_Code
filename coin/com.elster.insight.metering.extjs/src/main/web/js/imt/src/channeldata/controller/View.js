Ext.define('Imt.channeldata.controller.View', {
    extend: 'Ext.app.Controller',
    requires: [
        'Imt.channeldata.store.Channel',
        'Imt.channeldata.view.Setup'
    ],
    stores: [
        'Imt.channeldata.store.Channel'
    ],
    views: [
        'Imt.channeldata.view.ChannelList'
    ],
    refs: [
        {ref: 'overviewLink', selector: '#usage-point-overview-link'}
    ],
    init: function () {
    },

    showUsagePointChannels: function (mRID) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
       
        pageMainContent.setLoading(true);
        var widget = Ext.widget('channel-list-setup', {router: router, mRID: mRID});
        me.getApplication().fireEvent('changecontentevent', widget);
        me.getOverviewLink().setText(mRID);
        pageMainContent.setLoading(false);
    }
});

