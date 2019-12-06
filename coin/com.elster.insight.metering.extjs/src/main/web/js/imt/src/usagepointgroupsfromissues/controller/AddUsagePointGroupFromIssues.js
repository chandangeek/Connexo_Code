/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroupsfromissues.controller.AddUsagePointGroupFromIssues', {

    extend: 'Ext.app.Controller',

    requires: [
        'Imt.usagepointgroupsfromissues.service.UsagePointDomainSearchService'
    ],

    views: [
        'Imt.usagepointgroupsfromissues.view.Browse'
    ],

    models: [
        'Imt.usagepointgroupsfromissues.model.UsagePointGroupFromIssues'
    ],

    stores: [
        'Imt.usagepointgroupsfromissues.store.BufferedIssues',
        'Imt.usagepointgroupsfromissues.store.SearchResults'
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
            ref: 'selectUsagePointsStep',
            selector: '#add-group-browse #add-group-wizard #select-usage-points-step'
        },
        {
            ref: 'usagePointsGrid',
            selector: '#add-group-browse #add-group-wizard #select-usage-points-step #usage-points-grid'
        },
        {
            ref: 'usagePointsGridValidationErrorPopup',
            selector: '#add-group-browse #add-group-wizard #select-usage-points-step #selection-criteria-error'
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

        me.usagePointDomainSearchService = Ext.create('Imt.usagepointgroupsfromissues.service.UsagePointDomainSearchService', {
            router: me.getController('Uni.controller.history.Router')
        });

        me.control({
            '#add-group-browse add-group-navigation-panel': {
                movetostep: me.changeBrowseStep
            },
            '#add-group-browse add-group-wizard button[navigationBtn=true]': {
                click: me.changeBrowseStep
            },
            '#add-group-browse select-usage-points-step search-criteria-selector menu menucheckitem': {
                checkchange: function (field, checked) {
                    checked ? me.usagePointDomainSearchService.addProperty(field.criteria) : me.usagePointDomainSearchService.removeProperty(field.criteria);
                }
            },
            '#add-group-browse select-usage-points-step button[action=search]': {
                click: me.applyUsagePointsGridFilters
            },
            '#add-group-browse select-usage-points-step button[action=clearFilters]': {
                click: {
                    fn: me.usagePointDomainSearchService.clearFilters,
                    scope: me.usagePointDomainSearchService
                }
            }
        });
    },

    showUsagePointGroupWizard: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            issuesStore = me.getStore('Imt.usagepointgroupsfromissues.store.BufferedIssues'),
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
            if (name === 'usagePointGroup') {
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

        me.usagePointDomainSearchService.getSearchDomainsStore().load();

        widget = Ext.widget('add-group-browse', {
            itemId: 'add-group-browse',
            router: router,
            returnLink: router.getRoute('workspace/issues').buildUrl(),
            usagePointDomainSearchService: me.usagePointDomainSearchService
        });

        grid = widget.down('#select-issues-step').down('#prefiltered-issues-grid');
        grid.reconfigure(issuesStore);
        grid.filterParams = Ext.clone(filter);

        me.getApplication().fireEvent('changecontentevent', widget);

        widget.down('#add-group-wizard').loadRecord(Ext.create('Imt.usagepointgroupsfromissues.model.UsagePointGroupFromIssues'));

        issuesStore.data.clear();
        issuesStore.clearFilter(!!filter.length);
        issuesStore.filter(filter);

        me.prepareGeneralAttributesStep(me);
    },

    applyUsagePointsGridFilters: function () {
        this.usagePointDomainSearchService.applyFilters.apply(this.usagePointDomainSearchService, arguments);
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
                validationFunction = me.validateSelectUsagePointsStep;
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
                preparationFunction = me.prepareSelectUsagePointsStep;
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

        Ext.getStore('Imt.usagepointgroups.store.UsagePointGroups').load({
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
                    groupNameField.markInvalid(Uni.I18n.translate('general.name.shouldBeUnique', 'IMT', 'Name must be unique'));
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
            selectUsagePointsStep = controller.getSelectUsagePointsStep();

        if (Ext.isEmpty(issuesGrid.view.getSelectionModel().getSelection())) {
            issuesGridErrorPanel.show();
            selectIssuesStepErrorPanel.setVisible(true);
            return;
        }

        selectIssuesStepErrorPanel.setVisible(false);
        issuesGridErrorPanel.hide();

        selectUsagePointsStep.isGridDataLoaded = false;

        preparationFunctionForNextStep(controller, nextStepIndex, navMenuChangeStepCallBack);
    },

    prepareSelectUsagePointsStep: function (controller, nextStepIndex, navMenuChangeStepCallBack) {
        var buttons = controller.getGroupWizard().getDockedComponent('add-group-wizard-buttons'),
            nextBtn = buttons.down('[action=step-next]'),
            backBtn = buttons.down('[action=step-back]'),
            confirmBtn = buttons.down('[action=confirm]'),
            finishBtn = buttons.down('[action=finish]'),
            cancelBtn = buttons.down('[action=cancel]'),
            selectUsagePointsStep = controller.getSelectUsagePointsStep(),
            usagePointsGrid = controller.getUsagePointsGrid(),
            issuesGrid = controller.getIssuesGrid(),
            usagePointsGridService = selectUsagePointsStep.getService(),
            gridStore = Ext.getStore('Imt.usagepointgroupsfromissues.store.SearchResults'),
            selectedIssues = issuesGrid.getSelectionModel().getSelection(),
            usagePointsSet = new Set([]);

        nextBtn.show();
        backBtn.show();
        backBtn.enable();
        confirmBtn.hide();
        finishBtn.hide();
        cancelBtn.show();

        if (!selectUsagePointsStep.isGridDataLoaded) {

            Ext.each(selectedIssues, function (issue) {
                usagePointsSet.add(issue.raw.usagePointInfo.mRID);
            });

            var searchFieldsOnLoadListener = null;
            var usagePointGridState = {
                domain: 'com.elster.jupiter.metering.UsagePoint',
                filters: [
                    {
                        property: 'mRID',
                        value: [{
                            criteria: Array.from(usagePointsSet),
                            operator: 'IN'
                        }]
                    },
                ]
            };

            var domainsStore = usagePointsGridService.getSearchDomainsStore();

            usagePointsGridService.setSearchResultsStore(gridStore);

            var destroyListener = function () {
                if (searchFieldsOnLoadListener) {
                    searchFieldsOnLoadListener.destroy();
                }
            };
            destroyListener();

            searchFieldsOnLoadListener = usagePointsGridService.getSearchFieldsStore().on('load', function (store, items) {
                usagePointsGrid.getStore().model.setFields(items.map(function (field) {
                    return usagePointsGridService.createFieldDefinitionFromModel(field);
                }));
                usagePointsGrid.down('uni-search-column-picker').setColumns(items.map(function (field) {
                    return usagePointsGridService.createColumnDefinitionFromModel(field);
                }));
            }, usagePointsGrid, {
                destroyable: true
            });

            usagePointsGrid.on('destroy', destroyListener);

            usagePointsGridService.excludedCriteria = undefined;

            usagePointsGrid.setVisible(false);

            if (domainsStore.isLoading()) {
                usagePointsGrid.setVisible(true);
            } else {
                usagePointsGridService.applyState(usagePointGridState, function () {
                    usagePointsGrid.setVisible(true);
                });
            }

            usagePointsGrid.getStore().on('load', function () {
                usagePointsGrid.onClickCheckAllButton();
            });

            selectUsagePointsStep.isGridDataLoaded = true;
        }

        navMenuChangeStepCallBack(controller, nextStepIndex);
    },

    validateSelectUsagePointsStep: function (controller, nextStepIndex, navMenuChangeStepCallBack) {
        var wizard = controller.getGroupWizard(),
            usagePointsGrid = controller.getUsagePointsGrid(),
            usagePointsGridErrorPanel = controller.getUsagePointsGridValidationErrorPopup(),
            preparationFunctionForNextStep = controller.getPreparationFunctionForStep(nextStepIndex);

        if (usagePointsGrid.getStore().data.store.totalCount === 0) {
            usagePointsGridErrorPanel.show();
            return;
        }

        if (Ext.isEmpty(usagePointsGrid.view.getSelectionModel().getSelection())) {
            usagePointsGridErrorPanel.show();
            return;
        }

        usagePointsGridErrorPanel.hide();
        wizard.updateRecordWithItems(usagePointsGrid);

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
            usagePointGroupName = record.get('name'),
            devices = record.get('usagePoints'),
            confirmationTitle = Uni.I18n.translate('usagepointgroupfromissues.wizard.step.confirmation.text.confirmation', 'IMT', 'Add static usage point group \'{0}\' ?', usagePointGroupName),
            confirmationMessage = Uni.I18n.translate('usagepointgroupfromissues.wizard.step.confirmation.text.numberOfUsagePoints', 'IMT', 'Number of usage points: {0}', devices.length);

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
            usagePointGroupName = record.get('name');

        navigationMenu.jumpBack = false;
        nextBtn.hide();
        backBtn.hide();
        confirmBtn.hide();
        finishBtn.show();
        cancelBtn.hide();

        wizard.getRecord().save({
            backUrl: finishBtn.href,
            success: function () {
                statusStep.update(Uni.I18n.translate('usagepointgroupfromissues.wizard.step.status.text.success', 'IMT', 'Static usage point group {0} has been created.', usagePointGroupName));
            },
            failure: function () {
                var resultMessage = '<h3 style="color: #eb5642">' + Uni.I18n.translate('usagepointgroupfromissues.wizard.step.status.text.failure', 'IMT', 'Failed to add static usage point group {0}.', usagePointGroupName);
                statusStep.update(resultMessage);
            }
        });

        navMenuChangeStepCallBack(controller, nextStepIndex);
    }

});