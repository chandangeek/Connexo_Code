Ext.define('Mdc.usagepointmanagement.controller.ViewChannelsList', {
    extend: 'Ext.app.Controller',

    models: [
        'Mdc.usagepointmanagement.model.UsagePoint'
    ],

    stores: [
        'Mdc.usagepointmanagement.store.Channels'
    ],

    views: [
        'Mdc.usagepointmanagement.view.ViewChannelsList'
    ],

    refs: [
        {
            ref: 'preview',
            selector: '#view-channels-list #usage-point-channel-preview'
        }
    ],

    init: function () {
        var me = this;

        me.control({
            '#view-channels-list #usage-point-channels-grid': {
                select: me.showPreview
            }
        });
    },

    showOverview: function (mRID) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            channelsStore = me.getStore('Mdc.usagepointmanagement.store.Channels'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            widget = Ext.widget('view-channels-list', {
                itemId: 'view-channels-list',
                router: router,
                mRID: mRID
            });

        pageMainContent.setLoading();
        me.getModel('Mdc.usagepointmanagement.model.UsagePoint').load(mRID, {
            success: function (usagePoint) {
                app.fireEvent('usagePointLoaded', usagePoint);
                app.fireEvent('changecontentevent', widget);
                channelsStore.getProxy().setUrl(mRID);
                channelsStore.load();
            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        });
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreview();

        Ext.suspendLayouts();
        preview.setTitle(record.get('readingType').fullAliasName);
        preview.loadRecord(record);
        Ext.resumeLayouts(true);
    }
});