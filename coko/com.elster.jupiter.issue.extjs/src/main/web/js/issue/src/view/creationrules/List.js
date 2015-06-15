Ext.define('Isu.view.creationrules.List', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.layout.container.Column',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Isu.privileges.Issue'
    ],
    alias: 'widget.issues-creation-rules-list',
    store: 'Isu.store.CreationRules',
    columns: {
        items: [
            {
                itemId: 'Name',
                header: Uni.I18n.translate('general.title.name', 'ISU', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                itemId: 'templateColumn',
                header: Uni.I18n.translate('general.title.ruleTemplate', 'ISU', 'Rule template'),
                dataIndex: 'template_name',
                flex: 1
            },
            {
                itemId : 'issueType',
                header: Uni.I18n.translate('general.title.issueType', 'ISU', 'Issue type'),
                dataIndex: 'issueType_name',
                flex: 1
            },
            {   itemId: 'action',
                xtype: 'uni-actioncolumn',
                privileges: Isu.privileges.Issue.createRule,
                items: 'Isu.view.creationrules.ActionMenu'
            }
        ]
    },

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbartop.displayMsg', 'ISU', '{0} - {1} of {2} issue creation rules'),
                displayMoreMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbartop.displayMoreMsg', 'ISU', '{0} - {1} of more than {2} issue creation rules'),
                emptyMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbartop.emptyMsg', 'ISU', 'There are no issue creation rules to display'),
                items: [
                    {
                        itemId: 'createRule',
                        xtype: 'button',
                        text: Uni.I18n.translate('administration.issueCreationRules.add', 'ISU', 'Add rule'),
                        privileges: Isu.privileges.Issue.createRule,
                        href: '#/administration/creationrules/add',
                        action: 'create'
                    }
                ]
            },
            {
                itemId: 'pagingtoolbarbottom',
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbarbottom.itemsPerPage', 'ISU', 'Issue creation rules per page')
            }
        ];

        this.callParent(arguments);
    }
});