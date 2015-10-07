Ext.define('Mdc.controller.setup.DeviceChannelData', {
    extend: 'Ext.app.Controller',

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
        },
        {
            ref: 'editPropertyForm',
            selector: '#deviceLoadProfileChannelsEditCustomAttributes property-form'
        },
        {
            ref: 'editCustomAttributesPanel',
            selector: '#deviceLoadProfileChannelsEditCustomAttributes'
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
                        me.getApplication().fireEvent('channelOfLoadProfileOfDeviceLoad', channel);
                        var widget = Ext.widget('tabbedDeviceChannelsView', {
                            title: channel.get('name'),
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
                            filterDefault: activeTab === 1 ? me.setDataFilter(channel, contentName, router) : {}
                        });

                        me.getApplication().fireEvent('changecontentevent', widget);
                        viewport.setLoading(false);
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
        var customAttributesStore = this.getStore('Mdc.store.ChannelCustomAttributeSets');
        customAttributesStore.getProxy().setUrl(device.get('mRID'), channel.get('id'));
        widget.down('#deviceLoadProfileChannelsOverviewForm').loadRecord(channel);
        customAttributesStore.load(function() {
            widget.down('#custom-attribute-sets-placeholder-form-id').loadStore(customAttributesStore);
        });
        this.fromSpecification = true;
        widget.down('#deviceLoadProfileChannelsActionMenu').record = channel;
    },

    setupReadingsTab: function (device, channel) {
        var me = this,
            dataStore = me.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData');

        dataStore.getProxy().setUrl({
            mRID: device.get('mRID'),
            channelId: channel.getId()
        });
        dataStore.loadData([], false);
        dataStore.load();
    },

    makeLinkToChannels: function (router) {
        var link = '<a href="{0}">' + Uni.I18n.translate('general.channels', 'MDC', 'Channels').toLowerCase() + '</a>',
            filter = this.getStore('Mdc.store.Clipboard').get('latest-device-channels-filter'),
            queryParams = filter ? {filter: filter} : null;
        return Ext.String.format(link, router.getRoute('devices/device/channels').buildUrl(null, queryParams));
    },

    makeLinkToIssue: function (router, issueId) {
        var link = '<a href="{0}">' + Uni.I18n.translate('devicechannels.validationblocks', 'MDC', 'Validation blocks').toLowerCase() + '</a>';
        return Ext.String.format(link, router.getRoute('workspace/datavalidationissues/view').buildUrl({issueId: issueId}));
    },

    setDataFilter: function (channel, contentName, router) {
        var me = this,
            intervalStore = me.getStore('Mdc.store.DataIntervalAndZoomLevels'),
            dataIntervalAndZoomLevels = intervalStore.getIntervalRecord(channel.get('interval')),
            all = dataIntervalAndZoomLevels.get('all'),
            durationsStore = me.getStore('Mdc.store.LoadProfileDataDurations'),
            filter = {};

        durationsStore.loadData(dataIntervalAndZoomLevels.get('duration'));
        filter.options = [
            {
                display: Uni.I18n.translate('validationStatus.suspect', 'MDC', 'Suspect'),
                value: 'suspect'
            }
        ];
        if (contentName === 'block') {
            filter.fromDate = new Date(parseInt(router.queryParams['validationBlock']));
            filter.hideDateTtimeSelect = true;
        } else {
            filter.options.push({
                display: Uni.I18n.translate('validationStatus.ok', 'MDC', 'Not suspect'),
                value: 'nonSuspect'
            });
            filter.fromDate = dataIntervalAndZoomLevels.getIntervalStart((channel.get('lastReading') || new Date()));
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

        if(selectionModel.getSelection().length === 1){
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
        var me = this,
            point,
            grid = me.getPage().down('deviceLoadProfileChannelDataGrid'),
            chart = me.getPage().down('#deviceLoadProfileChannelGraphView').chart;

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

        me.getPage().down('#save-changes-button').show();
        me.getPage().down('#undo-button').show();
    },

    saveChannelDataChanges: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            changedData = me.getChangedData(me.getStore('Mdc.store.ChannelOfLoadProfileOfDeviceData')),
            viewport = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        viewport.setLoading();
        if (!Ext.isEmpty(changedData)) {
            Ext.Ajax.request({
                url: Ext.String.format('/api/ddr/devices/{0}/channels/{1}/data', encodeURIComponent(router.arguments.mRID), router.arguments.channelId),
                method: 'PUT',
                jsonData: Ext.encode(changedData),
                timeout: 300000,
                success: function () {
                    router.getRoute().forward(router.arguments, Uni.util.QueryString.getQueryStringValues());
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('devicechannels.successSavingMessage', 'MDC', 'Channel data have been saved'));
                },
                failure: function (response) {
                    viewport.setLoading(false);
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

    beforeEditRecord: function (editor, event) {
        var intervalFlags = event.record.get('intervalFlags');
        event.column.getEditor().allowBlank = !(intervalFlags && intervalFlags.length);
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
            me.getPage().down('#save-changes-button').isHidden() && me.showButtons();

            if (!event.record.get('value')) {
                point.update({ y: null });
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
            }

            if (event.column) {
                event.record.get('mainValidationInfo').validationResult = 'validationStatus.ok';
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
                var responseText = Ext.decode(operation.response.responseText, true),
                    chart = me.getPage().down('#deviceLoadProfileChannelGraphView').chart;

                Ext.suspendLayouts();
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
                    if (responseText.message) {
                        me.getReadingEstimationWindow().down('#error-label').show();
                        me.getReadingEstimationWindow().down('#error-label').setText('<div style="color: #FF0000">' + responseText.message + '</div>', false);
                    } else if (responseText.readings) {
                        me.getReadingEstimationWindow().down('#error-label').show();
                        var listOfFailedReadings = [];
                        Ext.Array.each(responseText.readings, function (readingTimestamp) {
                            listOfFailedReadings.push(Uni.I18n.translate('general.dateattime', 'MDC', '{0} At {1}',[Uni.DateTime.formatDateShort(new Date(readingTimestamp)),Uni.DateTime.formatTimeShort(new Date(readingTimestamp))], false).toLowerCase());
                        });
                        me.getReadingEstimationWindow().down('#error-label').setText('<div style="color: #FF0000">' +
                            Uni.I18n.translate('devicechannels.estimationErrorMessage', 'MDC', 'Could not estimate {0} with {1}',
                                [listOfFailedReadings.join(', '), me.getReadingEstimationWindow().down('#estimator-field').getRawValue().toLowerCase()]) + '</div>', false);
                    } else if (responseText.errors) {
                        me.getReadingEstimationWindow().down('#form-errors').show();
                        me.getReadingEstimationWindow().down('#property-form').markInvalid(responseText.errors);
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

    checkSuspect: function (menu) {
        var validationResult =  menu.record.get('validationResult'),
            mainStatus = false,
            bulkStatus = false;

        if (validationResult) {
            mainStatus = validationResult.main == 'suspect';
            bulkStatus = validationResult.bulk == 'suspect';
        }

        menu.down('#estimate-value').setVisible(mainStatus || bulkStatus);
        if (menu.record.get('confirmed') || menu.record.isModified('value') || menu.record.isModified('collectedValue')) {
            menu.down('#confirm-value').hide();
        } else {
            menu.down('#confirm-value').setVisible(mainStatus || bulkStatus);
        }

        if (menu.down('#remove-reading')) {
            menu.down('#remove-reading').setVisible(menu.record.get('value') || menu.record.get('collectedValue'));
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

                    if (!Ext.isEmpty(bulkValidationResult)) {
                        bulkStatus = validationInfo.bulkValidationInfo.validationResult.split('.')[1] == 'suspect';
                    }

                    if (mainStatus) {
                        rec.get('mainValidationInfo').confirmedNotSaved = true;
                        chart.get(rec.get('interval').start).update({ color: 'rgba(112,187,81,0.3)' });
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

        if (isBulk) {
            Ext.Array.each(record, function (reading) {
                func(reading);
            });
        } else {
            func(record);
        }

        me.getPage().down('#save-changes-button').isHidden() && me.showButtons();
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

    showEditChannelOfLoadProfileCustomAttributes: function (mRID, channelId, customAttributeSetId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];
        viewport.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                var model = Ext.ModelManager.getModel('Mdc.model.ChannelOfLoadProfilesOfDevice');
                model.getProxy().setUrl({
                    mRID: mRID
                });
                model.load(channelId, {
                    success: function (channel) {

                        var widget = Ext.widget('deviceLoadProfileChannelsEditCustomAttributes',{device: device});
                        me.getApplication().fireEvent('loadDevice', device);
                        me.getApplication().fireEvent('channelOfLoadProfileOfDeviceLoad', channel);
                        me.getApplication().fireEvent('channelOfLoadProfileCustomAttributes', device);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.loadPropertiesRecord(widget);
                    },
                    failure: function () {
                        viewport.setLoading(false);
                    }
                });
            }
        });

    },

    loadPropertiesRecord: function(widget) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            model = Ext.ModelManager.getModel('Cps.common.valuesobjects.model.AttributeSetOnObject'),
            form = widget.down('property-form'),
            router = this.getController('Uni.controller.history.Router'),
            routeParams = router.arguments,
            id = routeParams.customAttributeSetId;

        model.load(id, {
            success: function (record) {
                widget.down('#channelEditPanel').setTitle(Uni.I18n.translate('devicechannels.EditCustomAttributeSet', 'MDC', "Edit '{0}'",[record.get('name')]));
                me.getApplication().fireEvent('channelOfLoadProfileCustomAttributes', record);
                form.loadRecord(record);

            },
            callback: function () {
                viewport.setLoading(false);
            }

        });
    },

    restoreChannelOfLoadProfileCustomAttributes: function(){
        this.getEditPropertyForm().restoreAll();
    },

    toPreviousPage : function() {
        if(this.fromSpecification == true){
            this.getController('Uni.controller.history.Router').getRoute('devices/device/channels/channel').forward();
        } else {
            this.getController('Uni.controller.history.Router').getRoute('devices/device/channels').forward();
        }

    },

    saveChannelOfLoadProfileCustomAttributes : function() {
        var me = this,
            form = me.getEditPropertyForm(),
            editView = me.getEditCustomAttributesPanel();

        editView.setLoading();

        form.updateRecord();
        form.getRecord().save({
            callback: function (model, operation, success) {
                editView.setLoading(false);
                if (success) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('devicechannels.customAttributeSetSaved', 'MDC', 'Custom attributes saved.'));
                    me.toPreviousPage();
                }
            }
        });
    }

});