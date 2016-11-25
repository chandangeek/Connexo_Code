Ext.define('Mdc.usagepointmanagement.controller.ViewChannelDataAndReadingQualities', {
    extend: 'Ext.app.Controller',

    models: [
        'Mdc.usagepointmanagement.model.UsagePoint',
        'Mdc.usagepointmanagement.model.Channel',
        'Mdc.usagepointmanagement.model.ChannelReading'
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

    showOverview: function (usagePointId, channelId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            channelModel = me.getModel('Mdc.usagepointmanagement.model.Channel'),
            ChannelReading = me.getModel('Mdc.usagepointmanagement.model.ChannelReading'),
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
                    channelDataStore.getProxy().setParams(usagePointId, channelId);
                    app.fireEvent('changecontentevent', Ext.widget('view-channel-data-and-reading-qualities', {
                        itemId: 'view-channel-data-and-reading-qualities',
                        router: router,
                        channel: channel,
                        usagePointId: usagePointId,
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

        channelsStore.getProxy().setExtraParam('usagePointId', usagePointId);
        channelsStore.suspendEvent('beforeload');
        channelsStore.load(function () {
            channelsStore.resumeEvent('beforeload');
            onDependencyLoad();
        });

        me.getModel('Mdc.usagepointmanagement.model.UsagePoint').load(usagePointId, {
            success: function (record) {
                usagePoint = record;
                onDependencyLoad();
            }
        });

        channelModel.getProxy().setExtraParam('usagePointId', usagePointId);
        channelModel.load(channelId, {
            success: function (record) {
                channel = record;
                filter = me.setDataFilter(channel);
                onDependencyLoad();
            }
        });

        ChannelReading.getProxy().setExtraParam('usagePointId', usagePointId);
        ChannelReading.getProxy().setExtraParam('channelId', channelId);
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreview();

        preview.loadRecord(record);
    },

    setDataFilter: function (channel) {
        var me = this,
            dataIntervalAndZoomLevels = me.getStore('Uni.store.DataIntervalAndZoomLevels').getIntervalRecord(channel.get('interval')),
            all = dataIntervalAndZoomLevels.get('all'),
            filter = {};

        filter.durationStore = me.getStore('Mdc.store.LoadProfileDataDurations');
        filter.interval = dataIntervalAndZoomLevels;
        filter.defaultDuration = all.count + all.timeUnit;

        filter.durationStore.loadData(dataIntervalAndZoomLevels.get('duration'));

        return filter;
    }
});