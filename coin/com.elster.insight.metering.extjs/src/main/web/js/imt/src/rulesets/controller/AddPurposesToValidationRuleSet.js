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
        'Imt.rulesets.store.ValidationRuleSetPurposesToAdd'
    ],

    models: [
        'Cfg.model.ValidationRuleSet'
    ],

    mixins: [
        'Imt.rulesets.controller.mixins.AddPurposesCommon'
    ],

    refs: [
        {ref: 'previewPanel', selector: '#add-purposes-to-validation-rule-set #metrology-configuration-purpose-preview'}
    ],

    addPurposesLink: '/api/ucr/validationrulesets/{0}/purposes/add',

    init: function () {
        var me = this;

        me.control({
            '#add-purposes-to-validation-rule-set #add-metrology-configuration-purposes-grid': {
                select: me.showPreview,
                addSelected: me.addPurposesToRuleSet
            }
        });
    },

    showAddMetrologyConfigurationPurposes: function (ruleSetId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            availableToAddPurposesStore = me.getStore('Imt.rulesets.store.ValidationRuleSetPurposesToAdd');

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
                ruleSetId: ruleSetId
            }));
            availableToAddPurposesStore.getProxy().setExtraParam('ruleSetId', ruleSetId);
            availableToAddPurposesStore.load();
        }

        function loadCallback() {
            mainView.setLoading(false);
        }
    }
});