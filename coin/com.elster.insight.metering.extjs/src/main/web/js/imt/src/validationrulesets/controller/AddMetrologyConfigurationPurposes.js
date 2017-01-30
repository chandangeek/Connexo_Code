Ext.define('Imt.validationrulesets.controller.AddMetrologyConfigurationPurposes', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
    ],

    views: [
        'Imt.validationrulesets.view.AddMetrologyConfigurationPurposes'
    ],

    stores: [
        'Imt.validationrulesets.store.MetrologyConfigurationPurposesToAdd'
    ],

    models: [
        'Cfg.model.ValidationRuleSet'
    ],

    refs: [
        {ref: 'previewPanel', selector: '#add-metrology-configuration-purposes #metrology-configuration-purpose-preview'}
    ],

    init: function () {
        var me = this;

        // Imt.validationrulesets.controller.MetrologyConfigurationPurposes should be initialized first
        me.getController('Imt.validationrulesets.controller.MetrologyConfigurationPurposes');
        me.control({
            '#add-metrology-configuration-purposes #add-metrology-configuration-purposes-grid': {
                select: me.showPreview,
                addSelected: me.addPurposesToRuleSet
            }
        });
    },

    showAddMetrologyConfigurationPurposes: function (ruleSetId) {
        var me = this,
            app = me.getApplication(),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            availableToAddPurposesStore = me.getStore('Imt.validationrulesets.store.MetrologyConfigurationPurposesToAdd');

        mainView.setLoading();
        me.getModel('Cfg.model.ValidationRuleSet').load(id, {
            success: function (record) {
                app.fireEvent('loadRuleSet', record);
                app.fireEvent('changecontentevent', Ext.widget('add-metrology-configuration-purposes', {
                    itemId: 'add-metrology-configuration-purposes',
                    router: me.getController('Uni.controller.history.Router'),
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
            url: Ext.String.format('/api/ucr/validationruleset/{0}/purposes/add', router.arguments.ruleSetId),
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