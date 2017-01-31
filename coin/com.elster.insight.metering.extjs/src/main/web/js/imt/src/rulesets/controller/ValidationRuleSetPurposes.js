Ext.define('Imt.rulesets.controller.ValidationRuleSetPurposes', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
    ],

    views: [
        'Imt.rulesets.view.MetrologyConfigurationPurposes',
        'Uni.view.window.Confirmation'
    ],

    stores: [
        'Imt.rulesets.store.ValidationRuleSetPurposes'
    ],

    models: [
        'Imt.rulesets.model.MetrologyConfigurationPurpose',
        'Cfg.model.ValidationRuleSet'
    ],

    refs: [
        {ref: 'previewPanel', selector: '#validation-rule-set-purposes #metrology-configuration-purpose-preview'}
    ],

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
            success: function (record) {
                app.fireEvent('loadRuleSet', record);
                me.getModel('Imt.rulesets.model.MetrologyConfigurationPurpose').getProxy().setExtraParam('ruleSetId', ruleSetId);
                purposesStore.getProxy().setExtraParam('ruleSetId', ruleSetId);
                app.fireEvent('changecontentevent', Ext.widget('metrology-configuration-purposes', {
                    itemId: 'validation-rule-set-purposes',
                    sideMenu: 'ruleSetSubMenu',
                    purposesStore: purposesStore,
                    router: router,
                    addLink: router.getRoute('administration/rulesets/overview/metrologyconfigurationpurposes/add').buildUrl(),
                    ruleSetId: ruleSetId
                }));
            },
            callback: function () {
                mainView.setLoading(false);
            }
        });
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreviewPanel(),
            menu = preview.down('#metrology-configuration-purpose-action-menu');

        Ext.suspendLayouts();
        preview.setTitle(record.get('metrologyConfigurationInfo').name);
        preview.loadRecord(record);
        Ext.resumeLayouts(true);

        if (menu) {
            menu.record = record;
        }
    },

    chooseAction: function (menu, menuItem) {
        var me = this;

        switch (menuItem.action) {
            case 'remove':
                me.removePurpose(menu.record);
                break;
        }
    },

    removePurpose: function (record) {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        Ext.create('Uni.view.window.Confirmation').show({
            title: Uni.I18n.translate('general.removex', 'IMT', "Remove '{0}'?",
                record.get('purpose')),
            msg: Uni.I18n.translate('ruleSet.validation.metrologyConfigurationPurposes.removeConfirmation.msg', 'IMT', 'The validation rule set will no longer be available on this purpose of the metrology configuration.'),
            fn: remove
        });

        function remove(state) {
            if (state === 'confirm') {
                mainView.setLoading();
                record.destroy({
                    isNotEdit: true,
                    success: onSuccessRemove,
                    callback: removeCallback
                });
            }
        }

        function onSuccessRemove() {
            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.remove.success.msg', 'IMT', 'Metrology configuration purpose removed'));
            me.getController('Uni.controller.history.Router').getRoute().forward();
        }

        function removeCallback() {
            mainView.setLoading(false);
        }
    }
});