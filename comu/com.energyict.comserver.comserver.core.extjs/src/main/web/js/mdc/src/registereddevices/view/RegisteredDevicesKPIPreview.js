/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.registereddevices.view.RegisteredDevicesKPIPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.registered-devices-kpi-preview',
    requires: [
        'Mdc.registereddevices.view.ActionMenu',
        'Mdc.privileges.RegisteredDevicesKpi'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Mdc.privileges.RegisteredDevicesKpi.admin,
            menu: {
                xtype: 'registered-devices-kpi-action-menu',
                itemId: 'mdc-registered-devices-kpi-details-action-menu'
            }
        }
    ],

    items: [
        {
            xtype: 'form',
            itemId: 'mdc-registered-devices-kpi-details-form',
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
                            fieldLabel: Uni.I18n.translate('general.deviceGroup', 'MDC', 'Device group'),
                            dataIndex: 'deviceGroup',
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
                            fieldLabel: Uni.I18n.translate('general.calculationFrequency', 'MDC', 'Calculation frequency'),
                            renderer: function (value) {
                                return value ? Mdc.util.ScheduleToStringConverter.convert(value) : '';
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
                            name: 'target',
                            fieldLabel: Uni.I18n.translate('general.target', 'MDC', 'Target'),
                            renderer: function (value) {
                                if (!Ext.isEmpty(value)) {
                                    return value + '%';
                                } else {
                                    return 'No KPI';
                                }
                            }

                        },
                        {
                            name: 'latestCalculationDate',
                            fieldLabel: Uni.I18n.translate('general.lastCalculated', 'MDC', 'Last calculated'),
                            renderer: function (value) {
                                if (value) {
                                    return Uni.DateTime.formatDateTimeLong(value);
                                } else {
                                    return Uni.I18n.translate('general.never', 'MDC', 'Never');
                                }
                            }
                        }
                    ]
                }

            ]
        }
    ]
});
