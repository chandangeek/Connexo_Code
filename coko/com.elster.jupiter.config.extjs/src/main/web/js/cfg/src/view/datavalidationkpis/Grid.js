Ext.define('Cfg.view.datavalidationkpis.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.cfg-data-validation-kpis-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Cfg.view.datavalidationkpis.ActionMenu',
        'Uni.util.ScheduleToStringConverter'
    ],
    store: 'Cfg.store.DataValidationKpis',
    initComponent: function () {
        this.columns = [
            {
                header: Uni.I18n.translate('general.deviceGroup', 'CFG', 'Device group'),
                dataIndex: 'deviceGroup',
                flex: 1,
                renderer: function (value) {
                    return value ? Ext.String.htmlEncode(value.name) : '';
                }
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
                xtype: 'actioncolumn',
                privileges: Cfg.privileges.Validation.admin,
                align: 'right',
                header: Uni.I18n.translate('general.actions', 'CFG', 'Actions'),
                items: [
                    {
                        iconCls: 'uni-icon-delete',
                        itemId: 'cfg-data-validation-kpis-action-menu',
                        tooltip: Uni.I18n.translate('general.remove', 'CFG', 'Remove'),
                        handler: function (grid, rowIndex, colIndex, column, event, record) {
                            this.fireEvent('remove', record);
                        }
                    }
                ]
            }

        ];

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('datavalidationkpis.pagingtoolbartop.displayMsg', 'CFG', '{0} - {1} of {2} data validation KPIs'),
                displayMoreMsg: Uni.I18n.translate('datavalidationkpis.pagingtoolbartop.displayMoreMsg', 'CFG', '{0} - {1} of more than {2} data validation KPIs'),
                emptyMsg: Uni.I18n.translate('datavalidationkpis.pagingtoolbartop.emptyMsg', 'CFG', 'There are no data validation KPIs to display'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'btn-data-validation-kpi',
                        privileges: Cfg.privileges.Validation.admin,
                        text: Uni.I18n.translate('datavalidationkpis.add', 'CFG', 'Add data validation KPI'),
                        action: 'addDataValidationKpi'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('datavalidationkpis.pagingtoolbarbottom.kpisPerPage', 'CFG', 'Data validation KPIs per page')
            }
        ];

        this.callParent(arguments);
    }
});
