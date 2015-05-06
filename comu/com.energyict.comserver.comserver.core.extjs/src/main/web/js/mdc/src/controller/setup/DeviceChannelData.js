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
        'Mdc.store.Clipboard'
    ],

    refs: [
        {
            ref: 'deviceLoadProfileChannelGraphView',
            selector: '#deviceLoadProfileChannelGraphView'
        },
        {
            ref: 'page',
            selector: '#deviceLoadProfileChannelData'
        },
        {
            ref: 'sideFilter',
            selector: '#deviceLoadProfileChannelDataSideFilter'
        }
    ],

    channelModel: null,

    init: function () {
        this.control({
            'deviceLoadProfileChannelData #deviceLoadProfileChannelTableViewBtn': {
                click: this.toggleView
            },
            'deviceLoadProfileChannelData #deviceLoadProfileChannelGraphViewBtn': {
                click: this.toggleView
            },
            'deviceLoadProfileChannelData #deviceLoadProfileChannelDataGrid': {
                select: this.showPreview
            },
            'deviceLoadProfileChannelDataSideFilter #deviceLoadProfileDataFilterApplyBtn': {
                click: this.applyFilter
            },
            'deviceLoadProfileChannelDataSideFilter #deviceLoadProfileDataFilterResetBtn': {
                click: this.clearFilter
            },
            'deviceLoadProfileChannelData #deviceLoadProfileChannelGraphView': {
                resize: this.onGraphResize
            },
            'deviceLoadProfileChannelData #deviceloadprofileschanneldatafilterpanel': {
                removeFilter: this.removeFilterItem,
                clearAllFilters: this.clearFilter
            }
        });
    },

    showSpecView: function (mRID, channelId) {
        this.showOverview(mRID, channelId, 0)
    },

    showDataView: function (mRID, channelId) {
        this.showOverview(mRID, channelId, 1)
    },

    showOverview: function (mRID, channelId, activeTab) {
        var me = this,
            device = me.getModel('Mdc.model.Device'),
            channel = me.getModel('Mdc.model.ChannelOfLoadProfilesOfDevice'),
            router = me.getController('Uni.controller.history.Router'),
            dataStore = me.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData');

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
                            channelsListLink: me.makeLinkToList(router),
                            activeTab: activeTab
                        });

                        var dataIntervalAndZoomLevels = me.getStore('Mdc.store.DataIntervalAndZoomLevels').getIntervalRecord(channel.get('interval')),
                            durationsStore = me.getStore('Mdc.store.LoadProfileDataDurations'),
                            viewOnlySuspects;

                        durationsStore.loadData(dataIntervalAndZoomLevels.get('duration'));
                        widget.down('#deviceLoadProfileChannelDataSideFilter').setVisible(true);

                        if (Ext.isEmpty(router.filter.data.intervalStart)) {
                            viewOnlySuspects = (router.queryParams.onlySuspect === 'true');
                            me.setDefaults(channel, dataIntervalAndZoomLevels, viewOnlySuspects);
                            router.queryParams.onlySuspect = undefined;
                        }

                        me.getApplication().fireEvent('changecontentevent', widget);

                       me.loadReadings(channel, device)

                    }
                })
            }
        })
    },

    loadReadings: function (channel, device) {
        var me =this,
            dataStore = me.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData'),
            router = me.getController('Uni.controller.history.Router'),
            mRID = device.get('mRID'),
            channelId = channel.getId();
        dataStore.setFilterModel(router.filter);
        dataStore.getProxy().setUrl({
            mRID: mRID,
            channelId: channelId
        });
        dataStore.load({
            success: function () {
                me.showGraphView(channel, dataStore);
                me.getSideFilterForm().loadRecord(router.filter);
            }
        });
    },

    makeLinkToList: function (router) {
        var link = '<a href="{0}">' + Uni.I18n.translate('deviceloadprofiles.channels', 'MDC', 'Channels').toLowerCase() + '</a>',
            filter = this.getStore('Mdc.store.Clipboard').get('latest-device-channels-filter'),
            queryParams = filter ? {filter: filter} : null;
        return Ext.String.format(link, router.getRoute('devices/device/channels').buildUrl(null, queryParams));
    },

    showGraphView: function (channelRecord, dataStore) {
        var me = this,
            container = this.getDeviceLoadProfileChannelGraphView(),
//        dataStore = me.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData'),
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

//    if (dataStore.getTotalCount() > 0) {
        if (true) {
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

    setFilterView: function () {
        var filterForm = this.getSideFilterForm(),
            filterView = this.getFilterPanel(),
            intervalStartField = filterForm.down('[name=intervalStart]'),
            intervalEndField = filterForm.down('[name=duration]'),
            suspectField = filterForm.down('#suspect'),
            nonSuspectField = filterForm.down('#nonSuspect'),
            intervalStart = intervalStartField.getValue(),
            intervalEnd = intervalEndField.getRawValue(),
            suspect = suspectField.boxLabel,
            nonSuspect = nonSuspectField.boxLabel,
            eventDateText = '';
        eventDateText += intervalEnd + ' ' + intervalStartField.getFieldLabel().toLowerCase() + ' '
            + Uni.DateTime.formatDateShort(intervalStart);
        filterView.setFilter('eventDateChanged', filterForm.down('#dateContainer').getFieldLabel(), eventDateText, true);
        filterView.down('#Reset').setText('Reset');
        if (suspectField.getValue()) {
            filterView.setFilter('onlySuspect', filterForm.down('#suspectContainer').getFieldLabel(), suspect);
        }
        if (nonSuspectField.getValue()) {
            filterView.setFilter('onlyNonSuspect', filterForm.down('#suspectContainer').getFieldLabel(), nonSuspect);
        }

    }

});








