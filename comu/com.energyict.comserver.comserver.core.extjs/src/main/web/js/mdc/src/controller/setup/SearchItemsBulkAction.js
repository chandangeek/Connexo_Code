/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.SearchItemsBulkAction', {
    extend: 'Ext.app.Controller',
    views: [
        'Mdc.view.setup.searchitems.bulk.Step1',
        'Mdc.view.setup.searchitems.bulk.Step2',
        'Mdc.view.setup.searchitems.bulk.Step3',
        'Mdc.view.setup.searchitems.bulk.Step4',
        'Mdc.view.setup.searchitems.bulk.Browse',
        'Mdc.view.setup.searchitems.bulk.Navigation',
        'Mdc.view.setup.searchitems.bulk.Wizard'
    ],
    requires: [
        'Uni.view.window.Wizard',
        'Uni.data.reader.JsonBuffered',
        'Uni.view.window.Confirmation'
    ],
    stores: [
        'Mdc.store.CommunicationSchedulesWithoutPaging',
        'Mdc.store.DeviceConfigurations',
        'Mdc.store.BulkDeviceConfigurations',
        'Cfg.zones.store.ZoneTypes',
        'Isu.store.IssueDevices',
        'Isu.store.IssueReasons',
        'Isu.store.DueinTypes',
        'Isu.store.IssueWorkgroupAssignees',
        'Isu.store.UserList'
    ],
    refs: [
        {
            ref: 'selectedDevicesQty',
            selector: '#searchitems-bulk-step1 #devices-qty-txt'
        },
        {
            ref: 'selectedScheduleQty',
            selector: '#searchitems-bulk-step3 #schedule-qty-txt'
        },
        {
            ref: 'devicesGrid',
            selector: '#searchitems-bulk-step1 devices-selection-grid'
        },
        {
            ref: 'schedulesGrid',
            selector: '#schedulesgrid'
        },
        {
            ref: 'searchItemsWizard',
            selector: '#searchitemswizard'
        },
        {
            ref: 'backButton',
            selector: 'searchitems-wizard #backButton'
        },
        {
            ref: 'nextButton',
            selector: 'searchitems-wizard #nextButton'
        },
        {
            ref: 'confirmButton',
            selector: 'searchitems-wizard #confirmButton'
        },
        {
            ref: 'finishButton',
            selector: 'searchitems-wizard #finishButton'
        },
        {
            ref: 'failureFinishButton',
            selector: 'searchitems-wizard #failureFinishButton'
        },
        {
            ref: 'wizardCancelButton',
            selector: 'searchitems-wizard #wizardCancelButton'
        },
        {
            ref: 'communicationSchedulePreview',
            selector: 'searchitems-wizard #communicationschedulepreview'
        },
        {
            ref: 'navigationMenu',
            selector: '#searchitemsBulkNavigation'
        },
        {
            ref: 'statusPage',
            selector: '#searchitems-bulk-step5'
        },
        {
            ref: 'devicesErrorsPanel',
            selector: 'searchitems-bulk-step1 uni-form-error-message'
        },
        {
            ref: 'step3',
            selector: 'searchitems-bulk-step3'
        },
        {
            ref: 'step4',
            selector: 'searchitems-bulk-step4'
        },
        {
            ref: 'warningMessage',
            selector: 'searchitems-bulk-step3 #stepSelectionError'
        },
        {
            ref: 'deviceZoneForm',
            selector: 'add-to-zone-panel #device-zone-add-form'
        },
        {
            ref: 'manualIssueForm',
            selector: 'issue-manually-creation-rules-item-add issue-manually-creation-rules-item'
        }
    ],

    init: function () {
        this.control({
            'searchitems-bulk-step3 #schedulesgrid': {
                selectionchange: this.updateScheduleSelection
            },
            'searchitems-bulk-step3 #schedulesgrid gridview': {
                itemclick: this.previewCommunicationSchedule
            },
            'searchitems-wizard #backButton': {
                click: this.backClick
            },
            'searchitems-wizard #nextButton': {
                click: this.nextClick
            },
            'searchitems-wizard #confirmButton': {
                click: this.confirmClick
            },
            'searchitems-wizard #finishButton': {
                click: this.goBack
            },
            'searchitems-wizard #failureFinishButton': {
                click: this.goBack
            },
            'searchitems-wizard #wizardCancelButton': {
                click: this.goBack
            },
            'searchitems-wizard #createCommunicationSchedule': {
                click: this.createCommunicationSchedule
            },
            '#searchitemsBulkNavigation': {
                movetostep: this.navigateToStep
            },
            'searchitems-bulk-step5 #viewDevicesButton': {
                click: this.showViewDevices
            },
            'searchitems-bulk-step2 #searchitemsactionselect': {
                change: this.enableNextButton
            },
            'searchitems-bulk-step4 #strategyRadioGroup': {
                change: this.enableConfirmButton
            },
            'searchitems-bulk-step3 #start-process-form #processes-definition-combo': {
                select: this.processComboChange
            }
        });
    },

    showBulkAction: function () {
        var me = this,
            search = me.getController('Mdc.controller.Search'),
            searchResults = Ext.getStore('Uni.store.search.Results'),
            widget,
            goOnWithTheCurrentSearchResults = function() {
                if (!searchResults.getCount()) {
                    me.goBack();
                } else {
                    me.devices = null;
                    me.allDevices = false;
                    me.schedules = null;
                    me.operation = null;
                    me.configData = null;
                    me.shedulesUnchecked = false;
                    me.zoneTypeName = null;
                    me.zoneName = null;

                    var store = Ext.create('Ext.data.Store', {
                        buffered: true,
                        pageSize: 100,
                        remoteFilter: true,
                        model: searchResults.model,
                        filters: searchResults.filters.getRange(),
                        proxy: searchResults.getProxy()
                    });

                    // we replace reader to buffered due to our store is buffered
                    store.getProxy().setReader(Ext.create('Uni.data.reader.JsonBuffered', store.getProxy().getReader()));

                    store.addListener('beforeprefetch', function () {
                        me.getDevicesGrid().setLoading();
                    }, this);

                    store.addListener('prefetch', function () {
                        me.getDevicesGrid().setLoading(false);
                    }, this);


                    widget = Ext.widget('searchitems-bulk-browse', {
                        deviceStore: store
                    });
                    me.getApplication().fireEvent('changecontentevent', widget);
                    store.load();
                    me.getStore('Mdc.store.CommunicationSchedulesWithoutPaging').load();
                }
            };

        if (search.service.changedFiltersNotYetApplied) {
            var confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('general.apply', 'MDC', 'Apply'),
                secondConfirmText: Uni.I18n.translate('general.dontApply', 'MDC', "Don't apply"),
                green: true,
                confirmation: function (button) {
                    confirmationWindow.close();
                    if (button.action === 'confirm') { // (Re)apply the criteria first
                        searchResults.on('load', function() {
                            goOnWithTheCurrentSearchResults();
                        }, me, {single:true});
                        search.service.applyFilters();
                    } else if (button.action === 'confirm2') { // Don't (re)apply the cirteria
                        goOnWithTheCurrentSearchResults();
                        search.service.rollbackCriteriaChanges();
                    }
                }
            });
            confirmationWindow.show({
                title: Uni.I18n.translate('general.performBulkAction', 'MDC', 'Perform bulk action?'),
                msg: Uni.I18n.translate('general.unconfirmedSearchCriteria', 'MDC',
                    "Some search criteria haven't been applied. Do you want to apply them?")
            });
        } else {
            goOnWithTheCurrentSearchResults();
        }
    },

    enableNextButton: function() {
        var me = this;

        me.getNextButton().enable();
    },

    enableConfirmButton: function (group) {
        var me = this,
            wizard = me.getSearchItemsWizard();
        me.strategy = group.getChecked()[0].inputValue;
        wizard.down('#confirmButton').enable()
    },

    updateScheduleSelection: function (selModel, selected) {
        var count = selected.length;
        if (count === 0) {
            this.getCommunicationSchedulePreview().hide();
        }
        this.shedulesUnchecked = count === 0;
    },

    previewCommunicationSchedule: function (grid, record) {
        var me = this,
            preview = this.getCommunicationSchedulePreview(),
            communicationSchedules = grid.getSelectionModel().getSelection();

        if (!this.shedulesUnchecked) {
            preview.down('form').loadRecord(record);
            preview.setTitle(record.get('name'));
            preview.show();
        } else {
            this.shedulesUnchecked = false;
        }

        if (me.operation === 'add') {
            me.getStep3().down('#step3-errors').setVisible(false);
            me.getWarningMessage().hide();
            if (communicationSchedules.length > 1) {
                me.checkOverlap(communicationSchedules);
            }
        }
    },

    checkOverlap: function (communicationSchedules) {
        var me = this;
        var valuesToCheck = [];
        Ext.each(communicationSchedules, function (item) {
            valuesToCheck.push.apply(valuesToCheck, item.get('comTaskUsages'));
        });
        if (_.uniq(valuesToCheck, function (item) {
                return item.id;
            }).length === valuesToCheck.length) {
            me.getWarningMessage().hide();
            me.getStep3().down('#step3-errors').setVisible(false);
            return true;
        } else {
            me.getWarningMessage().update('<span style="color:red">' + Uni.I18n.translate('deviceCommunicationSchedule.ComTaskOverlap', 'MDC', "Shared communication schedules can't contain the same communication task.") + '</span>');
            me.getWarningMessage().show();
            me.getStep3().down('#step3-errors').setVisible(true);
            return false;
        }
    },

    backClick: function () {
        var layout = this.getSearchItemsWizard().getLayout(),
            currentCmp = layout.getActiveItem();

        this.changeContent(layout.getPrev(), currentCmp);
        (currentCmp.name !== 'statusPageViewDevices') && this.getNavigationMenu().movePrevStep();
    }
    ,

    nextClick: function () {
        var layout = this.getSearchItemsWizard().getLayout();
        this.changeContent(layout.getNext(), layout.getActiveItem()) && this.getNavigationMenu().moveNextStep();
    },

    removeZoneWarningPanel: function()
    {
        var warningPanel = this.getStep4().down('#warning-device-zones-panel');
        if(warningPanel)
            this.getStep4().remove(warningPanel);
    },

    removeScheduleStratey: function()
    {
        this.getStep4().isRemove();
    },

    devicesWithZoneTypeAlreadyDefined: function () {
        var me = this,
            wizard = me.getSearchItemsWizard(),
            deviceIds = [],
            zoneTypeId = me.getDeviceZoneForm().down("#zone-type").getValue(),
            zoneId = me.getDeviceZoneForm().down("#zone-name").getValue()
            filter = [],

            urlZones = '/api/ddr/devices/zones/byZoneTypeId';

        Ext.each(me.devices, function (item) {
            deviceIds.push(item.getId());
        });

        if (me.allDevices) {
            var store = me.getDevicesGrid().getStore();
            filter = store.getProxy().encodeFilters(store.filters.getRange());
        }

        wizard.setLoading(true);
        Ext.Ajax.request({
            url: urlZones,
            method: 'GET',
            params : {
                deviceIds: deviceIds,
                zoneTypeId: zoneTypeId,
                zoneId: zoneId,
                filter: filter
            },
            timeout: 180000,
            success: function (response) {
                var deviceList = Ext.JSON.decode(response.responseText);
                if(deviceList.total > 0)
                    me.getStep4().showWarningZoneTypeAlreadyLinkedToDevice(deviceList);
            },

            failure: function () {
            },
            callback: function () {
                wizard.setLoading(false);
            }
        });

    },
    confirmClick: function () {
        var me = this,
            wizard = me.getSearchItemsWizard(),
            finishBtn = wizard.down('#finishButton'),
            statusPage = me.getStatusPage(),
            scheduleIds = [],
            deviceIds = [],
            url = '/api/ddr/devices/schedules',
            request = {},
            jsonData,
            infoMessage;

        finishBtn.disable();
        statusPage.removeAll();
        wizard.setLoading(true);

        if (me.operation === 'startprocess') {
            var processJsonData = {
                    deploymentId: me.processValues.deploymentId,
                    id: me.processValues.processId,
                    processName: me.processValues.name,
                    processVersion: me.processValues.version,
                    versionDB: me.processRecord.versionDB,
                    properties: me.processValues.properties
                },


            processUrl = '/api/bpm/runtime/processcontent/' + processJsonData.deploymentId + '/' + processJsonData.id;

            processJsonData.bulkBusinessObjects = me.validatedForProcessDevices.map(function (mRID) {
                return {
                    id: 'deviceId',
                    type: 'device',
                    value: mRID
                }
            });

            Ext.Ajax.request({
                url: processUrl,
                method: 'PUT',
                jsonData: processJsonData,
                timeout: 180000,
                success: function (response) {
                    statusPage.showChangeDeviceConfigSuccess(
                        me.buildFinalMessage()
                    );
                    finishBtn.enable();
                    wizard.setLoading(false);
                },

                failure: function () {
                    finishBtn.enable();
                    wizard.setLoading(false);
                }
            });

        } if (me.operation == 'addToZone' || me.operation == 'removeFromZone') {
            var  urlZones = '/api/ddr/devices/zones';

            Ext.each(me.devices, function (item) {
                deviceIds.push(item.getId());
            });

            request.action = me.operation;
            request.zoneId = me.getDeviceZoneForm().down("#zone-name").getValue();
            request.zoneTypeId = me.getDeviceZoneForm().down("#zone-type").getValue();
            request.strategy = me.strategy;
            if (me.allDevices) {
                var store = me.getDevicesGrid().getStore();
                request.filter = store.getProxy().encodeFilters(store.filters.getRange());
            } else {
                request.deviceIds = deviceIds;
            }
            jsonData = Ext.encode(request);
            Ext.Ajax.request({
                url: urlZones,
                method: 'PUT',
                jsonData: jsonData,
                timeout: 180000,
                success: function (response) {
                    statusPage.showChangeDeviceConfigSuccess(
                        me.buildFinalMessage()
                    );
                    finishBtn.enable();
                    wizard.setLoading(false);
                },

                failure: function () {
                    finishBtn.enable();
                    wizard.setLoading(false);
                }
            });
        } else if (me.operation == 'createmanualissue') {
            var mDeviceIds = [];
            var manualIssueBulk = '/api/isu/issues/bulkadd';
            if (me.allDevices) {
                var store = me.getDevicesGrid().getStore();
                var deviceData = store.getProxy().getReader().jsonData;
                if (deviceData && deviceData.searchResults){
                    Ext.each(deviceData.searchResults, function (item) {
                        mDeviceIds.push(item.id);
                    });
                }
            } else {
                mDeviceIds = deviceIds;
            }
            var form = me.getManualIssueForm(),
               comboReason = form.down('#issueReason'),
               reasonEditedValue = comboReason.getRawValue(),
               reason = comboReason.store.find('name', reasonEditedValue);

            form.updateRecord();

            var record = form.getRecord();

            if(reason === -1 && reasonEditedValue.trim() != ''){
                var value = reasonEditedValue.trim();
                var id = value.toLowerCase().replace (/ /g, '.');
                var rec = {
                    id: id,
                    name: value
                };
                comboReason.store.add(rec);
                comboReason.setValue(comboReason.store.getAt(comboReason.store.count()-1).get('id'));
                record.set('reasonId', id)
            }
            var urgency = record.get('priority.urgency');
            var impact = record.get('priority.impact');
            if ( urgency !== undefined && impact !== undefined ) record.set('priority' , urgency + ':' + impact);
            if (form.down('#dueDateTrigger')) {
                if (form.down('#dueDateTrigger')) {
                    record.set('dueIn', {
                        number: form.down('[name=dueIn.number]').getValue(),
                        type: form.down('[name=dueIn.type]').getValue()
                    });
                } else {
                    record.set('dueIn', null);
                }
            }

            var jsonData = [];
            for (var i = 0; i < mDeviceIds.length; i++){
                var data = record.data;
                data.deviceMrid = mDeviceIds[i];
                jsonData.push(data);
            }

            Ext.Ajax.request({
                url: manualIssueBulk,
                method: 'POST',
                jsonData: jsonData,
                timeout: 180000,
                success: function (response) {
                    statusPage.showChangeDeviceConfigSuccess(
                        me.buildFinalMessage()
                    );
                    finishBtn.enable();
                    wizard.setLoading(false);
                },

                failure: function () {
                    finishBtn.enable();
                    wizard.setLoading(false);
                }
            });
        }
        else if (me.operation != 'changeconfig') {
            Ext.each(me.schedules, function (item) {
                scheduleIds.push(item.getId());
            });
            Ext.each(me.devices, function (item) {
                deviceIds.push(item.getId());
            });
            request.action = me.operation;
            request.scheduleIds = scheduleIds;
            request.strategy = me.strategy;
            if (me.allDevices) {
                var store = me.getDevicesGrid().getStore();
                request.filter = store.getProxy().encodeFilters(store.filters.getRange());
            } else {
                request.deviceIds = deviceIds;
            }
            jsonData = Ext.encode(request);
            Ext.Ajax.request({
                url: url,
                method: 'PUT',
                jsonData: jsonData,
                timeout: 180000,
                success: function (response) {
                    statusPage.showChangeDeviceConfigSuccess(
                        me.buildFinalMessage()
                    );
                    finishBtn.enable();
                    wizard.setLoading(false);
                },

                failure: function () {
                    finishBtn.enable();
                    wizard.setLoading(false);
                }
            });
        } else {
            var callback = function (success) {
                statusPage.setLoading(false);
                wizard.setLoading(false);
                if (success) {
                    statusPage.showChangeDeviceConfigSuccess(
                        me.buildFinalMessage()
                    );
                    finishBtn.enable();
                }
            };
            me.changeDeviceConfig(me.configData.fromconfig, me.configData.toconfig, callback)
        }
        me.nextClick();
    }
    ,

    goBack: function () {
        var me = this,
            grid = me.getDevicesGrid(),
            search = me.getController('Mdc.controller.Search'),
            router = me.getController('Uni.controller.history.Router'),
            queryParams;

        if (search.service.searchDomain) {
            queryParams = {
                restore: true
            };
        }

        router.getRoute('search').forward(null, queryParams);
    }
    ,

    showStatusMsg: function (msg) {
        var me = this;
        me.getStatusPage().add(msg);
    }
    ,

    buildMessage: function (message) {
        var messagePanel = {
            xType: 'panel'
        };

        messagePanel.html = '<h3>' + message + '</h3>';

        return messagePanel;
    }
    ,

    buildFinalMessage: function () {
        var me = this,
            message = '',
            finalMessage = '',
            scheduleList = '';

        switch (me.operation) {
            case 'add':
                message = Ext.isEmpty(me.devices)
                    ? Uni.I18n.translate('searchItems.bulk.successfullyAddedCommunicationSchedule.all1', 'MDC', "to all devices")
                    : Uni.I18n.translatePlural('searchItems.bulk.successfullyAddedCommunicationSchedule1', me.devices.length, 'MDC',
                    "to {0} devices",
                    "to {0} device",
                    "to {0} devices"
                );
                if (me.schedules.length === 1) {
                    finalMessage = Uni.I18n.translate('searchItems.bulk.addComScheduleToDevices.baseSuccessMsg', 'MDC',
                        "Successfully added communication schedule '{0}' {1}", [me.schedules[0].get('name'), message]);
                } else {
                    Ext.each(me.schedules, function (item, index) {
                        scheduleList += (index ? ', ' : '') + '\'' + item.get('name') + '\'';
                    });
                    finalMessage = Uni.I18n.translate('searchItems.bulk.addComSchedulesToDevices.baseSuccessMsg', 'MDC',
                        "Successfully added communication schedules {0} {1}", [scheduleList, message]);
                }
                break;
            case 'remove':
                message = Ext.isEmpty(me.devices)
                    ? Uni.I18n.translate('searchItems.bulk.successfullyRemovedCommunicationSchedule.all1', 'MDC', "from all devices")
                    : Uni.I18n.translatePlural('searchItems.bulk.successfullyRemovedCommunicationSchedule1', me.devices.length, 'MDC',
                    "from {0} devices",
                    "from {0} device",
                    "from {0} devices"
                );
                if (me.schedules.length === 1) {
                    finalMessage = Uni.I18n.translate('searchItems.bulk.removeComScheduleToDevices.baseSuccessMsg1', 'MDC',
                        "Successfully removed communication schedule '{0}' {1}", [me.schedules[0].get('name'), message]);
                } else {
                    Ext.each(me.schedules, function (item, index) {
                        scheduleList += (index ? ', ' : '') + '\'' + item.get('name') + '\'';
                    });
                    finalMessage = Uni.I18n.translate('searchItems.bulk.removeComSchedulesToDevices.baseSuccessMsg1', 'MDC',
                        "Successfully removed communication schedules {0} {1}", [scheduleList, message]);
                }
                break;
            case 'changeconfig':
                finalMessage = Ext.isEmpty(me.devices)
                    ? Uni.I18n.translate('searchItems.bulk.devConfigQueuedTitle.all', 'MDC', 'All devices are queued to change their configuration.')
                    : Uni.I18n.translatePlural('searchItems.bulk.devConfigQueuedTitle', me.devices.length, 'MDC',
                    'No devices are queued to change their configuration.',
                    'One device is queued to change its configuration.',
                    '{0} devices are queued to change their configuration.'
                );
                break;
            case 'startprocess':
                if (me.allDevices) {
                    pattern = Uni.I18n.translate('searchItems.bulk.startProcess.successMsg.all', 'MDC', "The process has been triggered for {0} out of all selected devices.")
                    finalMessage = Ext.String.format(pattern, me.validatedForProcessDevices.length, false);
                } else {
                    if (me.devices.length === 1) {
                        pattern = Uni.I18n.translate('searchItems.bulk.startProcess.successMsg.device', 'MDC', "The process has been triggered for {0} out of {1} selected device.")
                    } else {
                        pattern = Uni.I18n.translate('searchItems.bulk.startProcess.successMsg.devices', 'MDC', "The process has been triggered for {0} out of {1} selected devices.")
                    }
                    finalMessage = Ext.String.format(pattern, me.validatedForProcessDevices.length, me.devices.length, false);
                }
                break;

            case 'addToZone':
                message = Ext.isEmpty(me.devices)
                    ? Uni.I18n.translate('searchItems.bulk.successfullyAddedZone.all', 'MDC', "to all devices")
                    : Uni.I18n.translatePlural('searchItems.bulk.successfullyAddedZone1', me.devices.length, 'MDC',
                        "to {0} devices",
                        "to {0} device",
                        "to {0} devices"
                    );

                finalMessage = Uni.I18n.translate('searchItems.bulk.addZoneToDevices.baseSuccessMsg', 'MDC',
                    "Successfully added zone '{0}' {1}", [me.zoneName, message]);
                break;
            case 'removeFromZone':
                message = Ext.isEmpty(me.devices)
                    ? Uni.I18n.translate('searchItems.bulk.successfullyRemovedZone.all1', 'MDC', "from all devices")
                    : Uni.I18n.translatePlural('searchItems.bulk.successfullyRemovedZone1', me.devices.length, 'MDC',
                        "from {0} devices",
                        "from {0} device",
                        "from {0} devices"
                    );

                finalMessage = Uni.I18n.translate('searchItems.bulk.removeZoneToDevices.baseSuccessMsg1', 'MDC',
                    "Successfully removed zone '{0}' {1}", [me.zoneName, message]);
                break;
        }

        return finalMessage;
    },

    navigateToStep: function (index) {
        var me = this,
            layout = me.getSearchItemsWizard().getLayout(),
            currentCmp = layout.getActiveItem(),
            nextCmp = layout.getLayoutItems()[index - 1];
        me.changeContent(nextCmp, currentCmp);
    },

    isDeviceConfigEqual: function (devices) {
        var devConfigId,
            result = true;
        devConfigId = devices[0].get('deviceConfigurationId');
        Ext.each(devices, function (device) {
            if (devConfigId != device.get('deviceConfigurationId')) {
                result = false;
            }
        });
        return result;
    }
    ,

    checkConflictMappings: function (fromConfig, toConfig, callback) {
        var me = this,
            url = '/api/dtc/devicetypes/' + me.deviceType + '/deviceconfigurations/' + fromConfig + '/conflictmappings/' + toConfig + '/unsolved';
        Ext.Ajax.request({
            url: url,
            method: 'GET',
            timeout: 180000,
            callback: function (operation, success, response) {
                var conflictMappings = false,
                    resp = Ext.JSON.decode(response.responseText, true);
                if (success && resp) {
                    if (resp.total) conflictMappings = resp.conflictMappings[0].id;
                }
                callback(conflictMappings)
            }
        });
    },

    validateStartProcessAction: function (processValues, callback) {
        var me = this,
            store = me.getDevicesGrid().getStore(),
            url = '/api/ddr/devices/validatedevices',
            jsonData = {
                'action': 'ValidateDevices',
                'deviceIds': [],
                'filter': store.getProxy().encodeFilters(store.filters.getRange()),
                'name': processValues.name,
                'processId': processValues.processId,
                'version': processValues.version,
                'deploymentId': processValues.deploymentId,
                'properties': processValues.properties
            };
        if (!me.allDevices) {
            me.devices.forEach(function (item) {
                jsonData['deviceIds'].push(item.getId())
            });
        }

        Ext.Ajax.request({
            url: url,
            method: 'PUT',
            jsonData: jsonData,
            timeout: 180000,
            callback: function (operation, success, response) {

                callback(success, response);
            }
        });
    },


    changeDeviceConfig: function (fromConfig, toConfig, callback) {
        var me = this,
            store = me.getDevicesGrid().getStore(),
            url = '/api/ddr/devices/changedeviceconfig',
            jsonData = {
                'action': 'ChangeDeviceConfiguration',
                'deviceIds': [],
                'filter': store.getProxy().encodeFilters(store.filters.getRange()),
                'newDeviceConfiguration': toConfig
            };
        if (!me.allDevices) {
            me.devices.forEach(function (item) {
                jsonData['deviceIds'].push(item.getId())
            });
        }

        Ext.Ajax.request({
            url: url,
            method: 'PUT',
            jsonData: jsonData,
            timeout: 180000,
            callback: function (operation, success) {
                callback(success)
            }
        });
    },

    changeDeviceConfigAvailable: function (filters) {
        var me = this,
            result = false;
        Ext.each(filters, function (item) {
            if (item.id === 'deviceConfiguration') {
                if (item.value[0].criteria.length == 1) {
                    result = true;
                }
                me.deviceConfigId = item.value[0].criteria[0] - 0;
            } else if (item.id === 'deviceType') {
                me.deviceType = item.value[0].criteria[0] - 0;
            }
        });
        return result;
    },

    changeContent: function (nextCmp, currentCmp) {
        var me = this, errorPanel = null, additionalText, progressBar,
            router = me.getController('Uni.controller.history.Router'),
            search = me.getController('Mdc.controller.Search'),
            wizard = me.getSearchItemsWizard(),
            layout = wizard.getLayout(),
            errorContainer = currentCmp.down('#stepSelectionError'),
            errorIsAlreadyShown = false;

        this.removeZoneWarningPanel();
        this.removeScheduleStratey();

        switch (currentCmp.name) {
            case 'selectDevices':
                me.allDevices = me.getDevicesGrid().isAllSelected();
                errorPanel = currentCmp.down('#step1-errors');
                errorPanel.hide();
                if (!me.allDevices) {
                    me.devices = me.getDevicesGrid().getSelectionModel().getSelection();
                } else {
                    me.devices = null;

                }

                if (me.changeDeviceConfigAvailable(search.service.getFilters())) {
                    nextCmp.down('#searchitemschangeconfig').enable();
                } else {
                    nextCmp.down('#searchitemschangeconfig').disable();
                    if (nextCmp.down('#searchitemschangeconfig').getValue()) {
                        nextCmp.down('#searchitemsactionselect').setValue({operation: 'add'});
                    }

                }

                me.validation = me.allDevices || me.devices.length;
                break;
            case 'selectOperation':
                me.operation = currentCmp.down('#searchitemsactionselect').getValue().operation;
                if (nextCmp.name == 'selectActionItems') {
                    if (me.operation == 'changeconfig') {
                        var configStore = me.getStore('Mdc.store.BulkDeviceConfigurations'),
                            changeDeviceConfigForm = nextCmp.down('#change-device-configuration'),
                            currentConfigField = nextCmp.down('#current-device-config-selection');

                        nextCmp.down('#issue-manually-creation-rules-item-add-bulk').hide();
                        nextCmp.down('#device-zone-add-panel').hide();
                        nextCmp.down('#select-schedules-panel').hide();
                        nextCmp.down('#bulk-start-processes-panel').hide();
                        changeDeviceConfigForm.show();
                        changeDeviceConfigForm.getForm().clearInvalid();

                        var device = me.getDevicesGrid().getStore().getAt(0);
                        configStore.getProxy().setUrl({deviceType: me.deviceType});

                        wizard.setLoading(true);
                        configStore.clearFilter(false);
                        configStore.addFilter([
                            function (record) {
                                return record.get('id') !== me.deviceConfigId && !record.get('dataloggerEnabled');
                            }
                        ]);
                        configStore.load(function (operation, success) {
                            var deviceConfig = this.getById(me.deviceConfigId);
                            currentConfigField.setValue(deviceConfig.get('name'));
                            if (success && (configStore.getCount() < 1)) {
                                wizard.down('#nextButton').disable();
                                nextCmp.down('#new-device-config-selection').hide();
                                nextCmp.down('#no-device-configuration').show();
                            } else {
                                wizard.down('#nextButton').enable();
                                nextCmp.down('#new-device-config-selection').show();
                                nextCmp.down('#no-device-configuration').hide();
                            }
                            if (configStore.getCount() === 1) {
                                wizard.down('#new-device-config-selection').setValue(this.getAt(0).get('id'));
                                wizard.down('#nextButton').enable();
                            }
                            wizard.setLoading(false);
                        });
                    } else if(me.operation == 'startprocess') {
                        nextCmp.down('#issue-manually-creation-rules-item-add-bulk').hide();
                        nextCmp.down('#select-schedules-panel').hide();
                        nextCmp.down('#change-device-configuration').hide();
                        nextCmp.down('#device-zone-add-panel').hide();
                        nextCmp.down('#bulk-start-processes-panel').show();
                        nextCmp.down('#bulk-start-processes-panel').loadAvailableProcessesStore(function (visible) {
                            wizard.down('#nextButton').setDisabled(visible);
                        });

                    }
                    else if(me.operation == 'addToZone' || me.operation == 'removeFromZone'){
                        nextCmp.down('#issue-manually-creation-rules-item-add-bulk').hide();
                        nextCmp.down('#select-schedules-panel').hide();
                        nextCmp.down('#change-device-configuration').hide();
                        nextCmp.down('#bulk-start-processes-panel').hide();

                        var zoneTypesStore = me.getStore('Cfg.zones.store.ZoneTypes');
                        zoneTypesStore.load(function (operation, success) {
                            nextCmp.down('#device-zone-add-panel').show();
                        });
                    }
                     else if(me.operation == 'createmanualissue'){
                        nextCmp.down('#issue-manually-creation-rules-item-add-bulk').show();
                        nextCmp.down('#select-schedules-panel').hide();
                        nextCmp.down('#change-device-configuration').hide();
                        nextCmp.down('#bulk-start-processes-panel').hide();
                        nextCmp.down('#device-zone-add-panel').hide();
                    }
                     else {
                        nextCmp.down('#select-schedules-panel').show();
                        nextCmp.down('#issue-manually-creation-rules-item-add-bulk').hide();
                        nextCmp.down('#change-device-configuration').hide();
                        nextCmp.down('#bulk-start-processes-panel').hide();
                        nextCmp.down('#device-zone-add-panel').hide();
                    }
                }
                break;
            case 'selectActionItems':
                if (me.operation == 'startprocess') {
                    var startProcessPanel = currentCmp.down('#bulk-start-processes-panel'),
                        url = startProcessPanel.properties.successLink,
                        extraParams = startProcessPanel.properties.startProcessParams,
                        startProcessForm = startProcessPanel.down('#process-start-form'),
                        processStartContent = startProcessPanel.down('#process-start-content'),
                        startProcessRecord = processStartContent.startProcessRecord,
                        propertyForm = processStartContent.down('property-form'),
                        formErrorsPanel = startProcessPanel.down('#start-process-form').down('#form-errors'),
                        businessObject = {};
                    if (propertyForm.isValid() && startProcessRecord) {
                        me.validation = true;
                        if (!formErrorsPanel.isHidden()) {
                            formErrorsPanel.hide();
                            startProcessForm.getForm().clearInvalid();
                        }

                        propertyForm.updateRecord();

                        startProcessRecord.beginEdit();

                        Ext.Array.each(extraParams, function(param) {
                            businessObject[param.name] = param.value;
                        });

                        var processValues = {
                            'name': me.processRecord.name,
                            'processId': me.processRecord.processId,
                            'version': me.processRecord.version,
                            'deploymentId': me.processRecord.deploymentId,
                            'properties': startProcessRecord.getWriteData(true, true).properties
                        };
                        me.processValues = processValues;

                        wizard.setLoading(true);
                        me.validateStartProcessAction(processValues, function (success, response) {
                            var resp = Ext.JSON.decode(response.responseText, true);
                            if (success && resp) {
                                me.validatedForProcessDevices = resp;
                                me.validation = true;
                                if(nextCmp.name == 'confirmPage'){
                                    nextCmp.removeAll();
                                    wizard.down('#confirmButton').enable();
                                    var message = me.buildConfirmMessage();
                                    additionalText = Uni.I18n.translate('searchItems.bulk.changeDevConfigWarningMessage', 'MDC', 'Changing the device configuration could lead to critical data loss (security settings, connection methods, communication tasks,...).');
                                    nextCmp.showStartProcessConfirmation(message.title, message.body, null, additionalText)
                                }
                                wizard.setLoading(false);
                            } else {
                                if (response.status == 400) {
                                    var json = Ext.decode(operation.response.responseText, true);
                                    if (json && json.errors) {
                                        formErrorsPanel.show();
                                        propertyForm.markInvalid(json.errors);
                                    }
                                }
                                me.validation = false;
                            }
                        });
                    }
                    else {
                        formErrorsPanel.show();
                        me.validation = false;
                    }
                    errorContainer = null;
                } else if (me.operation == 'addToZone' || me.operation == 'removeFromZone'){

                    var zonePanel = currentCmp.down('#device-zone-add-panel'),
                        propertyFormZone = zonePanel.down('#device-zone-add-form'),
                        formErrorsPanelZone = propertyFormZone.down('#form-errors');

                    if (propertyFormZone.isValid()) {

                        if (!formErrorsPanelZone.isHidden()) {
                            formErrorsPanelZone.hide();
                            propertyFormZone.getForm().clearInvalid();
                        }

                        me.validation = true;
                        me.zoneName = me.getDeviceZoneForm().down("#zone-name").getRawValue();
                        me.zoneTypeName = me.getDeviceZoneForm().down("#zone-type").getRawValue();
                        if (me.operation == 'addToZone' && nextCmp.name == 'confirmPage')//to avoid sending the rest when click on back button
                            me.devicesWithZoneTypeAlreadyDefined();
                    } else {
                        formErrorsPanelZone.show();
                        me.validation = false;
                    }

                } else if (me.operation == 'createmanualissue') {
                    var form = currentCmp.down('#issue-manually-creation-rules-item-add-bulk').down('form');
                    var comboReason = form.down('#issueReason');
                    var reasonEditedValue = comboReason.getRawValue();
                    var errorPanel = currentCmp.down('#step3-errors');
                    if ( reasonEditedValue.trim() == '') {
                        errorPanel.show();
                        comboReason.markInvalid('This field is required');
                        me.validation = false;
                    }
                }else if (me.operation != 'changeconfig') {
                    me.schedules = me.getSchedulesGrid().getSelectionModel().getSelection();
                    errorPanel = currentCmp.down('#step3-errors');
                    me.validation = me.schedules.length;
                }  else {
                    var form = currentCmp.down('#change-device-configuration');
                    me.validation = currentCmp.down('#change-device-configuration').isValid();
                    me.validation && (me.configData = form.getValues());
                    me.configNames = {
                        fromconfig: form.down('#current-device-config-selection').getRawValue(),
                        toconfig: form.down('#new-device-config-selection').getRawValue()
                    };
                    errorPanel = currentCmp.down('#step3-errors');
                    errorContainer = null;
                }

                break;
        }
        (currentCmp.navigationIndex > nextCmp.navigationIndex) && (me.validation = true);

        errorIsAlreadyShown = errorPanel !== null && errorPanel.isVisible();
        if (me.validation && !errorIsAlreadyShown) {
            switch (nextCmp.name) {
                case 'confirmPage':
                    if (me.operation == 'startprocess'){

                    }
                    if (me.operation == 'addToZone' || me.operation == 'removeFromZone'){
                        nextCmp.showMessage(me.buildConfirmMessage());
                        wizard.down('#confirmButton').enable();
                    } else if (me.operation != 'changeconfig') {
                        nextCmp.showMessage(me.buildConfirmMessage());
                        wizard.down('#confirmButton').setDisabled(me.operation === 'add' && wizard.down('#strategyRadioGroup').getChecked().length === 0);
                        if (me.operation === 'add') {
                            nextCmp.showStrategyForm();
                        }
                    } else {
                        wizard.setLoading(true);
                        nextCmp.removeAll();
                        me.checkConflictMappings(me.deviceConfigId, me.configData['toconfig'], function (unsolvedConflicts) {
                            wizard.setLoading(false);
                            if (unsolvedConflicts) {
                                me.getNavigationMenu().markInvalid();
                                var title = me.devices?Uni.I18n.translatePlural('searchItems.bulk.devConfigUnsolvedConflictsTitle', me.devices.length, 'MDC', "Unable to change device configuration of {0} devices", "Unable to change device configuration of {0} device", "Unable to change device configuration of {0} devices"):
                                    Uni.I18n.translate('searchItems.bulk.devConfigUnsolvedConflictsTitleForSearch', 'MDC', "Unable to change device configuration of the selected devices"),
                                    text = Ext.String.format(Uni.I18n.translate('searchItems.bulk.devConfigUnsolvedConflictsMsg', 'MDC', 'The configuration of devices with current configuration \'{0}\' cannot be changed to \'{1}\' due to unsolved conflicts.'), me.configNames.fromconfig, me.configNames.toconfig);
                                text = text.replace('{fromconfig}', me.configNames.fromconfig).replace('{toconfig}', me.configNames.toconfig);
                                if (Mdc.privileges.DeviceType.canAdministrate()) {
                                    var solveLink = router.getRoute('administration/devicetypes/view/conflictmappings/edit').buildUrl({deviceTypeId: me.deviceType, id: unsolvedConflicts});
                                    me.getController('Mdc.controller.setup.DeviceConflictingMapping').returnInfo = {
                                        from: 'changeDeviceConfigurationBulk'
                                    };
                                    wizard.down('#confirmButton').disable();
                                    nextCmp.showChangeDeviceConfigConfirmation(title, text, solveLink, null, 'error');
                                } else {
                                    wizard.down('#confirmButton').hide();
                                    wizard.down('#backButton').hide();
                                    wizard.down('#wizardCancelButton').hide();
                                    wizard.down('#failureFinishButton').show();
                                    additionalText = Uni.I18n.translate('searchItems.bulk.changeDevConfigNoPrivileges', 'MDC', 'You cannot solve the conflicts in conflicting mappings on device type because you do not have the privileges. Contact the administrator.');
                                    nextCmp.showChangeDeviceConfigConfirmation(title, text, null, additionalText, 'error');
                                }
                            } else {
                                wizard.down('#confirmButton').enable();
                                var message = me.buildConfirmMessage();
                                additionalText = Uni.I18n.translate('searchItems.bulk.changeDevConfigWarningMessage', 'MDC', 'Changing the device configuration could lead to critical data loss (security settings, connection methods, communication tasks,...).');
                                nextCmp.showChangeDeviceConfigConfirmation(message.title, message.body, null, additionalText)
                            }
                        });
                    }
                    break;
                case 'statusPage':
                    if (currentCmp.name != 'statusPage') {
                        if (me.operation != 'changeconfig') {
                            progressBar = Ext.create('Ext.ProgressBar', {width: '50%'});
                            Ext.suspendLayouts();
                            nextCmp.removeAll(true);
                            nextCmp.add(
                                progressBar.wait({
                                    interval: 50,
                                    increment: 20,
                                    text: (me.operation === 'add' || me.operation === 'addToZone'  ? Uni.I18n.translate('general.adding', 'MDC', 'Adding...') : Uni.I18n.translate('general.removing', 'MDC', 'Removing...'))
                                })
                            );
                            Ext.resumeLayouts();
                            this.getNavigationMenu().jumpBack = false;
                        }
                    }
                    break;
            }
            errorPanel && errorPanel.hide();
            errorContainer && errorContainer.hide();
            layout.setActiveItem(nextCmp);
            this.updateButtonsState(nextCmp);
            this.updateTitles();
            me.getStatusPage().setLoading(false);
            return true;
        } else {
            errorPanel && errorPanel.show();
            if (errorContainer && !errorContainer.isVisible()) {

                if (me.operation == 'addToZone' || me.operation == 'removeFromZone' || me.operation == 'createmanualissue')
                    errorContainer.hide();
                else {
                    errorContainer.show();
                    errorContainer.update('<span style="color: #eb5642">' + Uni.I18n.translate('searchItems.bulk.selectAtLeastOneDevice', 'MDC', 'Select at least 1 device') + '</span>')
                }
            }
            me.getStatusPage().setLoading(false);
            return false;
        }
    },

    updateTitles: function () {
        var me = this,
            title;
        if (me.operation) {
            var items = Ext.ComponentQuery.query('#searchitemsbulkactiontitle');
            switch (me.operation){
                case 'add' : {
                    title = Uni.I18n.translate('searchItems.bulk.addActionTitle', 'MDC', 'Add shared communication schedules')
                }
                    break;
                case 'remove' : {
                    title = Uni.I18n.translate('searchItems.bulk.removeActionTitle', 'MDC', 'Remove shared communication schedules')
                }
                    break;
                case 'changeconfig' : {
                    title = Uni.I18n.translate('searchItems.bulk.changeConfigActionTitle', 'MDC', 'Change device configuration')
                }
                    break;
                case 'startprocess' : {
                    title = Uni.I18n.translate('searchItems.bulk.startProcess', 'MDC', 'Start process')
                }
                    break;
                case 'addToZone' : {
                    title = Uni.I18n.translate('searchItems.bulk.addToZone', 'MDC', 'Add to zone')
                }
                    break;
                case 'removeFromZone' : {
                    title = Uni.I18n.translate('searchItems.bulk.removeFromZone', 'MDC', 'Remove from zone')
                }
                    break;
                case 'createmanualissue' : {
                    title = Uni.I18n.translate('workspace.newManuallyIssue', 'ISU', 'Create issue')
                }
                    break;
            }
            (items.length > 0) && Ext.each(items, function (item) {
                item.setTitle(title);
            })
        }
    },

    buildConfirmMessage: function () {
        var me = this,
            message,
            pattern,
            titleText,
            bodyText,
            scheduleList = '';

        if (me.allDevices) {
            switch (me.operation) {
                case 'add':
                    if (me.schedules.length === 1) {
                        titleText = Uni.I18n.translate('searchItems.bulk.addOneComScheduleToAllDevices.confirmMsg', 'MDC',
                            "Add shared communication schedule '{0}' to all devices?", me.schedules[0].get('name'), false);
                    } else {
                        Ext.each(me.schedules, function (item, index) {
                            scheduleList += (index ? ', ' : '') + "'" + item.get('name') + "'";
                        });
                        titleText = Uni.I18n.translate('searchItems.bulk.addComSchedulesToAllDevices.confirmMsg', 'MDC',
                            "Add shared communication schedules {0} to all devices?", scheduleList, false);
                    }
                    break;
                case 'remove':
                    if (me.schedules.length === 1) {
                        titleText = Uni.I18n.translate('searchItems.bulk.removeOneComScheduleFromAllDevices.confirmMsg', 'MDC',
                            "Remove shared communication schedule '{0}' from all devices?", me.schedules[0].get('name'), false);
                    } else {
                        Ext.each(me.schedules, function (item, index) {
                            scheduleList += (index ? ', ' : '') + "'" + item.get('name') + "'";
                        });
                        titleText = Uni.I18n.translate('searchItems.bulk.removeComSchedulesFromAllDevices.confirmMsg', 'MDC',
                            "Remove shared communication schedules {0} from all devices?", scheduleList, false);
                    }
                    break;
                case 'changeconfig':
                    titleText = Uni.I18n.translate('searchItems.bulk.changeDevConfigTitle', 'MDC', 'Change device configuration of all devices');
                    break;
                case 'startprocess':
                    pattern = Uni.I18n.translate('searchItems.bulk.startProcess.confirmMsg', 'MDC', 'Start {0} process for an appropriate subset of selected devices?');
                    titleText = Ext.String.format(pattern, me.processRecord.name, false);
                    break;
                case 'addToZone':
                    pattern = Uni.I18n.translate('searchItems.bulk.addToZone.confirmMsg', 'MDC', 'Link all devices to zone "{0}" ?');
                    titleText = Ext.String.format(pattern, me.zoneName, false);
                    break;
                case 'removeFromZone':
                    pattern = Uni.I18n.translate('searchItems.bulk.removeFromZone.confirmMsg', 'MDC', 'Unlink all devices from zone "{0}" (if linked)?');
                    titleText = Ext.String.format(pattern, me.zoneName, false);
                    break;
                case 'createmanualissue':
                    titleText = Uni.I18n.translate('searchItems.bulk.createmanualissue.confirmMsg', 'ISU', 'Create issues for all devices?');
                    break;
            }
        } else {
            switch (me.operation) {
                case 'add':
                    if (me.schedules.length === 1) {
                        if (me.devices.length <= 1) {
                            pattern = Uni.I18n.translate('searchItems.bulk.addOneComScheduleToDevices.confirmMsg0', 'MDC', "Add shared communication schedule '{1}' to {0} device?")
                        } else {
                            pattern = Uni.I18n.translate('searchItems.bulk.addOneComScheduleToDevices.confirmMsgn', 'MDC', "Add shared communication schedule '{1}' to {0} devices?")
                        }
                        titleText = Ext.String.format(pattern, me.devices.length, me.schedules[0].get('name'), false);
                    } else {
                        Ext.each(me.schedules, function (item, index) {
                            scheduleList += (index ? ', ' : '') + "'" + item.get('name') + "'";
                        });
                        if (me.devices.length <= 1) {
                            pattern = Uni.I18n.translate('searchItems.bulk.addComSchedulesToDevices.confirmMsg0', 'MDC', "Add shared communication schedules {1} to {0} device?")
                        } else {
                            pattern = Uni.I18n.translate('searchItems.bulk.addComSchedulesToDevices.confirmMsgn', 'MDC', "Add shared communication schedules {1} to {0} devices?")
                        }
                        titleText = Ext.String.format(pattern, me.devices.length, scheduleList, false);
                    }
                    break;
                case 'remove':
                    if (me.schedules.length === 1) {
                        if (me.devices.length <= 1) {
                            pattern = Uni.I18n.translate('searchItems.bulk.removeOneComScheduleFromDevices.confirmMsg0', 'MDC', "Remove shared communication schedule '{1}' to {0} device?")
                        } else {
                            pattern = Uni.I18n.translate('searchItems.bulk.removeOneComScheduleFromDevices.confirmMsgn', 'MDC', "Remove shared communication schedule '{1}' to {0} devices?")
                        }
                        titleText = Ext.String.format(pattern, me.devices.length, me.schedules[0].get('name'), false);
                    } else {
                        Ext.each(me.schedules, function (item, index) {
                            scheduleList += (index ? ', ' : '') + "'" + item.get('name') + "'";
                        });
                        if (me.devices.length <= 1) {
                            pattern = Uni.I18n.translate('searchItems.bulk.removeComSchedulesFromDevices.confirmMsg0', 'MDC', "Remove shared communication schedules {1} to {0} device?")
                        } else {
                            pattern = Uni.I18n.translate('searchItems.bulk.removeComSchedulesFromDevices.confirmMsgn', 'MDC', "Remove shared communication schedules {1} to {0} devices?")
                        }
                        titleText = Ext.String.format(pattern, me.devices.length, scheduleList, false);
                    }
                    break;
                case 'changeconfig':
                    pattern = Uni.I18n.translatePlural('searchItems.bulk.changeDevConfigTitle', me.devices.length, 'MDC',
                        "Change device configuration of {0} devices?",
                        "Change device configuration of {0} device?",
                        "Change device configuration of {0} devices?"
                    );
                    titleText = Ext.String.format(pattern);
                    break;
                case 'startprocess':
                    pattern = Uni.I18n.translate('searchItems.bulk.startProcess.confirmMsg', 'MDC', 'Start {0} process for an appropriate subset of selected devices?');
                    titleText = Ext.String.format(pattern, me.processRecord.name, false);
                    break;

                case 'addToZone':
                    pattern = Uni.I18n.translatePlural('searchItems.bulk.addDevicesToZone.confirmMsg', me.devices.length, 'MDC',
                        'Link {0}  of {0} device',
                        'Link {0}  of {0} device',
                        'Link {0}  of {0} devices');

                    titleText = Uni.I18n.translate('searchItems.bulk.addDevicesToZone.confirmMsg1', 'MDC', "{0} to zone \"{1}\"?", [pattern, me.zoneName]);


                    break;
                case 'removeFromZone':
                    pattern = Uni.I18n.translatePlural('searchItems.bulk.removeDeviceFromZone.confirmMsg', me.devices.length, 'MDC',
                        'Unlink {0}  of {0} device',
                        'Unlink {0}  of {0} device',
                        'Unlink {0}  of {0} devices');

                    titleText = Uni.I18n.translate('searchItems.bulk.removeZoneToDevices.confirmMsg1', 'MDC', "{0} from zone '{1}'?", [pattern, me.zoneName]);
                    break;
                case 'createmanualissue':
                    pattern = Uni.I18n.translate('searchItems.bulk.createmanualissue.confirmMsg', 'ISU', 'Create issues for {0} devices?');
                    titleText = Ext.String.format(pattern, me.devices.length, false);
                    break;
            }
        }

        switch (me.operation) {
            case 'add':
                bodyText = Uni.I18n.translate('searchItems.bulk.addMsg', 'MDC', 'The selected devices will execute the chosen shared communication schedules');
                break;
            case 'remove':
                bodyText = Uni.I18n.translate('searchItems.bulk.removeMsg', 'MDC', 'The selected devices will not execute the chosen shared communication schedules');
                break;
            case 'changeconfig':
                bodyText = Uni.I18n.translate('searchItems.bulk.changeMsg', 'MDC', 'The devices will take over all data sources, communication features and rule sets from new device configuration.');
                break;
            case 'startprocess':
                if (me.allDevices) {
                    pattern = Uni.I18n.translate('searchItems.bulk.startProcess.infoMsg.all', 'MDC', "The process will start for {0} out of all selected devices.")
                    bodyText = Ext.String.format(pattern, me.validatedForProcessDevices.length, false);
                } else {
                    if (me.devices.length === 1) {
                        pattern = Uni.I18n.translate('searchItems.bulk.startProcess.infoMsg.device', 'MDC', "The process will start for {0} out of {1} selected device.")
                    } else {
                        pattern = Uni.I18n.translate('searchItems.bulk.startProcess.infoMsg.devices', 'MDC', "The process will start for {0} out of {1} selected devices.")
                    }
                    bodyText = Ext.String.format(pattern, me.validatedForProcessDevices.length, me.devices.length, false);
                }
                break;
            case 'addToZone':
                if (me.allDevices) {
                    bodyText = Uni.I18n.translate('searchItems.bulk.addToZoneMsg', 'MDC', 'The selected devices will be linked to the chosen zone');
                } else {
                    bodyText = Uni.I18n.translatePlural('searchItems.bulk.addDeviceToZoneMsg', me.devices.length, 'MDC',
                        'The selected device will be linked to the chosen zone',
                        'The selected device will be linked to the chosen zone',
                        'The selected devices will be linked to the chosen zone');
                }
                break;
            case 'removeFromZone':
                if (me.allDevices) {
                    bodyText = Uni.I18n.translate('searchItems.bulk.removeFromZoneMsg', 'MDC', 'The selected devices will be unlinked from the chosen zone');
                } else {
                    bodyText = Uni.I18n.translatePlural('searchItems.bulk.removeDeviceFromZoneMsg', me.devices.length, 'MDC',
                        'The selected device will be unlinked from the chosen zone',
                        'The selected device will be unlinked from the chosen zone',
                        'The selected devices will be unlinked from the chosen zone');
                }
                break;
        }

        message = {
            title: titleText,
            body: bodyText
        };
        return message;
    }
    ,

    updateButtonsState: function (activePage) {
        var me = this,
            wizard = me.getSearchItemsWizard(),
            backBtn = wizard.down('#backButton'),
            nextBtn = wizard.down('#nextButton'),
            confirmBtn = wizard.down('#confirmButton'),
            finishBtn = wizard.down('#finishButton'),
            falureFinishBtn = wizard.down('#failureFinishButton'),
            cancelBtn = wizard.down('#wizardCancelButton');
        activePage.name == 'selectDevices' ? backBtn.disable() : backBtn.enable();
        switch (activePage.name) {
            case 'selectDevices' :
                backBtn.show();
                nextBtn.show();
                nextBtn.setDisabled(false);
                confirmBtn.hide();
                finishBtn.hide();
                falureFinishBtn.hide();
                cancelBtn.show();
                break;
            case 'selectOperation' :
                backBtn.show();
                nextBtn.show();
                nextBtn.setDisabled(Ext.isEmpty(me.operation));
                confirmBtn.hide();
                finishBtn.hide();
                falureFinishBtn.hide();
                cancelBtn.show();
                break;
            case 'selectActionItems' :
                backBtn.show();
                nextBtn.show();
                nextBtn.enable();
                confirmBtn.hide();
                finishBtn.hide();
                falureFinishBtn.hide();
                cancelBtn.show();
                break;
            case 'confirmPage' :
                backBtn.show();
                nextBtn.hide();
                confirmBtn.show();
                finishBtn.hide();
                falureFinishBtn.hide();
                cancelBtn.show();
                break;
            case 'statusPage' :
                backBtn.hide();
                nextBtn.hide();
                confirmBtn.hide();
                finishBtn.show();
                cancelBtn.hide();
                break;
            case 'statusPageViewDevices' :
                backBtn.show();
                nextBtn.hide();
                confirmBtn.hide();
                finishBtn.hide();
                falureFinishBtn.hide();
                cancelBtn.hide();
                break;
        }
    }
    ,

    createCommunicationSchedule: function () {
        var newTab = window.open('#/administration/communicationschedules/add', '_blank');

        newTab && newTab.blur();
        window.focus();
    }
    ,

    showViewDevices: function (button) {
        var me = this, layout = me.getSearchItemsWizard().getLayout(),
            viewFailureDevices = me.getSearchItemsWizard().down('#searchitems-bulk-step5-viewdevices'),
            viewDevicesData = button.viewDevicesData;

        me.getStatusPage().setLoading(true);

        viewFailureDevices.down('#failuremessage').update(viewDevicesData.message);
        viewFailureDevices.down('#failuredevicesgrid').getStore().loadData(viewDevicesData.devices);

        me.changeContent(layout.getNext(), layout.getActiveItem());
    },

    processComboChange: function(combo){
        this.processRecord = combo.lastSelection[0].data
    }
});
