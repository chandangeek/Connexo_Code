/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.registereddevices.view.RegisteredDevicesKPIsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.registered-devices-kpis-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.registereddevices.store.RegisteredDevicesKPIs',
        'Mdc.registereddevices.view.ActionMenu',
        'Mdc.privileges.RegisteredDevicesKpi'
    ],
    store: 'Mdc.registereddevices.store.RegisteredDevicesKPIs',
    initComponent: function () {
        this.columns = [
            {
                header: Uni.I18n.translate('general.deviceGroup', 'MDC', 'Device group'),
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
                header: Uni.I18n.translate('general.calculationFrequency', 'MDC', 'Calculation frequency'),
                dataIndex: 'frequency',
                renderer: function (value) {
                    return Mdc.util.ScheduleToStringConverter.convert(value);
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.target', 'MDC', 'Target'),
                dataIndex: 'target',
                flex: 1,
                renderer: function (value) {
                    if (!Ext.isEmpty(value)) {
                        return value + '%';
                    } else {
                        return 'No KPI';
                    }
                }

            },
            {
                header: Uni.I18n.translate('general.lastCalculated', 'MDC', 'Last calculated'),
                dataIndex: 'latestCalculationDate',
                flex: 1,
                renderer: function (value) {
                    if (value) {
                        return Uni.DateTime.formatDateTimeShort(value);
                    } else {
                        return Uni.I18n.translate('general.never', 'MDC', 'Never');
                    }
                }
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                privileges: Mdc.privileges.RegisteredDevicesKpi.admin,
                menu: {
                    xtype: 'registered-devices-kpi-action-menu',
                    itemId: 'mdc-grid-registered-devices-kpi-action-menu'
                }
            }
        ];

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('registeredDevicesKPIs.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} registered devices KPIs'),
                displayMoreMsg: Uni.I18n.translate('registeredDevicesKPIs.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} registered devices KPIs'),
                emptyMsg: Uni.I18n.translate('registeredDevicesKPIs.pagingtoolbartop.emptyMsg', 'MDC', 'There are no registered devices KPIs to display'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'mdc-grid-registered-devices-kpi-add',
                        privileges: Mdc.privileges.RegisteredDevicesKpi.admin,
                        text: Uni.I18n.translate('registeredDevicesKPIs.add', 'MDC', 'Add registered devices KPI'),
                        action: 'addRegisteredDevicesKpi'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('registeredDevicesKPIs.pagingtoolbarbottom.kpisPerPage', 'MDC', 'Registered devices KPIs per page')
            }
        ];

        this.callParent(arguments);
    }
});

