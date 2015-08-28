Ext.define('Imt.channeldata.controller.View', {
    extend: 'Ext.app.Controller',
    requires: [
        'Imt.channeldata.store.Channel',
        'Imt.channeldata.view.Setup',
        'Imt.channeldata.view.DataSetup'
    ],
    models: [
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.model.DataIntervalAndZoomLevels'
    ],
    stores: [
             'Imt.channeldata.store.Channel',
             'Imt.channeldata.store.ChannelData',
             'Imt.store.DataIntervalAndZoomLevels'
    ],
    views: [
            'Imt.channeldata.view.ChannelList',
            'Imt.channeldata.view.ChannelGraph',
            'Imt.channeldata.view.Preview'
    ],
    refs: [
        {ref: 'page', selector: 'channel-graph'},
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
        me.getChannelList().getSelectionModel().select(0);
        pageMainContent.setLoading(false);
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
        widget.setTitle(record.get('readingTypeAlias'));
        previewContainer.removeAll();
        previewContainer.add(widget);
    
    },
    showUsagePointChannelData: function (mRID, channel) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            // TODO: Why does me.getModel() NOT work here?
            channelModel = Ext.create('Imt.channeldata.model.Channel'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
       
        pageMainContent.setLoading(true);
        var widget = Ext.widget('channel-data-setup', {router: router, mRID: mRID, channel: channel});
        // TODO: Should we be loading a full channel model from the back-end just so that
        // the event can contain the alias which is used by History.js to change the link
        // name in the breadcrumb?  For now, just create empty model and set this one field.
        channelModel.set('readingTypeAlias', 'Bulk A+ (kWh)');
        me.getApplication().fireEvent('channelDataLoaded', channelModel);
        me.getApplication().fireEvent('changecontentevent', widget);
        me.getOverviewLink().setText(mRID);
        pageMainContent.setLoading(false);
        me.showUsagePointChannelGraph(mRID, channel);
    },
    // TODO: Pass channel record instead of "id"
    showUsagePointChannelGraph: function(mRID, channel) {
        var me = this,
        container = this.getUsagePointChannelGraphView(),
        dataStore = me.getStore('Imt.channeldata.store.ChannelData'),
        zoomLevelsStore = me.getStore('Imt.store.DataIntervalAndZoomLevels'),
        channelName = channel,
        unitOfMeasure = 'Wh',
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
        //TODO: Get parameter from actual channel record.
        o=Object();
        o.timeUnit='minutes';
        o.count=15;
        intervalRecord = zoomLevelsStore.getIntervalRecord(o);
        intervalLengthInMs = zoomLevelsStore.getIntervalInMs(o);
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

