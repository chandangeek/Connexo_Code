Ext.define('Mdc.view.setup.registertype.RegisterTypeGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.registerTypeGrid',
    overflowY: 'auto',
    itemId: 'registertypegrid',
    selModel: {
        mode: 'SINGLE'
    },
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.RegisterTypes',
        'Mdc.view.setup.registertype.RegisterTypeActionMenu',
        'Uni.grid.column.Obis',
        'Uni.grid.column.ReadingType'
    ],
    store: 'RegisterTypes',
    withPaging: true,
    withActions: true,

    initComponent: function () {
        this.columns = [
            {
                header: Uni.I18n.translate('registerType.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 3
            },
            {
                xtype: 'reading-type-column',
                dataIndex: 'readingType'
            },
            {
                xtype: 'obis-column',
                dataIndex: 'obisCode'
            }
        ];
        if (this.withActions) {
            this.columns.push(
                {
                    xtype: 'uni-actioncolumn',
                    items: 'Mdc.view.setup.registertype.RegisterTypeActionMenu'
                }
            );
        }
        if (this.withPaging) {
            this.dockedItems = [
                {
                    xtype: 'pagingtoolbartop',
                    store: this.store,
                    dock: 'top',
                    displayMsg: Uni.I18n.translate('registerTypes.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} register types'),
                    displayMoreMsg: Uni.I18n.translate('registerTypes.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} register types'),
                    emptyMsg: Uni.I18n.translate('registerTypes.pagingtoolbartop.emptyMsg', 'MDC', 'There are no register types to display'),
                    items: [
                        {
                            xtype: 'component',
                            flex: 1
                        },
                        {
                            text: Uni.I18n.translate('registerType.addRegisterType', 'MDC', 'Add register type'),
                            itemId: 'createRegisterType',
                            xtype: 'button',
                            action: 'createRegisterType'
                        }
                    ]
                },
                {
                    xtype: 'pagingtoolbarbottom',
                    store: this.store,
                    dock: 'bottom',
                    itemsPerPageMsg: Uni.I18n.translate('registerTypes.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Register types per page')
                }
            ];
        }

        this.callParent();
    }
})
;
