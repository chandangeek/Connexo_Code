Ext.define('Cfg.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation'
    ],

    controllers: [
        'Cfg.controller.history.Validation',
        'Cfg.controller.history.EventType',
        'Cfg.controller.history.ValidationTask',
        'Cfg.controller.Administration',
        'Cfg.controller.Validation',
        'Cfg.controller.EventType',
        'Cfg.controller.Tasks',
        'Cfg.controller.Log'
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        this.initHistorians();
        this.initMenu();
    },

    /**
     * Forces history registration.
     */
    initHistorians: function () {
        this.getController('Cfg.controller.history.Validation');
        this.getController('Cfg.controller.history.ValidationTask');
    },

    initMenu: function () {

        var me = this;
        if (Uni.Auth.hasAnyPrivilege(['privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration',
                                    'privilege.view.fineTuneValidationConfiguration.onDevice','privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration'])) {
            var menuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'CFG', 'Administration'),
                href: me.getApplication().getController('Cfg.controller.history.Validation').tokenizeShowOverview(),
                portal: 'administration',
                glyph: 'settings',
                index: 3
            });

            Uni.store.MenuItems.add(menuItem);

            var portalItem1 = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.validation', 'CFG', 'Validation'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('general.validationRuleSets', 'CFG', 'Validation rule sets'),
                        href: '#/administration/validation/rulesets',
                        itemId: 'lnk-validation-rule-sets'
                    },
                    {
                        text: Uni.I18n.translate('dataValidationTasks.general.dataValidationTasks', 'CFG', 'Data validation tasks'),
                        href: '#/administration/datavalidationtasks',
                        itemId: 'lnk-data-validation-tasks'
                    }
                ]
            });


            Uni.store.PortalItems.add(
                portalItem1
            );

            this.getApplication().on('cfginitialized', function () {
                this.getController('Cfg.controller.Validation').mdcIsActive = true;
            });
        }
    },

    /**
     * @deprecated Fire an event instead, as shown below.
     */
    showContent: function (widget) {
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});
