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
        'Mdc.store.Devices',
        'Mdc.store.CommunicationSchedules'
    ],
    devices: null,
    schedules: null,
    operation: null,
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
            ref: 'deviceGrid',
            selector: '#searchitems-bulk-step1'
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
            ref: 'deviseSelectionRange',
            selector: '#deviceSelectionRange'
        },
        {
            ref: 'shceduleSelectionRange',
            selector: '#shceduleSelectionRange'
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
            ref: 'nextButton',
            selector: 'searchitems-wizard #confirmButton'
        },
        {
            ref: 'nextButton',
            selector: 'searchitems-wizard #finishButton'
        },
        {
            ref: 'nextButton',
            selector: 'searchitems-wizard #cancelButton'
        },
        {
            ref: 'communicationSchedulePreviewForm',
            selector: '#communicationschedulepreviewporm'
        },
        {
            ref: 'navigationMenu',
            selector: '#searchitemsBulkNavigation'
        },
        {
            ref: 'statusPage',
            selector: '#searchitems-bulk-step5'
        }
    ],
    init: function () {
        this.control({
            '#searchitems-bulk-step1': {
                selectionchange: this.updateDeviceSelection
            },
            '#deviceSelectionRange': {
                change: this.devicesSelectionRangeChange
            },
            'searchitems-bulk-step3 #schedulesgrid': {
                selectionchange: this.updateScheduleSelection,
                itemclick: this.previewCommunicationSchedule
            },
            '#shceduleSelectionRange': {
                change: this.schedulesSelectionRangeChange
            },
            '#searchitems-bulk-step1 #uncheck-all': {
                click: this.uncheckAllDevices
            },
            '#searchitems-bulk-step3 #uncheck-all': {
                click: this.uncheckAllSchedules
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
                click: this.finishClick
            },
            'searchitems-wizard #cancelButton': {
                click: this.cancelClick
            },
            '#searchitemsBulkNavigation': {
                movetostep: this.navigateToStep
            },
            'searchitems-wizard #createCommunicationSchedule': {
                click: this.createCommunicationSchedule
            }
        });
    },

    showBulkAction: function () {
        var me = this,
            widget = Ext.widget('searchitems-bulk-browse');
        me.getApplication().fireEvent('changecontentevent', widget);
        me.getDeviceGrid().getView().disable();
        me.getStore('Mdc.store.CommunicationSchedules').load({
            callback: function () {
                me.getSchedulesGrid().getSelectionModel().selectAll();
                me.getSearchItemsWizard().down('#allSchedules').setValue(true);
            }
        });
        me.getDeviceGrid().getSelectionModel().selectAll();
    },

    uncheckAllSchedules: function () {
        this.getSchedulesGrid().getSelectionModel().deselectAll()
    },

    updateScheduleSelection: function (selModel, selected) {
        var me = this,
            label = me.getSelectedScheduleQty(),
            count = selected.length,
            wizard = me.getSearchItemsWizard(),
            nextBtn = wizard.down('#nextButton'),
            scheduleSelectionRange = wizard.down('#shceduleSelectionRange');

        if ((selModel.getStore().getCount() > count) && (scheduleSelectionRange.getValue().scheduleRange === 'ALL')) {
            scheduleSelectionRange.down('#selectedSchedules').setValue(true);
        }

        if (count > 0) {
            nextBtn.enable();
            label.update('<span style="color: grey;">' +
                count + Uni.I18n.translate('searchItems.bulk.scheduleSelected', 'MDC', ' schedule selected') +
                '</span>')
        } else {
            nextBtn.disable();
            label.update('<span style="color: grey;">' +
                Uni.I18n.translate('searchItems.bulk.noScheduleSelected', 'MDC', 'No schedule selected') +
                '</span>');
        }
        count == 1 && this.previewCommunicationSchedule(null, selected[0]);
    },

    schedulesSelectionRangeChange: function (obj, newValue) {
        if (newValue.scheduleRange == 'ALL') {
            this.getSchedulesGrid().getSelectionModel().selectAll();
        }
    },

    previewCommunicationSchedule: function (grid, record) {
        var me = this;
        me.getCommunicationSchedulePreviewForm().loadRecord(record);
        me.getCommunicationSchedulePreviewForm().down('#comtaskpreviewcontainer').removeAll();
        if (record.comTaskUsages().data.items.length === 0) {
            me.getCommunicationSchedulePreviewForm().down('#comtaskpreviewcontainer').add({
                xtype: 'displayfield'
            });
        } else {
            Ext.each(record.comTaskUsages().data.items, function (comTaskUsage) {
                me.getCommunicationSchedulePreviewForm().down('#comtaskpreviewcontainer').add({
                    xtype: 'displayfield',
                    value: '<a>' + comTaskUsage.get('name') + '</a>'
                })
            });
        }
    },

    uncheckAllDevices: function () {
        this.getDeviceGrid().getSelectionModel().deselectAll()
    },

    updateDeviceSelection: function (selModel, selected) {
        var me = this,
            label = me.getSelectedDevicesQty(),
            count = selected.length,
            wizard = me.getSearchItemsWizard(),
            nextBtn = wizard.down('#nextButton');
        if (count > 0) {
            nextBtn.enable();
            label.update('<span style="color: grey;">' +
                count + Uni.I18n.translate('searchItems.bulk.devicesSelected', 'MDC', ' devices selected') +
                '</span>')
        } else {
            nextBtn.disable();
            label.update('<span style="color: grey;">' +
                Uni.I18n.translate('searchItems.bulk.noDeviceSelected', 'MDC', 'No devices selected') +
                '</span>')
        }
    },

    devicesSelectionRangeChange: function (obj, newValue) {
        (newValue.deviceRange == 'ALL') && this.getDeviceGrid().getSelectionModel().selectAll();
        if (newValue.deviceRange == 'ALL') {
            this.getDeviceGrid().getSelectionModel().selectAll();
            this.getDeviceGrid().getView().disable();
        } else if (newValue.deviceRange == 'SELECTED') {
            this.getDeviceGrid().getView().enable();
        }
    },

    backClick: function () {
        var me = this,
            layout = me.getSearchItemsWizard().getLayout();
        me.changeContent(layout.getPrev(), layout.getActiveItem());
        me.getNavigationMenu().movePrevStep();
    },

    nextClick: function () {
        var me = this,
            layout = me.getSearchItemsWizard().getLayout();
        me.changeContent(layout.getNext(), layout.getActiveItem());
        me.getNavigationMenu().moveNextStep();
    },

    confirmClick: function () {
        var me = this,
            finishBtn = me.getSearchItemsWizard().down('#finishButton'),
            scheduleIds = [],
            deviceMRID = [],
            url = '/api/ddr/devices/schedules',
            request = {},
            jsonData,
            method;
        (me.operation == 'add') && (method = 'PUT');
        (me.operation == 'remove') && (method = 'DELETE');
        Ext.each(me.schedules, function (item) {
            scheduleIds.push(item.getId())
        });
        Ext.each(me.devices, function (item) {
            deviceMRID.push(item.get('mRID'))
        });
        request.deviceMRIDs = deviceMRID;
        request.scheduleIds = scheduleIds;
        jsonData = Ext.encode(request);
        Ext.Ajax.request({
            url: url,
            method: method,
            jsonData: jsonData,
            success: function (response) {
                var resp = Ext.decode(response.responseText, true);
                me.getStatusPage().removeAll();
                Ext.each(resp ? resp.actions : [], function (item) {
                    (item.successCount > 0) &&
                    me.showStatusMsg(me.buildSuccessMessage(item));
                    (item.failCount > 0) &&
                    me.showStatusMsg(me.buildFailMessage(item));
                });
                finishBtn.enable();
            }
        });
        me.nextClick();
    },

    finishClick: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('search').forward();
    },

    cancelClick: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('search').forward();
    },

    showStatusMsg: function (msg) {
        var me = this;
        me.getStatusPage().add(msg)
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
                messageHeader = Uni.I18n.translatePlural('searchItems.bulk.successfullyAddedCommunicationSchedule', count, 'MDC', 'Successfully added communication schedule \'{0}\' to {1} devices');
                break;
            case 'remove':
                messageHeader = Uni.I18n.translatePlural('searchItems.bulk.successfullyRemovedCommunicationSchedule', count, 'MDC', 'Successfully removed communication schedule \'{0}\' from {1} devices');
                break;
        }

        messageHeader && (messageHeader = Ext.String.format(messageHeader, successful.actionTitle, count));

        message.title = messageHeader;

        return message;
    },

    buildFailMessage: function (failure) {
        var me = this,
            count = parseInt(failure.failCount),
            messageHeader = '',
            messageBody = '',
            grouping = [],
            message = {
                xtype: 'panel'
            };

        switch (me.operation) {
            case 'add':
                messageHeader = Uni.I18n.translatePlural('searchItems.bulk.failedToAddCommunicationSchedule', count, 'MDC', 'Failed to add communication schedule \'{0}\' to {1} devices');
                break;
            case 'remove':
                messageHeader = Uni.I18n.translatePlural('searchItems.bulk.failedToRemoveCommunicationSchedule', count, 'MDC', 'Failed to remove communication schedule \'{0}\' from {1} devices');
                break;
        }

        messageHeader && (messageHeader = Ext.String.format(messageHeader, failure.actionTitle, count));

        Ext.Array.each(failure.fails, function (item) {
            var sameMessageGroup = Ext.Array.findBy(grouping, function (search) {
                return search.messageGroup === item.messageGroup;
            });

            if (sameMessageGroup) {
                sameMessageGroup.devices.push(item.id);
            } else {
                grouping.push({
                    messageGroup: item.messageGroup,
                    message: item.message,
                    devices: [item.id]
                });
            }
        });

        Ext.Array.each(grouping, function (group) {
            messageBody += group.message +
                '<br><br>' +
                '<a href="javascript:void(0)">' + Uni.I18n.translate('searchItems.bulk.viewDevices', 'MDC', 'View devices') + '</a><br><br><br>';
        });

        message.title = messageHeader;
        message.html = messageBody;

        return message;
    },

    navigateToStep: function (index) {
        var me = this,
            layout = me.getSearchItemsWizard().getLayout(),
            currentCmp = layout.getActiveItem(),
            nextCmp = layout.getLayoutItems()[index - 1];
        me.changeContent(nextCmp, currentCmp);
    },

    changeContent: function (nextCmp, currentCmp) {
        var me = this,
            layout = me.getSearchItemsWizard().getLayout();
        if (currentCmp.name == 'selectDevices') {
            me.devices = currentCmp.getSelectionModel().getSelection();
        }
        if (currentCmp.name == 'selectOperation') {
            me.operation = currentCmp.down('#searchitemsactionselect').getValue().operation;
        }
        if (currentCmp.name == 'selectSchedules') {
            me.schedules = currentCmp.down('#schedulesgrid').getSelectionModel().getSelection();
        }
        if (nextCmp.name == 'confirmPage') {
            nextCmp.showMessage(me.buildConfirmMessage());
        }
        if (nextCmp.name == 'statusPage') {
            var pb = Ext.create('Ext.ProgressBar', {width: '50%'});
            nextCmp.removeAll(true);
            nextCmp.add(
                pb.wait({
                    interval: 50,
                    increment: 20,
                    text: (me.operation === 'add' ? 'Adding... ' : 'Removing...')
                })
            );
        }
        nextCmp = layout.setActiveItem(nextCmp);
        this.updateButtonsState(nextCmp);
        this.updateTitles();
    },

    updateTitles: function () {
        var me = this,
            title;
        if (me.operation) {
            var items = Ext.ComponentQuery.query('#searchitemsbulkactiontitle');
            (me.operation == 'add') && (title = Uni.I18n.translate('searchItems.bulk.addActionTitle', 'MDC', 'Add communication schedules'));
            (me.operation == 'remove') && (title = Uni.I18n.translate('searchItems.bulk.removeActionTitle', 'MDC', 'Remove communication schedules'));
            (items.length > 0) && Ext.each(items, function (item) {
                item.setTitle(title);
            })
        }
    },

    buildConfirmMessage: function () {
        var me = this,
            message,
            scheduleWord,
            deviceWord,
            startStr,
            finishStr,
            scheduleList = '',
            unit,
            deviceCount = me.devices.length;
        scheduleWord = Uni.I18n.translatePlural('searchItems.bulk.comSchedules', parseInt(me.schedules.length), 'MDC', 'communication schedules');
        deviceWord = Uni.I18n.translatePlural('searchItems.bulk.devices', parseInt(deviceCount), 'MDC', 'devices');
        if (me.schedules.length === 1) {
            scheduleList = me.schedules[0].get('name');
        } else {
            Ext.each(me.schedules, function (item, index) {
                scheduleList += (index ? ', ' : '') + '\'' + item.get('name') + '\'';
            });
        }
        switch (me.operation) {
            case 'add':
                startStr = Uni.I18n.translate('general.add', 'MDC', 'Add');
                finishStr = Uni.I18n.translate('searchItems.bulk.addMsg', 'MDC', 'The selected devices will execute the chosen communication schedules');
                unit = Uni.I18n.translate('general.unitTo', 'MDC', 'to');
                break;
            case 'remove':
                startStr = Uni.I18n.translate('general.remove', 'MDC', 'Remove');
                finishStr = Uni.I18n.translate('searchItems.bulk.removeMsg', 'MDC', 'The selected devices will not execute the chosen communication schedules');
                unit = Uni.I18n.translate('general.unitFrom', 'MDC', 'from');
                break;
        }
        message = {
            title: startStr + ' ' + scheduleWord + ' ' + scheduleList + ' ' + unit + ' ' +
                deviceCount + ' ' + deviceWord + '?',
            body: finishStr
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
            cancelBtn = wizard.down('#cancelButton'),
            communicationSchedulesStore = this.getStore('Mdc.store.CommunicationSchedules');
        activePage.name == 'selectDevices' ? backBtn.disable() : backBtn.enable();
        switch (activePage.name) {
            case 'selectDevices' :
                backBtn.show();
                nextBtn.show();
                nextBtn.setDisabled(false);
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 'selectOperation' :
                backBtn.show();
                nextBtn.show();
                nextBtn.setDisabled(false);
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 'selectSchedules' :
                backBtn.show();
                nextBtn.show();
                nextBtn.setDisabled(communicationSchedulesStore.getCount() ? false : true);
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 'confirmPage' :
                backBtn.show();
                nextBtn.hide();
                confirmBtn.show();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 'statusPage' :
                backBtn.hide();
                nextBtn.hide();
                confirmBtn.hide();
                finishBtn.show();
                finishBtn.disable();
                cancelBtn.hide();
                break;
        }
    },

    createCommunicationSchedule: function () {
        var title = Uni.I18n.translate('general.warning', 'MDC', 'Warning'),
            message = Uni.I18n.translate('searchItems.newComScheduleAddWarningMsg', 'MDC', 'When you have finished creating a new communication schedule on the other tab, you should refresh this page.'),
            config = {
                icon: Ext.MessageBox.WARNING
            },
            newTab = window.open('#/administration/communicationschedules/create','_blank');

        newTab && newTab.blur();
        window.focus();
        this.getApplication().getController('Uni.controller.Error').showError(title, message, config);
    }

});