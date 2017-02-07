/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.AddDeviceGroupAction', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.util.Filters',
        'Mdc.service.DeviceGroupSearch'
    ],

    views: [
        'Mdc.view.setup.devicegroup.Browse'
    ],

    stores: [
        'Mdc.store.DeviceGroups',
        'Mdc.store.DevicesOfDeviceGroupWithoutPagination',
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
        },
        {
            ref: 'filterPanel',
            selector: '#add-devicegroup-browse #device-group-filter'
        }
    ],

    filterObjectParam: 'filter',
    lastRequest: undefined,
    searchFieldsOnLoadListener: null,

    init: function () {
        var me = this;

        me.service = Ext.create('Mdc.service.DeviceGroupSearch', {
            router: me.getController('Uni.controller.history.Router')
        });

        me.control({
            '#add-devicegroup-browse #staticDynamicRadioButton': {
                change: function(f, val) {
                    var step2 = me.getAddDeviceGroupWizard().down('device-group-wizard-step2');

                    this.isDynamic = val.dynamic;
                    step2.isPrepared = false;
                }
            },
            '#add-devicegroup-browse adddevicegroup-wizard button[navigationBtn=true]': {
                click: me.moveTo
            },
            '#add-devicegroup-browse devicegroup-add-navigation': {
                movetostep: me.moveTo
            },
            '#add-devicegroup-browse search-criteria-selector menu menucheckitem': {
                checkchange: function (field, checked) {
                    checked
                        ? me.service.addProperty(field.criteria)
                        : me.service.removeProperty(field.criteria);
                }
            },
            '#add-devicegroup-browse button[action=search]': {
                click: me.applyFilters
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
                        me.isDynamic = isDynamic = record.get('dynamic');
                        me.state = {
                            domain: 'com.energyict.mdc.device.data.Device',
                            filters: isDynamic
                                ? Ext.decode(record.get('filter'), true)
                                : [{
                                property: 'name',
                                value: [{
                                    criteria: '*',
                                    operator: '=='
                                }]
                            }]
                        };
                        me.getApplication().fireEvent('loadDeviceGroup', record);
                        Ext.suspendLayouts();
                        widget.down('devicegroup-add-navigation').setTitle(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", [record.get('name')]));
                        widget.down('adddevicegroup-wizard').loadRecord(record);
                        Ext.resumeLayouts(true);
                        if (!isDynamic) {
                            devices = me.getStore('Mdc.store.DevicesOfDeviceGroupWithoutPagination');
                            devices.getProxy().setUrl(deviceGroupId);
                            devices.load(function (records) {
                                var staticGrid = widget.down('static-group-devices-grid'),
                                    selectionGroupType = {};

                                mainView.setLoading(false);
                                Ext.suspendLayouts();
                                widget.down('static-group-devices-grid').setDevices(records);
                                selectionGroupType[staticGrid.radioGroupName] = staticGrid.selectedInputValue;
                                staticGrid.getSelectionGroupType().setValue(selectionGroupType);
                                Ext.resumeLayouts(true);
                            });
                        } else {
                            mainView.setLoading(false);
                        }
                    }
                },
                failure: function () {
                    mainView.setLoading(false);
                }
            });
        } else {
            widget.down('adddevicegroup-wizard').loadRecord(Ext.create(deviceGroupModelName));
            me.state = {
                domain: 'com.energyict.mdc.device.data.Device',
                filters: []
            }
        }
        me.service.on('searchResultsBeforeLoad', me.availableClearAll, me);
        widget.on('destroy', function () {
            me.service.un('searchResultsBeforeLoad', me.availableClearAll, me);
        }, me)
    },

    moveTo: function (button) {
        var me = this,
            wizardLayout = me.getAddDeviceGroupWizard().getLayout(),
            currentStep = wizardLayout.getActiveItem().navigationIndex,
            direction,
            nextStep,
            changeStep = function () {
                Ext.suspendLayouts();
                me.prepareNextStep(nextStep);
                wizardLayout.setActiveItem(nextStep - 1);
                me.getNavigationMenu().moveToStep(nextStep);
                Ext.resumeLayouts(true);
            };

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

        if (direction > 0) {
            me.validateCurrentStep(currentStep, changeStep);
        } else {
            changeStep();
        }
    },

    validateCurrentStep: function (stepNumber, callback) {
        var me = this,
            doCallback = function () {
                if (Ext.isFunction(callback)) {
                    callback();
                }
            };

        switch (stepNumber) {
            case 1:
                me.validateStep1(function() {
                    doCallback();
                    me.prepareStep2();
                });
                break;
            case 2:
                if (me.validateStep2()) {
                    doCallback();
                }
                break;
            default:
                doCallback();
        }
    },

    validateStep1: function (callback) {
        var me = this,
            wizard = me.getAddDeviceGroupWizard(),
            group = wizard.getRecord(),
            step1ErrorMsg = me.getStep1FormErrorMessage(),
            nameField = me.getNameTextField(),
            name = nameField.getValue();

        if (!nameField.validate()) {
            step1ErrorMsg.show();
        } else if (name !== group.get('name')) {
            wizard.setLoading();
            me.getStore('Mdc.store.DeviceGroups').load({
                params: {
                    filter: Ext.encode([{
                        property: 'name',
                        value: name
                    }])
                },
                callback: function (records) {
                    wizard.setLoading(false);
                    if (!records.length) {
                        step1ErrorMsg.hide();
                        callback();
                    } else {
                        Ext.suspendLayouts();
                        step1ErrorMsg.show();
                        nameField.markInvalid(Uni.I18n.translate('general.name.shouldBeUnique', 'MDC', 'Name must be unique'));
                        Ext.resumeLayouts(true);
                    }
                }
            });
        } else {
            step1ErrorMsg.hide();
            callback();
        }
    },

    validateStep2: function () {
        var me = this,
            wizard = me.getAddDeviceGroupWizard(),
            record = Ext.clone(wizard.getRecord()),
            valid = true,
            isDynamic;

        wizard.updateRecord(record);
        isDynamic = record.get('dynamic');

        if (isDynamic) {
            valid = !!me.countNumberOfSearchCriteria();
            Ext.suspendLayouts();
            me.getStep2FormErrorMessage().setVisible(!valid);
            wizard.down('#selection-criteria-error').setVisible(!valid);
            Ext.resumeLayouts(true);
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
                wizard.down('#selection-criteria-error').hide();
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
                me.prepareStep4(wizard, finishBtn, navigationMenu);
                break;
        }
    },

    prepareStep2: function () {
        var me = this,
            wizard = me.getAddDeviceGroupWizard(),
            step2 = wizard.down('device-group-wizard-step2'),
            searchBtn = step2.down('#search-button'),
            domainsStore = me.service.getSearchDomainsStore(),
            staticGrid,
            isDynamic = me.isDynamic,
            selectionGroupType,
            devices = me.getStore('Mdc.store.DevicesOfDeviceGroupWithoutPagination'),
            store = me.getStore(isDynamic ? 'Mdc.store.DynamicGroupDevices' : 'Mdc.store.StaticGroupDevices');

        if (step2.isPrepared) {
            return
        }

        step2.getLayout().setActiveItem(isDynamic ? 1 : 0);
        me.service.setSearchResultsStore(store);
        me.setColumnPicker(isDynamic);

        if (!isDynamic) {
            searchBtn.setText(Uni.I18n.translate('general.apply', 'MDC', 'Apply'));
            me.service.excludedCriteria = undefined;
            staticGrid = step2.down('static-group-devices-grid');
            staticGrid.setVisible(false);
            if(wizard.isEdit) {
                staticGrid.setLoading(true);
            }
            if(!devices.getRange().length){
                selectionGroupType = {};
                staticGrid.getSelectionModel().deselectAll(true); // fix the ExtJS error: "getById called for ID that is not present in local cache"
                staticGrid.setDevices([]);
                staticGrid.getStore().data.clear();
                selectionGroupType[staticGrid.radioGroupName] = staticGrid.allInputValue;
                staticGrid.getSelectionGroupType().setValue(selectionGroupType);
            }
        } else {
            searchBtn.setText(Uni.I18n.translate('general.preview', 'MDC', 'Preview'));
            staticGrid = step2.down('dynamic-group-devices-grid');
            staticGrid.down('pagingtoolbartop').resetPaging();
            staticGrid.down('pagingtoolbarbottom').resetPaging();
        }
        if (domainsStore.isLoading()) {

            domainsStore.on('load', function () {
                if(!isDynamic) {
                    staticGrid.setVisible(true);
                    if(wizard.isEdit) {
                        staticGrid.setLoading(true);
                        store.on('load', function () {
                            staticGrid.setLoading(false);
                        })
                    }
                }
                me.service.applyState(me.state, function(){
                });

            }, me, {single: true});
        } else {
            me.service.applyState(me.state, function(){
                if(!isDynamic){
                    staticGrid.setVisible(true);
                    if(wizard.isEdit) {
                        staticGrid.setLoading(true);
                        store.on('load', function () {
                            staticGrid.setLoading(false);
                        })
                    }
                }
            });
        }
        step2.isPrepared = true;
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
        var me = this,
            step3 = wizard.down('device-group-wizard-step3'),
            progressbar = step3.down('progressbar'),
            record = Ext.clone(wizard.getRecord()),
            deviceGroupName,
            isDynamic,
            devices,
            confirmationTitle,
            confirmationMessage,
            showConfirmationMsg = function (numberOfDevices, numberOfSearchCriteria) {
                if (isDynamic) {
                    confirmationMessage = Uni.I18n.translate('devicegroup.wizard.dynamic.confirmationMessage.criteriaNumber', 'MDC', 'Number of specified search criteria: {0}', [numberOfSearchCriteria])
                        + '<br>'
                        + Uni.I18n.translate('devicegroup.wizard.dynamic.confirmationMessage.devicesNumber', 'MDC', 'Current number of devices: {0}', [numberOfDevices]);
                } else {
                    confirmationMessage = Uni.I18n.translate('devicegroup.wizard.static.confirmationMessage', 'MDC', 'Number of devices: {0}', [numberOfDevices]);
                }
                Ext.suspendLayouts();
                progressbar.hide();
                Ext.Array.each(buttons, function (button) {
                    button.show();
                });
                navigationMenu.jumpBack = true;
                step3.update('<h3>' + confirmationTitle + '</h3><br>' + confirmationMessage);
                Ext.resumeLayouts(true);
            };

        wizard.updateRecord(record);
        deviceGroupName = record.get('name');
        isDynamic = record.get('dynamic');
        devices = record.get('devices');

        if (wizard.isEdit) {
            confirmationTitle = Uni.I18n.translate('devicegroup.wizard.edit.confirmationTitle', 'MDC', "Save device group '{0}'?", [deviceGroupName]);
        } else {
            confirmationTitle = isDynamic
                ? Uni.I18n.translate('devicegroup.wizard.add.dynamic.confirmationTitle', 'MDC', "Add dynamic device group '{0}'?", [deviceGroupName])
                : Uni.I18n.translate('devicegroup.wizard.add.static.confirmationTitle', 'MDC', "Add static device group '{0}'?", [deviceGroupName]);
        }

        if (!isDynamic && devices) {
            showConfirmationMsg(devices.length);
        } else {
            Ext.suspendLayouts();
            step3.update(Uni.I18n.translate('devicegroup.wizard.progress.countingDevices', 'MDC', 'Counting of number of devices. Please wait...'));
            progressbar.show();
            progressbar.wait({
                interval: 50,
                increment: 20
            });
            Ext.resumeLayouts(true);
            record.getNumberOfSearchResults(function (options, success, response) {
                var responseResult,
                    numberOfSearchCriteria;

                if (success && response && response.responseText) {
                    responseResult = Ext.decode(response.responseText, true);
                    if (responseResult) {
                        if (isDynamic) {
                            numberOfSearchCriteria = me.countNumberOfSearchCriteria();
                        }
                        showConfirmationMsg(responseResult.numberOfSearchResults, numberOfSearchCriteria);
                    }
                }
            });
        }
    },

    countNumberOfSearchCriteria: function () {
        var me = this;

        return me.service.getFilters().length;
    },

    prepareStep4: function (wizard, finishBtn, navigationMenu) {
        var step4 = wizard.down('device-group-wizard-step4'),
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
        wizard.getRecord().save({
            backUrl: finishBtn.href,
            success: function (record) {
                var deviceGroupName = record.get('name');

                Ext.suspendLayouts();
                finishBtn.show();
                progressbar.hide();
                step4.update(wizard.isEdit
                    ? Uni.I18n.translate('devicegroup.wizard.save.success', 'MDC', "Device group '{0}' has been saved.", [deviceGroupName])
                    : Uni.I18n.translate('devicegroup.wizard.add.success', 'MDC', "Device group '{0}' has been created.", [deviceGroupName]));
                Ext.resumeLayouts(true);
            },
            failure: function (record, response) {
                var deviceGroupName = record.get('name'),
                    responseText = Ext.decode(response.response.responseText, true),
                    reasons = '';

                if (responseText && Ext.isArray(responseText.errors)) {
                    Ext.Array.each(responseText.errors, function (error, index) {
                        reasons += '<br>-' + error.msg;
                    })
                }
                Ext.suspendLayouts();
                navigationMenu.updateItemCls(4, true);
                finishBtn.setUI('remove');
                finishBtn.show();
                progressbar.hide();
                step4.update('<h3 style="color: #eb5642">'
                + (wizard.isEdit
                    ? Uni.I18n.translate('devicegroup.wizard.save.failure', 'MDC', "Failed to save device group '{0}'.", [deviceGroupName])
                    : Uni.I18n.translate('devicegroup.wizard.add.failure', 'MDC', "Failed to add device group '{0}'.", [deviceGroupName]))
                + '</h3>'
                + reasons);
                Ext.resumeLayouts(true);
            }
        });
    },

    applyFilters: function () {
        var me = this,
            wizard = me.getAddDeviceGroupWizard(),
            staticGrid = wizard.down('static-group-devices-grid'),
            dynamicGrid = wizard.down('dynamic-group-devices-grid'),
            record = Ext.clone(wizard.getRecord()),
            isDynamic;

        wizard.updateRecord(record);
        isDynamic = record.get('dynamic');
        if (isDynamic) {
            dynamicGrid.down('pagingtoolbartop').resetPaging();
            dynamicGrid.down('pagingtoolbarbottom').resetPaging();
        } else {
            staticGrid.getSelectionModel().deselectAll(true); // fix the ExtJS error: "getById called for ID that is not present in local cache"
        }
        me.service.applyFilters.apply(me.service, arguments);
    },

    availableClearAll: function () {
        var me = this,
            filterPanel = me.getFilterPanel(),
            filters = me.service.getFilters();

        filterPanel.down('[action=clearFilters]').setDisabled(!(filters && filters.length));
    }
});