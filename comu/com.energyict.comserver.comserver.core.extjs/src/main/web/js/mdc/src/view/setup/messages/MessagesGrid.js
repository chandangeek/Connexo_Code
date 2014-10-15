Ext.define('Mdc.view.setup.messages.MessagesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.messages-grid',
    requires: [
        'Mdc.view.setup.messages.PrivilegesInfoPanel',
        'Uni.view.toolbar.PagingTop'
    ],
    ui: 'medium',
    hidden: true,
    style: {
        paddingLeft: 0,
        paddingRight: 0
    },
    columns: [
        {
            header: Uni.I18n.translate('messages.grid.name', 'MDC', 'Messages'),
            dataIndex: 'name',
            flex: 4
        },
        {
            header: Uni.I18n.translate('messages.grid.Active', 'MDC', 'Active'),
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
            renderer: function (value) {
                var result = '',
                    id = Ext.id();

                if (Ext.isArray(value)) {
                    Ext.each(value, function (item) {
                        result += item.name.toString();
                        if (value.indexOf(item) != value.length - 1) result += ' - ';
                    });
                }

                Ext.defer(function () {
                    new Ext.button.Button({
                        renderTo: Ext.query('#' + id)[0],
                        name: 'messageInfoIcon',
                        iconCls: 'icon-info-small',
                        cls: 'uni-btn-transparent',
                        style: {
                            width: '16px',
                            display: 'inline-block',
                            textDecoration: 'none !important'
                        }
                    });
                }, 10);

                result = result.length > 0 ?
                    result + Ext.String.format('<span style="margin: 0 0 0 20px; padding: 0;" id="{0}"></span>', id) : result;

                return result;
            }
        },
        {
            xtype: 'uni-actioncolumn',
            menu: {
                xtype: 'menu',
                itemId: 'messages-actionmenu',
                plain: true
            }
        }
    ],
    initComponent: function () {
        var me = this;
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('messages.pagingtoolbartop.displayMsgs', 'MDC', '{0} - {1} of {2} messages'),
                displayMoreMsg: Uni.I18n.translate('messages.pagingtoolbartop.displayMoreMsgs', 'MDC', '{0} - {1} of more than {2} messages'),
                emptyMsg: Uni.I18n.translate('messages.pagingtoolbartop.emptyMsgsMessage', 'MDC', 'There are no messages to display'),
                items: [
                    '->',
                    {
                        xtype: 'button',
                        itemId: 'messages-actionbutton',
                        text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                        iconCls: 'x-uni-action-iconD'
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});