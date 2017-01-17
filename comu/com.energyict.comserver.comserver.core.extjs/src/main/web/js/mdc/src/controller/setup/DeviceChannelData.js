Ext.define('Mdc.controller.setup.DeviceChannelData', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.util.Common',
        'Uni.store.GasDayYearStart'
    ],

    views: [
        'Mdc.view.setup.devicechannels.TabbedDeviceChannelsView',
        'Mdc.view.setup.devicechannels.Overview',
        'Mdc.view.setup.devicechannels.ReadingEstimationWindow',
        'Mdc.view.setup.devicechannels.EditCustomAttributes'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LoadProfileOfDevice',
        'Mdc.model.ChannelOfLoadProfilesOfDevice',
        'Mdc.model.ChannelOfLoadProfilesOfDeviceDataFilter',
        'Mdc.model.DeviceChannelDataEstimate',
        'Mdc.customattributesonvaluesobjects.model.AttributeSetOnChannel'
    ],

    stores: [
        'Mdc.customattributesonvaluesobjects.store.ChannelCustomAttributeSets',
        'Mdc.store.ChannelOfLoadProfileOfDeviceData',
        'Uni.store.DataIntervalAndZoomLevels',
        'Mdc.store.LoadProfileDataDurations',
        'Mdc.store.Clipboard',
        'Mdc.store.Estimators',
        'Mdc.store.ValidationBlocks',
        'Mdc.store.TimeUnits',
        'Mdc.store.DataLoggerSlaveChannelHistory'
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
        },
        {
            ref: 'editPropertyForm',
            selector: '#deviceLoadProfileChannelsEditCustomAttributes property-form'
        },
        {
            ref: 'editCustomAttributesPanel',
            selector: '#deviceLoadProfileChannelsEditCustomAttributes'
        },
        {
            ref: 'editCustomAttributesRestoreBtn',
            selector: '#channelCustomAttributesRestoreBtn'
        }
    ],

    channelModel: null,
    fromSpecification: false,

    init: function () {
        this.control({
            '#deviceLoadProfileChannelData #deviceLoadProfileChannelDataGrid': {
                select: this.showPreview,
                beforeedit: this.beforeEditRecord,
                edit: this.resumeEditorFieldValidation,
                canceledit: this.resumeEditorFieldValidation,
                selectionchange: this.onDataGridSelectionChange
            },
            '#deviceLoadProfileChannelData #deviceLoadProfileChannelGraphView': {
                resize: this.onGraphResize
            },
            '#tabbedDeviceChannelsView #channelTabPanel': {
                tabchange: this.onTabChange
            },
            'deviceLoadProfileChannelDataActionMenu': {
                click: this.chooseAction
            },
            '#deviceLoadProfileChannelData #save-changes-button': {
                click: this.saveChannelDataChanges
            },
            '#deviceLoadProfileChannelData #undo-button': {
                click: this.undoChannelDataChanges
            },
            '#channel-reading-estimation-window #estimate-reading-button': {
                click: this.estimateReading
            },
            'channel-data-bulk-action-menu': {
                click: this.chooseBulkAction
            },
            '#deviceLoadProfileChannelsEditCustomAttributes #channelCustomAttributesSaveBtn': {
                click: this.saveChannelOfLoadProfileCustomAttributes
            },
            '#deviceLoadProfileChannelsEditCustomAttributes #channelCustomAttributesRestoreBtn': {
                click: this.restoreChannelOfLoadProfileCustomAttributes
            },
            '#deviceLoadProfileChannelsEditCustomAttributes #channelCustomAttributesCancelBtn': {
                click: this.toPreviousPage
            },
            '#deviceLoadProfileChannelsEditCustomAttributes #channel-custom-attributes-property-form': {
                showRestoreAllBtn: this.showRestoreAllBtn
            }
        });
    },

    showSpecifications: function (deviceId, channelId) {
        var me = this;
        me.fromSpecification = true;
        me.getController('Mdc.controller.setup.DeviceChannels').fromSpecification = true;
        me.showOverview({deviceId: deviceId, channelId: channelId}, 'spec');
    },

    showData: function (deviceId, channelId) {
        var me = this;
        me.showOverview({deviceId: deviceId, channelId: channelId}, 'data');
    },

    showValidationBlocks: function (deviceId, channelId, issueId) {
        var me = this,
            validationBlocksStore = me.getStore('Mdc.store.ValidationBlocks');

        validationBlocksStore.getProxy().setParams(deviceId, channelId, issueId);
        validationBlocksStore.load({
            callback: function () {
                me.showOverview({deviceId: deviceId, channelId: channelId, issueId: issueId}, 'block');
            }
        });
    },

    showOverview: function (params, contentName) {
        var me = this,
            deviceId = params['deviceId'],
            channelId = params['channelId'],
            issueId = params['issueId'],
            viewport = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            channelsView = viewport.down('#device-load-profile-channels-preview-container'),
            channelModel = me.getModel('Mdc.model.ChannelOfLoadProfilesOfDevice'),
            router = me.getController('Uni.controller.history.Router'),
            prevNextstore = contentName === 'block' ? 'Mdc.store.ValidationBlocks' : 'Mdc.store.ChannelsOfLoadProfilesOfDevice',
            prevNextListLink = contentName === 'block' ? me.makeLinkToIssue(router, issueId) : me.makeLinkToChannels(router),
            indexLocation = contentName === 'block' ? 'queryParams' : 'arguments',
            routerIdArgument = contentName === 'block' ? 'validationBlock' : 'channelId',
            isFullTotalCount = contentName === 'block',
            activeTab = contentName === 'spec' ? 0 : 1,
            timeUnitsStore = Ext.getStore('Mdc.store.TimeUnits'),
            slaveHistoryStore = me.getStore('Mdc.store.DataLoggerSlaveChannelHistory'),
            dependencyCounter = 3,
            onDependenciesLoad = function () {
                dependencyCounter--;
                if (!dependencyCounter) {
                    var widget = Ext.widget('tabbedDeviceChannelsView', {
                        title: channel.get('readingType').fullAliasName,
                        router: router,
                        channel: channel,
                        device: device,
                        contentName: contentName,
                        indexLocation: indexLocation,
                        prevNextListLink: prevNextListLink,
                        activeTab: activeTab,
                        prevNextstore: prevNextstore,
                        routerIdArgument: routerIdArgument,
                        isFullTotalCount: isFullTotalCount,
                        filterDefault: activeTab === 1 ? me.getDataFilter(channel, contentName, gasDayYearStart, router) : {},
                        mentionDataLoggerSlave: !Ext.isEmpty(device.get('isDataLogger')) && device.get('isDataLogger'),
                        dataLoggerSlaveHistoryStore: slaveHistoryStore
                    });
                    me.getApplication().fireEvent('changecontentevent', widget);
                    viewport.setLoading(false);
                    if (activeTab == 1) {
                        me.setupReadingsTab(device, channel, widget);
                    } else if (activeTab == 0) {
                        me.setupSpecificationsTab(device, channel, widget);
                    }
                }
            },
            device,
            channel,
            gasDayYearStart = undefined;

        viewport.setLoading(true);

        switch (prevNextstore) {
            case 'Mdc.store.ValidationBlocks':
                me.getStore(prevNextstore).getProxy().setParams(deviceId, channelId, issueId);
                break;
            case 'Mdc.store.ChannelsOfLoadProfilesOfDevice':
                me.getStore(prevNextstore).getProxy().setExtraParam('deviceId', deviceId);
                break;
        }

        if (channelsView) { // remove 'onload' handler to avoid changing channelId in router arguments
            channelsView.bindStore('ext-empty-store');
        }
        me.getStore(prevNextstore).load(onDependenciesLoad);

        if (contentName === 'spec') {
            dependencyCounter = dependencyCounter + 2;
            slaveHistoryStore.getProxy().setParams(deviceId, channelId);
            slaveHistoryStore.load(onDependenciesLoad);
            timeUnitsStore.load(onDependenciesLoad);
        }

        me.getModel('Mdc.model.Device').load(deviceId, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                device = record;
                onDependenciesLoad();
            }
        });

        channelModel.getProxy().setExtraParam('deviceId', deviceId);
        channelModel.load(channelId, {
            success: function (record) {
                me.getApplication().fireEvent('channelOfLoadProfileOfDeviceLoad', record);
                channel = record;
                if (channel.get('readingType').isGasRelated) {
                    var yearStartStore = me.getStore('Uni.store.GasDayYearStart');
                    yearStartStore.on('load',
                        function (store, records) {
                            gasDayYearStart = records[0];
                            onDependenciesLoad();
                        },
                        me, {single: true});
                    yearStartStore.load();
                } else {
                    onDependenciesLoad();
                }
            }
        });
    },

    setupSpecificationsTab: function (device, channel, widget) {
        var me = this,
            customAttributesStore = me.getStore('Mdc.customattributesonvaluesobjects.store.ChannelCustomAttributeSets'),
            calculatedReadingTypeField = widget.down('#calculatedReadingType'),
            multiplierField = widget.down('#mdc-channel-preview-multiplier'),
            menu = widget.down('#deviceLoadProfileChannelsActionMenu');

        customAttributesStore.getProxy().setParams(device.get('name'), channel.get('id'));
        Ext.suspendLayouts();
        widget.down('#deviceLoadProfileChannelsOverviewForm').loadRecord(channel);
        if (channel.get('calculatedReadingType')) {
            calculatedReadingTypeField.show();
        } else {
            calculatedReadingTypeField.hide();
        }
        if (channel.get('multiplier')) {
            multiplierField.show();
        } else {
            multiplierField.hide();
        }

        customAttributesStore.load(function () {
            if (widget.rendered) {
                widget.down('#custom-attribute-sets-placeholder-form-id').loadStore(customAttributesStore);
            }
        });
        me.fromSpecification = true;
        me.getController('Mdc.controller.setup.DeviceChannels').fromSpecification = true;
        if (menu) {
            menu.record = channel;
            var validateNowChannel = menu.down('#validateNowChannel');
            if (validateNowChannel) {
                validateNowChannel.setVisible(channel.get('validationInfo').validationActive);
            }
        }
        Ext.resumeLayouts(true);
    },

    setupReadingsTab: function (device, channel) {
        var me = this,
            dataStore = me.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData');

        dataStore.getProxy().setParams(device.get('name'), channel.getId());
        dataStore.loadData([], false);
        dataStore.load();
    },

    makeLinkToChannels: function (router) {
        var link = '<a href="{0}">' + Uni.I18n.translate('general.channels', 'MDC', 'Channels').toLowerCase() + '</a>',
            filter = this.getStore('Mdc.store.Clipboard').get('latest-device-channels-filter');

        return Ext.String.format(link, router.getRoute('devices/device/channels').buildUrl() + (filter ? '?' + filter : ''));
    },

    makeLinkToIssue: function (router, issueId) {
        var link = '<a href="{0}">' + Uni.I18n.translate('devicechannels.validationblocks', 'MDC', 'Validation blocks').toLowerCase() + '</a>';
        return Ext.String.format(link, router.getRoute('workspace/datavalidationissues/view').buildUrl({issueId: issueId}));
    },

    getDataFilter: function (channel, contentName, gasDayYearStart, router) {
        var me = this,
            intervalStore = me.getStore('Uni.store.DataIntervalAndZoomLevels'),
            dataIntervalAndZoomLevels = intervalStore.getIntervalRecord(channel.get('interval')),
            all = dataIntervalAndZoomLevels.get('all'),
            durationsStore = me.getStore('Mdc.store.LoadProfileDataDurations'),
            filter = {};

        durationsStore.loadData(dataIntervalAndZoomLevels.get('duration'));
        filter.options = [
            {
                display: Uni.I18n.translate('validationStatus.suspect', 'MDC', 'Suspect'),
                value: 'suspect',
                itemId: 'devicechannels-topfilter-suspect'
            }
        ];
        if (contentName === 'block') {
            filter.fromDate = new Date(parseInt(router.queryParams['validationBlock']));
            filter.hideDateTtimeSelect = true;
        } else {
            filter.options.push({
                display: Uni.I18n.translate('validationStatus.ok', 'MDC', 'Not suspect'),
                value: 'nonSuspect',
                itemId: 'devicechannels-topfilter-notsuspect'
            });
            if (Ext.isEmpty(channel.get('lastReading'))) {
                var fromDate = moment().startOf('day');
                if (!Ext.isEmpty(gasDayYearStart)) {
                    fromDate.add(gasDayYearStart.get('hours'), 'hours')
                        .add(gasDayYearStart.get('minutes'), 'minutes');
                }
                filter.fromDate = dataIntervalAndZoomLevels.getIntervalStart(fromDate.toDate());
            } else {
                var fromDate = channel.get('lastReading');
                if (!Ext.isEmpty(gasDayYearStart)) {
                    var lastReading = moment(channel.get('lastReading')),
                        lastReadingDayAtGasDayOffset = moment(channel.get('lastReading')).startOf('day').add(gasDayYearStart.get('hours'), 'hours').add(gasDayYearStart.get('minutes'), 'minutes');
                    if (lastReading.isBefore(lastReadingDayAtGasDayOffset) || lastReading.isSame(lastReadingDayAtGasDayOffset)) {
                        fromDate = lastReadingDayAtGasDayOffset;
                    } else {
                        lastReadingDayAtGasDayOffset.add(1, 'days');
                        fromDate = lastReadingDayAtGasDayOffset;
                    }
                }
                filter.fromDate = dataIntervalAndZoomLevels.getIntervalStart(fromDate);
            }
        }
        filter.duration = all.count + all.timeUnit;
        filter.durationStore = durationsStore;

        return filter;
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
            previewPanel;

        if (selectionModel.getSelection().length === 1) {
            previewPanel = me.getDeviceLoadProfileChannelDataPreview();
            previewPanel.updateForm(record);
        }
    },

    onGraphResize: function (graphView, width, height) {
        if (graphView.chart) {
            graphView.chart.setSize(width, height, false);
        }
    },

    chooseAction: function (menu, item) {
        var me = this;
        switch (item.action) {
            case 'editValue':
                me.getPage().down('#deviceLoadProfileChannelDataGrid').getPlugin('cellplugin').startEdit(menu.record, 1);
                break;
            case 'removeReading':
                me.removeReadings(menu.record);
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

        me.getPage().down('#save-changes-button').enable();
        me.getPage().down('#undo-button').enable();
    },

    saveChannelDataChanges: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            changedData = me.getChangedData(me.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData')),
            viewport = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        viewport.setLoading();
        if (!Ext.isEmpty(changedData)) {
            Ext.Ajax.request({
                url: Ext.String.format('/api/ddr/devices/{0}/channels/{1}/data', Uni.util.Common.encodeURIComponent(router.arguments.deviceId), router.arguments.channelId),
                method: 'PUT',
                jsonData: Ext.encode(changedData),
                timeout: 300000,
                success: function () {
                    router.getRoute().forward(router.arguments, Uni.util.QueryString.getQueryStringValues());
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('devicechannels.successSavingMessage', 'MDC', 'Channel data have been saved'));
                },
                failure: function (response) {
                    viewport.setLoading(false);
                    if (response.status == 400) {
                        var failureResponseText = Ext.decode(response.responseText, true);
                        if (failureResponseText && failureResponseText.error !== 'cannotAddChannelValueWhenLinkedToSlave') {
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
                    mainValidationInfo: {
                        isConfirmed: record.get('mainValidationInfo').confirmedNotSaved || false
                    },
                    bulkValidationInfo: {
                        isConfirmed: record.get('bulkValidationInfo').confirmedNotSaved || false
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

    beforeEditRecord: function (editor, context) {
        var intervalFlags = context.record.get('intervalFlags');
        context.column.getEditor().allowBlank = !(intervalFlags && intervalFlags.length);
        this.showPreview(context.grid.getSelectionModel(), context.record);
    },

    resumeEditorFieldValidation: function (editor, event) {
        var me = this,
            chart = me.getPage().down('#deviceLoadProfileChannelGraphView').chart,
            point = chart.get(event.record.get('interval').start),
            grid = me.getPage().down('deviceLoadProfileChannelDataGrid'),
            value = event.record.get('value'),
            collectedValue = event.record.get('collectedValue'),
            condition = (isNaN(point.y) && isNaN(value)) ? false : (point.y != value),
            updatedObj;

        if (event.column) {
            event.column.getEditor().allowBlank = true;
        }


        if (event.record.isModified('value')) {
            me.getPage().down('#save-changes-button').isDisabled() && me.showButtons();

            Ext.suspendLayouts(true);
            if (!event.record.get('value')) {
                point.update({y: null});
            } else {
                if (event.record.get('plotBand')) {
                    chart.xAxis[0].removePlotBand(event.record.get('interval').start);
                    event.record.set('plotBand', false);
                }
                updatedObj = {
                    y: value,
                    collectedValue: collectedValue,
                    color: 'rgba(112,187,81,0.3)'
                };
                point.update(updatedObj);
                point.select(false);
                me.getPage().down('#channel-data-preview-container').fireEvent('rowselect', event.record);
            }

            if (event.column) {
                event.record.get('mainValidationInfo').validationResult = 'validationStatus.ok';
                grid.getView().refreshNode(grid.getStore().indexOf(event.record));
                event.record.get('confirmed') && event.record.set('confirmed', false);
            }
            Ext.resumeLayouts();
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

        if (record.get('mainValidationInfo').estimatedByRule) {
            color = '#568343';
        } else if (properties.delta.notValidated) {
            color = '#71adc7';
        } else if (properties.delta.suspect) {
            color = 'rgba(235, 86, 66, 1)';
        } else if (properties.delta.informative) {
            color = '#dedc49';
        }

        record.get('confirmed') && record.set('confirmed', false);
        grid.getView().refreshNode(store.indexOf(record));
        point.update({
            y: parseFloat(record.get('value')),
            collectedValue: record.get('collectedValue'),
            color: color
        });
        record.reject();
        if (!store.getUpdatedRecords().length) {
            me.getPage().down('#save-changes-button').disable();
            me.getPage().down('#undo-button').disable();
        }
    },

    estimateValue: function (record) {
        var me = this,
            bothSuspected = false,
            mainValueSuspect = false,
            bulkValueSuspect = false;

        if (!Ext.isArray(record)) {
            bothSuspected = record.get('validationResult') &&
                record.get('validationResult').main == 'suspect' &&
                record.get('validationResult').bulk == 'suspect';
        } else {
            Ext.Array.findBy(record, function (item) {
                mainValueSuspect = item.get('validationResult') && item.get('validationResult').main == 'suspect';
                return mainValueSuspect;
            });
            Ext.Array.findBy(record, function (item) {
                bulkValueSuspect = item.get('validationResult') && item.get('validationResult').bulk == 'suspect';
                return bulkValueSuspect;
            });
            bothSuspected = mainValueSuspect && bulkValueSuspect;
        }
        me.getPage().setLoading();
        me.getStore('Mdc.store.Estimators').load(function () {
            me.getPage().setLoading(false);
            Ext.widget('reading-estimation-window', {
                itemId: 'channel-reading-estimation-window',
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

        !me.getReadingEstimationWindow().down('#form-errors').isHidden() && me.getReadingEstimationWindow().down('#form-errors').hide();
        !me.getReadingEstimationWindow().down('#error-label').isHidden() && me.getReadingEstimationWindow().down('#error-label').hide();
        propertyForm.clearInvalid();

        if (propertyForm.getRecord()) {
            propertyForm.updateRecord();
            model.set('estimatorImpl', me.getReadingEstimationWindow().down('#estimator-field').getValue());
            model.propertiesStore = propertyForm.getRecord().properties();
        }
        if (!me.getReadingEstimationWindow().down('#value-to-estimate-radio-group').isHidden()) {
            estimateBulk = me.getReadingEstimationWindow().down('#value-to-estimate-radio-group').getValue().isBulk;
        } else {
            if (!Ext.isArray(record)) {
                estimateBulk = record.get('validationResult') && (record.get('validationResult').bulk == 'suspect');
            } else {
                Ext.Array.findBy(record, function (item) {
                    estimateBulk = item.get('validationResult') && (item.get('validationResult').bulk == 'suspect');
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
        me.saveChannelDataEstimateModelr(model, record);
    },

    saveChannelDataEstimateModelr: function (record, readings) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        record.getProxy().setParams(encodeURIComponent(router.arguments.deviceId),router.arguments.channelId);
        me.getReadingEstimationWindow().setLoading();
        Ext.Ajax.suspendEvent('requestexception');
        record.save({
            callback: function (rec, operation, success) {
                Ext.Ajax.resumeEvent('requestexception');
                var responseText = Ext.decode(operation.response.responseText, true),
                    chart = me.getPage().down('#deviceLoadProfileChannelGraphView').chart;

                Ext.suspendLayouts();
                if (success && responseText[0]) {
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
                    me.getPage().down('#save-changes-button').isDisabled() && me.showButtons();
                } else {
                    me.getReadingEstimationWindow().setLoading(false);
                    if (responseText) {
                        if (responseText.message) {
                            me.getReadingEstimationWindow().down('#error-label').show();
                            me.getReadingEstimationWindow().down('#error-label').setText('<div style="color: #EB5642">' + responseText.message + '</div>', false);
                        } else if (responseText.readings) {
                            me.getReadingEstimationWindow().down('#error-label').show();
                            var listOfFailedReadings = [];
                            Ext.Array.each(responseText.readings, function (readingTimestamp) {
                                listOfFailedReadings.push(Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [Uni.DateTime.formatDateShort(new Date(readingTimestamp)), Uni.DateTime.formatTimeShort(new Date(readingTimestamp))], false));
                            });
                            me.getReadingEstimationWindow().down('#error-label').setText('<div style="color: #EB5642">' +
                                Uni.I18n.translate('devicechannels.estimationErrorMessage', 'MDC', 'Could not estimate {0} with {1}',
                                    [listOfFailedReadings.join(', '), me.getReadingEstimationWindow().down('#estimator-field').getRawValue().toLowerCase()]) + '</div>', false);
                        } else if (responseText.errors) {
                            me.getReadingEstimationWindow().down('#form-errors').show();
                            me.getReadingEstimationWindow().down('#property-form').markInvalid(responseText.errors);
                        }
                    }

                }
                Ext.resumeLayouts(true);
            }
        });
    },

    updateEstimatedValues: function (record, reading, estimatedReading) {
        var me = this,
            grid = me.getPage().down('deviceLoadProfileChannelDataGrid');

        if (record.get('estimateBulk')) {
            reading.set('collectedValue', estimatedReading.collectedValue);
            reading.get('bulkValidationInfo').validationResult = 'validationStatus.ok';
        } else {
            reading.set('value', estimatedReading.value);
            reading.get('mainValidationInfo').validationResult = 'validationStatus.ok';
        }
        grid.getView().refreshNode(grid.getStore().indexOf(reading));

        me.resumeEditorFieldValidation(grid.editingPlugin, {
            record: reading
        });
        reading.get('confirmed') && reading.set('confirmed', false);
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
            case 'removeReadings':
                me.removeReadings(records, true);
                break;
        }
    },

    confirmValue: function (record, isBulk) {
        var me = this,
            grid = me.getPage().down('deviceLoadProfileChannelDataGrid'),
            mainStatus = false,
            bulkStatus = false,
            isModified,
            chart = me.getPage().down('#deviceLoadProfileChannelGraphView').chart,
            func = function (rec) {
                isModified = rec.isModified('value') || rec.isModified('collectedValue');
                if (!rec.get('confirmed') && !isModified) {
                    var validationResult = rec.get('validationResult');

                    if (validationResult) {
                        mainStatus = validationResult.main == 'suspect';
                        bulkStatus = validationResult.bulk == 'suspect';
                    }

                    if (mainStatus) {
                        rec.get('mainValidationInfo').confirmedNotSaved = true;
                        chart.get(rec.get('interval').start).update({color: 'rgba(112,187,81,0.3)'});
                        chart.get(rec.get('interval').start).select(false);
                        me.getPage().down('#channel-data-preview-container').fireEvent('rowselect', record)
                    }

                    if (bulkStatus) {
                        rec.get('bulkValidationInfo').confirmedNotSaved = true;
                    }

                    if (mainStatus || bulkStatus) {
                        grid.getView().refreshNode(grid.getStore().indexOf(rec));
                        rec.set('confirmed', true);
                    }
                }
            };

        Ext.suspendLayouts(true);
        if (isBulk) {
            Ext.Array.each(record, function (reading) {
                func(reading);
            });
        } else {
            func(record);
        }
        Ext.resumeLayouts(true);

        me.getPage().down('#save-changes-button').isDisabled() && me.showButtons();
    },

    removeReadings: function (records) {
        var me = this,
            point,
            grid = me.getPage().down('deviceLoadProfileChannelDataGrid'),
            store = grid.getStore(),
            gridView = grid.getView(),
            chart = me.getPage().down('#deviceLoadProfileChannelGraphView').chart;

        Ext.suspendLayouts();
        Ext.Array.each(records, function (record) {
            record.beginEdit();
            record.set('value', null);
            record.set('collectedValue', null);
            if (record.get('intervalFlags').length) {
                record.set('intervalFlags', []);
            }
            if (record.get('confirmed')) {
                record.set('confirmed', false);
            }
            record.get('mainValidationInfo').validationResult = 'validationStatus.ok';
            record.endEdit(true);
            gridView.refreshNode(store.indexOf(record));
            point = chart.get(record.get('interval').start);
            point.update({y: null}, false);
        });
        chart.redraw();
        me.showButtons();
        Ext.resumeLayouts(true);
    },

    onDataGridSelectionChange: function (selectionModel, selectedRecords) {
        var me = this,
            button = me.getPage().down('#device-channel-data-bulk-action-button'),
            menu = button.down('menu');

        Ext.suspendLayouts();
        var suspects = selectedRecords.filter(function (record) {
            var validationResult = record.get('validationResult');
            return (validationResult.main == 'suspect')
                || (validationResult.bulk == 'suspect');
        });
        menu.down('#estimate-value').setVisible(suspects.length);

        var confirms = suspects.filter(function (record) {
            return !record.get('confirmed') && !record.isModified('value') && !record.isModified('collectedValue')
        });

        menu.down('#confirm-value').setVisible(confirms.length);
        menu.down('#remove-readings').setVisible(_.find(selectedRecords, function (record) {
            return record.get('value') || record.get('collectedValue')
        }));
        button.setDisabled(!menu.query('menuitem[hidden=false]').length);
        Ext.resumeLayouts();
    },

    showEditChannelOfLoadProfileCustomAttributes: function (deviceId, channelId, customAttributeSetId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        viewport.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                var model = Ext.ModelManager.getModel('Mdc.model.ChannelOfLoadProfilesOfDevice');
                model.getProxy().setExtraParam('deviceId', deviceId);
                model.load(channelId, {
                    success: function (channel) {

                        var widget = Ext.widget('deviceLoadProfileChannelsEditCustomAttributes', {device: device});
                        me.getApplication().fireEvent('loadDevice', device);
                        me.getApplication().fireEvent('channelOfLoadProfileOfDeviceLoad', channel);
                        me.getApplication().fireEvent('channelOfLoadProfileCustomAttributes', device);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.loadPropertiesRecord(widget, deviceId, channelId, customAttributeSetId);
                    },
                    failure: function () {
                        viewport.setLoading(false);
                    }
                });
            }
        });

    },

    loadPropertiesRecord: function (widget, deviceId, channelId, customAttributeSetId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            model = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetOnChannel'),
            form = widget.down('property-form');

        model.getProxy().setParams(deviceId, channelId);

        model.load(customAttributeSetId, {
            success: function (record) {
                widget.down('#channelEditPanel').setTitle(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", [record.get('name')]));
                me.getApplication().fireEvent('channelOfLoadProfileCustomAttributes', record);
                form.loadRecord(record);
            },
            callback: function () {
                viewport.setLoading(false);
            }
        });
    },

    restoreChannelOfLoadProfileCustomAttributes: function () {
        this.getEditPropertyForm().restoreAll();
    },

    toPreviousPage: function () {
        if (this.fromSpecification) {
            this.getController('Uni.controller.history.Router').getRoute('devices/device/channels/channel').forward();
        } else {
            this.getController('Uni.controller.history.Router').getRoute('devices/device/channels').forward();
        }
    },

    getPreviousPageUrl: function () {
        if (this.fromSpecification) {
            return this.getController('Uni.controller.history.Router').getRoute('devices/device/channels/channel').buildUrl();
        } else {
            return this.getController('Uni.controller.history.Router').getRoute('devices/device/channels').buildUrl();
        }
    },

    saveChannelOfLoadProfileCustomAttributes: function () {
        var me = this,
            form = me.getEditPropertyForm(),
            editView = me.getEditCustomAttributesPanel();

        editView.setLoading();

        form.updateRecord();
        form.getRecord().save({
            backUrl: me.getPreviousPageUrl(),
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('channelAttributes.saved', 'MDC', 'Channel attributes saved'));
                me.toPreviousPage();
            },
            callback: function () {
                editView.setLoading(false);
            }
        });
    },

    showRestoreAllBtn: function (value) {
        var restoreBtn = this.getEditCustomAttributesRestoreBtn();
        if (restoreBtn) {
            if (value) {
                restoreBtn.disable();
            } else {
                restoreBtn.enable();
            }
        }
    }
});