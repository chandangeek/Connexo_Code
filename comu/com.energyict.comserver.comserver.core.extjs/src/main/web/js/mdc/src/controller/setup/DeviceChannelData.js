Ext.define('Mdc.controller.setup.DeviceChannelData', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.devicechannels.TabbedDeviceChannelsView',
        'Mdc.view.setup.devicechannels.Overview',
        'Mdc.view.setup.devicechannels.ReadingEstimationWindow'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice',
        'Mdc.model.ChannelOfLoadProfilesOfDevice',
        'Mdc.model.ChannelOfLoadProfilesOfDeviceDataFilter',
        'Mdc.model.DeviceChannelDataEstimate'
    ],

    stores: [
        'Mdc.store.ChannelOfLoadProfileOfDeviceData',
        'Mdc.store.DataIntervalAndZoomLevels',
        'Mdc.store.LoadProfileDataDurations',
        'Mdc.store.Clipboard',
        'Mdc.store.Estimators',
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
        },
        {
            ref: 'readingEstimationWindow',
            selector: 'reading-estimation-window'
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
                beforeshow: this.checkSuspect,
                click: this.chooseAction
            },
            '#deviceLoadProfileChannelData #save-changes-button': {
                click: this.saveChannelDataChanges
            },
            '#deviceLoadProfileChannelData #undo-button': {
                click: this.undoChannelDataChanges
            },
            '#estimate-reading-button': {
                click: this.estimateReading
            },
            'channel-data-bulk-action-menu': {
                beforeshow: this.checkSuspects,
                click: this.chooseBulkAction
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
        var me = this,
            validationBlocksStore = me.getStore('Mdc.store.ValidationBlocks');

        validationBlocksStore.getProxy().setUrl(mRID, channelId, issueId);
        validationBlocksStore.load({
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
            isFullTotalCount = contentName === 'block',
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
                            routerIdArgument: routerIdArgument,
                            isFullTotalCount: isFullTotalCount
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
        return Ext.String.format(link, router.getRoute('workspace/datavalidationissues/view').buildUrl({issueId: issueId}));
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
        var data = [],
            missedValues = [],
            mesurementType = channelRecord.get('unitOfMeasure'),
            okColor = "#70BB51",
            estimatedColor = "#568343",
            suspectColor = 'rgba(235, 86, 66, 1)',
            informativeColor = "#dedc49",
            notValidatedColor = "#71adc7",
            tooltipOkColor = 'rgba(255, 255, 255, 0.85)',
            tooltipSuspectColor = 'rgba(235, 86, 66, 0.3)',
            tooltipEstimatedColor = 'rgba(86, 131, 67, 0.3)',
            tooltipInformativeColor = 'rgba(222, 220, 73, 0.3)',
            tooltipNotValidatedColor = 'rgba(0, 131, 200, 0.3)';

        dataStore.each(function (record) {
            var point = {},
                mainValidationInfo = record.getValidationInfo().getMainValidationInfo(),
                bulkValidationInfo = record.getValidationInfo().getBulkValidationInfo(),
                properties = record.get('readingProperties');

            point.x = record.get('interval').start;
            point.id = point.x;
            point.y = parseFloat(record.get('value'));
            point.intervalEnd = record.get('interval').end;
            point.collectedValue = record.get('collectedValue');
            point.mesurementType = mesurementType;
            point.color = okColor;
            point.tooltipColor = tooltipOkColor;

            if (mainValidationInfo.get('valueModificationFlag') == 'EDITED') {
                point.edited = true;
            } else if (mainValidationInfo.get('estimatedByRule')) {
                point.color = estimatedColor;
                point.tooltipColor = tooltipEstimatedColor;
            } else if (properties.delta.notValidated) {
                point.color = notValidatedColor;
                point.tooltipColor = tooltipNotValidatedColor
            } else if (properties.delta.suspect) {
                point.color = suspectColor;
                point.tooltipColor = tooltipSuspectColor
            } else if (properties.delta.informative) {
                point.color = informativeColor;
                point.tooltipColor = tooltipInformativeColor;
            }

            if (bulkValidationInfo.get('valueModificationFlag') == 'EDITED') {
                point.bulkEdited = true;
            }

            Ext.merge(point, properties);
            data.unshift(point);
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
            grid = me.getPage().down('deviceLoadProfileChannelDataGrid'),
            chart = me.getPage().down('#deviceLoadProfileChannelGraphView').chart;

        switch (item.action) {
            case 'editValue':
                me.getPage().down('#deviceLoadProfileChannelDataGrid').getPlugin('cellplugin').startEdit(menu.record, 1);
                break;
            case 'removeReading':
                menu.record.set('value', null);
                menu.record.set('intervalFlags', []);
                menu.record.get('confirmed') && menu.record.set('confirmed', false);
                menu.record.data.validationInfo.mainValidationInfo.validationResult = 'validationStatus.ok';
                grid.getView().refreshNode(grid.getStore().indexOf(menu.record));
                point = chart.get(menu.record.get('interval').start);
                point.update({ y: NaN });
                me.showButtons();
                break;
            case 'estimateValue':
                me.estimateValue(menu.record);
                break;
            case 'confirmValue':
                me.confirmValue(menu.record, false);
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
            router = me.getController('Uni.controller.history.Router'),
            changedData = me.getChangedData(me.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData'));

        me.getPage().setLoading();
        if (!Ext.isEmpty(changedData)) {
            Ext.Ajax.request({
                url: Ext.String.format('/api/ddr/devices/{0}/channels/{1}/data', encodeURIComponent(router.arguments.mRID), router.arguments.channelId),
                method: 'PUT',
                jsonData: Ext.encode(changedData),
                timeout: 300000,
                success: function () {
                    router.getRoute().forward(router.arguments, router.queryParams);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('devicechannels.successSavingMessage', 'MDC', 'Channel data have been saved'));
                },
                failure: function (response) {
                    me.getPage().setLoading(false);
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
                                    router.getRoute().forward(router.arguments, router.queryParams);
                                }
                            }).show({
                                msg: failureResponseText.message ? failureResponseText.message :
                                    Uni.I18n.translate('general.emptyField', 'MDC', 'Value field can not be empty'),
                                title: failureResponseText.error ? failureResponseText.error :
                                    Uni.I18n.translate('general.during.editing', 'MDC', 'Error during editing')
                            });
                        }
                    }
                }
            });
        }
    },

    undoChannelDataChanges: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute().forward(router.arguments, router.queryParams);
    },

    getChangedData: function (store) {
        var changedData = [],
            confirmedObj;

        Ext.Array.each(store.getUpdatedRecords(), function (record) {
            if (record.get('confirmed')) {
                confirmedObj = {
                    interval: record.get('interval'),
                    validationInfo: {
                        mainValidationInfo: {
                            isConfirmed: record.data.validationInfo.mainValidationInfo.confirmedNotSaved || false
                        },
                        bulkValidationInfo: {
                            isConfirmed: record.data.validationInfo.bulkValidationInfo.confirmedNotSaved || false
                        }
                    }
                };
                changedData.push(confirmedObj);
            } else if (record.isModified('value')) {
                changedData.push(_.pick(record.getData(), 'interval', 'value'));
            } else if (record.isModified('collectedValue')) {
                changedData.push(_.pick(record.getData(), 'interval', 'collectedValue'));
            }
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
            point = chart.get(event.record.get('interval').start),
            grid = me.getPage().down('deviceLoadProfileChannelDataGrid'),
            value = parseFloat(event.record.get('value')),
            collectedValue = event.record.get('collectedValue'),
            condition = (isNaN(point.y) && isNaN(value)) ? false : (point.y != value),
            updatedObj;

        if (event.column) {
            event.column.getEditor().allowBlank = true;
        }

        if (event.record.isModified('value')) {
            me.getPage().down('#save-changes-button').isHidden() && me.showButtons();

            if (!event.record.get('value')) {
                point.update({ y: NaN });
            } else {
                updatedObj = {
                    y: value,
                    collectedValue: collectedValue,
                    color: 'rgba(112,187,81,0.3)'
                };
                point.update(updatedObj);
            }

            if (event.column) {
                event.record.data.validationInfo.mainValidationInfo.validationResult = 'validationStatus.ok';
                grid.getView().refreshNode(grid.getStore().indexOf(event.record));
                event.record.get('confirmed') && event.record.set('confirmed', false);
            }
        } else if (condition) {
            me.resetChanges(event.record, point);
        }
    },

    resetChanges: function (record, point) {
        var me = this,
            properties = record.get('readingProperties'),
            grid = me.getPage().down('deviceLoadProfileChannelDataGrid'),
            store = grid.getStore(),
            color = '#70BB51';

        if (record.getValidationInfo().getMainValidationInfo().get('estimatedByRule')) {
            color = '#568343';
        } else if (properties.delta.notValidated) {
            color = '#71adc7';
        } else if (properties.delta.suspect) {
            color = 'rgba(235, 86, 66, 1)';
        } else if (properties.delta.informative) {
            color = '#dedc49';
        }
        record.data.validationInfo.mainValidationInfo.validationResult = record.getValidationInfo().getMainValidationInfo().get('validationResult');
        record.data.validationInfo.bulkValidationInfo.validationResult = record.getValidationInfo().getBulkValidationInfo().get('validationResult');
        record.get('confirmed') && record.set('confirmed', false);
        grid.getView().refreshNode(store.indexOf(record));
        point.update({
            y: parseFloat(record.get('value')),
            collectedValue: record.get('collectedValue'),
            color: color
        });
        record.reject();
        if (!store.getUpdatedRecords().length) {
            me.getPage().down('#save-changes-button').hide();
            me.getPage().down('#undo-button').hide();
        }
    },

    estimateValue: function (record) {
        var me = this,
            bothSuspected = false,
            mainValueSuspect = false,
            bulkValueSuspect = false;

        if (!Ext.isArray(record)) {
            bothSuspected = record.getValidationInfo().getMainValidationInfo().get('validationResult').split('.')[1] == 'suspect' &&
                record.getValidationInfo().getBulkValidationInfo().get('validationResult').split('.')[1] == 'suspect';
        } else {
            Ext.Array.findBy(record, function (item) {
                mainValueSuspect = item.getValidationInfo().getMainValidationInfo().get('validationResult').split('.')[1] == 'suspect';
                return mainValueSuspect;
            });
            Ext.Array.findBy(record, function (item) {
                bulkValueSuspect = item.getValidationInfo().getBulkValidationInfo().get('validationResult').split('.')[1] == 'suspect';
                return bulkValueSuspect;
            });
            bothSuspected = mainValueSuspect && bulkValueSuspect;
        }
        me.getPage().setLoading();
        me.getStore('Mdc.store.Estimators').load(function () {
            me.getPage().setLoading(false);
            Ext.widget('reading-estimation-window', {
                record: record,
                bothSuspected: bothSuspected
            }).show();
        });
    },

    estimateReading: function () {
        var me = this,
            propertyForm = me.getReadingEstimationWindow().down('#property-form'),
            model = Ext.create('Mdc.model.DeviceChannelDataEstimate'),
            estimateBulk = false,
            record = me.getReadingEstimationWindow().record,
            intervalsArray = [];

        !me.getReadingEstimationWindow().down('#error-label').isHidden() && me.getReadingEstimationWindow().down('#error-label').hide();

        if (propertyForm.getRecord()) {
            propertyForm.updateRecord();
            model.set('estimatorImpl', me.getReadingEstimationWindow().down('#estimator-field').getValue());
            model.propertiesStore = propertyForm.getRecord().properties();
        }
        if (!me.getReadingEstimationWindow().down('#value-to-estimate-radio-group').isHidden()) {
            estimateBulk = me.getReadingEstimationWindow().down('#value-to-estimate-radio-group').getValue().isBulk;
        } else {
            if (!Ext.isArray(record)) {
                estimateBulk = record.getValidationInfo().getBulkValidationInfo().get('validationResult').split('.')[1] == 'suspect';
            } else {
                Ext.Array.findBy(record, function (item) {
                    estimateBulk = item.getValidationInfo().getBulkValidationInfo().get('validationResult').split('.')[1] == 'suspect';
                    return estimateBulk;
                });
            }
        }
        if (!Ext.isArray(record)) {
            intervalsArray.push({
                start: record.get('interval').start,
                end: record.get('interval').end
            });
        } else {
            Ext.Array.each(record, function (item) {
                intervalsArray.push({
                    start: item.get('interval').start,
                    end: item.get('interval').end
                });
            });
        }
        model.set('estimateBulk', estimateBulk);
        model.set('intervals', intervalsArray);
        me.saveChannelDataEstimateModel(model, record);
    },

    saveChannelDataEstimateModel: function (record, readings) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        record.getProxy().setUrl(router.arguments);
        me.getReadingEstimationWindow().setLoading();
        Ext.Ajax.suspendEvent('requestexception');
        record.save({
            callback: function (rec, operation, success) {
                Ext.Ajax.resumeEvent('requestexception');
                var responseText = Ext.decode(operation.response.responseText, true);
                if (success) {
                    if (!Ext.isArray(readings)) {
                        me.updateEstimatedValues(record, readings, responseText[0]);
                    } else {
                        Ext.Array.each(responseText, function (estimatedReading) {
                            Ext.Array.findBy(readings, function (reading) {
                                if (estimatedReading.interval.start == reading.get('interval').start) {
                                    me.updateEstimatedValues(record, reading, estimatedReading);
                                    return true;
                                }
                            });
                        });
                    }
                    me.getReadingEstimationWindow().destroy();
                    me.getPage().down('#save-changes-button').isHidden() && me.showButtons();
                } else {
                    me.getReadingEstimationWindow().setLoading(false);
                    me.getReadingEstimationWindow().down('#error-label').show();
                    me.getReadingEstimationWindow().down('#error-label').setText('<div style="color: #FF0000">' + (responseText.message ? responseText.message : '') + '</div>', false);
                }
            }
        });
    },

    updateEstimatedValues: function (record, reading, estimatedReading) {
        var me = this,
            grid = me.getPage().down('deviceLoadProfileChannelDataGrid');

        if (record.get('estimateBulk')) {
            reading.set('collectedValue', estimatedReading.collectedValue);
            reading.data.validationInfo.bulkValidationInfo.validationResult = 'validationStatus.ok';
        } else {
            reading.set('value', estimatedReading.value);
            reading.data.validationInfo.mainValidationInfo.validationResult = 'validationStatus.ok';
        }
        grid.getView().refreshNode(grid.getStore().indexOf(reading));

        me.resumeEditorFieldValidation(grid.editingPlugin, {
            record: reading
        });
        reading.get('confirmed') && reading.set('confirmed', false);
    },

    checkSuspect: function (menu) {
        var mainStatus = menu.record.getValidationInfo().getMainValidationInfo().get('validationResult').split('.')[1] == 'suspect',
            bulkStatus = menu.record.getValidationInfo().getBulkValidationInfo().get('validationResult').split('.')[1] == 'suspect';

        menu.down('#estimate-value').setVisible(mainStatus || bulkStatus);
        if (menu.record.get('confirmed') || menu.record.isModified('value') || menu.record.isModified('collectedValue')) {
            menu.down('#confirm-value').hide();
        } else {
            menu.down('#confirm-value').setVisible(mainStatus || bulkStatus);
        }
    },

    checkSuspects: function (menu) {
        var me = this,
            records = me.getPage().down('deviceLoadProfileChannelDataGrid').getSelectionModel().getSelection(),
            mainValueSuspect,
            bulkValueSuspect;

        for (var i = 0; i < records.length; i++) {
            mainValueSuspect = records[i].getValidationInfo().getMainValidationInfo().get('validationResult').split('.')[1] == 'suspect';
            bulkValueSuspect = records[i].getValidationInfo().getBulkValidationInfo().get('validationResult').split('.')[1] == 'suspect';
            if (mainValueSuspect || bulkValueSuspect) {
                menu.down('#estimate-value').show();
                break;
            } else {
                menu.down('#estimate-value').hide();
            }
        }

        for (var j = 0; j < records.length; j++) {
            mainValueSuspect = records[j].getValidationInfo().getMainValidationInfo().get('validationResult').split('.')[1] == 'suspect';
            bulkValueSuspect = records[j].getValidationInfo().getBulkValidationInfo().get('validationResult').split('.')[1] == 'suspect';
            if (!records[j].get('confirmed') && !records[j].isModified('value') && !records[j].isModified('collectedValue') && (mainValueSuspect || bulkValueSuspect)) {
                menu.down('#confirm-value').show();
                break;
            } else {
                menu.down('#confirm-value').hide();
            }
        }
    },

    chooseBulkAction: function (menu, item) {
        var me = this,
            records = me.getPage().down('deviceLoadProfileChannelDataGrid').getSelectionModel().getSelection();

        switch (item.action) {
            case 'estimateValue':
                me.estimateValue(records);
                break;
            case 'confirmValue':
                me.confirmValue(records, true);
                break;
        }
    },

    confirmValue: function (record, isBulk) {
        var me = this,
            grid = me.getPage().down('deviceLoadProfileChannelDataGrid'),
            mainStatus,
            bulkStatus,
            isModified,
            chart = me.getPage().down('#deviceLoadProfileChannelGraphView').chart,
            func = function (rec) {
                isModified = rec.isModified('value') || rec.isModified('collectedValue');
                if (!rec.get('confirmed') && !isModified) {
                    mainStatus = rec.getValidationInfo().getMainValidationInfo().get('validationResult').split('.')[1] == 'suspect';
                    bulkStatus = rec.getValidationInfo().getBulkValidationInfo().get('validationResult').split('.')[1] == 'suspect';
                    if (mainStatus) {
                        rec.data.validationInfo.mainValidationInfo.confirmedNotSaved = true;
                        chart.get(rec.get('interval').start).update({ color: 'rgba(112,187,81,0.3)' });
                    }
                    if (bulkStatus) {
                        rec.data.validationInfo.bulkValidationInfo.confirmedNotSaved = true;
                    }
                    if (mainStatus || bulkStatus) {
                        grid.getView().refreshNode(grid.getStore().indexOf(rec));
                        rec.set('confirmed', true);
                    }
                }
            };

        if (isBulk) {
            Ext.Array.each(record, function (reading) {
                func(reading);
            });
        } else {
            func(record);
        }

        me.getPage().down('#save-changes-button').isHidden() && me.showButtons();
    }
});

