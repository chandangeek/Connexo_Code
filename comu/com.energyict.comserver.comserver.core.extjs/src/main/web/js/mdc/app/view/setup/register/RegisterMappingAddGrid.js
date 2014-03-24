Ext.define('Mdc.view.setup.register.RegisterMappingAddGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.registerMappingAddGrid',
    overflowY: 'auto',
    itemId: 'registermappingaddgrid',
    selModel: {
        mode: 'MULTI'
    },
    selType: 'checkboxmodel',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.AvailableRegisterTypes'
    ],
    nbrOfSelectedItems: 0,
    listeners: {
        selectionchange: function (view, selections, options) {
            this.nbrOfSelectedItems = selections.length;
            this.down('#pagingt').displayMsg = this.nbrOfSelectedItems + ' ' + Uni.I18n.translate('registerMappingsAdd.pagingtoolbartop.displayMsg', 'MDC', 'register types selected');
            this.down('#pagingt').onLoad();
        }
    },
    store: 'AvailableRegisterTypes',
    padding: '10 10 10 10',
    selModel: {
            checkOnly: true
        },
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('registerMappings.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 3,
                sortable: false,
                hideable: false
            },
            {
                xtype: 'actioncolumn',
                renderer: function (value, metaData, record) {
                    return '<div style="float:left; font-size: 13px; line-height: 1em;">'
                        + record.getReadingType().get('mrid') + '&nbsp' + '&nbsp'
                        + '</div>'
                },
                header: Uni.I18n.translate('registerMappings.readingType', 'MDC', 'Reading type'),
                flex: 2,
                items: [
                    {
                        icon: '../mdc/resources/images/information.png',
                        tooltip: Uni.I18n.translate('readingType.tooltip','MDC','Reading type info'),
                        handler: function (grid, rowIndex, colIndex, item, e, record, row) {
                            //var record = grid.getStore().getAt(rowIndex);
                            this.fireEvent('showReadingTypeInfo', record);
                        }
                    }
                ],
                tdCls: 'view',
                sortable: false,
                hideable: false
            },
            {
                header: Uni.I18n.translate('registerMappings.obisCode', 'MDC', 'OBIS code'),
                dataIndex: 'obisCode',
                flex: 1,
                sortable: false,
                hideable: false
            },
            {
                header: Uni.I18n.translate('registerMappings.type', 'MDC', 'Type'),
                renderer: function (value, metaData, record) {
                    return '<div style="float:left; font-size: 13px; line-height: 1em;">'
                        + record.getReadingType().get('measurementKind')
                        + '</div>'
                },
                flex: 1,
                sortable: false,
                hideable: false
            }
        ];
        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                itemId: 'pagingt',
                displayMsg: this.nbrOfSelectedItems + ' ' + Uni.I18n.translate('registerMappingsAdd.pagingtoolbartop.displayMsg', 'MDC', ' register types selected'),
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        xtype: 'component',
                        html:'<a href = "#/setup/registertypes" target = "_blank">' + Uni.I18n.translate('registerMappings.manageRegisterTypes', 'MDC', 'Manage register types') + '</a>'
                    }
                ]
            }
        ];

        this.callParent();
    }
});





