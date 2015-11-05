Ext.define('Mdc.controller.setup.AddDeviceGroupAction', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.util.Filters',
        'Mdc.service.Search'
    ],

    views: [
        'Mdc.view.setup.devicegroup.Browse'
    ],

    stores: [
        'Mdc.store.DeviceGroups',
        'Mdc.store.DevicesOfDeviceGroup',
        'Mdc.store.StaticGroupDevices',
        'Mdc.store.DynamicGroupDevices'
    ],

    models: [
        'Mdc.model.DeviceGroup'
    ],

    refs: [
        {
            ref: 'navigationMenu',
            selector: '#add-devicegroup-browse #devicegroupaddnavigation'
        },
        {
            ref: 'addDeviceGroupWizard',
            selector: '#add-devicegroup-browse #adddevicegroupwizard'
        },
        {
            ref: 'nameTextField',
            selector: '#add-devicegroup-browse #deviceGroupNameTextField'
        },
        {
            ref: 'step1FormErrorMessage',
            selector: '#add-devicegroup-browse #step1-adddevicegroup-errors'
        },
        {
            ref: 'step2FormErrorMessage',
            selector: '#add-devicegroup-browse #step2-adddevicegroup-errors'
        }
    ],

    filterObjectParam: 'filter',
    lastRequest: undefined,
    searchFieldsOnLoadListener: null,

    init: function () {
        var me = this;

        me.service = Ext.create('Mdc.service.Search', {
            router: me.getController('Uni.controller.history.Router')
        });

        me.control({
            '#add-devicegroup-browse #staticDynamicRadioButton': {
                change: me.prepareStep2
            },
            '#add-devicegroup-browse adddevicegroup-wizard button[navigationBtn=true]': {
                click: me.moveTo
            },
            '#add-devicegroup-browse devicegroup-add-navigation': {
                movetostep: me.moveTo
            },
            '#add-devicegroup-browse search-criteria-selector menu menucheckitem': {
                checkchange: function(field, checked) {
                    checked
                        ? me.service.addProperty(field.criteria)
                        : me.service.removeProperty(field.criteria);
                }
            },
            '#add-devicegroup-browse button[action=search]': {
                click: {
                    fn: me.service.applyFilters,
                    scope: me.service
                }
            },
            '#add-devicegroup-browse button[action=clearFilters]': {
                click: {
                    fn: me.service.clearFilters,
                    scope: me.service
                }
            }
        });
    },

    showWizard: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            deviceGroupModelName = 'Mdc.model.DeviceGroup',
            deviceGroupId = router.arguments.deviceGroupId,
            widget = Ext.widget('add-devicegroup-browse', {
                itemId: 'add-devicegroup-browse',
                router: router,
                service: me.service,
                isEdit: Ext.isDefined(deviceGroupId),
                returnLink: router.queryParams.fromDetails === 'true'
                    ? router.getRoute('devices/devicegroups/view').buildUrl()
                    : router.getRoute('devices/devicegroups').buildUrl()
            });

        me.service.getSearchDomainsStore().load();
        me.getApplication().fireEvent('changecontentevent', widget);
        if (Ext.isDefined(deviceGroupId)) {
            mainView.setLoading();
            me.getModel(deviceGroupModelName).load(deviceGroupId, {
                success: function (record) {
                    var isDynamic,
                        devices;

                    if (widget.rendered) {
                        isDynamic = record.get('dynamic');
                        me.getApplication().fireEvent('loadDeviceGroup', record);
                        Ext.suspendLayouts();
                        widget.down('devicegroup-add-navigation').setTitle(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", [record.get('name')]));
                        widget.down('adddevicegroup-wizard').loadRecord(record);
                        me.prepareStep2(null, {dynamic: isDynamic});
                        Ext.resumeLayouts(true);
                        if (!isDynamic) {
                            devices = me.getStore('Mdc.store.DevicesOfDeviceGroup');
                            devices.getProxy().setExtraParam('id', deviceGroupId);
                            devices.load(function (records) {
                                mainView.setLoading(false);
                                widget.down('static-group-devices-grid').setDevices(records);
                            });
                        }
                    }
                },
                failure: function () {
                    mainView.setLoading(false);
                }
            });
        } else {
            widget.down('adddevicegroup-wizard').loadRecord(Ext.create(deviceGroupModelName));
        }
    },

    moveTo: function (button) {
        var me = this,
            wizardLayout = me.getAddDeviceGroupWizard().getLayout(),
            currentStep = wizardLayout.getActiveItem().navigationIndex,
            direction,
            nextStep;

        if (button.action === 'step-next' || button.action === 'confirm-action') {
            direction = 1;
            nextStep = currentStep + direction;
        } else {
            direction = -1;
            if (button.action === 'step-back') {
                nextStep = currentStep + direction;
            } else {
                nextStep = button;
            }
        }

        Ext.suspendLayouts();

        if (direction > 0) {
            if (!me.validateCurrentStep(currentStep)) {
                Ext.resumeLayouts(true);
                return
            }
        }

        me.prepareNextStep(nextStep);
        wizardLayout.setActiveItem(nextStep - 1);
        me.getNavigationMenu().moveToStep(nextStep);

        Ext.resumeLayouts(true);
    },

    validateCurrentStep: function (stepNumber) {
        var me = this,
            valid = true;

        switch (stepNumber) {
            case 1:
                valid = me.validateStep1();
                break;
            case 2:
                valid = me.validateStep2();
                break;
        }

        return valid;
    },

    validateStep1: function () {
        var me = this,
            valid = true,
            group = me.getAddDeviceGroupWizard().getRecord(),
            step1ErrorMsg = me.getStep1FormErrorMessage(),
            nameField = me.getNameTextField(),
            name = nameField.getValue();

        if (!nameField.validate()) {
            valid = false;
            step1ErrorMsg.show();
        } else if (name !== group.get('name') && me.getStore('Mdc.store.DeviceGroups').find('name', name) >= 0) { // todo: postponed from the previous functionality, should be replaced
            valid = false;
            nameField.markInvalid(Uni.I18n.translate('general.name.shouldBeUnique', 'MDC', 'Name should be unique'));
            step1ErrorMsg.show();
        } else {
            step1ErrorMsg.hide();
        }

        return valid;
    },

    validateStep2: function () {
        var me = this,
            wizard = me.getAddDeviceGroupWizard(),
            record = Ext.clone(wizard.getRecord()),
            valid = true,
            isDynamic,
            devices;

        wizard.updateRecord(record);
        isDynamic = record.get('dynamic');
        devices = record.get('devices');

        if (!isDynamic) {
            valid = !devices || !!devices.length;
            me.getStep2FormErrorMessage().setVisible(!valid);
        }

        return valid;
    },

    prepareNextStep: function (stepNumber) {
        var me = this,
            wizard = me.getAddDeviceGroupWizard(),
            navigationMenu = me.getNavigationMenu(),
            buttons = wizard.getDockedComponent('device-group-wizard-buttons'),
            nextBtn = buttons.down('[action=step-next]'),
            backBtn = buttons.down('[action=step-back]'),
            confirmBtn = buttons.down('[action=confirm-action]'),
            finishBtn = buttons.down('[action=finish]'),
            cancelBtn = buttons.down('[action=cancel]');

        switch (stepNumber) {
            case 1:
                nextBtn.show();
                backBtn.show();
                backBtn.disable();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 2:
                me.getStep2FormErrorMessage().hide();
                nextBtn.show();
                backBtn.show();
                backBtn.enable();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 3:
                navigationMenu.jumpBack = false;
                nextBtn.hide();
                backBtn.hide();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.hide();
                me.prepareStep3(wizard, navigationMenu, [confirmBtn, backBtn, cancelBtn]);
                break;
            case 4:
                navigationMenu.jumpBack = false;
                nextBtn.hide();
                backBtn.hide();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.hide();
                me.prepareStep4(wizard, finishBtn);
                break;
        }
    },

    prepareStep2: function (field, newValue) {
        var me = this,
            wizard = me.getAddDeviceGroupWizard(),
            step2 =  wizard.down('device-group-wizard-step2'),
            deviceDomain = 'com.energyict.mdc.device.data.Device',
            domainsStore = me.service.getSearchDomainsStore(),
            isDynamic = newValue.dynamic,
            staticGrid,
            selectionGroupType;

        step2.getLayout().setActiveItem(isDynamic ? 1 : 0);
        me.service.setSearchResultsStore(me.getStore(isDynamic ? 'Mdc.store.DynamicGroupDevices' : 'Mdc.store.StaticGroupDevices'));
        me.setColumnPicker(isDynamic);
        if (!isDynamic) {
            staticGrid = step2.down('static-group-devices-grid');
            selectionGroupType = {};
            staticGrid.getSelectionModel().deselectAll();
            staticGrid.getStore().data.clear();
            selectionGroupType[staticGrid.radioGroupName] = staticGrid.allInputValue;
            staticGrid.getSelectionGroupType().setValue(selectionGroupType);
        }
        if (domainsStore.isLoading()) {
            domainsStore.on('load', function () {
                me.service.setDomain(deviceDomain);
            }, me, {single: true});
        } else {
            me.service.setDomain(deviceDomain);
        }
    },

    setColumnPicker: function (isDynamic) {
        var me = this,
            grid = me.getAddDeviceGroupWizard().down(isDynamic
                ? 'dynamic-group-devices-grid'
                : 'static-group-devices-grid'),
            destroyListener = function () {
                if (me.searchFieldsOnLoadListener) {
                    me.searchFieldsOnLoadListener.destroy();
                }
            };

        destroyListener();
        me.searchFieldsOnLoadListener = me.service.getSearchFieldsStore().on('load', function (store, items) {
            grid.getStore().model.setFields(items.map(function (field) {
                return me.service.createFieldDefinitionFromModel(field)
            }));

            grid.down('uni-search-column-picker').setColumns(items.map(function (field) {
                return me.service.createColumnDefinitionFromModel(field)
            }));
        }, grid, {
            destroyable: true
        });
        grid.on('destroy', destroyListener);
    },

    prepareStep3: function (wizard, navigationMenu, buttons) {
        var step3 =  wizard.down('device-group-wizard-step3'),
            progressbar = step3.down('progressbar'),
            record = Ext.clone(wizard.getRecord()),
            deviceGroupName,
            isDynamic,
            confirmationTitle,
            confirmationMessage;

        wizard.updateRecord(record);
        deviceGroupName = record.get('name');
        isDynamic = record.get('dynamic');

        if (wizard.isEdit) {
            confirmationTitle = Uni.I18n.translate('devicegroup.wizard.edit.confirmationTitle', 'MDC', "Save device group '{0}'?", [deviceGroupName]);
        } else {
            confirmationTitle = isDynamic
                ? Uni.I18n.translate('devicegroup.wizard.add.dynamic.confirmationTitle', 'MDC', "Add dynamic device group '{0}'?", [deviceGroupName])
                : Uni.I18n.translate('devicegroup.wizard.add.static.confirmationTitle', 'MDC', "Add static device group '{0}'?", [deviceGroupName]);
        }

        Ext.suspendLayouts();
        step3.update(Uni.I18n.translate('devicegroup.wizard.progress.countingDevices', 'MDC', 'Counting of number of devices. Please wait...'));
        progressbar.show();
        progressbar.wait({
            interval: 50,
            increment: 20
        });
        Ext.resumeLayouts(true);

        setTimeout(function () {
            if (isDynamic) {
                confirmationMessage = Uni.I18n.translate('devicegroup.wizard.dynamic.confirmationMessage.criteriaNumber', 'MDC', 'Number of specified search criteria: {0}', [5])
                    + '<br>'
                    + Uni.I18n.translate('devicegroup.wizard.dynamic.confirmationMessage.devicesNumber', 'MDC', 'Current number of devices: {0}', [20100]);
            } else {
                confirmationMessage = Uni.I18n.translate('devicegroup.wizard.static.confirmationMessage', 'MDC', 'Number of devices: {0}', [1321]);
            }
            Ext.suspendLayouts();
            progressbar.hide();
            Ext.Array.each(buttons, function (button) {
                button.show();
            });
            navigationMenu.jumpBack = true;
            step3.update('<h3>' + confirmationTitle + '</h3><br>' + confirmationMessage);
            Ext.resumeLayouts(true);
        }, 2000);
    },

    prepareStep4: function (wizard, finishBtn) {
        var step4 =  wizard.down('device-group-wizard-step4'),
            progressbar = step4.down('progressbar');

        Ext.suspendLayouts();
        step4.update(wizard.isEdit
            ? Uni.I18n.translate('devicegroup.wizard.progress.modificationGroup', 'MDC', 'Device group modification. Please wait...')
            : Uni.I18n.translate('devicegroup.wizard.progress.creationGroup', 'MDC', 'Device group creation. Please wait...'));
        progressbar.show();
        progressbar.wait({
            interval: 50,
            increment: 20
        });
        Ext.resumeLayouts(true);

        wizard.updateRecord();
        //wizard.getRecord().save({
        //    backUrl: finishBtn.href,
        //    success: function (record) {
        //        var deviceGroupName = record.get('name');
        //
        //        Ext.suspendLayouts();
        //        finishBtn.show();
        //        progressbar.hide();
        //        step4.update(wizard.isEdit
        //            ? Uni.I18n.translate('devicegroup.wizard.save.success', 'MDC', "Device group '{0}' has been saved.", [deviceGroupName])
        //            : Uni.I18n.translate('devicegroup.wizard.add.success', 'MDC', "Device group '{0}' has been created.", [deviceGroupName]));
        //        Ext.resumeLayouts(true);
        //    }
        //});
        setTimeout(function () {
            var deviceGroupName = wizard.getRecord().get('name');

            Ext.suspendLayouts();
            finishBtn.show();
            progressbar.hide();
            step4.update(wizard.isEdit
                ? Uni.I18n.translate('devicegroup.wizard.save.success', 'MDC', "Device group '{0}' has been saved.", [deviceGroupName])
                : Uni.I18n.translate('devicegroup.wizard.add.success', 'MDC', "Device group '{0}' has been created.", [deviceGroupName]));
            Ext.resumeLayouts(true);
        }, 2000);
    }
});
