Ext.define('Imt.validationrulesets.controller.MetrologyConfigurationPurposes', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
    ],

    views: [
        'Imt.validationrulesets.view.MetrologyConfigurationPurposes',
        'Uni.view.window.Confirmation'
    ],

    stores: [
        'Imt.validationrulesets.store.MetrologyConfigurationPurposes'
    ],

    models: [
        'Imt.validationrulesets.model.MetrologyConfigurationPurpose'
    ],

    refs: [
        {ref: 'previewPanel', selector: '#metrology-configuration-purposes #metrology-configuration-purpose-preview'}
    ],

    init: function () {
        var me = this;

        me.control({
            '#metrology-configuration-purposes #metrology-configuration-purposes-grid': {
                select: me.showPreview
            },
            '#metrology-configuration-purposes #metrology-configuration-purposes-grid uni-actioncolumn-remove': {
                remove: me.removePurpose
            },
            '#metrology-configuration-purposes #metrology-configuration-purpose-action-menu': {
                click: me.chooseAction
            }
        });

        // Cfg.controller.Validation should be initialized first
        me.getController('Cfg.controller.Validation');
        me.getApplication().on('validationrulesetmenurender', function (menu) {
            menu.add(
                {
                    text: Uni.I18n.translate('general.metrologyConfigurationPurposes', 'IMT', 'Metrology configuration purposes'),
                    itemId: 'metrology-configuration-purposes-link',
                    href: me.getController('Uni.controller.history.Router')
                        .getRoute('administration/rulesets/overview/metrologyconfigurationpurposes')
                        .buildUrl({ruleSetId: menu.ruleSetId})
                }
            );
        });
    },

    showMetrologyConfigurationPurposes: function (ruleSetId) {
        var me = this;

        me.getModel('Imt.validationrulesets.model.MetrologyConfigurationPurpose').getProxy().setExtraParam('ruleSetId', ruleSetId);
        me.getStore('Imt.validationrulesets.store.MetrologyConfigurationPurposes').getProxy().setExtraParam('ruleSetId', ruleSetId);
        me.getApplication().fireEvent('changecontentevent', Ext.widget('metrology-configuration-purposes', {
            itemId: 'metrology-configuration-purposes',
            router: me.getController('Uni.controller.history.Router'),
            ruleSetId: ruleSetId
        }));
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
            msg: Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.removeConfirmation.msg', 'IMT', 'The validation rule set will no longer be available on this purpose of the metrology configuration.'),
            fn: remove
        });

        function remove(state) {
            if (state === 'confirm') {
                mainView.setLoading();
                record.destroy({
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