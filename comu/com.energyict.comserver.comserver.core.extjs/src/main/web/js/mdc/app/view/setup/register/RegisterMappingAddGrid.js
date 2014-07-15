Ext.define('Mdc.view.setup.register.RegisterMappingAddGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.registerMappingAddGrid',
    overflowY: 'auto',
    itemId: 'registermappingaddgrid',
    selModel: {
        mode: 'MULTI',
        checkOnly: true
    },
    selType: 'checkboxmodel',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.AvailableRegisterTypes',
        'Uni.grid.column.Obis'
    ],
    nbrOfSelectedItems: 0,
    listeners: {
        selectionchange: function (view, selections, options) {
            this.nbrOfSelectedItems = selections.length;
            this.down('#pagingt').displayMsg = this.nbrOfSelectedItems + ' ' + Uni.I18n.translatePlural('registerMappingsAdd.pagingtoolbartop.displayMsg', this.nbrOfSelectedItems, 'MDC', 'register types selected');
            this.down('#pagingt').onLoad();
        }
    },
    plugins: {
        ptype: 'bufferedrenderer'
    },
    height: 400,
    store: 'AvailableRegisterTypes',

    columns: [
        {
            header: Uni.I18n.translate('registerMappings.name', 'MDC', 'Name'),
            dataIndex: 'name',
            flex: 3,
            sortable: false,
            fixed: true,
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
                    icon: '../mdc/resources/images/info.png',
                    iconCls: 'uni-info-icon',
                    tooltip: Uni.I18n.translate('readingType.tooltip','MDC','Reading type info'),
                    handler: function (grid, rowIndex, colIndex, item, e, record, row) {
                        //var record = grid.getStore().getAt(rowIndex);
                        this.fireEvent('showReadingTypeInfo', record);
                    }
                }
            ],
            tdCls: 'view',
            sortable: false,
            fixed: true,
            hideable: false
        },
        {
            xtype: 'obis-column',
            dataIndex: 'obisCode'
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
            fixed: true,
            hideable: false
        }
    ],

    tbar: {
        xtype: 'pagingtoolbartop',
        store: this.store,
        itemId: 'pagingt',
        displayMsg: this.nbrOfSelectedItems + ' ' + Uni.I18n.translatePlural('registerMappingsAdd.pagingtoolbartop.displayMsg', this.nbrOfSelectedItems, 'MDC', ' register types selected'),
        items: [
            {
                xtype: 'component',
                flex: 1
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('registerMappings.manageRegisterTypes', 'MDC', 'Manage register types'),
                ui: 'link',
                listeners: {
                    click: {
                        fn: function () {
                            window.location.href = '#/administration/registertypes';
                        }
                    }
                }
            }
        ]
    },

    initComponent: function () {
        var me = this;
        var store = Ext.data.StoreManager.lookup('AvailableRegisterTypes');
        store.getProxy().setExtraParam('filter',Ext.encode([{
            property:'available',
            value:true
        }]));

        this.callParent();
    }
});





