Ext.define('Imt.controller.SearchItemsBulkAction', {
    extend: 'Ext.app.Controller',

    requires: [
        'Imt.usagepointmanagement.view.bulk.Browse',
        'Imt.usagepointmanagement.view.bulk.Navigation',
        'Imt.usagepointmanagement.view.bulk.Wizard'
    ],

    stores: [
        'Imt.usagepointmanagement.store.ActiveCalendars'
    ],

    refs: [
        {
            ref: 'usagePointsItemsWizard',
            selector: '#usagepointswizard'
        },
        {
            ref: 'navigationMenu',
            selector: '#usagePointsBulkNavigation'
        },
        {
            ref: 'usagePointsGrid',
            selector: '#usagepoints-bulk-step1 usagepoints-selection-grid'
        },
        {
            ref: 'statusPage',
            selector: '#usagepoints-bulk-step5'
        },
    ],

    init: function(){
        this.control({
            'usagepoints-wizard #nextButton': {
                click: this.nextClick
            },
            'usagepoints-wizard #backButton': {
                click: this.backClick
            },
        });
    },

    showBulkAction: function(){
        var me = this,
            search = me.getController('Imt.controller.Search'),
            searchResults = Ext.getStore('Uni.store.search.Results'),
            widget;

        // in case user forgot to press apply filters on search page we need ensure that search filters state matches search results state
        search.service.applyFilters();
        if (!searchResults.getCount()) {
            this.goBack();
        } else {
            //me.devices = null;
            //me.allDevices = false;
            //me.schedules = null;
            //me.operation = null;
            //me.configData = null;
            //me.shedulesUnchecked = false;

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

            widget = Ext.widget('usagepoints-bulk-browse', {
                deviceStore: store
            });
            me.getApplication().fireEvent('changecontentevent', widget);
            widget.setLoading();
            store.load({
                callback: function () {
                    widget.setLoading(false);
                }
            });
            //me.getStore('Mdc.store.CommunicationSchedulesWithoutPaging').load();
        }
    },

    nextClick: function () {
        var layout = this.getUsagePointsItemsWizard().getLayout();
        this.changeContent(layout.getNext(), layout.getActiveItem()) && this.getNavigationMenu().moveNextStep();
    },

    backClick: function () {
        var layout = this.getUsagePointsItemsWizard().getLayout(),
            currentCmp = layout.getActiveItem();

        this.changeContent(layout.getPrev(), currentCmp);
        (currentCmp.name !== 'statusPageViewDevices') && this.getNavigationMenu().movePrevStep();
    },


    changeContent: function (nextCmp, currentCmp) {
        var me = this, errorPanel = null, additionalText, progressBar,
            router = me.getController('Uni.controller.history.Router'),
            search = me.getController('Imt.controller.Search'),
            wizard = me.getUsagePointsItemsWizard(),
            layout = wizard.getLayout(),
            errorContainer = currentCmp.down('#stepSelectionError');
        switch (currentCmp.name) {
            case 'selectDevices':
                me.allDevices = me.getUsagePointsGrid().isAllSelected();

                if (!me.allDevices) {
                    me.devices = me.getUsagePointsGrid().getSelectionModel().getSelection();
                }
                debugger;
                me.operation = 'addCalendar';
                nextCmp.down('#usagepointsactionselect').setValue({operation: 'addCalendar'});

                errorPanel = currentCmp.down('#step1-errors');
                me.validation = me.allDevices || me.devices.length;
                break;
            case 'selectOperation':
                me.operation = currentCmp.down('#usagepointsactionselect').getValue().operation;
                debugger;
                if (nextCmp.name == 'selectActionItems') {
                    if (me.operation == 'addCalendar') {
                        //var configStore = me.getStore('Mdc.store.BulkDeviceConfigurations'),
                        //    changeDeviceConfigForm = nextCmp.down('#change-device-configuration'),
                        //    currentConfigField = nextCmp.down('#current-device-config-selection');
                        //
                        //nextCmp.down('#select-schedules-panel').hide();
                        //changeDeviceConfigForm.show();
                        //changeDeviceConfigForm.getForm().clearInvalid();
                        //
                        //var device = me.getDevicesGrid().getStore().getAt(0);
                        //configStore.getProxy().setUrl({deviceType: me.deviceType});
                        //
                        //wizard.setLoading(true);
                        //configStore.clearFilter(false);
                        //configStore.addFilter([
                        //    function (record) {
                        //        return record.get('id') !== me.deviceConfigId && !record.get('dataloggerEnabled');
                        //    }
                        //]);
                        //configStore.load(function (operation, success) {
                        //    var deviceConfig = this.getById(me.deviceConfigId);
                        //    currentConfigField.setValue(deviceConfig.get('name'));
                        //    if (success && (configStore.getCount() < 1)) {
                        //        wizard.down('#nextButton').disable();
                        //        nextCmp.down('#new-device-config-selection').hide();
                        //        nextCmp.down('#no-device-configuration').show();
                        //    } else {
                        //        wizard.down('#nextButton').enable();
                        //        nextCmp.down('#new-device-config-selection').show();
                        //        nextCmp.down('#no-device-configuration').hide();
                        //    }
                        //    if (configStore.getCount() === 1) {
                        //        wizard.down('#new-device-config-selection').setValue(this.getAt(0).get('id'));
                        //        wizard.down('#nextButton').enable();
                        //    }
                        //    wizard.setLoading(false);
                        //});
                    }
                }
                break;
            case 'selectActionItems':
                //debugger;
                //if (me.operation != 'changeconfig') {
                //    me.schedules = me.getSchedulesGrid().getSelectionModel().getSelection();
                //    errorPanel = currentCmp.down('#step3-errors');
                //    me.validation = me.schedules.length;
                //} else {
                //    var form = currentCmp.down('#change-device-configuration');
                //    me.validation = currentCmp.down('#change-device-configuration').isValid();
                //    me.validation && (me.configData = form.getValues());
                //    me.configNames = {
                //        fromconfig: form.down('#current-device-config-selection').getRawValue(),
                //        toconfig: form.down('#new-device-config-selection').getRawValue()
                //    };
                //    errorPanel = currentCmp.down('#step3-errors');
                //    errorContainer = null;
                //}

                break;
        }

        (currentCmp.navigationIndex > nextCmp.navigationIndex) && (me.validation = true);

        if (me.validation) {
            //switch (nextCmp.name) {
            //    case 'confirmPage':
            //        if (me.operation != 'changeconfig') {
            //            nextCmp.showMessage(me.buildConfirmMessage());
            //            wizard.down('#confirmButton').enable()
            //        } else {
            //            wizard.setLoading(true);
            //            nextCmp.removeAll();
            //            me.checkConflictMappings(me.deviceConfigId, me.configData['toconfig'], function (unsolvedConflicts) {
            //                wizard.setLoading(false);
            //                if (unsolvedConflicts) {
            //                    me.getNavigationMenu().markInvalid();
            //                    var title = me.devices?Uni.I18n.translatePlural('searchItems.bulk.devConfigUnsolvedConflictsTitle', me.devices.length, 'MDC', "Unable to change device configuration of {0} devices", "Unable to change device configuration of {0} device", "Unable to change device configuration of {0} devices"):
            //                            Uni.I18n.translate('searchItems.bulk.devConfigUnsolvedConflictsTitleForSearch', 'MDC', "Unable to change device configuration of the selected devices"),
            //                        text = Ext.String.format(Uni.I18n.translate('searchItems.bulk.devConfigUnsolvedConflictsMsg', 'MDC', 'The configuration of devices with current configuration \'{0}\' cannot be changed to \'{1}\' due to unsolved conflicts.'), me.configNames.fromconfig, me.configNames.toconfig);
            //                    text = text.replace('{fromconfig}', me.configNames.fromconfig).replace('{toconfig}', me.configNames.toconfig);
            //                    if (Mdc.privileges.DeviceType.canAdministrate()) {
            //                        var solveLink = router.getRoute('administration/devicetypes/view/conflictmappings/edit').buildUrl({deviceTypeId: me.deviceType, id: unsolvedConflicts});
            //                        me.getController('Mdc.controller.setup.DeviceConflictingMapping').returnInfo = {
            //                            from: 'changeDeviceConfigurationBulk'
            //                        };
            //                        wizard.down('#confirmButton').disable();
            //                        nextCmp.showChangeDeviceConfigConfirmation(title, text, solveLink, null, 'error');
            //                    } else {
            //                        wizard.down('#confirmButton').hide();
            //                        wizard.down('#backButton').hide();
            //                        wizard.down('#wizardCancelButton').hide();
            //                        wizard.down('#failureFinishButton').show();
            //                        additionalText = Uni.I18n.translate('searchItems.bulk.changeDevConfigNoPrivileges', 'MDC', 'You cannot solve the conflicts in conflicting mappings on device type because you do not have the privileges. Contact the administrator.');
            //                        nextCmp.showChangeDeviceConfigConfirmation(title, text, null, additionalText, 'error');
            //                    }
            //                } else {
            //                    wizard.down('#confirmButton').enable();
            //                    var message = me.buildConfirmMessage();
            //                    additionalText = Uni.I18n.translate('searchItems.bulk.changeDevConfigWarningMessage', 'MDC', 'The device configuration change can possibly lead to critical data loss (security settings, connection attributes...).');
            //                    nextCmp.showChangeDeviceConfigConfirmation(message.title, message.body, null, additionalText)
            //                }
            //            });
            //        }
            //        break;
            //    case 'statusPage':
            //        if (currentCmp.name != 'statusPage') {
            //            if (me.operation != 'changeconfig') {
            //                progressBar = Ext.create('Ext.ProgressBar', {width: '50%'});
            //                Ext.suspendLayouts();
            //                nextCmp.removeAll(true);
            //                nextCmp.add(
            //                    progressBar.wait({
            //                        interval: 50,
            //                        increment: 20,
            //                        text: (me.operation === 'add' ? Uni.I18n.translate('general.adding', 'MDC', 'Adding...') : Uni.I18n.translate('general.removing', 'MDC', 'Removing...'))
            //                    })
            //                );
            //                Ext.resumeLayouts();
            //                this.getNavigationMenu().jumpBack = false;
            //            }
            //        }
            //        break;
            //}
            errorPanel && errorPanel.hide();
            errorContainer && errorContainer.hide();
            layout.setActiveItem(nextCmp);
            this.updateButtonsState(nextCmp);
            this.updateTitles();
         //   me.getStatusPage().setLoading(false);
            return true;
        } else {
            errorPanel && errorPanel.show();
            errorContainer && errorContainer.show();
         //   me.getStatusPage().setLoading(false);
            return false;
        }
    },

    updateButtonsState: function (activePage) {
        var me = this,
            wizard = me.getUsagePointsItemsWizard(),
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
                debugger;
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
            //case 'statusPage' :
            //    backBtn.hide();
            //    nextBtn.hide();
            //    confirmBtn.hide();
            //    finishBtn.show();
            //    cancelBtn.hide();
            //    break;
            //case 'statusPageViewDevices' :
            //    backBtn.show();
            //    nextBtn.hide();
            //    confirmBtn.hide();
            //    finishBtn.hide();
            //    falureFinishBtn.hide();
            //    cancelBtn.hide();
            //    break;
        }
    },

    updateTitles: function () {
        var me = this,
            title;
        //if (me.operation) {
        //    var items = Ext.ComponentQuery.query('#searchitemsbulkactiontitle');
        //    switch (me.operation){
        //        case 'add' : {
        //            title = Uni.I18n.translate('searchItems.bulk.addActionTitle', 'MDC', 'Add shared communication schedules')
        //        }
        //            break;
        //        case 'remove' : {
        //            title = Uni.I18n.translate('searchItems.bulk.removeActionTitle', 'MDC', 'Remove shared communication schedules')
        //        }
        //            break;
        //        case 'changeconfig' : {
        //            title = Uni.I18n.translate('searchItems.bulk.changeConfigActionTitle', 'MDC', 'Change device configuration')
        //        }
        //            break;
        //    }
        //    (items.length > 0) && Ext.each(items, function (item) {
        //        item.setTitle(title);
        //    })
        //}
    },

    goBack: function () {
        var me = this,
            grid = me.getUsagePointsGrid(),
            search = me.getController('Imt.controller.Search'),
            router = me.getController('Uni.controller.history.Router'),
            queryParams;

        if (grid && search.service.searchDomain) {
            queryParams = {
                restore: true
            };
        }

        router.getRoute('search').forward(null, queryParams);
    }

});
