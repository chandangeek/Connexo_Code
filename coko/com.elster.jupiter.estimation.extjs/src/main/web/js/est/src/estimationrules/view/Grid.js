Ext.define('Est.estimationrules.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.estimation-rules-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.grid.column.Action',
        'Est.estimationrules.view.ActionMenu'
    ],
    store: 'Est.estimationrules.store.Rules',
    router: null,
    actionMenuItemId: null,
    showButtons: true,
    showBottomPaging: false,
    editOrder: false,
    initComponent: function () {
        var me = this,
            buttons = [];

        me.columns = [
            {
                header: Uni.I18n.translate('general.order', 'EST', 'Order'),
                renderer: function (value, metaData, record, rowIndex) {
                    return ++rowIndex;
                }
            },
            {
                header: Uni.I18n.translate('general.estimationRule', 'EST', 'Estimation rule'),
                dataIndex: 'name',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return '<a href="' + me.router.getRoute('administration/estimationrulesets/estimationruleset/rules/rule').buildUrl({ruleId: record.getId()}) +'">' + Ext.String.htmlEncode(value) + '</a>';
                }
            },
            {
                header: Uni.I18n.translate('general.status', 'EST', 'Status'),
                dataIndex: 'active',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.active', 'EST', 'Active') : Uni.I18n.translate('general.inactive', 'EST', 'Inactive');
                }
            }
        ];

        if (me.editOrder) {
            me.viewConfig = {
                plugins: {
                    ptype: 'gridviewdragdrop',
                    dragText: '&nbsp;'
                },
                listeners: {
                    drop: {
                        fn: function () {
                            me.getView().refresh();
                        }
                    }
                }
            };
            me.selModel = {
                mode: 'MULTI'
            };
            me.columns.push({
                header: Uni.I18n.translate('general.ordering', 'EST', 'Ordering'),
                align: 'center',
                renderer: function () {
                    return '<span class="icon-stack3"></span>';
                }
            });
            if (me.showButtons) {
                buttons = [
                    {
                        xtype: 'button',
                        itemId: 'btn-save-estimation-rules-order',
                        text: Uni.I18n.translate('general.saveOrder', 'EST', 'Save order'),
                        action: 'saveEstimationRulesOrder',
                        hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.EstimationConfiguration')
                    },
                    {
                        xtype: 'button',
                        itemId: 'btn-undo-estimation-rules-order',
                        text: Uni.I18n.translate('general.undo', 'EST', 'Undo'),
                        action: 'undoEstimationRulesOrder',
                        href: me.router.getRoute(me.router.currentRoute).buildUrl(me.router.arguments, null),
                        hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.EstimationConfiguration')
                    }
                ]
            }
        } else {
            me.columns.push({
                xtype: 'uni-actioncolumn',
                privileges: Est.privileges.EstimationConfiguration.administrate,
                menu: {
                    xtype: 'estimation-rules-action-menu',
                    itemId: me.actionMenuItemId
                }
            });
            if (me.showButtons) {
                buttons = [
                    {
                        xtype: 'button',
                        itemId: 'btn-edit-order-estimation-rules',
                        text: Uni.I18n.translate('general.editOrder', 'EST', 'Edit order'),
                        action: 'editOrderEstimationRules',
                        href: me.router.getRoute('administration/estimationrulesets/estimationruleset/rules').buildUrl(me.router.arguments, {editOrder: true}),
                        hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.EstimationConfiguration')
                    },
                    {
                        xtype: 'button',
                        itemId: 'btn-add-estimation-rule',
                        text: Uni.I18n.translate('estimationrules.addEstimationRule', 'EST', 'Add estimation rule'),
                        action: 'addEstimationRule',
                        href: me.router.getRoute('administration/estimationrulesets/estimationruleset/rules/add').buildUrl(),
                        privileges: Est.privileges.EstimationConfiguration.administrate
                    }
                ]
            }
        }

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                noBottomPaging: true,
                usesExactCount: true,
                displayMsg: Uni.I18n.translate('estimationrules.pagingtoolbartop.displayMsg', 'EST', '{0} - {1} of {2} estimation rules'),
                displayMoreMsg: Uni.I18n.translate('estimationrules.pagingtoolbartop.displayMoreMsg', 'EST', '{0} - {1} of more than {2} estimation rules'),
                emptyMsg: Uni.I18n.translate('estimationrules.pagingtoolbartop.emptyMsg', 'EST', 'There are no estimation rules to display'),
                items: buttons
            }
        ];
        if (me.showBottomPaging) {
            me.dockedItems.push({
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                deferLoading: true,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('estimationrules.pagingtoolbarbottom.itemsPerPage', 'EST', 'Estimation rules per page')
            })
        }
        me.callParent(arguments);
    }
});