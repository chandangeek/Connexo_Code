Ext.define('Imt.validationrulesets.controller.AddMetrologyConfigurationPurposes', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
    ],

    views: [
        'Imt.validationrulesets.view.AddMetrologyConfigurationPurposes'
    ],

    stores: [
        'Imt.validationrulesets.store.MetrologyConfigurationPurposes'
    ],

    models: [
        'Imt.validationrulesets.model.MetrologyConfigurationPurpose'
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
                select: me.showPreview
            }
        });
    },

    showAddMetrologyConfigurationPurposes: function (ruleSetId) {
        var me = this,
            availableToAddPurposesStore = me.getStore('Imt.validationrulesets.store.MetrologyConfigurationPurposes');

        me.getApplication().fireEvent('changecontentevent', Ext.widget('add-metrology-configuration-purposes', {
            itemId: 'add-metrology-configuration-purposes',
            router: me.getController('Uni.controller.history.Router'),
            ruleSetId: ruleSetId
        }));
        availableToAddPurposesStore.getProxy().setExtraParam('ruleSetId', ruleSetId);
        availableToAddPurposesStore.load();
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreviewPanel();

        Ext.suspendLayouts();
        preview.setTitle(record.get('metrologyConfigurationInfo').name);
        preview.loadRecord(record);
        Ext.resumeLayouts(true);
    }
});