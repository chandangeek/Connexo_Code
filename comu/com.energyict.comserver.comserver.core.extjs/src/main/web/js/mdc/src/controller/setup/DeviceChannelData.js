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
                select: this.showPreview,
                beforeedit: this.beforeEditRecord,
                edit: this.resumeEditorFieldValidation,
                canceledit: this.resumeEditorFieldValidation
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
            },
            'deviceLoadProfileChannelDataActionMenu': {
                click: this.chooseAction
            },
            '#deviceLoadProfileChannelData #save-changes-button': {
                click: this.saveChannelDataChanges
            },
            '#deviceLoadProfileChannelData #undo-button': {
                click: this.undoChannelDataChanges
            }
        });
    },

    showSpecifications: function (mRID, channelId) {
        var me = this;
        me.showOverview({mRID: mRID, channelId: channelId}, 'spec')
    },

    showData: function (mRID, channelId) {
        var me = this;
        me.showOverview({mRID: mRID, channelId: channelId}, 'data')
    },

    showValidationBlocks: function (mRID, channelId, issueId) {
        var me = this;
        me.getStore('Mdc.store.ValidationBlocks').load({
            callback: function () {
                me.showOverview({mRID: mRID, channelId: channelId, issueId: issueId}, 'block');
            }
        });
    },

    showOverview: function (params, contentName) {
        var me = this,
            mRID = params['mRID'],
            channelId = params['channelId'],
            issueId = params['issueId'],
            device = me.getModel('Mdc.model.Device'),
            viewport = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            channel = me.getModel('Mdc.model.ChannelOfLoadProfilesOfDevice'),
            router = me.getController('Uni.controller.history.Router'),
            prevNextstore = contentName == 'block' ? 'Mdc.store.ValidationBlocks' : 'Mdc.store.ChannelsOfLoadProfilesOfDevice',
            prevNextListLink = contentName == 'block' ? me.makeLinkToIssue(router, issueId) : me.makeLinkToChannels(router),
            indexLocation = contentName == 'block' ? 'queryParams' : 'arguments',
            routerIdArgument = contentName == 'block' ? 'validationBlock' : 'channelId',
            activeTab = contentName == 'spec' ? 0 : 1;

        viewport.setLoading(true);
        device.load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                channel.getProxy().setUrl({
                    mRID: mRID
                });
                channel.load(channelId, {
                    success: function (channel) {
                        if (contentName == 'block' && router.filter && router.filter.get('duration').length == 0 && router.queryParams['validationBlock']) {
                            var intervalStore = me.getStore('Mdc.store.DataIntervalAndZoomLevels'),
                                intervalRecord = intervalStore.getIntervalRecord(channel.get('interval')),
                                all = intervalRecord.get('all'),
                                duration =  intervalStore.getIntervalRecord(intervalRecord.get('all')).get('intervalInMs');
                            var startDate = parseInt(router.queryParams['validationBlock']) - Math.floor(duration / 2),
                                intervalStart = Ext.Date.format(new Date(startDate), 'Y-m-dTH:i:s');
                            router.filter.set('intervalStart', intervalStart);
                            router.filter.set('duration', all.count + all.timeUnit);
                        }
                        me.getApplication().fireEvent('channelOfLoadProfileOfDeviceLoad', channel);
                        var widget = Ext.widget('tabbedDeviceChannelsView', {
                            router: router,
                            channel: channel,
                            device: device,
                            contentName: contentName,
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
        var viewport = Ext.ComponentQuery.query('viewport > #contentPanel')[0];
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
            dataStore.rejectChanges();
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
            seriesObject['turboThreshold'] = Number.MAX_VALUE;

            series.push(seriesObject);
            Ext.suspendLayouts();
            container.down('#graphContainer').show();
            container.drawGraph(yAxis, series, intervalLengthInMs, channelName, unitOfMeasure, zoomLevels, data.missedValues);
            Ext.resumeLayouts(true);
        } else {
            Ext.suspendLayouts();
            container.down('#graphContainer').hide();
            Ext.resumeLayouts(true);
        }
        me.getPage().doLayout();
    },

    formatData: function (dataStore, channelRecord) {
        var data = [];
        var missedValues = [];
        var mesurementType = channelRecord.get('unitOfMeasure');

        var missedColor = 'rgba(235, 86, 66, 0.3)';
        var okColor = "#70BB51";
        var estimatedColor = "#568343";
        var suspectColor = 'rgba(235, 86, 66, 1)';
        var informativeColor = "#dedc49";
        var editedColor = "rgba(0,0,0,0)";
        var notValidatedColor = "#71adc7";
        var tooltipOkColor = 'rgba(255, 255, 255, 0.85)';
        var tooltipEstimatedColor = 'rgba(86, 131, 67, 0.3)';
        var tooltipSuspectColor = 'rgba(235, 86, 66, 0.3)';
        var tooltipInformativeColor = 'rgba(222, 220, 73, 0.3)';
        var tooltipEditedColor = 'rgba(255, 255, 255, 0.85)';
        var tooltipNotValidatedColor = 'rgba(0, 131, 200, 0.3)';

        dataStore.each(function (record) {
            var point = {};
            var deltaValidationInfo = record.getValidationInfo().getMainValidationInfo();
            var bulkValidationInfo = record.getValidationInfo().getBulkValidationInfo();
            var deltaModificationFlag = deltaValidationInfo ? deltaValidationInfo.get('modificationFlag') : null;
            var bulkModificationFlag = bulkValidationInfo ? bulkValidationInfo.get('modificationFlag') : null;
            var properties = record.get('readingProperties');

            point.x = record.get('interval').start;
            point.id = point.x;
            point.y = parseFloat(record.get('value'));
            point.intervalEnd = record.get('interval').end;
            point.collectedValue = record.get('collectedValue');
            point.mesurementType = mesurementType;
            point.color = okColor;
            point.tooltipColor = tooltipOkColor;

            if (properties.delta.informative || properties.bulk.informative) {
                point.color = informativeColor;
                point.tooltipColor = tooltipInformativeColor
            }
            if (properties.delta.suspect || properties.bulk.suspect) {
                point.color = suspectColor;
                point.tooltipColor = tooltipSuspectColor
            }
            if (properties.delta.notValidated || properties.bulk.notValidated) {
                point.color = notValidatedColor;
                point.tooltipColor = tooltipNotValidatedColor
            }
            if (record.get('modificationFlag')) {
                point.edited = true;
            }

           /* if (estimated) {
                point.color = estimatedColor;
                point.tooltipColor = tooltipNotValidatedColor
            }
            if (confirmed) {
                point.color = okColor;
                point.tooltipColor = tooltipOkColor
            }*/

            Ext.merge(point, properties);
            data.unshift(point);

            !point.y && (point.y = null);
            if (!point.y) {
                missedValues.push({
                    id: record.get('interval').start,
                    from: record.get('interval').start,
                    to: record.get('interval').end,
                    color: missedColor
                });
                record.set('plotBand', true);
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
            previewPanel = me.getDeviceLoadProfileChannelDataPreview(),
            mainStatus = record.getValidationInfo().getMainValidationInfo().get('validationResult').split('.')[1],
            bulkStatus = record.getValidationInfo().getBulkValidationInfo().get('validationResult').split('.')[1];

        previewPanel.updateForm(record);
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
    },

    chooseAction: function (menu, item) {
        var me = this,
            point,
            chart = me.getPage().down('#deviceLoadProfileChannelGraphView').chart;

        switch (item.action) {
            case 'editValue':
                me.getPage().down('#deviceLoadProfileChannelDataGrid').getPlugin('cellplugin').startEdit(menu.record, 1);
                break;
            case 'removeReading':
                menu.record.set('value', null);
                menu.record.set('intervalFlags', []);

                chart.xAxis[0].addPlotBand({
                    id: menu.record.get('interval').start,
                    from: menu.record.get('interval').start,
                    to: menu.record.get('interval').end,
                    color: 'rgba(235, 86, 66, 0.3)'
                });

                menu.record.set('plotBand', true);
                point = chart.get(menu.record.get('interval').start);
                point.update(Ext.apply(point, { y: null }));
                me.showButtons();
                break;
        }
    },

    showButtons: function () {
        var me = this;

        me.getPage().down('#save-changes-button').show();
        me.getPage().down('#undo-button').show();
    },

    saveChannelDataChanges: function () {
        var me = this,
            page = me.getPage(),
            router = me.getController('Uni.controller.history.Router'),
            changedData = me.getChangedData(me.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData'));

        if (changedData.length) {
            page.setLoading(Uni.I18n.translate('general.saving', 'MDC', 'Saving...'));
            Ext.Ajax.request({
                url: Ext.String.format('/api/ddr/devices/{0}/channels/{1}/data', encodeURIComponent(router.arguments.mRID), router.arguments.channelId),
                method: 'PUT',
                jsonData: Ext.encode(changedData),
                timeout: 300000,
                callback: function () {
                    page.setLoading(false);
                },
                success: function () {
                    router.getRoute('devices/device/channels/channeldata').forward(router.arguments, router.queryParams);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('devicechannels.successSavingMessage', 'MDC', 'Channel data have been saved'));
                },
                failure: function (response) {
                    var failureResponseText;

                    if (response.status == 400) {
                        failureResponseText = Ext.decode(response.responseText, true);

                        if (failureResponseText) {
                            Ext.create('Uni.view.window.Confirmation', {
                                confirmText: Uni.I18n.translate('general.retry', 'MDC', 'Retry'),
                                closeAction: 'destroy',
                                confirmation: function () {
                                    this.close();
                                    me.saveChannelDataChanges();
                                },
                                cancellation: function () {
                                    this.close();
                                    router.getRoute('devices/device/channels/channeldata').forward(router.arguments, router.queryParams);
                                }
                            }).show({
                                msg: failureResponseText.message ? failureResponseText.message :
                                    Uni.I18n.translate('general.emptyField', 'MDC', 'Value field can not be empty'),
                                title: failureResponseText.error ? failureResponseText.error :
                                    Uni.I18n.translate('general.during.editing', 'MDC,', 'Error during editing')
                            });
                        }
                    }
                }
            });
        }
    },

    undoChannelDataChanges: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('devices/device/channels/channeldata').forward(router.arguments, router.queryParams);
    },

    getChangedData: function (store) {
        var changedData = [];

        Ext.Array.each(store.getUpdatedRecords(), function (record) {
            changedData.push(_.pick(record.getData(), 'interval', 'value'));
        });

        return changedData;
    },

    beforeEditRecord: function (editor, event) {
        var intervalFlags = event.record.get('intervalFlags');

        event.column.getEditor().allowBlank = !(intervalFlags && intervalFlags.length);
    },

    resumeEditorFieldValidation: function (editor, event) {
        var me = this,
            chart = me.getPage().down('#deviceLoadProfileChannelGraphView').chart,
            point,
            updatedObj;

        event.column.getEditor().allowBlank = true;

        if (event.record.isModified('value')) {
            me.getPage().down('#save-changes-button').isHidden() && me.showButtons();
            point = chart.get(event.record.get('interval').start);
            if (!event.record.get('value')) {
                chart.xAxis[0].addPlotBand({
                    id: event.record.get('interval').start,
                    from: event.record.get('interval').start,
                    to: event.record.get('interval').end,
                    color: 'rgba(235, 86, 66, 0.3)'
                });
                event.record.set('plotBand', true);
                point.update(Ext.apply(point, { y: null }));
            } else {
                if (event.record.get('plotBand')) {
                    chart.xAxis[0].removePlotBand(event.record.get('interval').start);
                    event.record.get('plotBand', false);
                }
                updatedObj = {
                    y: parseFloat(event.record.get('value')),
                    color: 'rgba(112,187,81,0.3)',
                    edited: true
                };
                point.update(Ext.apply(point, updatedObj));
            }
        }
    }
});

