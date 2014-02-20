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
            this.down('#pagingt').displayMsg = this.nbrOfSelectedItems + ' ' + I18n.translate('registerMappingsAdd.pagingtoolbartop.displayMsg', 'MDC', 'register types selected');
            this.down('#pagingt').onLoad();
        }
    },
    store: 'AvailableRegisterTypes',
    padding: '10 10 10 10',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: I18n.translate('registerMappings.name', 'MDC', 'Name'),
                dataIndex: 'name',
                sortable: false,
                hideable: false,
                renderer: function (value, b, record) {
                    return '<a href="#setup/devicetypes/' + me.deviceTypeId + '/registertypes/' + record.get('id') + '">' + value + '</a>';
                },
                flex: 2
            },
            {
                xtype: 'actioncolumn',
                renderer: function (value, metaData, record) {
                    return '<div style="float:left; font-size: 13px; line-height: 1em;">'
                        + record.get('mrid') + '&nbsp' + '&nbsp'
                        + '</div>'
                },
                header: I18n.translate('registerMappings.readingType', 'MDC', 'Reading type'),
                flex: 2,
                items: [
                    {
                        icon: 'resources/images/gear-16x16.png',
                        tooltip: 'Reading type info',
                        handler: function (grid, rowIndex, colIndex, item, e) {
                            var record = grid.getStore().getAt(rowIndex);
                            this.fireEvent('showReadingTypeInfo', record);
                        }
                    }
                ],
                tdCls: 'view',
                sortable: false,
                hideable: false
            },
            {
                header: I18n.translate('registerMappings.obisCode', 'MDC', 'OBIS code'),
                dataIndex: 'obisCode',
                flex: 1,
                sortable: false,
                hideable: false
            },
            {
                header: I18n.translate('registerMappings.type', 'MDC', 'Type'),
                dataIndex: 'measurementKind',
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
                displayMsg: this.nbrOfSelectedItems + ' ' + I18n.translate('registerMappingsAdd.pagingtoolbartop.displayMsg', 'MDC', ' register types selected'),
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        text: 'Manage register types',
                        itemId: 'manageRegisterMappingBtn',
                        xtype: 'button',
                        href: '',
                        hrefTarget: '_self',
                        action: 'manageRegisters'
                    },
                    {
                        text: 'Create register types',
                        itemId: 'createRegisterMappingBtn',
                        xtype: 'button',
                        href: '',
                        hrefTarget: '_self',
                        action: 'createRegisterMapping'
                    }
                ]
            }
        ];

        this.callParent();
    }
});





