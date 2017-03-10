/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.controller.AddUsagePointGroupAction', {
    extend: 'Ext.app.Controller',

    requires: [        
        'Imt.service.UsagePointGroupSearch'
    ],

    views: [
        'Imt.usagepointgroups.view.Browse'
    ],

    stores: [
        'Imt.usagepointgroups.store.UsagePointGroups',
        'Imt.usagepointgroups.store.UsagePointsOfUsagePointGroupWithoutPagination',
        'Imt.usagepointgroups.store.StaticGroupUsagePoints',
        'Imt.usagepointgroups.store.DynamicGroupUsagePoints'
    ],

    models: [
        'Imt.usagepointgroups.model.UsagePointGroup'
    ],

    refs: [
        {
            ref: 'navigationMenu',
            selector: 'add-usagepointgroup-browse usagepointgroup-add-navigation'
        },
        {
            ref: 'addUsagePointGroupWizard',
            selector: 'add-usagepointgroup-browse addusagepointgroup-wizard'
        },
        {
            ref: 'nameTextField',
            selector: 'add-usagepointgroup-browse #usagepoint-group-name-field'
        },
        {
            ref: 'step1FormErrorMessage',
            selector: 'add-usagepointgroup-browse #step1-add-usagepointgroup-errors'
        },
        {
            ref: 'step2FormErrorMessage',
            selector: 'add-usagepointgroup-browse #step2-add-usagepointgroup-errors'
        },
        {
            ref: 'filterPanel',
            selector: 'add-usagepointgroup-browse #usagepoint-group-filter'
        }
    ],

    filterObjectParam: 'filter',
    lastRequest: undefined,
    searchFieldsOnLoadListener: null,
    usagePointGroupName: null,

    init: function () {
        var me = this;

        me.service = Ext.create('Imt.service.UsagePointGroupSearch', {
            router: me.getController('Uni.controller.history.Router')
        });

        me.control({
            'add-usagepointgroup-browse #staticDynamicRadioButton': {
                change: function (f, val) {
                    var step2 = this.getAddUsagePointGroupWizard().down('usagepoint-group-wizard-step2');

                    this.isDynamic = val.dynamic;
                    step2.isPrepared = false;
                }
            },
            'add-usagepointgroup-browse addusagepointgroup-wizard button[navigationBtn=true]': {
                click: me.moveTo
            },
            'add-usagepointgroup-browse usagepointgroup-add-navigation': {
                movetostep: me.moveTo
            },
            'add-usagepointgroup-browse search-criteria-selector menu menucheckitem': {
                checkchange: function (field, checked) {
                    checked
                        ? me.service.addProperty(field.criteria)
                        : me.service.removeProperty(field.criteria);
                }
            },
            'add-usagepointgroup-browse button[action=search]': {
                click: me.applyFilters
            },
            'add-usagepointgroup-browse button[action=clearFilters]': {
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
            usagePointGroupModelName = 'Imt.usagepointgroups.model.UsagePointGroup',
            usagePointGroupId = router.arguments.usagePointGroupId,
            widget = Ext.widget('add-usagepointgroup-browse', {
                router: router,
                service: me.service,
                isEdit: Ext.isDefined(usagePointGroupId),
                returnLink: router.queryParams.fromDetails === 'true'
                    ? router.getRoute('usagepoints/usagepointgroups/view').buildUrl()
                    : router.getRoute('usagepoints/usagepointgroups').buildUrl()
            });

        if (me.usagePointGroupName) {
            me.usagePointGroupName = null;
        }
        me.service.getSearchDomainsStore().load();
        me.getApplication().fireEvent('changecontentevent', widget);
        if (Ext.isDefined(usagePointGroupId)) {
            mainView.setLoading();
            me.getModel(usagePointGroupModelName).load(usagePointGroupId, {
                success: function (record) {
                    var isDynamic,
                        usagePoints;

                    if (widget.rendered) {
                        me.isDynamic = isDynamic = record.get('dynamic');
                        me.state = {
                            domain: 'com.elster.jupiter.metering.UsagePoint',
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
                        me.getApplication().fireEvent('loadUsagePointGroup', record);
                        Ext.suspendLayouts();
                        widget.down('usagepointgroup-add-navigation').setTitle(Uni.I18n.translate('general.editx', 'IMT', "Edit '{0}'", [record.get('name')]));
                        widget.down('addusagepointgroup-wizard').loadRecord(record);
                        Ext.resumeLayouts(true);
                        if (!isDynamic) {
                            usagePoints = me.getStore('Imt.usagepointgroups.store.UsagePointsOfUsagePointGroupWithoutPagination');
                            usagePoints.getProxy().setUrl(usagePointGroupId);
                            usagePoints.load(function (records) {
                                var staticGrid = widget.down('static-group-usagepoints-grid'),
                                    selectionGroupType = {};

                                mainView.setLoading(false);
                                Ext.suspendLayouts();
                                widget.down('static-group-usagepoints-grid').setUsagePoints(records);
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
            widget.down('addusagepointgroup-wizard').loadRecord(Ext.create(usagePointGroupModelName));
            me.state = {
                domain: 'com.elster.jupiter.metering.UsagePoint',
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
            wizardLayout = this.getAddUsagePointGroupWizard().getLayout(),
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
                me.validateStep1(function () {
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
            wizard = this.getAddUsagePointGroupWizard(),
            record = wizard.getRecord(),            
            nameField = this.getNameTextField(),
            groupName = Ext.clone(record.get('name')),
            name,            
            modelProxy;

        if (!me.usagePointGroupName) {
            me.usagePointGroupName = groupName;
        }
        if (nameField) {
            name = nameField.getValue();
            wizard.clearInvalid();
            wizard.updateRecord();
            if (wizard.isEdit && name == me.usagePointGroupName) {
                callback();
            } else {
                modelProxy = record.getProxy();
                record.phantom = true;       // force 'POST' method for request otherwise 'PUT' will be performed
                modelProxy.appendId = false; // remove 'id' part from request url
                record.save({
                    params: {validate: true},
                    success: function () {
                        callback();
                    },
                    failure: function (record, options) {
                        var response = options.response,
                            errors = Ext.decode(response.responseText, true);

                        if (errors && Ext.isArray(errors.errors)) {
                            wizard.markInvalid(errors.errors);
                        }
                    }
                });
                record.phantom = false;     // restore id in the record data for normal functionality
                modelProxy.appendId = true; // restore id in the url for normal functionality
            }            
        } else {
            callback();
        }
    },

    validateStep2: function () {
        var me = this,
            wizard = this.getAddUsagePointGroupWizard(),
            record = Ext.clone(wizard.getRecord()),
            valid = true,
            isDynamic;

        wizard.updateRecord(record);
        isDynamic = record.get('dynamic');
        if (isDynamic) {
            valid = !!me.countNumberOfSearchCriteria();
            Ext.suspendLayouts();
            this.getStep2FormErrorMessage().setVisible(!valid);
            wizard.down('#selection-criteria-error').setVisible(!valid);
            Ext.resumeLayouts(true);
        }
        return valid;
    },

    prepareNextStep: function (stepNumber) {
        var me = this,
            wizard = this.getAddUsagePointGroupWizard(),
            navigationMenu = this.getNavigationMenu(),
            buttons = wizard.getDockedComponent('usagepoint-group-wizard-buttons'),
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
                this.getStep2FormErrorMessage().hide();
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
            wizard = this.getAddUsagePointGroupWizard(),
            step2 = wizard.down('usagepoint-group-wizard-step2'),
            searchBtn = step2.down('#search-button'),
            domainsStore = me.service.getSearchDomainsStore(),
            staticGrid,
            isDynamic = me.isDynamic,
            selectionGroupType,
            usagePoints = me.getStore('Imt.usagepointgroups.store.UsagePointsOfUsagePointGroupWithoutPagination'),
            store = me.getStore(isDynamic ? 'Imt.usagepointgroups.store.DynamicGroupUsagePoints' : 'Imt.usagepointgroups.store.StaticGroupUsagePoints');

        if (step2.isPrepared) {
            return;
        }
        step2.getLayout().setActiveItem(isDynamic ? 1 : 0);
        me.service.setSearchResultsStore(store);
        me.setColumnPicker(isDynamic);
        if (!isDynamic) {
            searchBtn.setText(Uni.I18n.translate('general.apply', 'IMT', 'Apply'));
            me.service.excludedCriteria = undefined;
            staticGrid = step2.down('static-group-usagepoints-grid');
            staticGrid.setVisible(false);
            if (wizard.isEdit) {
                staticGrid.setLoading(true);
            }
            if (!usagePoints.getRange().length) {
                selectionGroupType = {};
                staticGrid.getSelectionModel().deselectAll(true); // fix the ExtJS error: "getById called for ID that is not present in local cache"
                staticGrid.setUsagePoints([]);
                staticGrid.getStore().data.clear();
                selectionGroupType[staticGrid.radioGroupName] = staticGrid.allInputValue;
                staticGrid.getSelectionGroupType().setValue(selectionGroupType);
            }
        } else {
            searchBtn.setText(Uni.I18n.translate('general.preview', 'IMT', 'Preview'));
            staticGrid = step2.down('dynamic-group-usagepoints-grid');
            staticGrid.down('pagingtoolbartop').resetPaging();
            staticGrid.down('pagingtoolbarbottom').resetPaging();
        }
        if (domainsStore.isLoading()) {
            domainsStore.on('load', function () {
                if (!isDynamic) {
                    staticGrid.setVisible(true);
                    if (wizard.isEdit) {
                        staticGrid.setLoading(true);
                        store.on('load', function () {
                            staticGrid.setLoading(false);
                        })
                    }
                }
                me.service.applyState(me.state, function () {});
            }, me, {single: true});
        } else {
            me.service.applyState(me.state, function () {
                if (!isDynamic) {
                    staticGrid.setVisible(true);
                    if (wizard.isEdit) {
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
            grid = this.getAddUsagePointGroupWizard().down(isDynamic ? 'dynamic-group-usagepoints-grid' : 'static-group-usagepoints-grid'),
            destroyListener = function () {
                if (me.searchFieldsOnLoadListener) {
                    me.searchFieldsOnLoadListener.destroy();
                }
            };

        destroyListener();
        me.searchFieldsOnLoadListener = me.service.getSearchFieldsStore().on('load', function (store, items) {
            grid.getStore().model.setFields(items.map(function (field) {
                return me.service.createFieldDefinitionFromModel(field);
            }));
            grid.down('uni-search-column-picker').setColumns(items.map(function (field) {
                return me.service.createColumnDefinitionFromModel(field);
            }));
        }, grid, {destroyable: true});
        grid.on('destroy', destroyListener);
    },

    prepareStep3: function (wizard, navigationMenu, buttons) {
        var me = this,
            step3 = wizard.down('usagepoint-group-wizard-step3'),
            progressbar = step3.down('progressbar'),
            record = Ext.clone(wizard.getRecord()),
            usagePointGroupName,
            isDynamic,
            usagePoints,
            confirmationTitle,
            confirmationMessage,
            showConfirmationMsg = function (numberOfUsagePoints, numberOfSearchCriteria) {
                if (isDynamic) {
                    confirmationMessage = Uni.I18n.translate('usagepointgroup.wizard.dynamic.confirmationMessage.criteriaNumber', 'IMT', 'Number of specified search criteria: {0}', [numberOfSearchCriteria])
                        + '<br>'
                        + Uni.I18n.translate('usagepointgroup.wizard.dynamic.confirmationMessage.usagePointsNumber', 'IMT', 'Current number of usage points: {0}', [numberOfUsagePoints]);
                } else {
                    confirmationMessage = Uni.I18n.translate('usagepointgroup.wizard.static.confirmationMessage', 'IMT', 'Number of usage points: {0}', [numberOfUsagePoints]);
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
        usagePointGroupName = record.get('name');
        isDynamic = record.get('dynamic');
        usagePoints = record.get('usagePoints');
        if (wizard.isEdit) {
            confirmationTitle = Uni.I18n.translate('usagepointgroup.wizard.edit.confirmationTitle', 'IMT', "Save usage point group '{0}'?", [usagePointGroupName]);
        } else {
            confirmationTitle = isDynamic
                ? Uni.I18n.translate('usagepointgroup.wizard.add.dynamic.confirmationTitle', 'IMT', "Add dynamic usage point group '{0}'?", [usagePointGroupName])
                : Uni.I18n.translate('usagepointgroup.wizard.add.static.confirmationTitle', 'IMT', "Add static usage point group '{0}'?", [usagePointGroupName]);
        }
        if (!isDynamic && usagePoints) {
            showConfirmationMsg(usagePoints.length);
        } else {
            Ext.suspendLayouts();
            step3.update(Uni.I18n.translate('usagepointgroup.wizard.progress.countingUsagePoints', 'IMT', 'Counting of number of usage points. Please wait...'));
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
        var step4 = wizard.down('usagepoint-group-wizard-step4'),
            progressbar = step4.down('progressbar'),
            usagePointGroupName;

        Ext.suspendLayouts();
        step4.update(wizard.isEdit
            ? Uni.I18n.translate('usagepointgroup.wizard.progress.modificationGroup', 'IMT', 'Usage point group modification. Please wait...')
            : Uni.I18n.translate('usagepointgroup.wizard.progress.creationGroup', 'IMT', 'Usage point group creation. Please wait...'));
        progressbar.show();
        progressbar.wait({
            interval: 50,
            increment: 20
        });
        Ext.resumeLayouts(true);
        wizard.updateRecord();
        if (!wizard.isEdit) {
            wizard.getRecord().phantom = true;       // force 'POST' method for request otherwise 'PUT' will be performed
        }
        wizard.getRecord().save({
            backUrl: finishBtn.href,
            success: function (record) {
                usagePointGroupName = record.get('name');
                Ext.suspendLayouts();
                finishBtn.show();
                progressbar.hide();
                step4.update(wizard.isEdit
                    ? Uni.I18n.translate('usagepointgroup.wizard.save.success', 'IMT', "Usage point group '{0}' has been saved.", [usagePointGroupName])
                    : Uni.I18n.translate('usagepointgroup.wizard.add.success', 'IMT', "Usage point group '{0}' has been created.", [usagePointGroupName]));
                Ext.resumeLayouts(true);
            },
            failure: function (record, response) {
                var responseText = Ext.decode(response.response.responseText, true),
                    reasons = '';

                usagePointGroupName = record.get('name');
                if (responseText && Ext.isArray(responseText.errors)) {
                    Ext.Array.each(responseText.errors, function (error) {
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
                        ? Uni.I18n.translate('usagepointgroup.wizard.save.failure', 'IMT', "Failed to save usage point group '{0}'.", [usagePointGroupName])
                        : Uni.I18n.translate('usagepointgroup.wizard.add.failure', 'IMT', "Failed to add usage point group '{0}'.", [usagePointGroupName]))
                    + '</h3>'
                    + reasons);
                Ext.resumeLayouts(true);
            }
        });
    },

    applyFilters: function () {
        var me = this,
            wizard = this.getAddUsagePointGroupWizard(),
            staticGrid = wizard.down('static-group-usagepoints-grid'),
            dynamicGrid = wizard.down('dynamic-group-usagepoints-grid'),
            record = Ext.clone(wizard.getRecord()),
            isDynamic;

        wizard.updateRecord(record);
        isDynamic = record.get('dynamic');
        if (isDynamic) {
            dynamicGrid.down('pagingtoolbartop').resetPaging();
            dynamicGrid.down('pagingtoolbarbottom').resetPaging();
        } else {
            staticGrid.getSelectionModel().deselectAll(true); // fix the ExtJS error: "getById called for ID that is not present in local cache"
            staticGrid.setLoading();
        }
        me.service.applyFilters.apply(me.service, arguments);
    },

    availableClearAll: function () {
        var me = this,
            filterPanel = this.getFilterPanel(),
            filters = me.service.getFilters();

        filterPanel.down('[action=clearFilters]').setDisabled(!(filters && filters.length));
    }
});
