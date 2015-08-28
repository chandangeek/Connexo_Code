Ext.define('Imt.channeldata.controller.View', {
    extend: 'Ext.app.Controller',
    requires: [
        'Imt.channeldata.store.Channel',
        'Imt.channeldata.view.Setup'
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
            'Imt.channeldata.view.ChannelGraph'
    ],
    refs: [
        {ref: 'page', selector: 'channel-graph'},
        {ref: 'overviewLink', selector: '#usage-point-overview-link'},
        {ref: 'usagePointChannelGraphView', selector: '#usagePointChannelGraphView'}
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
    },
    // TODO: Pass channel record instead of "id"
    showUsagePointChannel: function(mRID, id) {
        var me = this,
        container = this.getUsagePointChannelGraphView(),
        dataStore = me.getStore('Imt.channeldata.store.ChannelData'),
        zoomLevelsStore = me.getStore('Imt.store.DataIntervalAndZoomLevels'),
        channelName = id,
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

