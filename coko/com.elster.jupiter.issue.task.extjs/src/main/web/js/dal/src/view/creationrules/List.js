/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.creationrules.List', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.layout.container.Column',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    alias: 'widget.issues-creation-rules-list',
    store: 'Itk.store.CreationRules',
    columns: {
        items: [
            {
                itemId: 'Name',
                header: Uni.I18n.translate('general.title.name', 'ITK', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                itemId: 'templateColumn',
                header: Uni.I18n.translate('general.title.ruleTemplate', 'ITK', 'Rule template'),
                dataIndex: 'template_name',
                flex: 1
            },
            {
                itemId: 'statusColumn',
                header: Uni.I18n.translate('general.title.status', 'ITK', 'Status'),
                dataIndex: 'active',
                renderer: function (value) {
                    return value ? Uni.I18n.translate('administration.issueCreationRules.active', 'ITK', 'Active')
                        : Uni.I18n.translate('administration.issueCreationRules.inactive', 'ITK', 'Inactive');
                },
                flex: 1
            },
            {   itemId: 'action',
                xtype: 'uni-actioncolumn',
                privileges: Itk.privileges.Issue.createIssueRule,
                menu: { xtype: 'issue-creation-rule-action-menu' }
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
                displayMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbartop.displayMsg', 'ITK', '{0} - {1} of {2} issue creation rules'),
                displayMoreMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbartop.displayMoreMsg', 'ITK', '{0} - {1} of more than {2} issue creation rules'),
                emptyMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbartop.emptyMsg', 'ITK', 'There are no issue creation rules to display'),
                items: [
                    {
                        itemId: 'createRule',
                        xtype: 'button',
                        text: Uni.I18n.translate('administration.issueCreationRules.add', 'ITK', 'Add rule'),
                        privileges: Itk.privileges.Issue.createIssueRule,
                        href: '#/administration/issuecreationrules/add',
                        action: 'create'
                    }
                ]
            },
            {
                itemId: 'pagingtoolbarbottom',
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbarbottom.itemsPerPage', 'ITK', 'Issue creation rules per page')
            }
        ];

        this.callParent(arguments);
    }
});