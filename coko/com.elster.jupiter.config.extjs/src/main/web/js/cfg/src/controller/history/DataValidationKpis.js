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

    commonConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'CFG', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                datavalidationkpis: {
                    title: Uni.I18n.translate('general.dataQualityKpis', 'CFG', 'Data quality KPIs'),
                    route: 'datavalidationkpis',
                    items: {
                        add: {
                            title: Uni.I18n.translate('dataqualitykpis.add', 'CFG', 'Add data quality KPI'),
                            route: 'add'
                        }
                    }
                }
            }
        }
    },
    configPerApp: {
        'MultiSense': {
            administration: {
                items: {
                    datavalidationkpis: {
                        controller: 'Cfg.controller.DataValidationKpi',
                        privileges: Cfg.privileges.Validation.view,
                        action: 'showDataValidationKPIs',
                        items: {
                            add: {
                                controller: 'Cfg.controller.DataValidationKpi',
                                privileges: Cfg.privileges.Validation.admin,
                                action: 'showAddDataValidationKpi'
                            }
                        }
                    }
                }
            }
        },
        'MdmApp': {
            administration: {
                items: {
                    datavalidationkpis: {
                        controller: 'Cfg.insight.dataqualitykpi.controller.DataQualityKpiOverview',
                        privileges: Cfg.privileges.Validation.view,
                        action: 'showDataQualityKPIs',
                        items: {
                            add: {
                                controller: 'Cfg.insight.dataqualitykpi.controller.DataQualityKpiAdd',
                                privileges: Cfg.privileges.Validation.admin,
                                action: 'showAddDataQualityKpi'
                            }
                        }
                    }
                }
            }
        }
    },

    init: function () {
        var me = this,
            appName = Uni.util.Application.getAppName();

        me.routeConfig = Ext.merge(me.commonConfig, me.configPerApp[appName]);

        me.callParent(arguments);
    }
});
