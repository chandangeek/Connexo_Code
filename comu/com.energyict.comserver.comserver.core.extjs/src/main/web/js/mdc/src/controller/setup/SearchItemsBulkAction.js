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
                click: this.finishClick
            },
            'searchitems-wizard #wizardCancelButton': {
                click: this.cancelClick
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
            me.shedulesUnchecked = false;

            me.getStore('Mdc.store.DevicesBuffered').data.clear();
            me.getStore('Mdc.store.DevicesBuffered').load({
                callback: function () {
                    me.getApplication().fireEvent('changecontentevent', widget);
                    widget.up().setLoading(false)
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
            finishBtn = me.getSearchItemsWizard().down('#finishButton'),
            scheduleIds = [],
            deviceMRID = [],
            url = '/api/ddr/devices/schedules',
            request = {},
            jsonData,
            method,
            params;

        finishBtn.disable();
        switch (me.operation) {
            case 'add':
                method = 'PUT';
                break;
            case 'remove':
                method = 'DELETE';
                break;
        }

        Ext.each(me.schedules, function (item) {
            scheduleIds.push(item.getId());
        });

        Ext.each(me.devices, function (item) {
            deviceMRID.push(item.get('mRID'));
        });

        request.deviceMRIDs = deviceMRID;
        request.scheduleIds = scheduleIds;
        jsonData = Ext.encode(request);

        params = me.getStore('Mdc.store.Devices').getProxy().extraParams;
        params.all = me.allDevices;
        Ext.Ajax.request({
            url: url,
            method: method,
            params: params,
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
                messageHeader = Uni.I18n.translatePlural('searchItems.bulk.successfullyAddedCommunicationSchedule', count, 'MDC', 'Successfully added communication schedule \'{1}\' to {0} devices');
                break;
            case 'remove':
                messageHeader = Uni.I18n.translatePlural('searchItems.bulk.successfullyRemovedCommunicationSchedule', count, 'MDC', 'Successfully removed communication schedule \'{1}\' from {0} devices');
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

    changeContent: function (nextCmp, currentCmp) {
        var me = this,
            layout = me.getSearchItemsWizard().getLayout(),
            errorContainer = currentCmp.down('#stepSelectionError'),
            errorPanel = null, progressBar;

        switch (currentCmp.name) {
            case 'selectDevices':
                if (me.getDevicesGrid().isAllSelected()) {
                    me.devices = me.getStore('Mdc.store.Devices').data.items;
                } else {
                    me.devices = me.getDevicesGrid().getSelectionModel().getSelection();
                }

                me.allDevices = me.getDevicesGrid().isAllSelected();
                errorPanel = currentCmp.down('#step1-errors');
                validation = me.devices.length || me.allDevices;
                break;
            case 'selectOperation':
                me.operation = currentCmp.down('#searchitemsactionselect').getValue().operation;
                break;
            case 'selectSchedules':
                me.schedules = me.getSchedulesGrid().getSelectionModel().getSelection();
                errorPanel = currentCmp.down('#step3-errors');
                validation = me.schedules.length;
                break;
        }

        (currentCmp.navigationIndex > nextCmp.navigationIndex) && (validation = true);

        if (validation) {
            switch (nextCmp.name) {
                case 'confirmPage':
                    nextCmp.showMessage(me.buildConfirmMessage());
                    break;
                case 'statusPage':
                    if (currentCmp.name != 'statusPageViewDevices') {
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
            errorPanel.show();
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
            (me.operation == 'add') && (title = Uni.I18n.translate('searchItems.bulk.addActionTitle', 'MDC', 'Add shared communication schedules'));
            (me.operation == 'remove') && (title = Uni.I18n.translate('searchItems.bulk.removeActionTitle', 'MDC', 'Remove shared communication schedules'));
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
        scheduleWord = Uni.I18n.translatePlural('searchItems.bulk.comSchedules', parseInt(me.schedules.length), 'MDC', 'shared communication schedules');
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
                finishStr = Uni.I18n.translate('searchItems.bulk.addMsg', 'MDC', 'The selected devices will execute the chosen shared communication schedules');
                unit = Uni.I18n.translate('general.unitTo', 'MDC', 'to');
                break;
            case 'remove':
                startStr = Uni.I18n.translate('general.remove', 'MDC', 'Remove');
                finishStr = Uni.I18n.translate('searchItems.bulk.removeMsg', 'MDC', 'The selected devices will not execute the chosen shared communication schedules');
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
            cancelBtn = wizard.down('#wizardCancelButton');
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
                cancelBtn.hide();
                break;
            case 'statusPageViewDevices' :
                backBtn.show();
                nextBtn.hide();
                confirmBtn.hide();
                finishBtn.hide();
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