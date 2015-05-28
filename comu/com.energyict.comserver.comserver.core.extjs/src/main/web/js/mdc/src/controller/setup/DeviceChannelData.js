Ext.define('Mdc.controller.setup.DeviceChannelData', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.devicechannels.TabbedDeviceChannelsView',
        'Mdc.view.setup.devicechannels.Overview'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice',
        'Mdc.model.ChannelOfLoadProfilesOfDevice',
        'Mdc.model.ChannelOfLoadProfilesOfDeviceDataFilter'
    ],

    stores: [
        'Mdc.store.ChannelOfLoadProfileOfDeviceData',
        'Mdc.store.DataIntervalAndZoomLevels',
        'Mdc.store.LoadProfileDataDurations',
        'Mdc.store.Clipboard',
        'Mdc.store.ValidationBlocks'
    ],

    refs: [
        {
            ref: 'deviceLoadProfileChannelGraphView',
            selector: '#deviceLoadProfileChannelGraphView'
        },
        {
            ref: 'tabbedDeviceChannelsView',
            selector: '#tabbedDeviceChannelsView'
        },
        {
            ref: 'page',
            selector: '#deviceLoadProfileChannelData'
        },
        {
            ref: 'sideFilter',
            selector: '#deviceLoadProfileChannelDataSideFilter'
        },
        {
            ref: 'deviceLoadProfileChannelDataPreview',
            selector: '#deviceLoadProfileChannelDataPreview'
        },
        {
            ref: 'sideFilterForm',
            selector: '#deviceLoadProfileChannelDataFilterForm'
        },
        {
            ref: 'filterPanel',
            selector: '#deviceLoadProfileChannelData #deviceloadprofileschanneldatafilterpanel'
        }
    ],

    channelModel: null,

    init: function () {
        this.control({
            '#deviceLoadProfileChannelData #deviceLoadProfileChannelDataGrid': {
                select: this.showPreview
            },
            '#deviceLoadProfileChannelDataSideFilter #deviceLoadProfileDataFilterApplyBtn': {
                click: this.applyFilter
            },
            '#deviceLoadProfileChannelDataSideFilter #deviceLoadProfileDataFilterResetBtn': {
                click: this.clearFilter
            },
            '#deviceLoadProfileChannelData #deviceLoadProfileChannelGraphView': {
                resize: this.onGraphResize
            },
            '#tabbedDeviceChannelsView #channelTabPanel': {
                tabchange: this.onTabChange
            },
            '#deviceLoadProfileChannelData #deviceloadprofileschanneldatafilterpanel': {
                removeFilter: this.removeFilterItem,
                clearAllFilters: this.clearFilter
            }
        });
    },

    showSpecifications: function (mRID, channelId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');
        me.showOverview(mRID, channelId, 0, 'Mdc.store.ChannelsOfLoadProfilesOfDevice', me.makeLinkToChannels(router), 'channelId', 'arguments')
    },

    showData: function (mRID, channelId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');
        me.showOverview(mRID, channelId, 1, 'Mdc.store.ChannelsOfLoadProfilesOfDevice', me.makeLinkToChannels(router), 'channelId', 'arguments')
    },

    showValidationData: function (mRID, channelId, issueId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');
        // todo Replace 'Mdc.store.ChannelsOfLoadProfilesOfDevice' with validation blocks store
        me.getStore( 'Mdc.store.ValidationBlocks').load();
        me.showOverview(mRID, channelId, 1, 'Mdc.store.ValidationBlocks', me.makeLinkToIssue(router, issueId), 'validationBlock', 'queryParams');
    },

    showOverview: function (mRID, channelId, activeTab, prevNextstore, prevNextListLink, routerIdArgument, indexLocation) {
        var me = this,
            device = me.getModel('Mdc.model.Device'),
            viewport = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            channel = me.getModel('Mdc.model.ChannelOfLoadProfilesOfDevice'),
            router = me.getController('Uni.controller.history.Router');
        viewport.setLoading(true);
        device.load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                channel.getProxy().setUrl({
                    mRID: mRID
                });
                channel.load(channelId, {
                    success: function (channel) {
                        me.getApplication().fireEvent('channelOfLoadProfileOfDeviceLoad', channel);
                        var widget = Ext.widget('tabbedDeviceChannelsView', {
                            router: router,
                            channel: channel,
                            device: device,
                            indexLocation: indexLocation,
                            prevNextListLink: prevNextListLink,
                            activeTab: activeTab,
                            prevNextstore: prevNextstore,
                            routerIdArgument: routerIdArgument
                        });
                        widget.down('#channelTabPanel').setTitle(channel.get('name'));
                        me.getApplication().fireEvent('changecontentevent', widget);
                        if (activeTab == 1) {
                            me.setupReadingsTab(device, channel, widget);
                        } else if (activeTab == 0) {
                            me.setupSpecificationsTab(device, channel, widget);
                        }
                    }
                })
            }
        })
    },

    setupSpecificationsTab: function (device, channel, widget) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
        viewport.setLoading(false);
        widget.down('#deviceLoadProfileChannelsOverviewForm').loadRecord(channel);
        widget.down('#deviceLoadProfileChannelsActionMenu').record = channel;
    },

    setupReadingsTab: function (device, channel) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            viewport = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            dataIntervalAndZoomLevels = me.getStore('Mdc.store.DataIntervalAndZoomLevels').getIntervalRecord(channel.get('interval')),
            durationsStore = me.getStore('Mdc.store.LoadProfileDataDurations'),
            dataGrid = me.getPage().down('#deviceLoadProfileChannelDataGrid'),
            dataStore = me.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData');
        durationsStore.loadData(dataIntervalAndZoomLevels.get('duration'));
        if (Ext.isEmpty(router.filter.data.intervalStart)) {
            me.setDefaults(channel, dataIntervalAndZoomLevels, (router.queryParams.onlySuspect === 'true'));
            router.queryParams.onlySuspect = undefined;
        }
        me.getTabbedDeviceChannelsView().setFilterView(router.filter, durationsStore);
        viewport.setLoading(false);
        me.getSideFilterForm().loadRecord(router.filter);
        dataGrid.setLoading(true);
        dataStore.setFilterModel(router.filter);
        dataStore.getProxy().setUrl({
            mRID: device.get('mRID'),
            channelId: channel.getId()
        });
        dataStore.load(function () {
            dataGrid.setLoading(false);
            me.showGraphView(channel, dataStore)
        });
    },


    makeLinkToChannels: function (router) {
        var link = '<a href="{0}">' + Uni.I18n.translate('deviceloadprofiles.channels', 'MDC', 'Channels').toLowerCase() + '</a>',
            filter = this.getStore('Mdc.store.Clipboard').get('latest-device-channels-filter'),
            queryParams = filter ? {filter: filter} : null;
        return Ext.String.format(link, router.getRoute('devices/device/channels').buildUrl(null, queryParams));
    },

    makeLinkToIssue: function (router, issueId) {
        var link = '<a href="{0}">' + Uni.I18n.translate('devicechannels.validationblocks', 'MDC', 'Validation blocks').toLowerCase() + '</a>';
        // todo Replace 'workspace/datacollectionissues/{issueId}' with correct route to validation issue
        return Ext.String.format(link, router.getRoute('workspace/datacollectionissues/{issueId}').buildUrl({issueId: issueId}));
    },

    showGraphView: function (channelRecord, dataStore) {
        var me = this,
            container = this.getDeviceLoadProfileChannelGraphView(),
            zoomLevelsStore = me.getStore('Mdc.store.DataIntervalAndZoomLevels'),
            channelName = channelRecord.get('name'),
            unitOfMeasure = channelRecord.get('unitOfMeasure').unit,
            seriesObject = {marker: {
                enabled: false
            },
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
            series = [],
            intervalRecord,
            zoomLevels,
            intervalLengthInMs;

        seriesObject['data'] = [];

        intervalRecord = zoomLevelsStore.getIntervalRecord(channelRecord.get('interval'));
        intervalLengthInMs = zoomLevelsStore.getIntervalInMs(channelRecord.get('interval'));
        zoomLevels = intervalRecord.get('zoomLevels');

        switch (channelRecord.get('flowUnit')) {
            case 'flow':
                seriesObject['type'] = 'line';
                seriesObject['step'] = false;
                break;
            case 'volume':
                seriesObject['type'] = 'column';
                seriesObject['step'] = true;
                break;
        }

        if (dataStore.getTotalCount() > 0) {
            var data = me.formatData(dataStore, channelRecord);
            seriesObject['data'] = data.data;
            seriesObject['turboThreshold'] = 2000;

            series.push(seriesObject);
            Ext.suspendLayouts();
            container.down('#graphContainer').show();
            container.down('#ctr-graph-no-data').hide();
            container.drawGraph(yAxis, series, intervalLengthInMs, channelName, unitOfMeasure, zoomLevels, data.missedValues);
            Ext.resumeLayouts(true);
        } else {
            Ext.suspendLayouts();
            container.down('#graphContainer').hide();
            container.down('#ctr-graph-no-data').show();
            Ext.resumeLayouts(true);
        }
        me.getPage().doLayout();
    },

    formatData: function (dataStore, channelRecord) {
        var data = [];
        var missedValues = [];
        var mesurementType = channelRecord.get('unitOfMeasure');

        var missedColor = 'rgba(235, 86, 66, 0.3)';
        var okColor = "#70BB51"; // "#70BB51"
        var estColor = "#568343"; // "#568343"
        var susColor = 'rgba(235, 86, 66, 1)';//"#eb5642"; // "#eb5642"
        var infColor = "#dedc49"; // "#dedc49"
        var edColor = "rgba(0,0,0,0)"; //"#00aaaa"
        var nvalColor = "#71adc7";
        var tokColor = 'rgba(255, 255, 255, 0.85)';
        var testColor = 'rgba(86, 131, 67, 0.3)';
        var tsusColor = 'rgba(235, 86, 66, 0.3)';
        var tinfColor = 'rgba(222, 220, 73, 0.3)';
        var tedColor = 'rgba(255, 255, 255, 0.85)';
        var tnvalColor = 'rgba(0, 131, 200, 0.3)';

        dataStore.each(function (record) {
            var point = {};
            var deltaValidationInfo = record.getDeltaValidationInfo();
            var bulkValidationInfo = record.getBulkValidationInfo();
            var deltaModificationFlag = deltaValidationInfo ? deltaValidationInfo.get('modificationFlag') : null;
            var bulkModificationFlag = bulkValidationInfo ? bulkValidationInfo.get('modificationFlag') : null;
            var informative = false;
            var suspect = false;
            var confirmed = false;
            var estimated = false;
            var edited = deltaModificationFlag || bulkModificationFlag;
            var properties = record.get('readingProperties');

            point.x = record.get('interval').start;
            point.y = parseFloat(record.get('value'));
            point.intervalEnd = record.get('interval').end;
            point.collectedValue = record.get('collectedValue');
            point.mesurementType = mesurementType;
            point.color = okColor;
            point.tooltipColor = tokColor;

            if (properties.delta.informative || properties.bulk.informative) {
                point.color = infColor;
                point.tooltipColor = tinfColor
            }
            if (properties.delta.suspect || properties.bulk.suspect) {
                point.color = susColor;
                point.tooltipColor = tsusColor
            }
            if (properties.delta.notValidated || properties.bulk.notValidated) {
                point.color = nvalColor;
                point.tooltipColor = tnvalColor
            }
            if (estimated) {
                point.color = estColor;
                point.tooltipColor = tnvalColor
            }
            if (confirmed) {
                point.color = okColor;
                point.tooltipColor = tokColor
            }
            if (edited) {
                point.color = okColor;
                point.tooltipColor = tokColor
            }
            Ext.merge(point, properties);
            data.unshift(point);
            if (!point.y) {
                missedValues.push({
                    from: record.get('interval').start,
                    to: record.get('interval').end,
                    color: missedColor
                })
            }
        });
        return {data: data, missedValues: missedValues};
    },

    setDefaults: function (channel, dataIntervalAndZoomLevels, viewOnlySuspects) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            all = dataIntervalAndZoomLevels.get('all'),
            intervalStart = dataIntervalAndZoomLevels.getIntervalStart((channel.get('lastReading') || new Date().getTime()));
        router.filter = Ext.create('Mdc.model.ChannelOfLoadProfilesOfDeviceDataFilter');
        router.filter.beginEdit();
        router.filter.set('intervalStart', intervalStart);
        router.filter.set('duration', all.count + all.timeUnit);
        router.filter.set('onlySuspect', viewOnlySuspects);
        router.filter.set('onlyNonSuspect', false);
        router.filter.endEdit();
        me.getSideFilter().down('#suspect').setValue(viewOnlySuspects);
    },

    onTabChange: function (tabPanel, newTab) {
        var router = this.getController('Uni.controller.history.Router'),
            routeParams = router.arguments,
            route,
            filterParams = {};
        if (newTab.itemId === 'deviceLoadProfileChannelData') {

            filterParams.onlySuspect = false;
            route = 'devices/device/channels/channeldata';
            route && (route = router.getRoute(route));
            route && route.forward(routeParams, filterParams);

        } else if (newTab.itemId === 'channel-specifications') {
            route = 'devices/device/channels/channel';
            route && (route = router.getRoute(route));
            route && route.forward(routeParams, filterParams);
        }
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            previewPanel = me.getDeviceLoadProfileChannelDataPreview();
        previewPanel.updateForm(record)
    },

    applyFilter: function () {
        Ext.ComponentQuery.query('viewport > #contentPanel')[0].setLoading();
        var filterForm = this.getSideFilterForm();
        filterForm.updateRecord();
        filterForm.getRecord().save();
    },

    clearFilter: function () {
        this.getSideFilterForm().getRecord().getProxy().destroy();
    },

    removeFilterItem: function (key) {
        var router = this.getController('Uni.controller.history.Router'),
            record = router.filter;

        if (key === 'onlySuspect' || key === 'onlyNonSuspect') {
            record.set(key, false);
        }
        record.save();
    },

    onGraphResize: function (graphView, width, height) {
        if (graphView.chart) {
            graphView.chart.setSize(width, height, false);
        }
    }


});

