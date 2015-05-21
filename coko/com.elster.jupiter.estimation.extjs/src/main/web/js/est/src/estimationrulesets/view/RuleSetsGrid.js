Ext.define('Est.estimationrulesets.view.RuleSetsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.rule-sets-grid',
    overflowY: 'auto',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Est.estimationrulesets.view.ActionMenu'
    ],
    router: null,
    store: 'Est.estimationrulesets.store.EstimationRuleSetsStore',
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('estimationrulesets.estimationruleset', 'EST', 'Estimation rule set'),
                dataIndex: 'name',
                renderer: function (value, b, record) {
                    return '<a href="' +
                        me.router.getRoute('administration/estimationrulesets/estimationruleset').buildUrl({ruleSetId: record.getId()}) +
                        '">' +
                        Ext.String.htmlEncode(value) +
                        '</a>'
                },
                flex: 2
            },
            {
                header: Uni.I18n.translate('estimationrulesets.activeRules', 'EST', 'Active rules'),
                dataIndex: 'numberOfActiveRules',
                flex: 1
            },
            {
                header: Uni.I18n.translate('estimationrulesets.inactiveRules', 'EST', 'Inactive rules'),
                dataIndex: 'numberOfInactiveRules',
                flex: 1
            }
        ];
        if (Uni.Auth.hasAnyPrivilege(['privilege.administrate.EstimationConfiguration'])) {
            me.columns.push({
                xtype: 'uni-actioncolumn',
                privileges: Est.privileges.EstimationConfiguration.administrate,
                menu: {
                    xtype: 'estimation-rule-sets-action-menu'
                }
            })
        }
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('estimationrulesets.pagingtoolbartop.displayMsg', 'EST', '{0} - {1} of {2} estimation rule sets'),
                displayMoreMsg: Uni.I18n.translate('estimationrulesets.pagingtoolbartop.displayMoreMsg', 'EST', '{0} - {1} of more than {2} estimation rule sets'),
                emptyMsg: Uni.I18n.translate('estimationrulesets.pagingtoolbartop.emptyMsg', 'EST', 'There are no estimation rule sets to display'),
                items: [
                    {
                        text: Uni.I18n.translate('estimationrulesets.add.title', 'EST', 'Add estimation rule set'),
                        itemId: 'add-estimation-rule-set-button',
                        privileges: Est.privileges.EstimationConfiguration.administrate,
                        xtype: 'button',
                        href: me.router.getRoute(me.router.currentRoute + '/addruleset').buildUrl()
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('estimationrulesets.pagingtoolbarbottom.itemsPerPage', 'EST', 'Estimation rule sets per page')
            }
        ];

        me.callParent();
    }
});
