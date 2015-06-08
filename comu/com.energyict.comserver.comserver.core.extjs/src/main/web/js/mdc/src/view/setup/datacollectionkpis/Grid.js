Ext.define('Mdc.view.setup.datacollectionkpis.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.dataCollectionKpisGrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.view.setup.datacollectionkpis.ActionMenu'
    ],
    store: 'Mdc.store.DataCollectionKpis',
    initComponent: function () {
        this.columns = [
            {
                header: Uni.I18n.translate('datacollectionkpis.deviceGroup', 'MDC', 'Device group'),
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
                header: Uni.I18n.translate('datacollectionkpis.calculationFrequency', 'MDC', 'Calculation frequency'),
                dataIndex: 'frequency',
                renderer: function (value) {
                    return Mdc.util.ScheduleToStringConverter.convert(value);
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('datacollectionkpis.connectiontarget', 'MDC', 'Connection target'),
                dataIndex: 'connectionTarget',
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
                header: Uni.I18n.translate('datacollectionkpis.communicationtarget', 'MDC', 'Communication target'),
                dataIndex: 'communicationTarget',
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
                header: Uni.I18n.translate('datacollectionkpis.lastcalculated', 'MDC', 'Last calculated'),
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
                menu: {
                    xtype: 'dataCollectionKpisActionMenu',
                    itemId: 'dataCollectionKpisGridActionMenu'
                }
            }

        ];

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('datacollectionkpis.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} data collection KPIs'),
                displayMoreMsg: Uni.I18n.translate('datacollectionkpis.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} data collection KPIs'),
                emptyMsg: Uni.I18n.translate('datacollectionkpis.pagingtoolbartop.emptyMsg', 'MDC', 'There are no data collection KPIs to display'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'btn-data-collection-kpi',
                        text: Uni.I18n.translate('datacollectionkpis.add', 'MDC', 'Add data collection KPI'),
                        action: 'addDataCollectionKpi'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('datacollectionkpis.pagingtoolbarbottom.kpisPerPage', 'MDC', 'Data collection KPIs per page')
            }
        ];

        this.callParent(arguments);
    }
});
