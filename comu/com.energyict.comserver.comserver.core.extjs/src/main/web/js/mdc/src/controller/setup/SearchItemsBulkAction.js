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
        'Uni.view.window.Wizard'
    ],
    stores: [
        'Mdc.store.CommunicationSchedulesWithoutPaging',
        'Mdc.store.DeviceConfigurations'
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
            ref: 'stepSelectionError',
            selector: '#stepSelectionError'
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
            }
        });
    },

    showBulkAction: function () {
        var me = this,
            searchResults = Ext.getStore('Uni.store.search.Results'),
            widget;

        if (!searchResults.getCount()) {
            this.goBack();
        } else {
            me.devices = null;
            me.allDevices = false;
            me.schedules = null;
            me.operation = null;
            me.configData = null;
            me.shedulesUnchecked = false;

            var store = Ext.create('Ext.data.Store', {
                buffered: true,
                pageSize: 200,
                remoteFilter: true,
                model: searchResults.model,
                filters: searchResults.filters.getRange(),
                proxy: searchResults.getProxy()
            });

            widget = Ext.widget('searchitems-bulk-browse', {
                deviceStore: store
            });
            me.getApplication().fireEvent('changecontentevent', widget);
            widget.setLoading();
            store.load({
                callback: function () {
                    widget.setLoading(false);
                }
            });
            me.getStore('Mdc.store.CommunicationSchedulesWithoutPaging').load();
        }
    },

    updateScheduleSelection: function (selModel, selected) {
        var count = selected.length;
        if (count === 0) {
            this.getCommunicationSchedulePreview().hide();
        }
        this.shedulesUnchecked = count === 0;
    },

    previewCommunicationSchedule: function (grid, record) {
        var preview = this.getCommunicationSchedulePreview();

        if (!this.shedulesUnchecked) {
            preview.down('form').loadRecord(record);
            preview.setTitle(record.get('name'));
            preview.show();
        } else {
            this.shedulesUnchecked = false;
        }
    },

    backClick: function () {
        var layout = this.getSearchItemsWizard().getLayout(),
            currentCmp = layout.getActiveItem();

        this.changeContent(layout.getPrev(), currentCmp);
        (currentCmp.name !== 'statusPageViewDevices') && this.getNavigationMenu().movePrevStep();
    },

    nextClick: function () {
        var layout = this.getSearchItemsWizard().getLayout();
        this.changeContent(layout.getNext(), layout.getActiveItem()) && this.getNavigationMenu().moveNextStep();
    },

    confirmClick: function () {
        var me = this,
            wizard = me.getSearchItemsWizard(),
            finishBtn = wizard.down('#finishButton'),
            statusPage = me.getStatusPage(),
            scheduleIds = [],
            deviceMRID = [],
            url = '/api/ddr/devices/schedules',
            request = {},
            jsonData,
            params;

        finishBtn.disable();

        if (me.operation != 'changeconfig') {
            Ext.each(me.schedules, function (item) {
                scheduleIds.push(item.getId());
            });
            Ext.each(me.devices, function (item) {
                deviceMRID.push(item.get('mRID'));
            });
            request.action = me.operation;
            request.scheduleIds = scheduleIds;
            if (me.allDevices) {
                request.filter =  me.getDevicesGrid().getStore().filters.getRange();
            } else {
                request.deviceMRIDs = deviceMRID;
            }
            jsonData = Ext.encode(request);
            Ext.Ajax.request({
                url: url,
                method: 'PUT',
                //       params: params,
                jsonData: jsonData,
                timeout: 180000,
                success: function (response) {
                    var resp = Ext.decode(response.responseText, true);
                    statusPage.removeAll();
                    Ext.each(resp ? resp.actions : [], function (item) {
                        (item.successCount > 0) &&
                        me.showStatusMsg(me.buildSuccessMessage(item));
                        (item.failCount > 0) &&
                        me.showStatusMsg(me.buildFailMessage(item));
                    });
                    finishBtn.enable();
                }
            });
        } else {
            wizard.setLoading(true);
            var callback = function (success) {
                statusPage.setLoading(false);
                if (success) {
                    wizard.setLoading(false);
                    statusPage.showChangeDeviceConfigSuccess(Uni.I18n.translate('searchItems.bulk.devicesAddedToQueueTitle', 'MDC', 'This task has been put on the queue successfully'),
                        Ext.String.format(Uni.I18n.translatePlural('searchItems.bulk.devConfigQueuedTitle', me.devices.length, 'MDC', "The {0} devices are queued to change their configuration", "The {0} device is queued to change its configuration", "The {0} devices are queued to change their configuration"))
                    );
                    finishBtn.enable();
                }
            };
            me.changeDeviceConfig(me.configData.fromconfig, me.configData.toconfig, callback)
        }
        me.nextClick();
    },

    goBack: function () {
        // todo: restore search state
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('search').forward();
    },

    showStatusMsg: function (msg) {
        var me = this;
        me.getStatusPage().add(msg);
    },

    buildSuccessMessage: function (successful) {
        var me = this,
            count = parseInt(successful.successCount),
            messageHeader = '',
            message = {
                xtype: 'panel'
            };

        switch (me.operation) {
            case 'add':
                messageHeader = Uni.I18n.translatePlural('searchItems.bulk.successfullyAddedCommunicationSchedule', count, 'MDC',
                    "Successfully added communication schedule '{1}' to {0} devices",
                    "Successfully added communication schedule '{1}' to {0} device",
                    "Successfully added communication schedule '{1}' to {0} devices"
                );
                break;
            case 'remove':
                messageHeader = Uni.I18n.translatePlural('searchItems.bulk.successfullyRemovedCommunicationSchedule', count, 'MDC',
                    "Successfully removed communication schedule '{1}' from {0} devices",
                    "Successfully removed communication schedule '{1}' from {0} device",
                    "Successfully removed communication schedule '{1}' from {0} devices"
                );
                break;
        }

        messageHeader && (messageHeader = Ext.String.format(messageHeader, count, successful.actionTitle));

        message.html = '<h3>' + messageHeader + '</h3>';

        return message;
    },

    buildFailMessage: function (failure) {
        var me = this,
            count = parseInt(failure.failCount),
            messageHeader = '',
            messageBody = [],
            grouping = [],
            message = {
                xtype: 'panel'
            };

        switch (me.operation) {
            case 'add':
                messageHeader = Uni.I18n.translatePlural('searchItems.bulk.failedToAddCommunicationSchedule', count, 'MDC',
                    "Failed to add communication schedule '{1}' to {0} devices",
                    "Failed to add communication schedule '{1}' to {0} device",
                    "Failed to add communication schedule '{1}' to {0} devices"
                );
                break;
            case 'remove':
                messageHeader = Uni.I18n.translatePlural('searchItems.bulk.failedToRemoveCommunicationSchedule', count, 'MDC',
                    "Failed to remove communication schedule '{1}' from {0} devices",
                    "Failed to remove communication schedule '{1}' from {0} device",
                    "Failed to remove communication schedule '{1}' from {0} devices"
                );
                break;
        }

        messageHeader && (messageHeader = Ext.String.format(messageHeader, count, failure.actionTitle));

        Ext.Array.each(failure.fails, function (item) {
            var sameMessageGroup = Ext.Array.findBy(grouping, function (search) {
                return search.messageGroup === item.messageGroup;
            });

            if (sameMessageGroup) {
                sameMessageGroup.devices.push(item.device);
            } else {
                grouping.push({
                    messageGroup: item.messageGroup,
                    message: item.message,
                    devices: [item.device]
                });
            }
        });

        messageBody.push({
            html: '<h3>' + Ext.String.htmlEncode(messageHeader) + '</h3><br>'
        });

        Ext.Array.each(grouping, function (group) {
            messageBody.push({
                html: Ext.String.htmlEncode(group.message),
                bbar: [
                    {
                        text: Uni.I18n.translate('searchItems.bulk.viewDevices', 'MDC', 'View devices'),
                        ui: 'link',
                        action: 'viewDevices',
                        itemId: 'viewDevicesButton',
                        viewDevicesData: group
                    }
                ]
            });
        });

        message.items = messageBody;

        return message;
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
            if (devConfigId != device.get('deviceConfigurationId')){
                result = false;
            }
        });
        return result;
    },

    checkConflictMappings: function (fromConfig, toConfig, callback) {
        var me = this,
        //TODO: change URL conflicts
            url = '/api/ddc/devices/conflictingmappings',
            jsonData = {
                'fromconfig': fromConfig,
                'toconfig': toConfig
            };
        Ext.Ajax.request({
            url: url,
            method: 'GET',
            jsonData: jsonData,
            timeout: 180000,
            callback: function (operation, success, response) {
                var conflictMappings = false,
                    resp = Ext.JSON.decode(response.responseText, true);
                if (success && resp) {
                    conflictMappings = resp['conflictingMappings'];
                }
                callback(conflictMappings)
            }
        });
    },


    changeDeviceConfig: function (fromConfig, toConfig, callback) {
        var me = this,
        //TODO: change URL for update CDC
            url = '/api/ddc/devices',
            jsonData = {
                'action': 'ChangeDeviceConfiguration',
                'devices': [],
                'newDeviceConfiguration': toConfig
            };
        me.devices.each(function (item) {
            jsonData['devices'].push(item.get('mRID'))
        });
        Ext.Ajax.request({
            url: url,
            method: 'PUT',
            jsonData: jsonData,
            timeout: 180000,
            callback: function (operation, success, response) {
                callback(success)
            }
        });
    },
    //todo split this function
    changeContent: function (nextCmp, currentCmp) {
        var me = this,
            additionalText,
            router = me.getController('Uni.controller.history.Router'),
            wizard = me.getSearchItemsWizard(),
            layout = wizard.getLayout(),
            errorContainer = currentCmp.down('#stepSelectionError'),
            errorPanel = null,
            progressBar;

        switch (currentCmp.name) {
            case 'selectDevices':
                me.allDevices = me.getDevicesGrid().isAllSelected();

                if (!me.allDevices) {
                    me.devices = me.getDevicesGrid().getSelectionModel().getSelection();
                }

                if (!me.allDevices && me.isDeviceConfigEqual(me.devices)) {
                    nextCmp.down('#searchitemschangeconfig').enable();
                } else {
                    nextCmp.down('#searchitemschangeconfig').disable();
                    if (nextCmp.down('#searchitemschangeconfig').getValue()) {
                        nextCmp.down('#searchitemsactionselect').setValue({operation: 'add'});
                    }
                }

                errorPanel = currentCmp.down('#step1-errors');
                me.validation = me.allDevices || me.devices.length;
                break;
            case 'selectOperation':
                me.operation = currentCmp.down('#searchitemsactionselect').getValue().operation;
                if (nextCmp.name == 'selectActionItems') {
                    if (me.operation == 'changeconfig') {
                        var changeDeviceConfigForm = nextCmp.down('#change-device-configuration');
                        nextCmp.down('#select-schedules-panel').hide();
                        changeDeviceConfigForm.show();
                        changeDeviceConfigForm.getForm().clearInvalid();
                        var configStore = me.getStore('Mdc.store.DeviceConfigurations'),
                            device = me.devices[0],
                            deviceConfigName = device.get('deviceConfigurationName'),
                            deviceConfigId = device.get('deviceConfigurationId'),
                            currentConfigField = nextCmp.down('#current-device-config-selection');

                        me.deviceType = device.get('deviceTypeId');
                        currentConfigField.setValue(deviceConfigName);
                        configStore.getProxy().setUrl({deviceType: me.deviceType});
                        wizard.setLoading(true);
                        configStore.load(function (operation, success) {
                            wizard.setLoading(false);
                            configStore.filter({
                                filterFn: function (record) {
                                    return record.get('id') !== deviceConfigId
                                }
                            });
                            if (success && (configStore.getCount() < 1)) {
                                wizard.down('#nextButton').disable();
                                nextCmp.down('#new-device-config-selection').hide();
                                nextCmp.down('#no-device-configuration').show();
                            } else {
                                wizard.down('#nextButton').enable();
                                nextCmp.down('#new-device-config-selection').show();
                                nextCmp.down('#no-device-configuration').hide();
                            }

                        });
                    } else {
                        nextCmp.down('#select-schedules-panel').show();
                        nextCmp.down('#change-device-configuration').hide();
                    }
                }
                break;
            case
            'selectActionItems':
                if (me.operation != 'changeconfig') {
                    me.schedules = me.getSchedulesGrid().getSelectionModel().getSelection();
                    errorPanel = currentCmp.down('#step3-errors');
                    me.validation = me.schedules.length;
                } else {
                    var form = currentCmp.down('#change-device-configuration');
                    me.validation = currentCmp.down('#change-device-configuration').isValid();
                    me.validation && (me.configData = form.getValues());
                    me.configNames = {
                        fromconfig: form.down('#current-device-config-selection').getRawValue(),
                        toconfig: form.down('#new-device-config-selection').getRawValue()};
                    errorPanel = currentCmp.down('#step3-errors');
                    errorContainer = null;
                }

                break;
        }

        (currentCmp.navigationIndex > nextCmp.navigationIndex) && (me.validation = true);

        if (me.validation) {
            switch (nextCmp.name) {
                case 'confirmPage':
                    if (me.operation != 'changeconfig') {
                        nextCmp.showMessage(me.buildConfirmMessage());
                        wizard.down('#confirmButton').enable()
                    } else {
                        wizard.setLoading(true);
                        nextCmp.removeAll();
                        me.checkConflictMappings(me.configData['fromconfig'], me.configData['toconfig'], function (unsolvedConflicts) {
                            wizard.setLoading(false);
                            if (unsolvedConflicts) {
                                me.getNavigationMenu().markInvalid();
                                var title = Uni.I18n.translatePlural('searchItems.bulk.devConfigUnsolvedConflictsTitle', me.devices.length, 'MDC', "Unable to change device configuration of {0} devices", "Unable to change device configuration of {0} device", "Unable to change device configuration of {0} devices"),
                                    text = Uni.I18n.translate('searchItems.bulk.devConfigUnsolvedConflictsMsg', 'MDC', 'The configuration of devices with current configuration \'{fromconfig}\' cannot be changed to \'{toconfig}\' due to unsolved conflicts.');
                                text = text.replace('{fromconfig}', me.configNames.fromconfig).replace('{toconfig}', me.configNames.toconfig);
                                if (Mdc.privileges.DeviceType.canAdministrate()) {
                                    var solveLink = '<a href="' +
                                        router.getRoute('administration/devicetypes/view/conflictmappings').buildUrl({deviceTypeId: me.deviceType}) + '">' +
                                        Ext.String.htmlEncode(Uni.I18n.translate('searchItems.bulk.solveTheConflicts', 'MDC', 'Solve the conflicts')) +
                                        '</a>';
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
                                additionalText = Uni.I18n.translate('searchItems.bulk.changeDevConfigWarningMessage', 'MDC', 'The device configuration change can possibly lead to critical data loss (security settings, connection attributes...).');
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
                                    text: (me.operation === 'add' ? Uni.I18n.translate('general.adding', 'MDC', 'Adding...') : Uni.I18n.translate('general.removing', 'MDC', 'Removing...'))
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
            errorContainer && errorContainer.show();
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
                            "Add shared communication schedule '{0}' to all devices?", [me.schedules[0].get('name')]);
                    } else {
                        Ext.each(me.schedules, function (item, index) {
                            scheduleList += (index ? ', ' : '') + '\'' + item.get('name') + '\'';
                        });
                        titleText = Uni.I18n.translate('searchItems.bulk.addComSchedulesToAllDevices.confirmMsg', 'MDC',
                            "Add shared communication schedules {0} to all devices?", [scheduleList]);
                    }
                    break;
                case 'remove':
                    if (me.schedules.length === 1) {
                        titleText = Uni.I18n.translate('searchItems.bulk.removeOneComScheduleFromAllDevices.confirmMsg', 'MDC',
                            "Remove shared communication schedule '{0}' from all devices?", [me.schedules[0].get('name')]);
                    } else {
                        Ext.each(me.schedules, function (item, index) {
                            scheduleList += (index ? ', ' : '') + '\'' + item.get('name') + '\'';
                        });
                        titleText = Uni.I18n.translate('searchItems.bulk.removeComSchedulesFromAllDevices.confirmMsg', 'MDC',
                            "Remove shared communication schedules {0} from all devices?", [scheduleList]);
                    }
                    break;
                 case 'changeconfig':
                        titleText = Uni.I18n.translate('searchItems.bulk.changeDevConfigTitle', 'MDC', 'Change device configuration of all devices');  
                    break;
                
            }
        } else {
            switch (me.operation) {
                case 'add':
                    if (me.schedules.length === 1) {
                        pattern = Uni.I18n.translatePlural('searchItems.bulk.addOneComScheduleToDevices.confirmMsg', me.devices.length, 'MDC',
                            "Add shared communication schedule '{1}' to {0} devices?",
                            "Add shared communication schedule '{1}' to {0} device?",
                            "Add shared communication schedule '{1}' to {0} devices?"
                        );
                        titleText = Ext.String.format(pattern, 1/*notused*/, Ext.String.htmlEncode(me.schedules[0].get('name')));
                    } else {
                        Ext.each(me.schedules, function (item, index) {
                            scheduleList += (index ? ', ' : '') + '\'' + item.get('name') + '\'';
                        });
                        pattern = Uni.I18n.translatePlural('searchItems.bulk.addComSchedulesToDevices.confirmMsg', me.devices.length, 'MDC',
                            "Add shared communication schedules {1} to {0} devices?",
                            "Add shared communication schedules {1} to {0} device?",
                            "Add shared communication schedules {1} to {0} devices?"
                        );
                        titleText = Ext.String.format(pattern, 1/*notused*/, Ext.String.htmlEncode(scheduleList));
                    }
                    break;
                case 'remove':
                    if (me.schedules.length === 1) {
                        pattern = Uni.I18n.translatePlural('searchItems.bulk.removeOneComScheduleFromDevices.confirmMsg', me.devices.length, 'MDC',
                            "Remove shared communication schedule '{1}' from {0} devices?",
                            "Remove shared communication schedule '{1}' from {0} device?",
                            "Remove shared communication schedule '{1}' from {0} devices?"
                        );
                        titleText = Ext.String.format(pattern, 1/*notused*/, Ext.String.htmlEncode(me.schedules[0].get('name')));
                    } else {
                        Ext.each(me.schedules, function (item, index) {
                            scheduleList += (index ? ', ' : '') + '\'' + item.get('name') + '\'';
                        });
                        pattern = Uni.I18n.translatePlural('searchItems.bulk.removeComSchedulesFromDevices.confirmMsg', me.devices.length, 'MDC',
                            "Remove shared communication schedules {1} from {0} devices?",
                            "Remove shared communication schedules {1} from {0} device?",
                            "Remove shared communication schedules {1} from {0} devices?"
                        );
                        titleText = Ext.String.format(pattern, 1/*notused*/, Ext.String.htmlEncode(scheduleList));
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
                bodyText =  Uni.I18n.translate('searchItems.bulk.changeMsg', 'MDC', 'The devices will take over all data sources, communication features and rule sets from new device configuration.');
                break;
        }

        message = {
            title: titleText,
            body: bodyText
        };
        return message;
    },

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
                nextBtn.setDisabled(false);
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
    },

    createCommunicationSchedule: function () {
        var newTab = window.open('#/administration/communicationschedules/add', '_blank');

        newTab && newTab.blur();
        window.focus();
    },

    showViewDevices: function (button) {
        var me = this, layout = me.getSearchItemsWizard().getLayout(),
            viewFailureDevices = me.getSearchItemsWizard().down('#searchitems-bulk-step5-viewdevices'),
            viewDevicesData = button.viewDevicesData;

        me.getStatusPage().setLoading(true);

        viewFailureDevices.down('#failuremessage').update(viewDevicesData.message);
        viewFailureDevices.down('#failuredevicesgrid').getStore().loadData(viewDevicesData.devices);

        me.changeContent(layout.getNext(), layout.getActiveItem());
    }
})
;