//
//
//viewport = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
//    models = {
//        device: me.getModel('Mdc.model.Device'),
//        channel: me.getModel('Mdc.model.ChannelOfLoadProfilesOfDevice')
//    },
//    dataStore = me.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData'),
//    router = me.getController('Uni.controller.history.Router'),
//    widget,
//    tabWidget,
//    defer = {
//        param: null,
//        callback: null,
//        resolve: function (arg) {
//            arg && this.callback.apply(this, this.param)
//        },
//        setCallback: function (fn) {
//            this.callback = fn;
//            this.resolve(this.param)
//        },
//        setParam: function () {
//            this.param = arguments;
//            this.resolve(this.callback)
//        }
//    };
//
//viewport.setLoading();
//dataStore.getProxy().setUrl({
//    mRID: mRID,
//    channelId: channelId
//});
//
//models.device.load(mRID, {
//    success: function (record) {
//        me.getApplication().fireEvent('loadDevice', record);
//        defer.setParam(record)
//    }
//});
//
//models.channel.getProxy().setUrl({
//    mRID: mRID
//});
//
//models.channel.load(channelId, {
//    success: function (record) {
//        defer.setCallback(function (device) {
//            var dataIntervalAndZoomLevels = me.getStore('Mdc.store.DataIntervalAndZoomLevels').getIntervalRecord(record.get('interval')),
//                durationsStore = me.getStore('Mdc.store.LoadProfileDataDurations'),
//                viewOnlySuspects;
//
//            durationsStore.loadData(dataIntervalAndZoomLevels.get('duration'));
//            dataStore.loadData([], false);

//            tabWidget = Ext.widget('tabbedDeviceChannelsView', {
//                router: router,
//                device: device,
//                channelsListLink: me.makeLinkToList(router)
//            });
//
//            widget = Ext.widget('deviceLoadProfileChannelData', {
//                router: me.getController('Uni.controller.history.Router'),
//                channel: record,
//                device: device,
//                mRID: device.get('mRID'),
//                viewType: isTable ? 'table' : 'graph'
//            });
//            me.channelModel = record;
//
//            tabWidget.down('#channel-data').add(widget);
//            tabWidget.down('#deviceLoadProfileChannelDataSideFilter').setVisible(true);

//            me.getApplication().fireEvent('channelOfLoadProfileOfDeviceLoad', record);
//            me.getApplication().fireEvent('changecontentevent', tabWidget);

