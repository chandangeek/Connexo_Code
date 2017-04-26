/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.DeviceChannelData', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.util.Common',
        'Uni.store.GasDayYearStart'
    ],

    views: [
        'Cfg.view.common.CopyFromReferenceWindow',
        'Cfg.view.common.EditEstimationComment',
        'Mdc.view.setup.devicechannels.TabbedDeviceChannelsView',
        'Mdc.view.setup.devicechannels.Overview',
        'Mdc.view.setup.devicechannels.ReadingEstimationWindow',
        'Mdc.view.setup.devicechannels.ReadingEstimationWithRuleWindow',
        'Mdc.view.setup.devicechannels.EditCustomAttributes',
        'Mdc.view.setup.devicechannels.History',
        'Uni.view.readings.CorrectValuesWindow',
        'Cfg.configuration.view.RuleWithAttributesEdit'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.CopyFromReference',
        'Mdc.model.LoadProfileOfDevice',
        'Mdc.model.ChannelOfLoadProfilesOfDevice',
        'Mdc.model.ChannelOfLoadProfilesOfDeviceDataFilter',
        'Mdc.model.DeviceChannelDataEstimate',
        'Mdc.customattributesonvaluesobjects.model.AttributeSetOnChannel',
        'Mdc.model.ChannelValidationConfigurationForReadingType',
        'Mdc.model.ChannelEstimationConfigurationForReadingType'
    ],

    stores: [
        'Mdc.customattributesonvaluesobjects.store.ChannelCustomAttributeSets',
        'Mdc.store.ChannelOfLoadProfileOfDeviceData',
        'Mdc.store.Devices',
        'Uni.store.DataIntervalAndZoomLevels',
        'Mdc.store.LoadProfileDataDurations',
        'Mdc.store.Clipboard',
        'Mdc.store.Estimators',
        'Mdc.store.ValidationBlocks',
        'Mdc.store.TimeUnits',
        'Mdc.store.DataLoggerSlaveChannelHistory',
        'Mdc.store.EstimationRulesOnChannelMainValue',
        'Mdc.store.DataLoggerSlaveChannelHistory',
        'Mdc.store.HistoryChannels',
        'Mdc.store.ChannelValidationConfiguration',
        'Mdc.store.ChannelEstimationConfiguration'
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
            ref: 'deviceLoadProfileHistoryChannelDataPreview',
            selector: '#deviceLoadProfileHistoryChannelDataPreview'
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
            selector: '#deviceLoadProfileChannelData #mdc-device-channels-topfilter'
        },
        {
            ref: 'editEstimationComment',
            selector: 'reading-edit-estimation-comment-window'
        },
        {
            ref: 'readingCopyFromReferenceWindow',
            selector: 'reading-copy-from-reference-window'
        },
        {
            ref: 'readingEstimationWindow',
            selector: 'reading-estimation-window'
        },
        {
            ref: 'readingEstimationWithRuleWindow',
            selector: 'reading-estimation-with-rule-window'
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
        },
        {
            ref: 'correctReadingWindow',
            selector: 'correct-values-window'
        }
    ],

    channelModel: null,
    fromSpecification: false,
    hasEstimationRule: false,

    tabLookupTable: {
        spec: 0,
        data: 1,
        validation: 2,
        estimation: 3
    },

    init: function () {
        this.control({
            'device-channels-history-grid': {
                select: this.showHistoryPreview
            },
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
                beforeshow: this.beforeShowMenu,
                click: this.chooseAction
            },
            '#deviceLoadProfileChannelData #save-changes-button': {
                click: this.saveChannelDataChanges
            },
            '#deviceLoadProfileChannelData #undo-button': {
                click: this.undoChannelDataChanges
            },
            'reading-copy-from-reference-window #copy-reading-button': {
                click: this.copyFromReferenceUpdateGrid
            },
            'reading-edit-estimation-comment-window #edit-comment-button': {
                click: this.saveEstimationComment
            },
            '#channel-reading-estimation-window #estimate-reading-button': {
                click: this.estimateReading
            },
            '#channel-reading-estimation-with-rule-window #estimate-reading-button': {
                click: this.estimateReadingWithRule
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
            },
            '#channel-reading-estimation-with-rule-window #value-to-estimate-radio-group': {
                change: this.updateEstimateWithRuleWindow
            },
            'correct-values-window #correct-reading-button': {
                click: this.correctReadings
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

    showValidationConfiguration: function (deviceId, channelId) {
        var me = this;
        me.showOverview({deviceId: deviceId, channelId: channelId}, 'validation');
    },

    showEstimationConfiguration: function (deviceId, channelId) {
        var me = this;
        me.showOverview({deviceId: deviceId, channelId: channelId}, 'estimation');
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
            activeTab = me.tabLookupTable[contentName],
            timeUnitsStore = Ext.getStore('Mdc.store.TimeUnits'),
            slaveHistoryStore = me.getStore('Mdc.store.DataLoggerSlaveChannelHistory'),
            validationConfigurationStore = me.getStore('Mdc.store.ChannelValidationConfiguration'),
            estimationConfigurationStore = me.getStore('Mdc.store.ChannelEstimationConfiguration'),
            dependencyCounter = 6,
            onDependenciesLoad = function () {
                dependencyCounter--;
                if (!dependencyCounter) {
                    var hasValidationRules = validationConfigurationStore.first().rulesForCollectedReadingType().getCount() || validationConfigurationStore.first().rulesForCalculatedReadingType().getCount(),
                        hasEstimationRules = estimationConfigurationStore.first().rulesForCollectedReadingType().getCount() || estimationConfigurationStore.first().rulesForCalculatedReadingType().getCount();

                    if ((contentName === 'validation' && !hasValidationRules) || (contentName === 'estimation' && !hasEstimationRules)) {
                        window.location.replace(router.getRoute('devices/device/channels/channeldata').buildUrl());
                        return;
                    } else if (contentName === 'estimation' && !hasValidationRules) {
                        activeTab = 2;
                    }
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
                        dataLoggerSlaveHistoryStore: slaveHistoryStore,
                        application: me.getApplication(),
                        validationConfigurationStore: validationConfigurationStore,
                        estimationConfigurationStore: estimationConfigurationStore
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
                me.currentChannel = record;
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
        var estimationRulesStore = me.getStore('Mdc.store.EstimationRulesOnChannelMainValue');
        estimationRulesStore.getProxy().extraParams = {deviceId: deviceId, channelId: channelId, isBulk: false};
        estimationRulesStore.load(function (records) {
            if (records.length) {
                me.hasEstimationRule = true;
                onDependenciesLoad();
            } else {
                estimationRulesStore.getProxy().extraParams = {deviceId: deviceId, channelId: channelId, isBulk: true};
                estimationRulesStore.load(function (records) {
                    me.hasEstimationRule = Boolean(records.length);
                    onDependenciesLoad();
                });
            }
        });

        validationConfigurationStore.getProxy().extraParams = {deviceId: deviceId, channelId: channelId};
        validationConfigurationStore.load(function () {
            onDependenciesLoad();
        });

        estimationConfigurationStore.getProxy().extraParams = {deviceId: deviceId, channelId: channelId};
        estimationConfigurationStore.load(function () {
            onDependenciesLoad();
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

        switch (newTab.itemId) {
            case 'deviceLoadProfileChannelData':
                filterParams.onlySuspect = false;
                route = 'devices/device/channels/channeldata';
                break;
            case 'channel-specifications':
                route = 'devices/device/channels/channel';
                break;
            case 'channel-validation-configuration':
                route = 'devices/device/channels/validation';
                break;
            case 'channel-estimation-configuration':
                route = 'devices/device/channels/estimation';
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward(routeParams, filterParams);
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            previewPanel;

        if (selectionModel.getSelection().length === 1) {
            previewPanel = me.getDeviceLoadProfileChannelDataPreview();
            previewPanel.updateForm(record);
        }
    },

    showHistoryPreview: function (selectionModel, record) {
        var me = this,
            previewPanel;

        if (selectionModel.getSelection().length === 1) {
            previewPanel = me.getDeviceLoadProfileHistoryChannelDataPreview();
            previewPanel.updateForm(record);
        }
    },

    onGraphResize: function (graphView, width, height) {
        if (graphView.chart) {
            graphView.chart.setSize(width, height, false);
        }
    },

    chooseAction: function (menu, item) {
        var me = this,
            records = me.getPage().down('deviceLoadProfileChannelDataGrid').getSelectionModel().getSelection(),
            router = this.getController('Uni.controller.history.Router'),
            routeParams = router.arguments,
            route,
            filterParams = {};

        switch (item.action) {
            case 'editValue':
                me.getPage().down('#deviceLoadProfileChannelDataGrid').getPlugin('cellplugin').startEdit(menu.record, 1);
                break;
            case 'removeReading':
                me.removeReadings(menu.record);
                break;
            case 'copyFromReference':
                me.copyFromReference(menu.record);
                break;
            case 'correctValue':
                me.openCorrectWindow(menu.record);
                break;
            case 'editEstimationComment':
                me.editEstimationComment(menu.record);
                break;
            case 'estimateValue':
                me.estimateValue(menu.record);
                break;
            case 'estimateWithRule':
                me.estimateValueWithRule(menu.record);
                break;
            case 'confirmValue':
                me.confirmValue(menu.record, false);
                break;
            case 'viewHistory':
                route = 'devices/device/channels/channeldata/history';
                filterParams = {
                    endInterval: Number(menu.record.get('interval').end - 1) + '-' + Number(menu.record.get('interval').end),
                    isBulk: false
                };
                route && (route = router.getRoute(route));
                route && route.forward(routeParams, filterParams);
                break;
        }

    },

    beforeShowMenu: function (menu) {
        var me = this,
            validationResult = menu.record.get('validationResult'),
            estimationRulesCount = me.hasEstimationRule,
            mainStatus = false,
            bulkStatus = false,
            canEditingComment = false,
            flagForComment = function (value) {
                if (value === 'EDITED' ||
                    value === 'ESTIMATED' ||
                    value === 'REMOVED') {
                    return true;
                } else {
                    return false;
                }
            };

        if (validationResult) {
            mainStatus = validationResult.main === 'suspect';
            bulkStatus = validationResult.bulk === 'suspect';
        }

        if (menu.record.get('mainModificationState') && menu.record.get('mainModificationState').flag ||
            menu.record.get('bulkModificationState') && menu.record.get('bulkModificationState').flag) {
            canEditingComment = flagForComment(menu.record.get('mainModificationState').flag);
            if (!canEditingComment) {
                canEditingComment = flagForComment(menu.record.get('bulkModificationState').flag);
            }
        }

        menu.down('#estimate-value').setVisible(mainStatus || bulkStatus || menu.record.get('estimatedNotSaved') === true);
        menu.down('#estimate-value-with-rule').setVisible(estimationRulesCount && (mainStatus || bulkStatus));
        if (menu.record.get('confirmed') || menu.record.isModified('value') || menu.record.isModified('collectedValue')) {
            menu.down('#confirm-value').hide();
        } else {
            menu.down('#confirm-value').setVisible(mainStatus || bulkStatus);
        }

        if (menu.down('#edit-estimation-comment')) {
            menu.down('#edit-estimation-comment').setVisible(canEditingComment);
        }
        if (menu.down('#remove-reading')) {
            menu.down('#remove-reading').setVisible(menu.record.get('value') || menu.record.get('collectedValue'));
        }
        if (menu.down('#correct-value')) {
            menu.down('#correct-value').setVisible(!Ext.isEmpty(menu.record.get('value')))
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
        router.getRoute().forward(router.arguments, Uni.util.QueryString.getQueryStringValues());
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
            } else if (record.get('mainValidationInfo').ruleId || record.get('bulkValidationInfo').ruleId) {
                confirmedObj = {
                    interval: record.get('interval'),
                    value: record.get('value'),
                    collectedValue: record.get('collectedValue'),
                    mainValidationInfo: {
                        isConfirmed: false,
                        ruleId: record.get('mainValidationInfo').estimatedNotSaved ? record.get('mainValidationInfo').ruleId : null
                    },
                    bulkValidationInfo: {
                        isConfirmed: false,
                        ruleId: record.get('bulkValidationInfo').estimatedNotSaved ? record.get('bulkValidationInfo').ruleId : null
                    }
                };
            } else if (record.isModified('value')) {
                confirmedObj = {
                    interval: record.get('interval'),
                    value: record.get('value')
                };
                if (record.isModified('collectedValue')) {
                    confirmedObj.collectedValue = record.get('collectedValue');
                }

            } else if (record.isModified('collectedValue')) {
                confirmedObj = _.pick(record.getData(), 'interval', 'collectedValue');
            }
            confirmedObj.mainValidationInfo = {};
            confirmedObj.bulkValidationInfo = {};
            if (record.get('mainValidationInfo') && record.get('mainValidationInfo').commentId) {
                confirmedObj.mainValidationInfo.commentId = record.get('mainValidationInfo').commentId;
            } else {
                confirmedObj.mainValidationInfo.commentId = null;
            }
            if (record.get('bulkValidationInfo') && record.get('bulkValidationInfo').commentId) {
                confirmedObj.bulkValidationInfo.commentId = record.get('bulkValidationInfo').commentId;
            } else {
                confirmedObj.bulkValidationInfo.commentId = null;
            }
            changedData.push(confirmedObj);
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
                if (!event.record.get('estimatedNotSaved')) {
                    event.record.set('mainModificationState', Uni.util.ReadingEditor.modificationState('EDITED'));
                }
                grid.getView().refreshNode(grid.getStore().indexOf(event.record));
                event.record.get('confirmed') && event.record.set('confirmed', false);
            }
            Ext.resumeLayouts();
        } else if (!event.record.isModified('collectedValue') && condition) {
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

    editEstimationComment: function (records) {
        Ext.widget('reading-edit-estimation-comment-window',
            {
                itemId: 'channel-edit-estimation-comment-window',
                records: records
            }).show();
    },

    saveEstimationComment: function (button) {
        var me = this,
            window = me.getEditEstimationComment(),
            commentCombo = window.down('#estimation-comment-box'),
            commentValue = commentCombo.getRawValue(),
            commentId = commentCombo.getValue(),
            radioValue = window.down('#value-edit').getValue(),
            readings = button.readings,
            comment = {};

        if (commentId !== -1) {
            comment = {
                commentId: commentId,
                commentValue: commentValue
            };
        }

        if (!Array.isArray(readings)) {
            if (radioValue) {
                readings.set('bulkValidationInfo', comment);
            } else {
                readings.set('mainValidationInfo', comment);
            }
        } else {
            if (radioValue) {
                _.each(readings, function (reading) {
                    reading.set('bulkValidationInfo', comment);
                });
            } else {
                _.each(readings, function (reading) {
                    reading.set('mainValidationInfo', comment);
                });
            }
        }
        me.showButtons();
        window.close();
    },

    copyFromReference: function (records) {
        var window = Ext.widget('reading-copy-from-reference-window',
            {
                itemId: 'channel-copy-from-reference-window',
                records: records
            }).show();

        window.down('#device-field').getStore().getProxy().extraParams = {
            page: 1,
            start: 0,
            limit: 50,
            nameOnly: true
        };

        window.down('#readingType-field').getStore().getProxy().extraParams = {
            page: 1,
            start: 0,
            limit: 50,
            property: 'fullAliasName'
        };
    },

    copyFromReferenceUpdateGrid: function () {
        var me = this,
            intervals = [],
            window = me.getReadingCopyFromReferenceWindow(),
            form = window.down('#reading-copy-window-form'),
            model = Ext.create('Mdc.model.CopyFromReference'),
            router = me.getController('Uni.controller.history.Router'),
            commentCombo = window.down('#estimation-comment-box'),
            commentValue = commentCombo.getRawValue(),
            commentId = commentCombo.getValue(),
            comment = {};

        if (commentId !== -1) {
            comment = {
                commentId: commentId,
                commentValue: commentValue
            };
        }

        window.setLoading(true);
        form.updateRecord(model);
        model.getProxy().extraParams = {
            usagePointId: decodeURIComponent(router.arguments.deviceId),
            purposeId: router.arguments.channelId
        };

        if (Array.isArray(window.records)) {
            _.each(window.records, function (record) {
                intervals.push(record.get('interval'));
            });
        } else {
            intervals.push(window.records.get('interval'));
        }
        model.set('intervals', intervals);
        model.save({
            failure: function (record, operation) {
                var response = JSON.parse(operation.response.responseText);

                _.each(response.errors, function (error) {
                    error.msg = '<span style="white-space: normal">' + error.msg + '</span>';
                });
                form.getForm().markInvalid(response.errors);
            },
            success: function (record, operation) {
                var item = null,
                    response = JSON.parse(operation.response.responseText);

                if (response[0]) {
                    Ext.suspendLayouts();
                    if (Array.isArray(window.records)) {
                        _.each(window.records, function (record) {
                            item = record.get('interval').end;
                            item = _.find(response, function (rec) {
                                return rec.interval.end === item;
                            });
                            if (record.get('value') !== item.value) {
                                record.set('value', item.value);
                                record.set('isProjected', model.get('projectedValue'));
                                record.set('mainValidationInfo', Ext.merge(item.mainValidationInfo, comment));
                            }
                        });
                    } else {
                        if (window.records.get('value') !== response[0].value) {
                            window.records.set('value', response[0].value);
                            window.records.set('isProjected', model.get('projectedValue'));
                            window.records.set('mainValidationInfo', Ext.merge(response[0].mainValidationInfo, comment));
                        }
                    }
                    me.showButtons();
                    Ext.resumeLayouts(true);
                }
                window.close();
            },
            callback: function () {
                window.setLoading(false);
            }
        });

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

    estimateValueWithRule: function (record) {
        var me = this,
            bothSuspected = false,
            mainValueSuspect = false,
            bulkValueSuspect = false,
            estimationRulesStore = me.getStore('Mdc.store.EstimationRulesOnChannelMainValue');

        if (!Ext.isArray(record)) {
            if (record.get('validationResult')) {
                mainValueSuspect = record.get('validationResult').main == 'suspect';
                bulkValueSuspect = record.get('validationResult').bulk == 'suspect';
                bothSuspected = mainValueSuspect && bulkValueSuspect;
            }
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

        estimationRulesStore.getProxy().setExtraParam('isBulk', !bothSuspected ? bulkValueSuspect : false);
        estimationRulesStore.load(function (records) {
            showWindow(Boolean(records.length));
        });

        function showWindow(hasRules) {
            Ext.widget('reading-estimation-with-rule-window', {
                itemId: 'channel-reading-estimation-with-rule-window',
                record: record,
                bothSuspected: bothSuspected,
                hasRules: hasRules
            }).show();
        }
    },

    updateEstimateWithRuleWindow: function (radiogroup, newValue, oldValue, eOpts) {
        var me = this,
            estimationRulesStore = me.getStore('Mdc.store.EstimationRulesOnChannelMainValue'),
            window = me.getReadingEstimationWithRuleWindow();

        window.down('#estimator-field').reset();
        estimationRulesStore.getProxy().setExtraParam('isBulk', newValue.isBulk);
        estimationRulesStore.load(function (records) {
            var isNotEmpty = Boolean(records.length);
            Ext.suspendLayouts();
            window.down('#estimator-field').setVisible(isNotEmpty);
            window.down('#no-estimation-rules-component').setVisible(!isNotEmpty);
            window.down('#property-form').removeAll(true);
            Ext.resumeLayouts(true);
        });
    },

    estimateReading: function () {
        var me = this,
            window = me.getReadingEstimationWindow(),
            propertyForm = window.down('#property-form'),
            model = Ext.create('Mdc.model.DeviceChannelDataEstimate'),
            estimateBulk = false,
            record = window.record,
            intervalsArray = [];

        !window.down('#form-errors').isHidden() && window.down('#form-errors').hide();
        !window.down('#error-label').isHidden() && window.down('#error-label').hide();
        propertyForm.clearInvalid();

        if (propertyForm.getRecord()) {
            propertyForm.updateRecord();
            model.set('estimatorImpl', window.down('#estimator-field').getValue());
            model.propertiesStore = propertyForm.getRecord().properties();
        }
        if (!window.down('#value-to-estimate-radio-group').isHidden()) {
            estimateBulk = window.down('#value-to-estimate-radio-group').getValue().isBulk;
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
        me.saveChannelDataEstimateModelr(model, record, window, null, 'editWithEstimator');
    },

    estimateReadingWithRule: function () {
        var me = this,
            window = me.getReadingEstimationWithRuleWindow(),
            propertyForm = window.down('#property-form'),
            estimationRuleId = window.down('#estimator-field').getValue(),
            model = Ext.create('Mdc.model.DeviceChannelDataEstimate'),
            estimateBulk = false,
            record = window.record,
            intervalsArray = [];

        !window.down('#form-errors').isHidden() && window.down('#form-errors').hide();
        !window.down('#error-label').isHidden() && window.down('#error-label').hide();


        if (propertyForm.getRecord()) {
            model.set('estimatorImpl', window.getEstimator());
            model.propertiesStore = propertyForm.getRecord().properties();
        }
        if (!window.down('#value-to-estimate-radio-group').isHidden()) {
            estimateBulk = window.down('#value-to-estimate-radio-group').getValue().isBulk;
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
        me.saveChannelDataEstimateModelr(model, record, window, estimationRuleId, 'estimate');
    },

    saveChannelDataEstimateModelr: function (record, readings, window, ruleId, action) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            commentCombo = window.down('#estimation-comment-box'),
            commentValue = commentCombo ? commentCombo.getRawValue() : null,
            commentId = commentCombo ? commentCombo.getValue() : -1,
            adjustedPropertyFormErrors,
            validationInfoName = record.get('estimateBulk') ? 'bulkValidationInfo' : 'mainValidationInfo',
            validationInfo = {},
            comment = {
                commentId: commentId,
                commentValue: commentValue
            };

        record.getProxy().setParams(decodeURIComponent(router.arguments.deviceId), router.arguments.channelId);
        window.setLoading();
        Ext.Ajax.suspendEvent('requestexception');
        record.save({
            callback: function (rec, operation, success) {
                Ext.Ajax.resumeEvent('requestexception');
                var responseText = Ext.decode(operation.response.responseText, true),
                    chart = me.getPage().down('#deviceLoadProfileChannelGraphView').chart;

                Ext.suspendLayouts();
                if (success && responseText[0]) {
                    if (!Ext.isArray(readings)) {
                        if (commentId !== -1) {
                            readings.set(validationInfoName, comment);
                        }
                        me.updateEstimatedValues(record, readings, responseText[0], ruleId, action);
                    } else {
                        Ext.Array.each(responseText, function (estimatedReading) {
                            Ext.Array.findBy(readings, function (reading) {
                                if (estimatedReading.interval.start == reading.get('interval').start) {
                                    if (commentId !== -1) {
                                        readings.set(validationInfoName, comment);
                                    }
                                    me.updateEstimatedValues(record, reading, estimatedReading, ruleId, action);
                                    return true;
                                }
                            });
                        });
                    }
                    window.destroy();
                    me.getPage().down('#save-changes-button').isDisabled() && me.showButtons();
                } else {
                    window.setLoading(false);
                    if (responseText) {
                        if (responseText.message) {
                            window.down('#error-label').show();
                            window.down('#error-label').setText('<div style="color: #EB5642">' + responseText.message + '</div>', false);
                        } else if (responseText.readings) {
                            window.down('#error-label').show();
                            var listOfFailedReadings = [];
                            Ext.Array.each(responseText.readings, function (readingTimestamp) {
                                listOfFailedReadings.push(Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [Uni.DateTime.formatDateShort(new Date(readingTimestamp)), Uni.DateTime.formatTimeShort(new Date(readingTimestamp))], false));
                            });
                            var errorMessage = window.down('#estimator-field') ? Uni.I18n.translate('devicechannels.estimationErrorMessageWithIntervals', 'MDC', 'Could not estimate {0} with {1}',
                                [listOfFailedReadings.join(', '), window.down('#estimator-field').getRawValue()]) : Uni.I18n.translate('devicechannels.estimationErrorMessage', 'MDC', 'Could not estimate {0}',
                                listOfFailedReadings.join(', '));
                            window.down('#error-label').setText('<div style="color: #EB5642">' + errorMessage + '</div>', false);
                        } else if (responseText.errors) {
                            window.down('#form-errors').show();
                            if (Ext.isArray(responseText.errors)) {
                                adjustedPropertyFormErrors = responseText.errors.map(function (error) {
                                    if (error.id.startsWith('properties.')) {
                                        error.id = error.id.slice(11);
                                    }
                                    return error;
                                });
                            }
                            window.down('#property-form').markInvalid(adjustedPropertyFormErrors);
                        }
                    } else {
                        window.down('#error-label').show();
                        window.down('#error-label').setText('<div style="color: #EB5642">' + Uni.I18n.translate('value.cannot.be.estimted', 'MDC', 'Value cannot be estimated') + '</div>', false);
                    }

                }
                Ext.resumeLayouts(true);
            }
        });
    },

    updateEstimatedValues: function (record, reading, estimatedReading, ruleId, action) {
        var me = this,
            grid = me.getPage().down('deviceLoadProfileChannelDataGrid');

        if (record.get('estimateBulk')) {
            reading.set('collectedValue', estimatedReading.collectedValue);
            if (ruleId) {
                reading.get('bulkValidationInfo').ruleId = ruleId;
            }
            reading.get('bulkValidationInfo').validationResult = 'validationStatus.ok';
            reading.get('bulkValidationInfo').estimatedNotSaved = true;
            if (action === 'editWithEstimator') {
                reading.set('bulkModificationState', Uni.util.ReadingEditor.modificationState('EDITED'));
            } else if (action === 'estimate') {
                reading.get('bulkValidationInfo').estimatedByRule = true;
            }
        } else {
            reading.set('value', estimatedReading.value);
            if (ruleId) {
                reading.get('mainValidationInfo').ruleId = ruleId;
            }
            reading.get('mainValidationInfo').validationResult = 'validationStatus.ok';
            if (action === 'estimate') {
                reading.get('mainValidationInfo').estimatedByRule = true;
                reading.get('mainValidationInfo').estimatedNotSaved = true;
            }

        }
        grid.getView().refreshNode(grid.getStore().indexOf(reading));

        me.resumeEditorFieldValidation(grid.editingPlugin, {
            record: reading
        });
        reading.get('confirmed') && reading.set('confirmed', false);
    },

    chooseBulkAction: function (menu, item) {
        var me = this,
            records = me.getPage().down('deviceLoadProfileChannelDataGrid').getSelectionModel().getSelection(),
            router = this.getController('Uni.controller.history.Router'),
            routeParams = router.arguments,
            route,
            filterParams = {};

        switch (item.action) {
            case 'editEstimationComment':
                me.editEstimationComment(records);
                break;
            case 'estimateValue':
                me.estimateValue(records);
                break;
            case 'estimateWithRule':
                me.estimateValueWithRule(records);
                break;
            case 'copyFromReference':
                me.copyFromReference(records);
                break;
            case 'confirmValue':
                me.confirmValue(records, true);
                break;
            case 'correctValue':
                me.openCorrectWindow(records);
                break;
            case 'removeReadings':
                me.removeReadings(records, true);
                break;
            case 'viewHistory':
                route = 'devices/device/channels/channeldata/history';
                filterParams = {
                    interval: me.getFilterPanel().down('#devicechannels-topfilter-duration').getParamValue(),
                    isBulk: true,
                    changedDataOnly: 'yes'
                };
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward(routeParams, filterParams);
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
                        rec.get('mainValidationInfo').isConfirmed = true;
                        chart.get(rec.get('interval').start).update({color: 'rgba(112,187,81,0.3)'});
                        chart.get(rec.get('interval').start).select(false);
                        me.getPage().down('#channel-data-preview-container').fireEvent('rowselect', record)
                    }

                    if (bulkStatus) {
                        rec.get('bulkValidationInfo').confirmedNotSaved = true;
                        rec.get('bulkValidationInfo').isConfirmed = true;
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
            record.set('mainModificationState', Uni.util.ReadingEditor.modificationState('REMOVED'));
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
            estimationRulesCount = me.hasEstimationRule,
            menu = button.down('menu'),
            flagForComment = function (value) {
                if (value === 'EDITED' ||
                    value === 'ESTIMATED' ||
                    value === 'REMOVED') {
                    return true;
                } else {
                    return false;
                }
            };

        Ext.suspendLayouts();
        var suspects = selectedRecords.filter(function (record) {
            var validationResult = record.get('validationResult');
            return (validationResult.main == 'suspect')
                || (validationResult.bulk == 'suspect') || record.get('estimatedNotSaved') === true;
        });
        menu.down('#estimate-value').setVisible(suspects.length);
        menu.down('#estimate-value-with-rule').setVisible(suspects.length && estimationRulesCount);

        var confirms = suspects.filter(function (record) {
            return !record.get('confirmed') && !record.isModified('value') && !record.isModified('collectedValue')
        });
        if (menu.down('#edit-estimation-comment')) {
            menu.down('#edit-estimation-comment').setVisible(
                _.find(selectedRecords, function (record) {
                    var canEditingComment;
                    if (record.get('mainModificationState') && record.get('mainModificationState').flag ||
                        record.get('bulkModificationState') && record.get('bulkModificationState').flag) {
                        canEditingComment = flagForComment(record.get('mainModificationState').flag);
                        if (!canEditingComment) {
                            canEditingComment = flagForComment(record.get('bulkModificationState').flag);
                        }
                    }
                    return canEditingComment;
                })
            );
        }
        menu.down('#confirm-value').setVisible(confirms.length);
        menu.down('#remove-readings').setVisible(_.find(selectedRecords, function (record) {
            return record.get('value') || record.get('collectedValue')
        }));

        menu.down('#correct-value').setVisible(_.find(selectedRecords, function (record) {
            return record.get('value')
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
    },

    viewHistory: function (deviceId, channelId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            viewport = Ext.ComponentQuery.query('viewport')[0];

        viewport.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                var model = Ext.ModelManager.getModel('Mdc.model.ChannelOfLoadProfilesOfDevice');
                model.getProxy().setExtraParam('deviceId', deviceId);
                model.load(channelId, {
                    success: function (channel) {
                        var intervalStore = me.getStore('Uni.store.DataIntervalAndZoomLevels'),
                            dataIntervalAndZoomLevels = intervalStore.getIntervalRecord(channel.get('interval')),
                            durationsStore = me.getStore('Mdc.store.LoadProfileDataDurations');

                        durationsStore.loadData(dataIntervalAndZoomLevels.get('duration'));

                        var widget = Ext.widget('device-channels-history', {
                            device: device,
                            router: router,
                            channel: channel,
                            filterDefault: {
                                durationStore: Ext.getStore('Mdc.store.LoadProfileDataDurations'),
                                fromDate: router.queryParams.interval ? new Date(Number(router.queryParams.interval.split('-')[0])) : undefined,
                                duration: router.queryParams.interval ? router.queryParams.interval.split('-')[1] : undefined
                            },
                            showFilter: router.queryParams.isBulk == "true"
                        });
                        var store = me.getStore('Mdc.store.HistoryChannels');

                        store.getProxy().setUrl(router.arguments.deviceId, router.arguments.channelId);
                        store.load({
                            callback: function () {
                                me.getApplication().fireEvent('loadDevice', device);
                                me.getApplication().fireEvent('channelOfLoadProfileOfDeviceLoad', channel);
                                me.getApplication().fireEvent('changecontentevent', widget);
                                viewport.setLoading(false);
                            }
                        });

                    },
                    failure: function () {
                        viewport.setLoading(false);
                    }
                });
            },
            failure: function () {
                viewport.setLoading(false);
            }
        });
    },

    openCorrectWindow: function (record) {
        var me = this;
        if (!Ext.isArray(record)) {
            record = [record];
        }
        Ext.widget('correct-values-window', {
            itemId: 'channel-reading-correct-values-window',
            record: record,
            showInfoMessage: true,
            hideProjectedField: true,
            infoMessageText: Uni.I18n.translate('correct.window.info.message', 'MDC', 'The correction will be applied to {0}', me.currentChannel.get('readingType').fullAliasName),
        }).show();
    },

    correctReadings: function () {
        var me = this,
            model = Ext.create('Uni.model.readings.ReadingCorrection'),
            window = me.getCorrectReadingWindow(),
            records = window.record,
            router = me.getController('Uni.controller.history.Router'),
            commentCombo = window.down('#estimation-comment-box'),
            commentValue = commentCombo.getRawValue(),
            commentId = commentCombo.getValue(),
            mainValidationInfo = {},
            intervalsArray = [],
            comment = {
                commentId: commentId,
                commentValue: commentValue
            };

        window.updateRecord(model);

        if (!Ext.isArray(records)) {
            records = [records];
        }

        Ext.Array.each(records, function (item) {
            if (model.get('onlySuspectOrEstimated')) {
                if (Uni.util.ReadingEditor.checkReadingInfoStatus(item.get('mainValidationInfo')).isSuspectOrEstimated()) {
                    intervalsArray.push({
                        start: item.get('interval').start,
                        end: item.get('interval').end
                    });
                }
            } else {
                intervalsArray.push({
                    start: item.get('interval').start,
                    end: item.get('interval').end
                });
            }
        });

        model.set('intervals', intervalsArray);
        model.set('projected', undefined);
        model.getProxy().setMdcUrl(decodeURIComponent(router.arguments.deviceId), router.arguments.channelId);

        window.setLoading();
        Ext.Ajax.suspendEvent('requestexception');
        model.phantom = false;
        model.save({
            callback: function (rec, operation, success) {
                Ext.Ajax.resumeEvent('requestexception');
                var responseText = Ext.decode(operation.response.responseText, true),
                    chart = me.getPage().down('#deviceLoadProfileChannelGraphView').chart;

                Ext.suspendLayouts();
                if (success && responseText[0]) {
                    Ext.Array.each(responseText, function (correctedInterval) {
                        Ext.Array.findBy(records, function (reading) {
                            if (correctedInterval.interval.start == reading.get('interval').start) {
                                if (commentId !== -1) {
                                    reading.set('mainValidationInfo', comment);
                                }
                                me.updateCorrectedValues(reading, correctedInterval);
                                return true;
                            }
                        });
                    });
                    window.destroy();
                    me.getPage().down('#save-changes-button').isDisabled() && me.showButtons();
                } else {
                    window.setLoading(false);
                    if (responseText) {
                        if (responseText.message) {
                            window.down('#error-label').show();
                            window.down('#error-label').setText('<div style="color: #EB5642">' + responseText.message + '</div>', false);
                        } else if (responseText.readings) {
                            window.down('#error-label').show();
                            var listOfFailedReadings = [];
                            Ext.Array.each(responseText.readings, function (readingTimestamp) {
                                listOfFailedReadings.push(Uni.I18n.translate('general.dateAtTime', 'MDC', '{0} at {1}', [Uni.DateTime.formatDateShort(new Date(readingTimestamp)), Uni.DateTime.formatTimeShort(new Date(readingTimestamp))], false));
                            });
                            window.down('#error-label').setText('<div style="color: #EB5642">' +
                                Uni.I18n.translate('devicechannels.correctionErrorMessage', 'MDC', 'Could not correct {0}',
                                    listOfFailedReadings.join(', ')) + '</div>', false);
                        } else if (responseText.errors) {
                            window.down('#form-errors').show();
                            window.down('#property-form').markInvalid(responseText.errors);
                        }
                    } else {
                        window.destroy();
                    }

                }
                Ext.resumeLayouts(true);
            }
        });
    },

    updateCorrectedValues: function (reading, correctedInterval) {
        var me = this,
            grid = me.getPage().down('deviceLoadProfileChannelDataGrid');

        reading.beginEdit();
        reading.set('value', correctedInterval.value);
        reading.set('mainModificationState', Uni.util.ReadingEditor.modificationState('EDITED'));
        reading.get('mainValidationInfo').validationResult = 'validationStatus.ok';
        reading.endEdit(true);

        grid.getView().refreshNode(grid.getStore().indexOf(reading));

        me.resumeEditorFieldValidation(grid.editingPlugin, {
            record: reading
        });
    },

    showEditValidationRuleWithAttributes: function () {
        this.showEditRuleWithAttributes('validation');
    },

    showEditEstimationRuleWithAttributes: function () {
        this.showEditRuleWithAttributes('estimation');
    },

    showEditRuleWithAttributes: function (type) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            deviceId = router.arguments.deviceId,
            channelId = router.arguments.channelId,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            channelModel = me.getModel('Mdc.model.ChannelOfLoadProfilesOfDevice'),
            dependenciesCounter = 3,
            deviceModel = me.getModel('Mdc.model.Device'),
            ruleWithAttributesModel = type === 'validation' ? me.getModel('Mdc.model.ChannelValidationConfigurationForReadingType') : me.getModel('Mdc.model.ChannelEstimationConfigurationForReadingType'),
            route = type === 'validation' ? router.getRoute('devices/device/channels/validation') : router.getRoute('devices/device/channels/estimation'),
            form,
            rule,
            widget;

        mainView.setLoading();
        ruleWithAttributesModel.getProxy().extraParams = {
            deviceId: Uni.util.Common.encodeURIComponent(router.arguments.deviceId),
            channelId: channelId,
            readingType: router.queryParams.readingType
        };
        channelModel.getProxy().setExtraParam('deviceId', deviceId);
        deviceModel.load(deviceId, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                displayPage();
            }
        });
        channelModel.load(channelId, {
            success: function (record) {
                me.getApplication().fireEvent('channelOfLoadProfileOfDeviceLoad', record);
                displayPage();
            }
        });
        ruleWithAttributesModel.load(router.arguments.ruleId, {
            success: function (record) {
                rule = record;
                displayPage();
            }
        });

        function displayPage() {
            dependenciesCounter--;
            if (!dependenciesCounter) {
                mainView.setLoading(false);
                widget = Ext.widget('rule-with-attributes-edit', {
                    itemId: 'rule-with-attributes-edit-' + type,
                    type: type,
                    route: route,
                    application: me.getApplication()
                });
                form = widget.down('#rule-with-attributes-edit-form');
                form.loadRecord(rule);
                form.setTitle(Ext.String.format("{0} '{1}'", Uni.I18n.translate('general.editAttributesFor', 'MDC', 'Edit attributes for'), rule.get('name')));
                form.down('property-form').loadRecord(rule);
                app.fireEvent('rule-with-attributes-loaded', rule);
                app.fireEvent('changecontentevent', widget);
            }
        }
    }
});
