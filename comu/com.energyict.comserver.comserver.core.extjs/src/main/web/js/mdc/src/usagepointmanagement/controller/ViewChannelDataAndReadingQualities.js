Ext.define('Mdc.usagepointmanagement.controller.ViewChannelDataAndReadingQualities', {
    extend: 'Ext.app.Controller',

    models: [
        'Mdc.usagepointmanagement.model.UsagePoint',
        'Mdc.usagepointmanagement.model.Channel'
    ],

    stores: [
        //'Mdc.usagepointmanagement.store.Channels'
    ],

    views: [
        //'Mdc.usagepointmanagement.view.ViewChannelsList'
    ],

    refs: [
        //{
        //    ref: 'preview',
        //    selector: '#view-channels-list #usage-point-channel-preview'
        //}
    ],

    init: function () {
        var me = this;

        //me.control({
        //    '#view-channels-list #usage-point-channels-grid': {
        //        select: me.showPreview
        //    }
        //});
    },

    showOverview: function (mRID, channelId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            dependenciesCounter = 2,
            onDependencyLoad = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    pageMainContent.setLoading(false);
                    Ext.suspendLayouts();
                    app.fireEvent('usagePointLoaded', usagePoint);
                    app.fireEvent('usagePointChannelLoaded', channel);
                    Ext.resumeLayouts(true);
                }
            },
            channelModel = me.getModel('Mdc.usagepointmanagement.model.Channel'),
            usagePoint,
            channel;

        pageMainContent.setLoading();
        me.getModel('Mdc.usagepointmanagement.model.UsagePoint').load(mRID, {
            success: function (record) {
                usagePoint = record;
                onDependencyLoad();
            }
        });
        channelModel.getProxy().setUrl(mRID);
        channelModel.load(channelId, {
            success: function (record) {
                channel = record;
                onDependencyLoad();
            }
        });
    },

    showPreview: function (selectionModel, record) {
        //var me = this,
        //    preview = me.getPreview();
        //
        //Ext.suspendLayouts();
        //preview.setTitle(record.get('readingType').fullAliasName);
        //preview.loadRecord(record);
        //Ext.resumeLayouts(true);
    }
});