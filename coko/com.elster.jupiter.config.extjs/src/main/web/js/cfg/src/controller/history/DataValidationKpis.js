Ext.define('Cfg.controller.history.DataValidationKpis', {
    extend: 'Uni.controller.history.Converter',
    requires: [
        'Cfg.privileges.Validation'
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
                    action: 'showDataValidationKPIs',
                    items: {
                        add: {
                            title: Uni.I18n.translate('dataValidationKPIs.general.addDataValidationKPI', 'CFG', 'Add data validation KPI'),
                            route: 'add',
                            controller: 'Cfg.controller.DataValidationKpi',
                            privileges: Cfg.privileges.Validation.admin,
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
