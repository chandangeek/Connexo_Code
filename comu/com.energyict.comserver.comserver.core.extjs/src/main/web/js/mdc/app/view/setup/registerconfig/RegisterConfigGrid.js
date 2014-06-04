Ext.define('Mdc.view.setup.registerconfig.RegisterConfigGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.registerConfigGrid',
    overflowY: 'auto',
    deviceTypeId: null,
    deviceConfigId: null,
    itemId: 'registerconfiggrid',
    selModel: {
        mode: 'SINGLE'
    },
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.RegisterConfigsOfDeviceConfig'
    ],
    store: 'RegisterConfigsOfDeviceConfig',
    //padding: '10 10 10 10',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('registerConfigs.name', 'MDC', 'Register type'),
                dataIndex: 'name',
                flex: 3
            },
            {
                xtype: 'actioncolumn',
                renderer: function (value, metaData, record) {
                    return '<div style="float:left; font-size: 13px; line-height: 1em;">'
                        + record.getReadingType().get('mrid') + '&nbsp' + '&nbsp'
                        + '</div>'
                },
                header: Uni.I18n.translate('registerConfigs.readingType', 'MDC', 'Reading type'),
                items: [
                    {
                        icon: '../mdc/resources/images/info.png',
                        iconCls: 'uni-info-icon',
                        tooltip: Uni.I18n.translate('readingType.tooltip', 'MDC', 'Reading type info'),
                        handler: function (grid, rowIndex, colIndex, item, e, record, row) {
                            //var record = grid.getStore().getAt(rowIndex);
                            this.fireEvent('showReadingTypeInfo', record);
                        }
                    }
                ],
                width: 300,
                tdCls: 'view'
            },
            {
                header: Uni.I18n.translate('registerConfigs.obisCode', 'MDC', 'OBIS code'),
                dataIndex: 'overruledObisCode',
                flex: 1
            },

            {
                xtype: 'uni-actioncolumn',
                items: [
                    {
                        text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                        action: 'editItem'
                    },
                    {
                        text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                        action: 'deleteItem'
                    }

                ]
            }

        ];
        this.dockedItems = [
            {

                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('registerConfigs.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} register configurations'),
                displayMoreMsg: Uni.I18n.translate('registerConfigs.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} register configurations'),
                emptyMsg: Uni.I18n.translate('registerConfigs.pagingtoolbartop.emptyMsg', 'MDC', 'There are no register configurations to display'),
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {

                        text: Uni.I18n.translate('registerConfigs.createRegisterConfig', 'MDC', 'Add register configuration'),
                        itemId: 'createRegisterConfigBtn',
                        xtype: 'button',
                        action: 'createRegisterConfig'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                params: [
                    {deviceType: this.deviceTypeId},
                    {deviceConfig: this.deviceConfigId}
                ],
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('registerConfigs.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Register configurations per page')
            }
        ];

        this.callParent();
    }
});
