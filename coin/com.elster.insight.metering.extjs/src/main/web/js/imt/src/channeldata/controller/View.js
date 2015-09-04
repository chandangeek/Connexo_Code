Ext.define('Imt.channeldata.controller.View', {
    extend: 'Ext.app.Controller',
    requires: [
        'Imt.channeldata.store.Channel',
        'Imt.channeldata.store.ChannelData',
        'Imt.channeldata.view.Setup',
        'Imt.channeldata.view.DataSetup'
    ],
    models: [
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.model.DataIntervalAndZoomLevels',
        'Imt.model.ChannelDataDuration'
    ],
    stores: [
             'Imt.channeldata.store.Channel',
             'Imt.channeldata.store.ChannelData',
             'Imt.store.DataIntervalAndZoomLevels',
             'Imt.store.ChannelDataDurations'
    ],
    views: [
            'Imt.channeldata.view.ChannelList',
            'Imt.channeldata.view.ChannelGraph',
            'Imt.channeldata.view.Preview'
    ],
    refs: [
        {ref: 'page', selector: 'channel-graph'},
        {ref: 'dataGrid', selector: 'channel-data-grid'},
        {ref: 'channelList', selector: '#channelList'},
        {ref: 'overviewLink', selector: '#usage-point-overview-link'},
        {ref: 'channelListSetup', selector: '#channel-list-setup'},
        {ref: 'usagePointChannelGraphView', selector: '#usagePointChannelGraphView'}
    ],
    init: function () {
        var me = this;
        me.control({
            '#channelList': {
                select: me.onChannelListSelect
            }
        });        
    },
    showUsagePointChannels: function (mRID) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            dataStore = me.getStore('Imt.channeldata.store.Channel'),
            usagePoint = Ext.create('Imt.usagepointmanagement.model.UsagePoint'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
        
        pageMainContent.setLoading(true);
        var widget = Ext.widget('channel-list-setup', {router: router, mRID: mRID});
        usagePoint.set('mRID', mRID);
        me.getApplication().fireEvent('usagePointLoaded', usagePoint);
        me.getOverviewLink().setText(mRID);
        me.getApplication().fireEvent('changecontentevent', widget);
        dataStore.getProxy().setUrl(mRID);
        dataStore.load(function() {
            me.getChannelList().getSelectionModel().select(0);
            pageMainContent.setLoading(false);
        });
    },
    onChannelListSelect: function (rowmodel, record, index) {
        var me = this;
        me.previewChannelData(record);
    },
    previewChannelData: function (record) {
        var me = this,
            widget = Ext.widget('channel-preview'), 
            form = widget.down('#channelPreviewForm'),
            previewContainer = me.getChannelListSetup().down('#previewComponentContainer');
        
        form.loadRecord(record);
        widget.setTitle(record.get('readingTypeFullAliasName'));
        previewContainer.removeAll();
        previewContainer.add(widget);
    },
    showUsagePointChannelData: function (mRID, channel) {
        var me = this,
            dataStore = me.getStore('Imt.channeldata.store.ChannelData'),
            router = me.getController('Uni.controller.history.Router'),
            channelModel = me.getModel('Imt.channeldata.model.Channel'),
            durationsStore = me.getStore('Imt.store.ChannelDataDurations'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
       
        pageMainContent.setLoading(true);
        channelModel.getProxy().setUrl(mRID);
        channelModel.load(channel, {
            success: function (record) {
                var dataIntervalAndZoomLevels = me.getStore('Imt.store.DataIntervalAndZoomLevels').getIntervalRecord(record.get('interval')),
                intervalStart = dataIntervalAndZoomLevels.getIntervalStart((record.get('lastValueTimestamp') || new Date().getTime())),
                all = dataIntervalAndZoomLevels.get('all');
                durationsStore.loadData(dataIntervalAndZoomLevels.get('duration'));
                var widget = Ext.widget('channel-data-setup', {
                        router: router, 
                        mRID: mRID, 
                        channel: record,
                        filter: {
                            fromDate: intervalStart,
                            duration: all.count + all.timeUnit,
                            durationStore: durationsStore
                        }
                });
                me.getApplication().fireEvent('channelDataLoaded', record);
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getOverviewLink().setText(mRID);
                dataStore.getProxy().setUrl({mRID: mRID, channelId: channel});
                dataStore.on('load', function () {
                    if (!widget.isDestroyed) {
                        me.showUsagePointChannelGraph(mRID, record);                                                
                        widget.setLoading(false);
                        Ext.getBody().unmask();
                    }
                }, me);

                dataStore.load();
            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        });
    },
    showUsagePointChannelGraph: function(mRID, channel) {
        var me = this,
        container = me.getUsagePointChannelGraphView(),
        dataStore = me.getStore('Imt.channeldata.store.ChannelData'),
        zoomLevelsStore = me.getStore('Imt.store.DataIntervalAndZoomLevels'),
        channelName = channel.get('readingTypeFullAliasName'),
        unitOfMeasure = channel.get('unitOfMeasure'),
        seriesObject = { 
            marker: { enabled: false },
            name: channelName
        },
        yAxis = {
            opposite: false,
            gridLineDashStyle: 'Dot',
            showEmpty: false,
            title: {
                rotation: 270,
                text: unitOfMeasure
            }
        },
        series = [];

        seriesObject['data'] = [];
        intervalRecord = zoomLevelsStore.getIntervalRecord(channel.get('interval'));
        intervalLengthInMs = zoomLevelsStore.getIntervalInMs(channel.get('interval'));
        zoomLevels = intervalRecord.get('zoomLevels');
        
//    switch (channelRecord.get('flowUnit')) {
//        case 'flow':
//            seriesObject['type'] = 'line';
//            seriesObject['step'] = false;
//            break;
//        case 'volume':
            seriesObject['type'] = 'column';
            seriesObject['step'] = true;
//            break;
//    }

        if (dataStore.getCount() > 0) {
            dataStore.each(function (record) {
                if (record.get('value')) {
                    seriesObject['data'].unshift([record.get('interval').start, parseFloat(record.get('value'))]);
                } else {
                    seriesObject['data'].unshift([record.get('interval').start, null]);
                }
            });
            series.push(seriesObject);
            Ext.suspendLayouts();
            container.down('#graphContainer').show();
            container.down('#ctr-graph-no-data').hide();
            container.drawGraph(yAxis, series, intervalLengthInMs, channelName, unitOfMeasure, zoomLevels);
            Ext.resumeLayouts(true);
        } else {
            Ext.suspendLayouts();
            container.down('#graphContainer').hide();
            container.down('#ctr-graph-no-data').show();
            Ext.resumeLayouts(true);
        }
        me.getPage().doLayout();        
    }
});

