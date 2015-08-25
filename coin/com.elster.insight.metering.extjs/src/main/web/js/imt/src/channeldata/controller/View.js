Ext.define('Imt.channeldata.controller.View', {
    extend: 'Ext.app.Controller',
    requires: [
        'Imt.channeldata.store.Channel',
        'Imt.channeldata.view.Setup'
    ],
    models: [
        'Imt.usagepointmanagement.model.UsagePoint'
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
            router = me.getController('Uni.controller.history.Router'),
            // TODO: Why does me.getModel() NOT work here?
            usagePoint = Ext.create('Imt.usagepointmanagement.model.UsagePoint'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
       
        pageMainContent.setLoading(true);
        var widget = Ext.widget('channel-list-setup', {router: router, mRID: mRID});
        // TODO: Should we be loading a full Usage Point model from the back-end just so that
        // the event can contain the mRID which is used by History.js to change the link
        // name in the breadcrumb?  For now, just create empty model and set this one field.
        usagePoint.set('mRID', mRID);
        me.getApplication().fireEvent('usagePointLoaded', usagePoint);
        me.getApplication().fireEvent('changecontentevent', widget);
        me.getOverviewLink().setText(mRID);
        pageMainContent.setLoading(false);
    }
});

