/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        'Cfg.controller.Log',
        'Cfg.insight.dataqualitykpi.controller.DataQualityKpiOverview',
        'Cfg.insight.dataqualitykpi.controller.DataQualityKpiAdd'
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
            validationKpiRoute = router.getRoute('administration/dataqualitykpis');

        if (Cfg.privileges.Validation.canView() || Cfg.privileges.Validation.canViewOrAdministerDataQuality()) {
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
                        itemId: 'lnk-validation-rule-sets',
                        privileges: Cfg.privileges.Validation.view
                    },
                    {
                        text: Uni.I18n.translate('validationTasks.general.validationTasks', 'CFG', 'Validation tasks'),
                        href: '#/administration/validationtasks',
                        hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration', 'privilege.view.validationConfiguration'),
                        itemId: 'lnk-validation-tasks',
                        privileges: Cfg.privileges.Validation.view
                    },
                    {
                        text: validationKpiRoute.getTitle(),
                        href: validationKpiRoute.buildUrl(),
                        itemId: 'lnk-data-validation-kpis',
                        privileges: Cfg.privileges.Validation.viewOrAdministerDataQuality
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
    }
});
