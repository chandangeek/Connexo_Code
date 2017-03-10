/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.messages.MessagesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.messages-grid',
    requires: [
        'Mdc.view.setup.messages.PrivilegesInfoPanel',
        'Uni.view.toolbar.PagingTop',
        'Mdc.store.MessagesGridStore'
    ],
    store: 'MessagesGridStore',
    ui: 'medium',
    hidden: true,
    style: {
        paddingLeft: 0,
        paddingRight: 0
    },
    columns: [
        {
            header: Uni.I18n.translate('commands.grid.name', 'MDC', 'Commands'),
            dataIndex: 'name',
            flex: 4
        },
        {
            header: Uni.I18n.translate('general.active', 'MDC', 'Active'),
            dataIndex: 'active',
            flex: 2,
            renderer: function (value) {
                return value ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No');
            }
        },
        {
            header: Uni.I18n.translate('messages.grid.privileges', 'MDC', 'Privileges'),
            dataIndex: 'privileges',
            flex: 3,
            renderer: function (value, metaData, record) {
                var result = '',
                    id = Ext.id();

                if (Ext.isArray(value)) {
                    Ext.each(value, function (item) {
                        result += Ext.String.htmlEncode(item.name.toString());
                        if (value.indexOf(item) != value.length - 1) result += ' - ';
                    });
                }

                Ext.defer(function () {
                    new Ext.button.Button({
                        renderTo: Ext.query('#' + id)[0],
                        name: 'messageInfoIcon',
                        text: '<span class="icon-info" style="display:inline-block; color:#A9A9A9; font-size:16px;"></span>',
                        ui: 'blank',
                        record: record
                    });
                }, 10);

                if (result.length > 0) {
                    result += Ext.String.format('<span style="margin-left:7px; padding:0;" id="{0}"></span>', id);
                }
                return Ext.isEmpty(result) ? '-' : result;
            }
        },
        {
            xtype: 'uni-actioncolumn',
            privileges: Mdc.privileges.DeviceType.admin,
            menu: {
                xtype: 'menu',
                itemId: 'messages-actionmenu',
                plain: true
            }
        }
    ],
    initComponent: function () {
        var me = this;
        me.dockedItems = [{
            xtype: 'pagingtoolbartop',
            dock: 'top',
            store: me.store,
            usesExactCount: true,
            displayMsg: Uni.I18n.translatePlural('commands.pagingtoolbartop.displayMsg', 0 ,'MDC', 'No commands', '{0} command', '{0} commands'),
            emptyMsg: Uni.I18n.translate('commands.pagingtoolbartop.emptyMsg', 'MDC', 'There are no commands'),
            items: [
                {
                    xtype: 'component'
                },
                {
                    xtype: 'uni-button-action',
                    privileges: Mdc.privileges.DeviceType.admin,
                    itemId: 'messages-actionbutton'
                }
            ]
        }


        ];
        me.callParent(arguments);
    }
});