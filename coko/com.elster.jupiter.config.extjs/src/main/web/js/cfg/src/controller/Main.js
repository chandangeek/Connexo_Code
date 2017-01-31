Ext.define('Cfg.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.Navigation',
        'Uni.Auth',
        'Cfg.privileges.Validation'
    ],

    controllers: [
        'Cfg.controller.history.Validation',
        'Cfg.controller.history.EventType',
        'Cfg.controller.history.ValidationTask',
        'Cfg.controller.history.DataValidationKpis',
        'Cfg.controller.DataValidationKpi',
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

        this.control({
            'ruleSetSubMenu': {
                beforerender: this.onRuleSetMenuBeforeRender
            }
        });

        this.callParent();
    },

    /**
     * Forces history registration.
     */
    initHistorians: function () {
        this.getController('Cfg.controller.history.Validation');
        this.getController('Cfg.controller.history.ValidationTask');
        this.getController('Cfg.controller.history.DataValidationKpis');
    },

    initMenu: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            validationKpiRoute = router.getRoute('administration/datavalidationkpis');

        if (Cfg.privileges.Validation.canView()) {
            var menuItem = Ext.create('Uni.model.MenuItem', {
                text: Uni.I18n.translate('general.administration', 'CFG', 'Administration'),
                href: me.getApplication().getController('Cfg.controller.history.Validation').tokenizeShowOverview(),
                portal: 'administration',
                glyph: 'settings',
                index: 3
            });

            Uni.store.MenuItems.add(menuItem);

            var portalItem1 = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.validation', 'CFG', ' Data Validation'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('general.validationRuleSets', 'CFG', 'Validation rule sets'),
                        href: '#/administration/validation/rulesets',
                        itemId: 'lnk-validation-rule-sets'
                    },
                    {
                        text: Uni.I18n.translate('validationTasks.general.validationTasks', 'CFG', 'Validation tasks'),
                        href: '#/administration/validationtasks',
                        hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration'),
                        itemId: 'lnk-validation-tasks'
                    },
                    {
                        text: validationKpiRoute.getTitle(),
                        href: validationKpiRoute.buildUrl(),
                        hidden: validationKpiRoute.disabled,
                        itemId: 'lnk-data-validation-kpis'
                    }
                ]
            });


            Uni.store.PortalItems.add(
                portalItem1
            );
        }
    },

    /**
     * @deprecated Fire an event instead, as shown below.
     */
    showContent: function (widget) {
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    onRuleSetMenuBeforeRender: function (menu) {
        this.getApplication().fireEvent('validationrulesetmenurender', menu);
    }
});
