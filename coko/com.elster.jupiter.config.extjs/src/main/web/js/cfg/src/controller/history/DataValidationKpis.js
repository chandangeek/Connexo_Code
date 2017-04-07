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
                dataqualitykpis: {
                    title: Uni.I18n.translate('general.dataQualityKpis', 'CFG', 'Data quality KPIs'),
                    route: 'dataqualitykpis',
                    items: {
                        add: {
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
                    dataqualitykpis: {
                        controller: 'Cfg.controller.DataValidationKpi',
                        privileges: Cfg.privileges.Validation.viewOrAdministerDataQuality,
                        action: 'showDataValidationKPIs',
                        items: {
                            add: {
                                title: Uni.I18n.translate('dataqualitykpis.add', 'CFG', 'Add data quality KPI'),
                                controller: 'Cfg.controller.DataValidationKpi',
                                privileges: Cfg.privileges.Validation.administerDataQuality,
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
                    dataqualitykpis: {
                        controller: 'Cfg.insight.dataqualitykpi.controller.DataQualityKpiOverview',
                        privileges: Cfg.privileges.Validation.viewOrAdministerDataQuality,
                        action: 'showDataQualityKPIs',
                        items: {
                            add: {
                                title: Uni.I18n.translate('general.adddataqualitykpis', 'CFG', 'Add data quality KPIs'),
                                controller: 'Cfg.insight.dataqualitykpi.controller.DataQualityKpiAdd',
                                privileges: Cfg.privileges.Validation.administerDataQuality,
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
