/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.rulesets.controller.AddPurposesToValidationRuleSet', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router',
        'Imt.rulesets.controller.mixins.AddPurposesCommon'
    ],

    views: [
        'Imt.rulesets.view.AddMetrologyConfigurationPurposes'
    ],

    stores: [
        'Imt.rulesets.store.ValidationRuleSetPurposesToAdd',
        'Imt.rulesets.store.UsagePointStatesToAdd'
    ],

    models: [
        'Cfg.model.ValidationRuleSet'
    ],

    mixins: [
        'Imt.rulesets.controller.mixins.AddPurposesCommon'
    ],

    refs: [
        {ref: 'page', selector: '#add-purposes-to-validation-rule-set'},
        {ref: 'previewPanel', selector: '#add-purposes-to-validation-rule-set #metrology-configuration-purpose-preview'},
        {ref: 'purposesGrid', selector: '#add-purposes-to-validation-rule-set #add-metrology-configuration-purposes-grid'},
        {ref: 'statesGrid', selector: '#add-purposes-to-validation-rule-set #add-usage-point-states-grid'},
        {ref: 'statesErrorMessage', selector: '#add-purposes-to-validation-rule-set #add-usage-point-states-error'}
    ],

    addPurposesLink: '/api/ucr/validationrulesets/{0}/purposes/add',

    init: function () {
        var me = this;

        me.control({
            '#add-purposes-to-validation-rule-set #add-metrology-configuration-purposes-grid': {
                select: me.showPreview,
                addSelected: me.addPurposesToRuleSet
            },
            '#add-purposes-to-validation-rule-set #add-metrology-configuration-purposes-bottom-toolbar #addButton': {
                click: me.addPurposesAndStatesToRuleSet
            }
        });
    },

    showAddMetrologyConfigurationPurposes: function (ruleSetId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            availableToAddPurposesStore = me.getStore('Imt.rulesets.store.ValidationRuleSetPurposesToAdd'),
            usagePointStatesStore = me.getStore('Imt.rulesets.store.UsagePointStatesToAdd');

        mainView.setLoading();
        me.getModel('Cfg.model.ValidationRuleSet').load(ruleSetId, {
            success: onSuccessLoad,
            callback: loadCallback
        });

        function onSuccessLoad(record) {
            app.fireEvent('loadRuleSet', record);
            app.fireEvent('changecontentevent', Ext.widget('add-metrology-configuration-purposes', {
                itemId: 'add-purposes-to-validation-rule-set',
                sideMenu: 'ruleSetSubMenu',
                router: router,
                cancelHref: router.getRoute('administration/rulesets/overview/metrologyconfigurationpurposes').buildUrl(),
                purposesStore: availableToAddPurposesStore,
                usagePointStatesStore: usagePointStatesStore,
                ruleSetId: ruleSetId
            }));
            availableToAddPurposesStore.getProxy().setExtraParam('ruleSetId', ruleSetId);
            me.getPage().down('#stepsMenu').setHeader(record.get('name'));
            availableToAddPurposesStore.load();
            usagePointStatesStore.load();
        }

        function loadCallback() {
            mainView.setLoading(false);
        }
    }
});