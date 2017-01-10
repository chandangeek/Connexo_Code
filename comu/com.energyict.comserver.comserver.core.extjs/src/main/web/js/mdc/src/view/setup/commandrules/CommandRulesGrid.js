Ext.define('Mdc.view.setup.commandrules.CommandRulesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.commandRulesGrid',
    store: 'Mdc.store.CommandLimitationRules',
    router: undefined,

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.view.setup.commandrules.CommandRuleActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                sortable: false,
                menuDisabled: true,
                dataIndex: 'name',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return '<a href="#/administration/commandrules/' + record.get('id') + '">' + Ext.String.htmlEncode(value) + '</a>';
                }
            },
            {
                header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                sortable: false,
                menuDisabled: true,
                dataIndex: 'active',
                flex: 1,
                renderer: function (value, metaData, record) {
                    var pendingChanges = record.get('statusMessage'),
                        icon = Ext.isEmpty(pendingChanges) ? '' :
                                '<span class="icon-info" style="margin-left:10px; position:absolute;" data-qtip="' + pendingChanges + '"></span>',
                        text = value ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                    return text + icon;
                }
            },
            {
                itemId: 'action',
                sortable: false,
                menuDisabled: true,
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.CommandLimitationRules.view,
                menu: {
                    xtype: 'commandRuleActionMenu'
                }
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                displayMsg: Uni.I18n.translate('commandRules.display.msg', 'MDC', '{0} - {1} of {2} command limitation rules'),
                displayMoreMsg: Uni.I18n.translate('commandRules.display.more.msg', 'MDC', '{0} - {1} of more than {2} command limitation rules'),
                emptyMsg: Uni.I18n.translate('commandRules.empty.msg', 'MDC', 'No command limitation rules'),
                isFullTotalCount: true,
                dock: 'top',
                border: false,
                items: [
                    {
                        xtype: 'button',
                        itemId: 'mdc-add-command-rule-btn',
                        text: Uni.I18n.translate('commandRules.create', 'MDC', 'Add command limitation rule'),
                        privileges: Mdc.privileges.CommandLimitationRules.admin,
                        action: 'addCommandRule',
                        href: me.router.getRoute('administration/commandrules/add').buildUrl()
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                itemsPerPageMsg: Uni.I18n.translate('commandRules.items.per.page.msg', 'MDC', 'Command limitation rules per page'),
                store: me.store,
                dock: 'bottom'
            }
        ];
        me.callParent(arguments);
    }
});
