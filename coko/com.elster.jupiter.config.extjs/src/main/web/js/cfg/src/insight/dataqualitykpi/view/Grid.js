/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.insight.dataqualitykpi.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.ins-data-quality-kpi-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.util.ScheduleToStringConverter',
        'Uni.grid.column.RemoveAction'
    ],
    store: 'Cfg.insight.dataqualitykpi.store.DataQualityKpis',
    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.uagePointGroup', 'CFG', 'Usage point group'),
                dataIndex: 'usagePointGroup',
                flex: 1,
                renderer: function (value) {
                    return value ? Ext.String.htmlEncode(value.name) : '-';
                }
            },
            {
                header: Uni.I18n.translate('general.Purpose', 'CFG', 'Purpose'),
                dataIndex: 'purpose',
                flex: 1
            },
            {
                header: Uni.I18n.translate('datavalidationkpis.calculationFrequency', 'CFG', 'Calculation frequency'),
                dataIndex: 'frequency',
                renderer: function (value) {
                    return Uni.util.ScheduleToStringConverter.convert(value);
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('datavalidationkpis.lastcalculated', 'CFG', 'Last calculated'),
                dataIndex: 'latestCalculationDate',
                flex: 1,
                renderer: function (value) {
                    if (value) {
                        return Uni.DateTime.formatDateTimeShort(value);
                    } else {
                        return Uni.I18n.translate('general.never', 'CFG', 'Never');
                    }
                }
            },
            {
                xtype: 'uni-actioncolumn-remove',
                privileges: Cfg.privileges.Validation.admin
            }

        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('dataqualitykpis.pagingtoolbartop.displayMsg', 'CFG', '{0} - {1} of {2} data quality KPIs'),
                displayMoreMsg: Uni.I18n.translate('dataqualitykpis.pagingtoolbartop.displayMoreMsg', 'CFG', '{0} - {1} of more than {2} data quality KPIs'),
                emptyMsg: Uni.I18n.translate('dataqualitykpis.pagingtoolbartop.emptyMsg', 'CFG', 'There are no data quality KPIs to display'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'add-data-quality-kpi-btn',
                        privileges: Cfg.privileges.Validation.admin,
                        text: Uni.I18n.translate('dataqualitykpis.add', 'CFG', 'Add data quality KPI'),
                        href: me.router.getRoute('administration/datavalidationkpis/add').buildUrl()
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('dataqualitykpis.pagingtoolbarbottom.kpisPerPage', 'CFG', 'Data quality KPIs per page')
            }
        ];

        me.callParent(arguments);
    }
});