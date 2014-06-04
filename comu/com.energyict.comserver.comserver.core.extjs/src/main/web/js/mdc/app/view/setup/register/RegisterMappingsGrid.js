Ext.define('Mdc.view.setup.register.RegisterMappingsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.registerMappingsGrid',
    overflowY: 'auto',
    deviceTypeId: null,
    itemId: 'registermappinggrid',
    selModel: {
        mode: 'SINGLE'
    },
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.RegisterTypesOfDevicetype'
    ],
    store: 'RegisterTypesOfDevicetype',
    //padding: '10 10 10 10',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('registerMappings.name', 'MDC', 'Name'),
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
                header: Uni.I18n.translate('registerMappings.readingType', 'MDC', 'Reading type'),
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
                flex: 2,
                tdCls: 'view',
                width: 300
            },
            {
                header: Uni.I18n.translate('registerMappings.obisCode', 'MDC', 'OBIS code'),
                dataIndex: 'obisCode',
                flex: 1
            },

            {
                xtype: 'uni-actioncolumn',
                items: [
                    {
                        text: Uni.I18n.translate('registerMappings.remove', 'MDC', 'Remove'),
                        action: 'removeItem'
                    }

                ]
            }
        ];
        this.dockedItems = [
            {

                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('registerMappings.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} register types'),
                displayMoreMsg: Uni.I18n.translate('registerMappings.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} register types'),
                emptyMsg: Uni.I18n.translate('registerMappings.pagingtoolbartop.emptyMsg', 'MDC', 'There are no register types to display'),
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {

                        text: Uni.I18n.translate('registerMapping.addRegisterMapping', 'MDC', 'Add register types'),
                        itemId: 'addRegisterMappingBtn',
                        xtype: 'button',
                        href: '#/administration/devicetypes/' + this.deviceTypeId + '/registertypes/add',
                        hrefTarget: '_self',
                        action: 'addRegisterMapping'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                params: [
                    {deviceType: this.deviceTypeId}
                ],
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('registerTypes.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Register types per page')
            }
        ];

        this.callParent();
    }
});
