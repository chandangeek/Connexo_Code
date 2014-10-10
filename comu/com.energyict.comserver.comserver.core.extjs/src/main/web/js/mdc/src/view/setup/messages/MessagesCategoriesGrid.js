Ext.define('Mdc.view.setup.messages.MessagesCategoriesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.messages-categories-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DeviceConfigMessages'
    ],
    store: 'DeviceConfigMessages',
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('messages.category.name', 'MDC', 'Message category'),
                dataIndex: 'DeviceMessageCategory',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'menu',
                    itemId: 'messages-categories-actionmenu',
                    plain: true
                }
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('messages.pagingtoolbartop.displayMsgMessageCategories', 'MDC', '{0} - {1} of {2} message categories'),
                displayMoreMsg: Uni.I18n.translate('messages.pagingtoolbartop.displayMoreMsgMessageCategories', 'MDC', '{0} - {1} of more than {2} message categories'),
                emptyMsg: Uni.I18n.translate('messages.pagingtoolbartop.emptyMsgMessageCategories', 'MDC', 'There are no message categories to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('message.pagingtoolbarbottom.itemsPerPageMessageCategories', 'MDC', 'Message categories per page'),
                dock: 'bottom'
            }
        ];
        me.callParent(arguments);
    }
});