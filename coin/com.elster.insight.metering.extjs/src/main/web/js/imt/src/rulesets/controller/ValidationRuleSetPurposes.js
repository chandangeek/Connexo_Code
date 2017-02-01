/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.rulesets.controller.ValidationRuleSetPurposes', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router',
        'Imt.rulesets.controller.mixins.ViewPurposesCommon'
    ],

    views: [
        'Imt.rulesets.view.MetrologyConfigurationPurposes',
        'Uni.view.window.Confirmation'
    ],

    stores: [
        'Imt.rulesets.store.ValidationRuleSetPurposes'
    ],

    models: [
        'Imt.rulesets.model.ValidationRuleSetPurpose',
        'Cfg.model.ValidationRuleSet'
    ],

    mixins: [
        'Imt.rulesets.controller.mixins.ViewPurposesCommon'
    ],

    refs: [
        {ref: 'previewPanel', selector: '#validation-rule-set-purposes #metrology-configuration-purpose-preview'}
    ],

    confirmRemoveMsg: Uni.I18n.translate('ruleSet.validation.metrologyConfigurationPurposes.removeConfirmation.msg', 'IMT', 'The validation rule set will no longer be available on this purpose of the metrology configuration.'),

    init: function () {
        var me = this;

        me.control({
            '#validation-rule-set-purposes #metrology-configuration-purposes-grid': {
                select: me.showPreview
            },
            '#validation-rule-set-purposes #metrology-configuration-purposes-grid uni-actioncolumn-remove': {
                remove: me.removePurpose
            },
            '#validation-rule-set-purposes #metrology-configuration-purpose-action-menu': {
                click: me.chooseAction
            }
        });
    },

    showMetrologyConfigurationPurposes: function (ruleSetId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            purposesStore = me.getStore('Imt.rulesets.store.ValidationRuleSetPurposes'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        mainView.setLoading();
        me.getModel('Cfg.model.ValidationRuleSet').load(ruleSetId, {
            success: onSuccessLoad,
            callback: loadCallback
        });

        function onSuccessLoad(record) {
            app.fireEvent('loadRuleSet', record);
            me.getModel('Imt.rulesets.model.ValidationRuleSetPurpose').getProxy().setExtraParam('ruleSetId', ruleSetId);
            purposesStore.getProxy().setExtraParam('ruleSetId', ruleSetId);
            app.fireEvent('changecontentevent', Ext.widget('metrology-configuration-purposes', {
                itemId: 'validation-rule-set-purposes',
                sideMenu: 'ruleSetSubMenu',
                purposesStore: purposesStore,
                router: router,
                addLink: router.getRoute('administration/rulesets/overview/metrologyconfigurationpurposes/add').buildUrl(),
                adminPrivileges: Imt.privileges.MetrologyConfig.adminValidation,
                ruleSetId: ruleSetId
            }));
        }

        function loadCallback() {
            mainView.setLoading(false);
        }
    }
});