/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.controller.BulkAction', {
    extend: 'Ext.app.Controller',

    views: [
        'Mtr.readingtypes.view.bulk.Browse',
        'Mtr.readingtypes.view.bulk.Wizard'
    ],

    requires: [],

    stores: [
        'Mtr.readingtypes.store.ReadingTypes',
        'Mtr.readingtypes.store.ReadingTypesBulk'

    ],


    refs: [
        {
            ref: 'navigationMenu',
            selector: '#reading-types-bulk-navigation'
        },
        {
            ref: 'wizard',
            selector: '#reading-types-wizard'
        },
        {
            ref: 'readingTypesGrid',
            selector: '#reading-types-selection-grid'
        },
        {
            ref: 'confirmPage',
            selector: '#reading-types-bulk-step4'
        },
        {
            ref: 'statusPage',
            selector: '#reading-types-bulk-step5'
        }
    ],

    operation: 'edit',

    init: function () {
        this.control({
            'reading-types-wizard #reading-types-bulk-next': {
                click: this.nextClick
            },
            'reading-types-wizard #reading-types-bulk-confirm': {
                click: this.nextClick
            },
            'reading-types-wizard #reading-types-bulk-back': {
                click: this.backClick
            },
            'reading-types-wizard #reading-types-bulk-cancel': {
                click: this.goBack
            },
            'reading-types-wizard #reading-types-bulk-finish': {
                click: this.goBack
            },
            'reading-types-wizard #failure-reading-types-bulk-finish': {
                click: this.goBack
            },
            '#reading-types-bulk-navigation': {
                movetostep: this.navigateToStep
            }
        });
    },

    showOverview: function () {
        var me = this,
            readingTypesStore = Ext.getStore('Mtr.readingtypes.store.ReadingTypes'),
            readingTypesStoreBulk = Ext.getStore('Mtr.readingtypes.store.ReadingTypesBulk'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            widget;

        viewport.setLoading(true);
        if (readingTypesStore.getCount()) {
            readingTypesStoreBulk.load({
                callback: function () {
                    widget = Ext.widget('reading-types-bulk-browse');
                    me.getApplication().fireEvent('changecontentevent', widget);
                    viewport.setLoading(false);
                }
            });
        } else {
            me.goBack();
            viewport.setLoading(false);
        }

    },

    nextClick: function () {
        var me = this,
            layout = this.getWizard().getLayout(),
            currentCmp = layout.getActiveItem();
        Ext.suspendLayouts();

        switch (currentCmp.name) {
            case 'selectReadingTypes':
                me.AllreadingTypes = me.getReadingTypesGrid().isAllSelected();
                if (!me.AllreadingTypes) {
                    me.readingTypes = me.getReadingTypesGrid().getSelectionModel().getSelection();
                    if (me.readingTypes.length) {
                        currentCmp.down('#step-selection-error').hide();
                        currentCmp.down('#step1-errors').hide();
                        me.goNextStep();
                    } else {
                        currentCmp.down('#step-selection-error').show();
                        currentCmp.down('#step1-errors').show();
                    }
                } else {
                    currentCmp.down('#step-selection-error').hide();
                    currentCmp.down('#step1-errors').hide();
                    me.goNextStep();
                }
                break;
            case 'selectOperation':
                me.operation = currentCmp.down('#reading-types-bulk-step2-radio-group').getValue().operation;
                if (me.operation != 'edit') {
                    me.updateTitles(me.operation);
                    me.getNavigationMenu().items.getAt(2).disable();
                    me.goNextStep();
                }
                me.goNextStep();
                break;
            case 'selectActionItems':
                if (currentCmp.down('#new-alias').getValue()) {
                    currentCmp.down('#step3-errors').hide();
                    currentCmp.down('#new-alias').clearInvalid();
                    me.newAlias = currentCmp.down('#new-alias').getValue();
                    me.updateTitles('edit');
                    me.goNextStep();
                } else {
                    currentCmp.down('#step3-errors').show();
                    currentCmp.down('#new-alias').markInvalid(Uni.I18n.translate('readingtypesmanagment.bulk.thisfieldisrequired', 'MTR', 'This field is required'));
                }
                break;
            case 'confirmPage':
                me.saveReadingType();
                break;
        }

        Ext.resumeLayouts(true);
    },

    goNextStep: function () {
        var me = this,
            layout = this.getWizard().getLayout(),
            nextCmp = layout.getNext();

        this.getNavigationMenu().moveNextStep();
        layout.setActiveItem(nextCmp);
        me.updateButtonsState(nextCmp);


    },

    backClick: function (btn, e, eOpts) {
        var me = this,
            layout = this.getWizard().getLayout(),
            currentCmp = layout.getActiveItem(),
            prevCmp = layout.getPrev();

        Ext.suspendLayouts();
        this.getNavigationMenu().movePrevStep();
        layout.setActiveItem(prevCmp);
        me.updateButtonsState(prevCmp);
        if (me.operation != 'edit' && currentCmp.name == 'confirmPage') {
            me.getNavigationMenu().items.getAt(2).enable();
            me.backClick(btn, e, eOpts);
        }
        Ext.resumeLayouts(true);
    },

    saveReadingType: function () {
        var me = this, jsonData = {}, params = '', listOfReadingTypes = [],
            url = '/api/mtr/readingtypes/activate';

        switch (me.operation) {
            case 'activate':
                jsonData.active = true;
                break;
            case 'edit':
                jsonData.aliasName = me.newAlias;
                url = '/api/mtr/readingtypes';
                break;
            case 'deactivate':
                jsonData.active = false;
                break;
        }

        if (!me.AllreadingTypes) {
            me.readingTypes.forEach(function (readingType) {
                listOfReadingTypes.push(readingType.get('mRID'))
            });
            jsonData.mRIDs = listOfReadingTypes;
        } else {
            params = me.getStore('Mtr.readingtypes.store.ReadingTypesBulk').getProxy().extraParams;
        }

        Ext.Ajax.request({
            url: url,
            method: 'PUT',
            params: params,
            jsonData: jsonData,
            success: function (response) {
                me.getWizard().down('#reading-types-bulk-finish').hide();
                me.goNextStep();
            },
            failure: function (response, request) {
                me.getStatusPage().down('#step5-error-panel').show();
                me.getStatusPage().down('#step5-success-panel').hide();
                me.getWizard().down('#failure-reading-types-bulk-finish').show();
                me.goNextStep();
                me.getWizard().down('#reading-types-bulk-finish').hide();
                me.getNavigationMenu().markInvalid();
            }
        });
    },

    updateTitles: function (operation) {
        var me = this, title, message, confirmPageMsg, statusPageMsg, readingTypesCountTitle;

        if (me.AllreadingTypes) {
            readingTypesCountTitle = Uni.I18n.translate('readingtypesmanagment.bulk.allreadingtypesx', 'MTR', 'All reading types');
        } else {
            readingTypesCountTitle = Uni.I18n.translatePlural('readingtypesmanagment.bulk.selectedreadingtypes', me.readingTypes.length, 'MTR',
                "{0} reading types",
                "{0} reading type",
                "{0} reading types"
            );
        }

        switch (operation) {
            case 'activate':
                title = Uni.I18n.translate('readingtypesmanagment.bulk.activatereadingtypes', 'MTR', 'Activate reading types');
                message = Uni.I18n.translate('readingtypesmanagment.bulk.step4applyActivatex', 'MTR', 'Activate {0}?', readingTypesCountTitle.toLowerCase());
                confirmPageMsg = Uni.I18n.translate('readingtypesmanagment.bulk.step4activateMsgx', 'MTR', 'The reading types will become available');
                statusPageMsg = Uni.I18n.translate('readingtypesmanagment.bulk.step5activateMsg1', 'MTR', '{0} reading types have been queued to activate', readingTypesCountTitle);
                break;
            case 'edit':
                title = Uni.I18n.translate('readingtypesmanagment.bulk.editereadingtypes', 'MTR', 'Edit reading types');
                message = Ext.String.format(Uni.I18n.translate('readingtypesmanagment.bulk.step4applyEditx', 'MTR', 'Apply alias \'{0}\' to {1}?'), me.newAlias, readingTypesCountTitle.toLowerCase());
                confirmPageMsg = Uni.I18n.translate('readingtypesmanagment.bulk.step4editMsg', 'MTR', 'The reading types will change their names');
                statusPageMsg = Ext.String.format(Uni.I18n.translate('readingtypesmanagment.bulk.step5editMsg1', 'MTR', '{1} have been queued to set alias \'{0}\''), me.newAlias, readingTypesCountTitle);
                break;
            case 'deactivate':
                title = Uni.I18n.translate('readingtypesmanagment.bulk.deactivatereadingtypes', 'MTR', 'Deactivate reading types');
                message = Uni.I18n.translate('readingtypesmanagment.bulk.step4applyDeactivate', 'MTR', 'Deactivate {0}?', readingTypesCountTitle.toLowerCase());
                confirmPageMsg = Uni.I18n.translate('readingtypesmanagment.bulk.step4deactivateMsgx', 'MTR', 'The reading types will become unavailable');
                statusPageMsg = Uni.I18n.translate('readingtypesmanagment.bulk.step5deactivateMsg1', 'MTR', '{0} have been queued to deactivate', readingTypesCountTitle);
                break;
        }

        me.getConfirmPage().addNotificationPanel(title, message, confirmPageMsg);
        me.getStatusPage().addNotificationPanel(title, statusPageMsg);
    },

    updateButtonsState: function (nextCmp) {
        var me = this,
            wizard = me.getWizard(),
            backBtn = wizard.down('#reading-types-bulk-back'),
            nextBtn = wizard.down('#reading-types-bulk-next'),
            confirmBtn = wizard.down('#reading-types-bulk-confirm'),
            finishBtn = wizard.down('#reading-types-bulk-finish'),
            cancelBtn = wizard.down('#reading-types-bulk-cancel');
        nextCmp.name == 'selectReadingTypes' ? backBtn.disable() : backBtn.enable();

        switch (nextCmp.name) {
            case 'selectReadingTypes':
            case 'selectOperation':
            case 'selectActionItems':
                backBtn.show();
                nextBtn.show();
                cancelBtn.show();
                confirmBtn.hide();
                finishBtn.hide();
                break;
            case 'confirmPage':
                backBtn.show();
                nextBtn.hide();
                cancelBtn.show();
                confirmBtn.show();
                finishBtn.hide();
                break;
            case 'statusPage':
                backBtn.hide();
                nextBtn.hide();
                cancelBtn.hide();
                confirmBtn.hide();
                finishBtn.show();
                break;
        }
    },

    navigateToStep: function (index) {
        var me = this,
            layout = me.getWizard().getLayout(),
            nextCmp = layout.getLayoutItems()[index - 1];
        layout.setActiveItem(nextCmp);
        me.updateButtonsState(nextCmp);
        if (index < 3) me.getNavigationMenu().items.getAt(2).enable();
    },

    goBack: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('administration/readingtypes').forward(null,
            router.getQueryStringValues()
        );
    }
});
