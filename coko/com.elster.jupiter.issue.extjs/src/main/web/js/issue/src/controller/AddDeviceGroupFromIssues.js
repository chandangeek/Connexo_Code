/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.controller.AddDeviceGroupFromIssues', {

    extend: 'Ext.app.Controller',

    requires: [
        'Isu.service.DeviceDomainSearchService'
    ],

    views: [
        'Isu.view.devicegroupfromissues.Browse'
    ],

    models: [
        'Isu.model.DeviceGroupFromIssues'
    ],

    stores: [
        'Isu.store.BufferedIssues',
        'Isu.store.SearchResults'
    ],

    refs: [
        {
            ref: 'groupBrowse',
            selector: '#add-group-browse'
        },
        {
            ref: 'groupWizard',
            selector: '#add-group-browse #add-group-wizard'
        },
        {
            ref: 'navigationMenu',
            selector: '#add-group-browse #add-group-navigation-panel'
        },
        {
            ref: 'generalAttributesStepErrorPopup',
            selector: '#add-group-browse #add-group-wizard #general-attributes-step #step-error-popup'
        },
        {
            ref: 'groupNameTextField',
            selector: '#add-group-browse #add-group-wizard #general-attributes-step #group-name'
        },
        {
            ref: 'selectIssuesStep',
            selector: '#add-group-browse #add-group-wizard #select-issues-step'
        },
        {
            ref: 'selectIssuesStepErrorPopup',
            selector: '#add-group-browse #add-group-wizard #select-issues-step #step-errors'
        },
        {
            ref: 'issuesGrid',
            selector: '#add-group-browse #add-group-wizard #select-issues-step #prefiltered-issues-grid'
        },
        {
            ref: 'issuesGridValidationErrorPopup',
            selector: '#add-group-browse #add-group-wizard #select-issues-step #issues-grid-validation-error'
        },
        {
            ref: 'selectDevicesStep',
            selector: '#add-group-browse #add-group-wizard #select-devices-step'
        },
        {
            ref: 'devicesGrid',
            selector: '#add-group-browse #add-group-wizard #select-devices-step #devices-grid'
        },
        {
            ref: 'devicesGridValidationErrorPopup',
            selector: '#add-group-browse #add-group-wizard #select-devices-step #selection-criteria-error'
        },
        {
            ref: 'confirmationStep',
            selector: '#add-group-browse #add-group-wizard #confirmation-step'
        },
        {
            ref: 'statusStep',
            selector: '#add-group-browse #add-group-wizard #status-step'
        }
    ],

    deviceGridSearchFieldsOnLoadListener: null,

    init: function () {
        var me = this;

        me.deviceDomainSearchService = Ext.create('Isu.service.DeviceDomainSearchService', {
            router: me.getController('Uni.controller.history.Router')
        });

        me.control({
            '#add-group-browse add-group-navigation-panel': {
                movetostep: me.changeBrowseStep
            },
            '#add-group-browse add-group-wizard button[navigationBtn=true]': {
                click: me.changeBrowseStep
            },
            '#add-group-browse #select-devices-step search-criteria-selector menu menucheckitem': {
                checkchange: function (field, checked) {
                    checked ? me.deviceDomainSearchService.addProperty(field.criteria) : me.deviceDomainSearchService.removeProperty(field.criteria);
                }
            },
            '#add-group-browse #select-devices-step button[action=search]': {
                click: me.applyDeviceGridFilters
            },
            '#add-group-browse #select-devices-step button[action=clearFilters]': {
                click: {
                    fn: me.deviceDomainSearchService.clearFilters,
                    scope: me.deviceDomainSearchService
                }
            }
        });
    },

    showDeviceGroupWizard: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            issuesStore = me.getStore('Isu.store.BufferedIssues'),
            issuesStoreProxy = issuesStore.getProxy(),
            queryStringValues = Uni.util.QueryString.getQueryStringValues(false),
            filter = [],
            widget,
            grid;

        issuesStoreProxy.extraParams = {};
        if (queryStringValues.sort) {
            issuesStoreProxy.setExtraParam('sort', queryStringValues.sort);
            delete queryStringValues.sort;
        }
        if (Ext.isDefined(queryStringValues.groupingType) && Ext.isDefined(queryStringValues.groupingValue) && Ext.isEmpty(queryStringValues[queryStringValues.groupingType])) {
            filter.push({
                property: queryStringValues.groupingType,
                value: queryStringValues.groupingValue
            });
        }
        delete queryStringValues.groupingType;
        delete queryStringValues.groupingValue;

        Ext.iterate(queryStringValues, function (name, value) {
            if (name === 'deviceGroup') {
                if (!Array.isArray(value)) {
                    value = Array.of(parseInt(value));
                }
                var array = value.map(function (groupId) {
                    return parseInt(groupId, 10);
                });
                filter.push({
                    property: name,
                    value: array
                });
            } else {
                filter.push({
                    property: name,
                    value: value
                });
            }
        });

        filter.push({
            property: 'application',
            value: Uni.util.Application.getAppName() === 'MdmApp' ? 'INS' :
                Uni.util.Application.getAppName() === 'MultiSense' ? 'MultiSense' : ''
        });

        me.deviceDomainSearchService.getSearchDomainsStore().load();

        widget = Ext.widget('add-group-browse', {
            itemId: 'add-group-browse',
            router: router,
            returnLink: router.getRoute('workspace/issues').buildUrl(),
            deviceDomainSearchService: me.deviceDomainSearchService
        });

        grid = widget.down('#select-issues-step').down('#prefiltered-issues-grid');
        grid.reconfigure(issuesStore);
        grid.filterParams = Ext.clone(filter);

        me.getApplication().fireEvent('changecontentevent', widget);

        widget.down('#add-group-wizard').loadRecord(Ext.create('Isu.model.DeviceGroupFromIssues'));

        issuesStore.data.clear();
        issuesStore.clearFilter(!!filter.length);
        issuesStore.filter(filter);

        me.prepareGeneralAttributesStep(me);
    },

    applyDeviceGridFilters: function () {
        this.deviceDomainSearchService.applyFilters.apply(this.deviceDomainSearchService, arguments);
    },

    availableClearAll: function () {
        this.getSearchOverview().down('[action=clearFilters]').setDisabled(!(filters && filters.length));
    },

    navMenuChangeStepCallBack: function (controller, stepIndex) {
        controller.getGroupWizard().getLayout().setActiveItem(stepIndex - 1);
        controller.getNavigationMenu().moveToStep(stepIndex);
    },

    changeBrowseStep: function (button) {
        var me = this,
            direction = (button.action === 'step-next' || button.action === 'confirm') ? 1 : -1,
            currentStepIndex = me.getGroupWizard().getLayout().getActiveItem().navigationIndex,
            nextStepIndex = currentStepIndex + direction;

        Ext.suspendLayouts();
        me.changeWizardStep(currentStepIndex, nextStepIndex, direction, me.navMenuChangeStepCallBack);
        Ext.resumeLayouts(true);
    },

    changeWizardStep: function (currentStepIndex, nextStepIndex, direction, navMenuChangeStepCallBack) {
        var me = this;

        if (direction > 0) {
            var validationFunctionOfCurrentStep = me.getValidationFunctionForStep(currentStepIndex);
            validationFunctionOfCurrentStep(me, nextStepIndex, navMenuChangeStepCallBack);
        } else {
            var preparationFunctionForNextStep = me.getPreparationFunctionForStep(nextStepIndex);
            preparationFunctionForNextStep(me, nextStepIndex, navMenuChangeStepCallBack);
        }
    },

    getValidationFunctionForStep: function (currentStepIndex) {
        var me = this,
            validationFunction = null;

        switch (currentStepIndex) {
            case 1:
                validationFunction = me.validateGeneralAttributesStep;
                break;
            case 2:
                validationFunction = me.validateSelectIssuesStep;
                break;
            case 3:
                validationFunction = me.validateSelectDevicesStep;
                break;
            case 4:
                validationFunction = me.validateConfirmationStep;
                break;
        }

        return validationFunction;
    },

    getPreparationFunctionForStep: function (stepIndex) {
        var me = this,
            preparationFunction = null;

        switch (stepIndex) {
            case 1:
                preparationFunction = me.prepareGeneralAttributesStep;
                break;
            case 2:
                preparationFunction = me.prepareSelectIssuesStep;
                break;
            case 3:
                preparationFunction = me.prepareSelectDevicesStep;
                break;
            case 4:
                preparationFunction = me.prepareConfirmationStep;
                break;
            case 5:
                preparationFunction = me.prepareStatusStep;
                break;
        }

        return preparationFunction;
    },

    prepareGeneralAttributesStep: function (controller, nextStepIndex, navMenuChangeStepCallBack) {
        var wizard = controller.getGroupWizard(),
            buttons = wizard.getDockedComponent('add-group-wizard-buttons'),
            nextBtn = buttons.down('[action=step-next]'),
            backBtn = buttons.down('[action=step-back]'),
            confirmBtn = buttons.down('[action=confirm]'),
            finishBtn = buttons.down('[action=finish]'),
            cancelBtn = buttons.down('[action=cancel]');

        nextBtn.show();
        backBtn.hide();
        backBtn.disable();
        confirmBtn.hide();
        finishBtn.hide();
        cancelBtn.show();

        if (navMenuChangeStepCallBack) {
            navMenuChangeStepCallBack(controller, nextStepIndex);
        }
    },

    validateGeneralAttributesStep: function (controller, nextStepIndex, navMenuChangeStepCallBack) {
        var wizard = controller.getGroupWizard(),
            errorPopup = controller.getGeneralAttributesStepErrorPopup(),
            groupNameField = controller.getGroupNameTextField(),
            groupNameValue = groupNameField.getValue(),
            preparationFunctionForNextStep = controller.getPreparationFunctionForStep(nextStepIndex),
            selectIssuesStep = controller.getSelectIssuesStep();

        if (!groupNameField.validate()) {
            errorPopup.show();
            return false;
        }

        wizard.setLoading(true);

        Ext.getStore('Mdc.store.DeviceGroups').load({
            scope: this,
            params: {
                filter: Ext.encode([{
                    property: 'name',
                    value: groupNameValue
                }])
            },
            callback: function (records) {
                wizard.setLoading(false);
                if (records.length) {
                    Ext.suspendLayouts(true);
                    errorPopup.show();
                    groupNameField.markInvalid(Uni.I18n.translate('general.name.shouldBeUnique', 'MDC', 'Name must be unique'));
                    Ext.resumeLayouts(true);
                } else {
                    errorPopup.hide();
                    wizard.updateRecordWithGroupName();
                    selectIssuesStep.isGridDataLoaded = false;
                    preparationFunctionForNextStep(controller, nextStepIndex, navMenuChangeStepCallBack);
                }
            }
        });
    },

    prepareSelectIssuesStep: function (controller, nextStepIndex, navMenuChangeStepCallBack) {
        var buttons = controller.getGroupWizard().getDockedComponent('add-group-wizard-buttons'),
            nextBtn = buttons.down('[action=step-next]'),
            backBtn = buttons.down('[action=step-back]'),
            confirmBtn = buttons.down('[action=confirm]'),
            finishBtn = buttons.down('[action=finish]'),
            cancelBtn = buttons.down('[action=cancel]'),
            issueGrid = controller.getIssuesGrid(),
            selectIssuesStep = controller.getSelectIssuesStep();

        nextBtn.show();
        backBtn.show();
        backBtn.enable();
        confirmBtn.hide();
        finishBtn.hide();
        cancelBtn.show();

        if (!selectIssuesStep.isGridDataLoaded) {
            issueGrid.onClickCheckAllButton();
        }

        selectIssuesStep.isGridDataLoaded = true;

        navMenuChangeStepCallBack(controller, nextStepIndex);
    },

    validateSelectIssuesStep: function (controller, nextStepIndex, navMenuChangeStepCallBack) {
        var issuesGrid = controller.getIssuesGrid(),
            selectIssuesStepErrorPanel = controller.getSelectIssuesStepErrorPopup(),
            issuesGridErrorPanel = controller.getIssuesGridValidationErrorPopup(),
            preparationFunctionForNextStep = controller.getPreparationFunctionForStep(nextStepIndex),
            selectDevicesStep = controller.getSelectDevicesStep();

        if (Ext.isEmpty(issuesGrid.view.getSelectionModel().getSelection())) {
            issuesGridErrorPanel.show();
            selectIssuesStepErrorPanel.setVisible(true);
            return;
        }

        selectIssuesStepErrorPanel.setVisible(false);
        issuesGridErrorPanel.hide();

        selectDevicesStep.isGridDataLoaded = false;

        preparationFunctionForNextStep(controller, nextStepIndex, navMenuChangeStepCallBack);
    },

    prepareSelectDevicesStep: function (controller, nextStepIndex, navMenuChangeStepCallBack) {
        var buttons = controller.getGroupWizard().getDockedComponent('add-group-wizard-buttons'),
            nextBtn = buttons.down('[action=step-next]'),
            backBtn = buttons.down('[action=step-back]'),
            confirmBtn = buttons.down('[action=confirm]'),
            finishBtn = buttons.down('[action=finish]'),
            cancelBtn = buttons.down('[action=cancel]'),
            selectDevicesStep = controller.getSelectDevicesStep(),
            devicesGrid = controller.getDevicesGrid(),
            issuesGrid = controller.getIssuesGrid(),
            devicesGridService = selectDevicesStep.getService(),
            gridStore = Ext.getStore('Isu.store.SearchResults'),
            selectedIssues = issuesGrid.getSelectionModel().getSelection(),
            devicesSet = new Set([]);

        nextBtn.show();
        backBtn.show();
        backBtn.enable();
        confirmBtn.hide();
        finishBtn.hide();
        cancelBtn.show();

        if (!selectDevicesStep.isGridDataLoaded) {

            Ext.each(selectedIssues, function (issue) {
                devicesSet.add(issue.data.device.mRID);
            });

            var searchFieldsOnLoadListener = null;
            var deviceGridState = {
                domain: 'com.energyict.mdc.common.device.data.Device',
                filters: [
                    {
                        property: 'mRID',
                        value: [{
                            criteria: Array.from(devicesSet),
                            operator: 'IN'
                        }]
                    },
                ]
            };

            var domainsStore = devicesGridService.getSearchDomainsStore();

            devicesGridService.setSearchResultsStore(gridStore);

            var destroyListener = function () {
                if (searchFieldsOnLoadListener) {
                    searchFieldsOnLoadListener.destroy();
                }
            };
            destroyListener();

            searchFieldsOnLoadListener = devicesGridService.getSearchFieldsStore().on('load', function (store, items) {
                devicesGrid.getStore().model.setFields(items.map(function (field) {
                    return devicesGridService.createFieldDefinitionFromModel(field);
                }));
                devicesGrid.down('uni-search-column-picker').setColumns(items.map(function (field) {
                    return devicesGridService.createColumnDefinitionFromModel(field);
                }));
            }, devicesGrid, {
                destroyable: true
            });

            devicesGrid.on('destroy', destroyListener);

            devicesGridService.excludedCriteria = undefined;

            devicesGrid.setVisible(false);

            if (domainsStore.isLoading()) {
                devicesGrid.setVisible(true);
            } else {
                devicesGridService.applyState(deviceGridState, function () {
                    devicesGrid.setVisible(true);
                });
            }

            devicesGrid.getStore().on('load', function () {
                devicesGrid.onClickCheckAllButton();
            });

            selectDevicesStep.isGridDataLoaded = true;
        }

        navMenuChangeStepCallBack(controller, nextStepIndex);
    },

    validateSelectDevicesStep: function (controller, nextStepIndex, navMenuChangeStepCallBack) {
        var wizard = controller.getGroupWizard(),
            devicesGrid = controller.getDevicesGrid(),
            devicesGridErrorPanel = controller.getDevicesGridValidationErrorPopup(),
            preparationFunctionForNextStep = controller.getPreparationFunctionForStep(nextStepIndex);

        if (devicesGrid.getStore().data.store.totalCount === 0) {
            devicesGridErrorPanel.show();
            return;
        }

        if (Ext.isEmpty(devicesGrid.view.getSelectionModel().getSelection())) {
            devicesGridErrorPanel.show();
            return;
        }

        devicesGridErrorPanel.hide();
        wizard.updateRecordWithItems(devicesGrid);

        preparationFunctionForNextStep(controller, nextStepIndex, navMenuChangeStepCallBack);
    },

    prepareConfirmationStep: function (controller, nextStepIndex, navMenuChangeStepCallBack) {
        var buttons = controller.getGroupWizard().getDockedComponent('add-group-wizard-buttons'),
            nextBtn = buttons.down('[action=step-next]'),
            backBtn = buttons.down('[action=step-back]'),
            confirmBtn = buttons.down('[action=confirm]'),
            finishBtn = buttons.down('[action=finish]'),
            cancelBtn = buttons.down('[action=cancel]'),
            confirmationStep = controller.getConfirmationStep(),
            record = Ext.clone(controller.getGroupWizard().getRecord()),
            deviceGroupName = record.get('name'),
            devices = record.get('devices'),
            confirmationTitle = Uni.I18n.translate('devicegroupfromissues.wizard.step.confirmation.text.confirmation', 'ISU', 'Add static device group \'{0}\' ?', deviceGroupName),
            confirmationMessage = Uni.I18n.translate('devicegroupfromissues.wizard.step.confirmation.text.numberOfDevices', 'ISU', 'Number of devices: {0}', devices.length);

        confirmationStep.update('<h3>' + confirmationTitle + '</h3><br>' + confirmationMessage);

        nextBtn.hide();
        backBtn.show();
        backBtn.enable();
        confirmBtn.show();
        finishBtn.hide();
        cancelBtn.show();

        navMenuChangeStepCallBack(controller, nextStepIndex);
    },

    validateConfirmationStep: function (controller, nextStepIndex, navMenuChangeStepCallBack) {
        var preparationFunctionForNextStep = controller.getPreparationFunctionForStep(nextStepIndex);

        // NOOP: NOTHING TO VALIDATE

        preparationFunctionForNextStep(controller, nextStepIndex, navMenuChangeStepCallBack);
    },

    prepareStatusStep: function (controller, nextStepIndex, navMenuChangeStepCallBack) {
        var wizard = controller.getGroupWizard(),
            buttons = wizard.getDockedComponent('add-group-wizard-buttons'),
            nextBtn = buttons.down('[action=step-next]'),
            backBtn = buttons.down('[action=step-back]'),
            confirmBtn = buttons.down('[action=confirm]'),
            finishBtn = buttons.down('[action=finish]'),
            cancelBtn = buttons.down('[action=cancel]'),
            navigationMenu = controller.getNavigationMenu(),
            record = Ext.clone(wizard.getRecord()),
            statusStep = controller.getStatusStep(),
            deviceGroupName = record.get('name');

        navigationMenu.jumpBack = false;
        nextBtn.hide();
        backBtn.hide();
        confirmBtn.hide();
        finishBtn.show();
        cancelBtn.hide();

        wizard.getRecord().save({
            backUrl: finishBtn.href,
            success: function () {
                statusStep.update(Uni.I18n.translate('devicegroupfromissues.wizard.step.status.text.success', 'ISU', 'Static device group {0} has been created.', deviceGroupName));
            },
            failure: function () {
                var resultMessage = '<h3 style="color: #eb5642">' + Uni.I18n.translate('devicegroupfromissues.wizard.step.status.text.failure', 'ISU', 'Failed to add static device group {0}.', deviceGroupName);
                statusStep.update(resultMessage);
            }
        });

        navMenuChangeStepCallBack(controller, nextStepIndex);
    }

});