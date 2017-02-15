/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.insight.dataqualitykpi.view.DetailsForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.ins-data-quality-kpi-details-form',
    requires: [
        'Cfg.view.datavalidationkpis.ActionMenu',
        'Uni.util.ScheduleToStringConverter'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            itemId: 'data-quality-kpi-details-action-btn',
            privileges: Cfg.privileges.Validation.admin,
            menu: {
                xtype: 'cfg-data-validation-kpis-action-menu',
                itemId: 'ins-data-quality-kpi-action-menu'
            }
        }
    ],

    layout: 'column',

    items: [
        {
            xtype: 'container',
            columnWidth: 0.5,
            layout: 'form',
            defaults: {
                xtype: 'displayfield',
                labelWidth: 250
            },
            items: [
                {
                    name: 'usagePointGroup',
                    itemId: 'data-quality-kpis-details-usage-point-group',
                    fieldLabel: Uni.I18n.translate('general.uagePointGroup', 'CFG', 'Usage point group')
                },
                {
                    name: 'purpose',
                    itemId: 'data-quality-kpis-details-purpose',
                    fieldLabel: Uni.I18n.translate('general.Purpose', 'CFG', 'Purpose')
                }
            ]
        },
        {
            xtype: 'container',
            columnWidth: 0.5,
            layout: 'form',
            defaults: {
                xtype: 'displayfield',
                labelWidth: 250
            },
            items: [
                {
                    name: 'frequency',
                    itemId: 'data-quality-kpis-details-frequency',
                    fieldLabel: Uni.I18n.translate('datavalidationkpis.calculationFrequency', 'CFG', 'Calculation frequency'),
                    renderer: function (value) {
                        return value ? Uni.util.ScheduleToStringConverter.convert(value) : '-';
                    }
                },
                {
                    name: 'latestCalculationDate',
                    itemId: 'data-quality-kpis-details-latest-calculation',
                    fieldLabel: Uni.I18n.translate('datavalidationkpis.lastcalculated', 'CFG', 'Last calculated'),
                    renderer: function (value) {
                        if (value) {
                            return Uni.DateTime.formatDateTimeLong(value);
                        } else {
                            return Uni.I18n.translate('general.never', 'CFG', 'Never');
                        }
                    }
                }
            ]
        }

    ]
});