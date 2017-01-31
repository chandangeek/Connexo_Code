Ext.define('Imt.rulesets.controller.AddPurposesToEstimationRuleSet', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
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

    refs: [
        {ref: 'previewPanel', selector: '#add-purposes-to-estimation-rule-set #metrology-configuration-purpose-preview'}
    ],

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
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreviewPanel();

        Ext.suspendLayouts();
        preview.setTitle(record.get('metrologyConfigurationInfo').name);
        preview.loadRecord(record);
        Ext.resumeLayouts(true);
    },

    addPurposesToRuleSet: function (grid, records) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        mainView.setLoading();
        Ext.Ajax.request({
            method: 'PUT',
            url: Ext.String.format('/api/ucr/estimationruleset/{0}/purposes/add', router.arguments.ruleSetId),
            jsonData: formatData(),
            success: onSuccessAdd,
            callback: addCallback
        });

        function formatData() {
            return _.map(records, function (record) {
                return record.getId();
            });
        }

        function onSuccessAdd() {
            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.add.success.msg', 'IMT', '{0} purposes of metrology configuirations added',
                records.length));
            if (grid.rendered) {
                window.location.href = grid.cancelHref;
            }
        }

        function addCallback() {
            mainView.setLoading(false);
        }
    }
});