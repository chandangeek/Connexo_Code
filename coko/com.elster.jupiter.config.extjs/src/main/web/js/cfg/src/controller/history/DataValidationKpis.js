/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.controller.history.DataValidationKpis', {
    extend: 'Uni.controller.history.Converter',
    requires: [
        'Cfg.privileges.Validation',
        'Uni.util.Application'
    ],

    rootToken: 'administration',
    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'CFG', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                datavalidationkpis: {
                    title: Uni.I18n.translate('dataValidationKPIs.general.dataValidationKPIs', 'CFG', 'Data validation KPIs'),
                    route: 'datavalidationkpis',
                    controller: 'Cfg.controller.DataValidationKpi',
                    privileges: Cfg.privileges.Validation.view,
                    disabled: typeof(MdmApp) != 'undefined',
                    action: 'showDataValidationKPIs',
                    items: {
                        add: {
                            title: Uni.I18n.translate('dataValidationKPIs.general.addDataValidationKPI', 'CFG', 'Add data validation KPI'),
                            route: 'add',
                            controller: 'Cfg.controller.DataValidationKpi',
                            privileges: Cfg.privileges.Validation.admin,
                            disabled: typeof(MdmApp) != 'undefined',
                            action: 'showAddDataValidationKpi'
                        }
                    }
                }
            }
        }
    },

    init: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
    }
});
