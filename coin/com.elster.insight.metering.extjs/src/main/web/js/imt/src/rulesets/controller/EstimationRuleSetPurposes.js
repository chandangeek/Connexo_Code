Ext.define('Imt.rulesets.controller.EstimationRuleSetPurposes', {
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
        'Imt.rulesets.store.EstimationRuleSetPurposes'
    ],

    models: [
        'Imt.rulesets.model.MetrologyConfigurationPurpose',
        'Est.estimationrulesets.model.EstimationRuleSet'
    ],

    mixins: [
        'Imt.rulesets.controller.mixins.ViewPurposesCommon'
    ],

    refs: [
        {ref: 'previewPanel', selector: '#estimation-rule-set-purposes #metrology-configuration-purpose-preview'}
    ],

    confirmRemoveMsg: Uni.I18n.translate('ruleSet.estimation.metrologyConfigurationPurposes.removeConfirmation.msg', 'IMT', 'The estimation rule set will no longer be available on this purpose of the metrology configuration.'),

    init: function () {
        var me = this;

        me.control({
            '#estimation-rule-set-purposes #metrology-configuration-purposes-grid': {
                select: me.showPreview
            },
            '#estimation-rule-set-purposes #metrology-configuration-purposes-grid uni-actioncolumn-remove': {
                remove: me.removePurpose
            },
            '#estimation-rule-set-purposes #metrology-configuration-purpose-action-menu': {
                click: me.chooseAction
            }
        });
    },

    showMetrologyConfigurationPurposes: function (ruleSetId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            purposesStore = me.getStore('Imt.rulesets.store.EstimationRuleSetPurposes'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        mainView.setLoading();
        me.getModel('Est.estimationrulesets.model.EstimationRuleSet').load(ruleSetId, {
            success: onSuccessLoad,
            callback: loadCallback
        });

        function onSuccessLoad(record) {
            app.fireEvent('loadEstimationRuleSet', record);
            me.getModel('Imt.rulesets.model.MetrologyConfigurationPurpose').getProxy().setExtraParam('ruleSetId', ruleSetId);
            purposesStore.getProxy().setExtraParam('ruleSetId', ruleSetId);
            app.fireEvent('changecontentevent', Ext.widget('metrology-configuration-purposes', {
                itemId: 'estimation-rule-set-purposes',
                sideMenu: 'estimation-rule-set-side-menu',
                purposesStore: purposesStore,
                router: router,
                addLink: router.getRoute('administration/estimationrulesets/estimationruleset/metrologyconfigurationpurposes/add').buildUrl(),
                adminPrivileges: Imt.privileges.MetrologyConfig.adminEstimation,
                ruleSetId: ruleSetId
            }));
        }

        function loadCallback() {
            mainView.setLoading(false);
        }
    }
});