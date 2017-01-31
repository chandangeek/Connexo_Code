Ext.define('Imt.rulesets.controller.AddPurposesToEstimationRuleSet', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router',
        'Imt.rulesets.controller.mixins.AddPurposesCommon'
    ],

    views: [
        'Imt.rulesets.view.AddMetrologyConfigurationPurposes'
    ],

    stores: [
        'Imt.rulesets.store.EstimationRuleSetPurposesToAdd'
    ],

    models: [
        'Est.estimationrulesets.model.EstimationRuleSet'
    ],

    mixins: [
        'Imt.rulesets.controller.mixins.AddPurposesCommon'
    ],

    refs: [
        {ref: 'previewPanel', selector: '#add-purposes-to-estimation-rule-set #metrology-configuration-purpose-preview'}
    ],

    addPurposesLink: '/api/ucr/estimationrulesets/{0}/purposes/add',

    init: function () {
        var me = this;

        me.control({
            '#add-purposes-to-estimation-rule-set #add-metrology-configuration-purposes-grid': {
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
            availableToAddPurposesStore = me.getStore('Imt.rulesets.store.EstimationRuleSetPurposesToAdd');

        mainView.setLoading();
        me.getModel('Est.estimationrulesets.model.EstimationRuleSet').load(ruleSetId, {
            success: function (record) {
                app.fireEvent('loadEstimationRuleSet', record);
                app.fireEvent('changecontentevent', Ext.widget('add-metrology-configuration-purposes', {
                    itemId: 'add-purposes-to-estimation-rule-set',
                    sideMenu: 'estimation-rule-set-side-menu',
                    router: router,
                    cancelHref: router.getRoute('administration/estimationrulesets/estimationruleset/metrologyconfigurationpurposes').buildUrl(),
                    purposesStore: availableToAddPurposesStore,
                    ruleSetId: ruleSetId
                }));
                availableToAddPurposesStore.getProxy().setExtraParam('ruleSetId', ruleSetId);
                availableToAddPurposesStore.load();
            },
            callback: function () {
                mainView.setLoading(false);
            }
        });
    }
});