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
        'Mdc.store.DevicesBuffered',
        'Mdc.store.CommunicationSchedulesWithoutPaging'
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
            ref: 'deviceGrid',
            selector: '#searchitems-bulk-step1 gridpanel'
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
            '#searchitems-bulk-step1 #devicesgrid': {
                selectionchange: this.updateDeviceSelection
            },
            '#deviceSelectionRange': {
                change: this.devicesSelectionRangeChange
            },
            'searchitems-bulk-step3 #schedulesgrid': {
                selectionchange: this.updateScheduleSelection
            },
            'searchitems-bulk-step3 #schedulesgrid gridview': {
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
            'searchitems-wizard #createCommunicationSchedule': {
                click: this.createCommunicationSchedule
            },
            '#searchitemsBulkNavigation': {
                movetostep: this.navigateToStep
            }
        });
    },

    showBulkAction: function () {
        var me = this,
            devicesStore = this.getStore('Mdc.store.Devices'),
            widget;

        if (!devicesStore.getCount()) {
            this.cancelClick();
        } else {
            widget = Ext.widget('searchitems-bulk-browse');
            me.devices = null;
            me.allDevices = false;
            me.schedules = null;
            me.operation = null;
            me.getApplication().fireEvent('changecontentevent', widget);
            me.getDeviceGrid().disable();
            me.getStore('Mdc.store.DevicesBuffered').load();
            me.getStore('Mdc.store.CommunicationSchedulesWithoutPaging').load(function () {
                me.getShceduleSelectionRange().down('#allSchedules').setValue(true);
            });
        }
    },

    uncheckAllSchedules: function () {
        this.getSchedulesGrid().getSelectionModel().deselectAll();
        this.getShceduleSelectionRange().down('#selectedSchedules').setValue(true);
    },

    updateScheduleSelection: function (selModel, selected) {
        var label = this.getSelectedScheduleQty(),
            count = selected.length;

        if (count) {
            label.update('<span style="color: grey;">'
                + Ext.String.format(Uni.I18n.translatePlural('searchItems.bulk.scheduleSelected', count, 'MDC', '{0} schedules selected'), count)
                + '</span>');
        } else {
            label.update('<span style="color: grey;">' +
                Uni.I18n.translate('searchItems.bulk.noScheduleSelected', 'MDC', 'No schedule selected') +
                '</span>');
        }

        count == 1 && this.previewCommunicationSchedule(null, selected[0]);
    },

    schedulesSelectionRangeChange: function (obj, newValue) {
        var schedulesGrid = this.getSchedulesGrid();

        switch (newValue.scheduleRange) {
            case 'ALL':
                schedulesGrid.hide();
                schedulesGrid.getSelectionModel().selectAll();
                schedulesGrid.disable();
                schedulesGrid.show();
                break;
            case 'SELECTED':
                schedulesGrid.enable();
                break;
        }
    },

    previewCommunicationSchedule: function (grid, record) {
        var preview = this.getCommunicationSchedulePreview();

        preview.down('form').loadRecord(record);
        preview.setTitle(record.get('name'));
        preview.show();
    },

    uncheckAllDevices: function () {
        this.getDeviseSelectionRange().down('#selectedDevices').setValue(true);
        this.getDeviceGrid().getSelectionModel().deselectAll();
    },

    updateDeviceSelection: function (selModel, selected) {
        var me = this,
            label = me.getSelectedDevicesQty(),
            count = selected.length;

        if (count > 0) {
            label.update('<span style="color: grey;">'
                + Ext.String.format(Uni.I18n.translatePlural('searchItems.bulk.devicesSelected', count, 'MDC', '{0} devices selected'), count)
                + '</span>')
        } else {
            label.update('<span style="color: grey;">' +
                Uni.I18n.translate('searchItems.bulk.noDeviceSelected', 'MDC', 'No devices selected') +
                '</span>')
        }
    },

    devicesSelectionRangeChange: function (obj, newValue) {
        var devicesGrid = this.getDeviceGrid();

        switch (newValue.deviceRange) {
            case 'ALL':
                devicesGrid.disable();
                devicesGrid.getSelectionModel().deselectAll();
                break;
            case 'SELECTED':
                devicesGrid.enable();
                break;
        }
    },

    backClick: function () {
        var layout = this.getSearchItemsWizard().getLayout();

        this.changeContent(layout.getPrev(), layout.getActiveItem());
        this.getNavigationMenu().movePrevStep();
    },

    nextClick: function () {
        var layout = this.getSearchItemsWizard().getLayout();

        this.changeContent(layout.getNext(), layout.getActiveItem()) && this.getNavigationMenu().moveNextStep();
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

        switch (me.operation) {
            case 'add':
                method = 'PUT';
                break;
            case 'remove':
                method = 'DELETE';
                break;
        }
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
            params: {
                all: me.allDevices
            },
            jsonData: jsonData,
            timeout: 180000,
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
                messageHeader = Uni.I18n.translatePlural('searchItems.bulk.successfullyAddedCommunicationSchedule', count, 'MDC', 'Successfully added communication schedule \'{1}\' to {0} devices');
                break;
            case 'remove':
                messageHeader = Uni.I18n.translatePlural('searchItems.bulk.successfullyRemovedCommunicationSchedule', count, 'MDC', 'Successfully removed communication schedule \'{1}\' from {0} devices');
                break;
        }

        messageHeader && (messageHeader = Ext.String.format(messageHeader, count, successful.actionTitle));

        message.title = messageHeader;

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
                messageHeader = Uni.I18n.translatePlural('searchItems.bulk.failedToAddCommunicationSchedule', count, 'MDC', 'Failed to add communication schedule \'{1}\' to {0} devices');
                break;
            case 'remove':
                messageHeader = Uni.I18n.translatePlural('searchItems.bulk.failedToRemoveCommunicationSchedule', count, 'MDC', 'Failed to remove communication schedule \'{1}\' from {0} devices');
                break;
        }

        messageHeader && (messageHeader = Ext.String.format(messageHeader, count, failure.actionTitle));

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
            messageBody.push({
                html: group.message,
                bbar: [
                    {
                        text: Uni.I18n.translate('searchItems.bulk.viewDevices', 'MDC', 'View devices'),
                        ui: 'link',
                        action: 'viewDevices',
                        itemId: 'viewDevicesButton'
                    }
                ]
            });
        });

        message.title = messageHeader;
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

    changeContent: function (nextCmp, currentCmp) {
        var me = this,
            layout = me.getSearchItemsWizard().getLayout(),
            validation = true,
            errorPanel = null,
            progressBar;

        switch (currentCmp.name) {
            case 'selectDevices':
                me.devices = currentCmp.down('#devicesgrid').getSelectionModel().getSelection();
                me.allDevices = me.getDeviseSelectionRange().getValue().deviceRange == 'ALL';
                errorPanel = currentCmp.down('#step1-errors');
                validation = (me.devices.length || me.allDevices) ? true : false;
                break;
            case 'selectOperation':
                me.operation = currentCmp.down('#searchitemsactionselect').getValue().operation;
                break;
            case 'selectSchedules':
                me.schedules = currentCmp.down('#schedulesgrid').getSelectionModel().getSelection();
                errorPanel = currentCmp.down('#step3-errors');
                validation = me.schedules.length ? true : false;
                break;
        }

        (layout.getPrev().name === nextCmp.name) && (validation = true);

        if (validation) {
            switch (nextCmp.name) {
                case 'confirmPage':
                    nextCmp.showMessage(me.buildConfirmMessage());
                    break;
                case 'statusPage':
                    progressBar = Ext.create('Ext.ProgressBar', {width: '50%'});
                    nextCmp.removeAll(true);
                    nextCmp.add(
                        progressBar.wait({
                            interval: 50,
                            increment: 20,
                            text: (me.operation === 'add' ? 'Adding... ' : 'Removing...')
                        })
                    );
                    this.getNavigationMenu().jumpBack = false;
                    break;
            }
            errorPanel && errorPanel.hide();
            layout.setActiveItem(nextCmp);
            this.updateButtonsState(nextCmp);
            this.updateTitles();
            return true;
        } else {
            errorPanel.show();
            return false;
        }
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
            deviceCount = (me.allDevices ? Uni.I18n.translate('searchItems.bulk.all', 'MDC', 'all') : false) || me.devices.length;
        scheduleWord = Uni.I18n.translatePlural('searchItems.bulk.comSchedules', parseInt(me.schedules.length), 'MDC', 'communication schedules');
        deviceWord = Uni.I18n.translatePlural('searchItems.bulk.devices', parseInt(Ext.isString(deviceCount) ? 0 : deviceCount), 'MDC', 'devices');
        if (me.schedules.length === 1) {
            scheduleList = '\'' + me.schedules[0].get('name') + '\'';
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
            cancelBtn = wizard.down('#cancelButton');
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
                nextBtn.enable();
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
            newTab = window.open('#/administration/communicationschedules/create', '_blank');

        newTab && newTab.blur();
        window.focus();
        this.getApplication().getController('Uni.controller.Error').showError(title, message, config);
    }

});