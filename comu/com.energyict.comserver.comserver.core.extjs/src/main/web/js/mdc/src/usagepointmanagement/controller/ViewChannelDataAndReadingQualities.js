Ext.define('Mdc.usagepointmanagement.controller.ViewChannelDataAndReadingQualities', {
    extend: 'Ext.app.Controller',

    models: [
        'Mdc.usagepointmanagement.model.UsagePoint',
        'Mdc.usagepointmanagement.model.Channel'
    ],

    stores: [
        'Mdc.usagepointmanagement.store.Channels',
        'Mdc.usagepointmanagement.store.ChannelData',
        'Mdc.store.LoadProfileDataDurations',
        'Uni.store.DataIntervalAndZoomLevels'
    ],

    views: [
        'Mdc.usagepointmanagement.view.ViewChannelDataAndReadingQualities'
    ],

    refs: [
        {
            ref: 'preview',
            selector: '#view-channel-data-and-reading-qualities #channel-data-preview'
        }
    ],

    init: function () {
        var me = this;

        me.control({
            '#view-channel-data-and-reading-qualities #channel-data-grid': {
                select: me.showPreview
            }
        });
    },

    showOverview: function (mRID, channelId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            channelModel = me.getModel('Mdc.usagepointmanagement.model.Channel'),
            channelsStore = me.getStore('Mdc.usagepointmanagement.store.Channels'),
            channelDataStore = me.getStore('Mdc.usagepointmanagement.store.ChannelData'),
            dependenciesCounter = 3,
            onDependencyLoad = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    pageMainContent.setLoading(false);
                    Ext.suspendLayouts();
                    app.fireEvent('usagePointLoaded', usagePoint);
                    app.fireEvent('usagePointChannelLoaded', channel);
                    channelDataStore.getProxy().setUrl(mRID, channelId);
                    app.fireEvent('changecontentevent', Ext.widget('view-channel-data-and-reading-qualities', {
                        itemId: 'view-channel-data-and-reading-qualities',
                        router: router,
                        channel: channel,
                        mRID: mRID,
                        filter: filter
                    }));
                    channelDataStore.load();
                    Ext.resumeLayouts(true);
                }
            },
            usagePoint,
            channel,
            filter;

        pageMainContent.setLoading();

        channelsStore.getProxy().setUrl(mRID);
        channelsStore.load(onDependencyLoad);

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
                filter = me.setDataFilter(channel);
                onDependencyLoad();
            }
        });
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreview(),
            interval = record.get('interval');

        preview.loadRecord(record);
    },

    setDataFilter: function (channel) {
        var me = this,
            dataIntervalAndZoomLevels = me.getStore('Uni.store.DataIntervalAndZoomLevels').getIntervalRecord(channel.get('interval')),
            all = dataIntervalAndZoomLevels.get('all'),
            filter = {};

        filter.durationStore = me.getStore('Mdc.store.LoadProfileDataDurations');
        filter.defaultFromDate = dataIntervalAndZoomLevels.getIntervalStart((channel.get('lastReading') || new Date()));
        filter.defaultDuration = all.count + all.timeUnit;

        filter.durationStore.loadData(dataIntervalAndZoomLevels.get('duration'));

        return filter;
    }
});