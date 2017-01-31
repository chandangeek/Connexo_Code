/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                header: Uni.I18n.translate('commands.category.name', 'MDC', 'Command category'),
                dataIndex: 'name',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.DeviceType.admin,
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
                displayMsg: Uni.I18n.translate('commands.pagingtoolbartop.displayMsgMessageCategories', 'MDC', '{0} - {1} of {2} command categories'),
                displayMoreMsg: Uni.I18n.translate('commands.pagingtoolbartop.displayMoreMsgMessageCategories', 'MDC', '{0} - {1} of more than {2} command categories'),
                emptyMsg: Uni.I18n.translate('commands.pagingtoolbartop.emptyMsgMessageCategories', 'MDC', 'There are no command categories to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('commands.pagingtoolbarbottom.itemsPerPageMessageCategories', 'MDC', 'Command categories per page'),
                dock: 'bottom'
            }
        ];
        me.callParent(arguments);
    }
});