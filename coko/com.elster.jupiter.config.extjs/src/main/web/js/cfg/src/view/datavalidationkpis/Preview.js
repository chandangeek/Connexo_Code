/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.datavalidationkpis.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.cfg-data-validation-kpis-preview',
    requires: [
        'Cfg.view.datavalidationkpis.ActionMenu',
        'Uni.util.ScheduleToStringConverter'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Cfg.privileges.Validation.admin,
            menu: {
                xtype: 'cfg-data-validation-kpis-action-menu',
                itemId: 'data-validation-kpis-preview-action-menu'
            }
        }
    ],

    items: [
        {
            xtype: 'form',
            itemId: 'data-validation-kpis-details-form',
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
                            fieldLabel: Uni.I18n.translate('general.deviceGroup', 'CFG', 'Device group'),
                            dataIndex: 'deviceGroup',
                            itemId: 'device-group-data-validation-kpis-preview',
                            flex: 1,
                            renderer: function (value) {
                                if (value) {
                                    return Ext.String.htmlEncode(value.name);
                                } else {
                                    return null;
                                }
                            }
                        },
                        {
                            name: 'frequency',
                            fieldLabel: Uni.I18n.translate('datavalidationkpis.calculationFrequency', 'CFG', 'Calculation frequency'),
                            itemId: 'frequency-data-validation-kpis-preview',
                            renderer: function (value) {
                                return value ? Uni.util.ScheduleToStringConverter.convert(value) : '';
                            }
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
                            name: 'latestCalculationDate',
                            itemId: 'latest-data-validation-kpis-preview',
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
        }
    ]
});