//            tabController.showTab(1);
//
//            Ext.suspendLayouts();
//            tabWidget.down('#channelTabPanel').setTitle(record.get('name'));
//            widget.down('#deviceLoadProfileChannelGraphViewBtn').setDisabled(!isTable);
//            widget.down('#deviceLoadProfileChannelTableViewBtn').setDisabled(isTable);
//            dataStore.on('load', function () {
//                if (!widget.isDestroyed) {
//                    if (!isTable) {
//                        me.showGraphView(record);
//                    }
//                }
//            }, me);
//            if (Ext.isEmpty(router.filter.data.intervalStart)) {
//                viewOnlySuspects = (router.queryParams.onlySuspect === 'true');
//                me.setDefaults(dataIntervalAndZoomLevels, viewOnlySuspects);
//                router.queryParams.onlySuspect = undefined;
//            }
//            dataStore.setFilterModel(router.filter);
//            me.getSideFilterForm().loadRecord(router.filter);
//            me.setFilterView();
//            viewport.setLoading();
//            dataStore.load(function () {
//                viewport.setLoading(false);
//            });
//            Ext.resumeLayouts(true);
//        });
//    }
//});
//},
//
//makeLinkToList: function (router) {
//    var link = '<a href="{0}">' + Uni.I18n.translate('deviceloadprofiles.channels', 'MDC', 'Channels').toLowerCase() + '</a>',
//        filter = this.getStore('Mdc.store.Clipboard').get('latest-device-channels-filter'),
//        queryParams = filter ? {filter: filter} : null;
//
//    return Ext.String.format(link, router.getRoute('devices/device/channels').buildUrl(null, queryParams));
//},
//
//showGraphView: function (channelRecord) {
//    var me = this,
//        container = this.getDeviceLoadProfileChannelGraphView(),
//        dataStore = me.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData'),
//        zoomLevelsStore = me.getStore('Mdc.store.DataIntervalAndZoomLevels'),
//        channelName = channelRecord.get('name'),
//        unitOfMeasure = channelRecord.get('unitOfMeasure').unit,
//        seriesObject = {marker: {
//            enabled: false
//        },
//            name: channelName
//        },
//        yAxis = {
//            opposite: false,
//            gridLineDashStyle: 'Dot',
//            showEmpty: false,
//            title: {
//                rotation: 270,
//                text: unitOfMeasure
//            }
//        },
//        series = [],
//        intervalRecord,
//        zoomLevels,
//        intervalLengthInMs;
//
//    seriesObject['data'] = [];
//
//    intervalRecord = zoomLevelsStore.getIntervalRecord(channelRecord.get('interval'));
//    intervalLengthInMs = zoomLevelsStore.getIntervalInMs(channelRecord.get('interval'));
//    zoomLevels = intervalRecord.get('zoomLevels');
//
//    switch (channelRecord.get('flowUnit')) {
//        case 'flow':
//            seriesObject['type'] = 'line';
//            seriesObject['step'] = false;
//            break;
//        case 'volume':
//            seriesObject['type'] = 'column';
//            seriesObject['step'] = true;
//            break;
//    }
//
//    if (dataStore.getTotalCount() > 0) {
//        dataStore.each(function (record) {
//            if (record.get('value')) {
//                seriesObject['data'].unshift([record.get('interval').start, parseFloat(record.get('value'))]);
//            } else {
//                seriesObject['data'].unshift([record.get('interval').start, null]);
//            }
//        });
//        series.push(seriesObject);
//        Ext.suspendLayouts();
//        container.down('#graphContainer').show();
//        container.down('#ctr-graph-no-data').hide();
//        container.drawGraph(yAxis, series, intervalLengthInMs, channelName, unitOfMeasure, zoomLevels);
//        Ext.resumeLayouts(true);
//    } else {
//        Ext.suspendLayouts();
//        container.down('#graphContainer').hide();
//        container.down('#ctr-graph-no-data').show();
//        Ext.resumeLayouts(true);
//    }
//    me.getPage().doLayout();
//},
//
//onGraphResize: function (graphView, width, height) {
//    if (graphView.chart) {
//        graphView.chart.setSize(width, height, false);
//    }
//},
//
//
//toggleView: function (button) {
//    var router = this.getController('Uni.controller.history.Router'),
//        showTable = button.action === 'showTableView';
//
//    if (showTable) {
//        router.getRoute('devices/device/channels/channeltableData').forward(router.arguments, router.queryParams);
//    } else {
//        router.getRoute('devices/device/channels/channeldata').forward(router.arguments, router.queryParams);
//    }
//
//},
//
//showPreview: function (selectionModel, record) {
//    var me = this,
//        previewPanel = me.getDeviceLoadProfileChannelDataPreview(),
//        bulkValueField = previewPanel.down('displayfield[name=collectedValue]'),
//        form = previewPanel.down('form'),
//        intervalEnd = record.get('interval_end');
//
//    Ext.suspendLayouts();
//    previewPanel.setTitle(Uni.DateTime.formatDateLong(intervalEnd)
//        + ' ' + Uni.I18n.translate('general.at', 'MDC', 'At').toLowerCase() + ' '
//        + Uni.DateTime.formatTimeLong(intervalEnd));
//    bulkValueField.setVisible(record.get('isBulk'));
//    form.loadRecord(record);
//    Ext.resumeLayouts(true);
//},
//
//applyFilter: function () {
//    var filterForm = this.getSideFilterForm();
//    filterForm.updateRecord();
//    filterForm.getRecord().save();
//},
//
//clearFilter: function () {
//    this.getSideFilterForm().getRecord().getProxy().destroy();
//},
//
//removeFilterItem: function (key) {
//    var router = this.getController('Uni.controller.history.Router'),
//        record = router.filter;
//
//    if (key === 'onlySuspect' || key === 'onlyNonSuspect') {
//        record.set(key, false);
//    }
//    record.save();
//},
//
//setDefaults: function (dataIntervalAndZoomLevels, viewOnlySuspects) {
//    var me = this,
//        router = me.getController('Uni.controller.history.Router'),
//        all = dataIntervalAndZoomLevels.get('all'),
//        intervalStart = dataIntervalAndZoomLevels.getIntervalStart((me.channelModel.get('lastReading') || new Date().getTime()));
//    router.filter = Ext.create('Mdc.model.ChannelOfLoadProfilesOfDeviceDataFilter');
//    router.filter.beginEdit();
//    router.filter.set('intervalStart', intervalStart);
//    router.filter.set('duration', all.count + all.timeUnit);
//    router.filter.set('onlySuspect', viewOnlySuspects);
//    router.filter.set('onlyNonSuspect', false);
//    router.filter.endEdit();
//    me.getSideFilter().down('#suspect').setValue(viewOnlySuspects);
//},
//
//setFilterView: function () {
//    var filterForm = this.getSideFilterForm(),
//        filterView = this.getFilterPanel(),
//        intervalStartField = filterForm.down('[name=intervalStart]'),
//        intervalEndField = filterForm.down('[name=duration]'),
//        suspectField = filterForm.down('#suspect'),
//        nonSuspectField = filterForm.down('#nonSuspect'),
//        intervalStart = intervalStartField.getValue(),
//        intervalEnd = intervalEndField.getRawValue(),
//        suspect = suspectField.boxLabel,
//        nonSuspect = nonSuspectField.boxLabel,
//        eventDateText = '';
//    eventDateText += intervalEnd + ' ' + intervalStartField.getFieldLabel().toLowerCase() + ' '
//        + Uni.DateTime.formatDateShort(intervalStart);
//    filterView.setFilter('eventDateChanged', filterForm.down('#dateContainer').getFieldLabel(), eventDateText, true);
//    filterView.down('#Reset').setText('Reset');
//    if (suspectField.getValue()) {
//        filterView.setFilter('onlySuspect', filterForm.down('#suspectContainer').getFieldLabel(), suspect);
//    }
//    if (nonSuspectField.getValue()) {
//        filterView.setFilter('onlyNonSuspect', filterForm.down('#suspectContainer').getFieldLabel(), nonSuspect);
//    }

//refs: [
//    {
//        ref: 'page',
//        selector: 'deviceLoadProfileChannelData'
//    },
//    {
//        ref: 'deviceLoadProfileChannelGraphView',
//        selector: '#deviceLoadProfileChannelGraphView'
//    },
//    {
//        ref: 'readingsCount',
//        selector: 'deviceLoadProfileChannelData #readingsCount'
//    },
//    {ref: 'deviceLoadProfileChannelDataPreview', selector: '#deviceLoadProfileChannelDataPreview'},
//    {
//        ref: 'sideFilter',
//        selector: '#deviceLoadProfileChannelDataSideFilter'
//    },
//    {
//        ref: 'sideFilterForm',
//        selector: '#deviceLoadProfileChannelDataFilterForm'
//    },
//    {
//        ref: 'filterPanel',
//        selector: 'deviceLoadProfileChannelData filter-top-panel'
//    }
//],