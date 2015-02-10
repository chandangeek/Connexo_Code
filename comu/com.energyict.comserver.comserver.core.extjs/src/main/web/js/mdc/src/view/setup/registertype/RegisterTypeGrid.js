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
    topPagging: true,
    bottomPagging: true,
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                xtype: 'reading-type-column',
                dataIndex: 'readingType',
                flex: 1.5
            },
            {
                xtype: 'obis-column',
                dataIndex: 'obisCode',
                flex: 1
            }
        ];
        if (me.withActions) {
            me.columns.push(
                {
                    xtype: 'uni-actioncolumn',
                    hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.masterData'),
                    items: 'Mdc.view.setup.registertype.RegisterTypeActionMenu'
                }
            );
        }
        if (me.withPaging) {
            me.dockedItems = [];
            if (me.topPagging) {
                if (me.topPagging instanceof Object) {
                    me.dockedItems.push(me.topPagging)
                } else {
                    me.dockedItems.push({
                        xtype: 'pagingtoolbartop',
                        store: me.store,
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
                                hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.masterData'),
                                itemId: 'createRegisterType',
                                xtype: 'button',
                                action: 'createRegisterType'
                            }
                        ]
                    });
                }
            }
            if (me.bottomPagging) {
                if (me.bottomPagging instanceof Object) {
                    me.dockedItems.push(me.bottomPagging)
                } else {
                    me.dockedItems.push({
                        xtype: 'pagingtoolbarbottom',
                        store: me.store,
                        dock: 'bottom',
                        itemsPerPageMsg: Uni.I18n.translate('registerTypes.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Register types per page')
                    })
                }
            }
        }
        me.callParent();
    }
})
;
