Ext.define('Isu.view.administration.datacollection.issuecreationrules.List', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.layout.container.Column',
        'Ext.grid.column.Template',
        'Ext.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    alias: 'widget.issues-creation-rules-list',
    border: false,
    itemId: 'createRuleGrid',
    xtype: 'grid',
    store: 'Isu.store.CreationRule',
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'Name',
                header: 'Name',
                dataIndex: 'name',
                tdCls: 'isu-grid-description',
                flex: 1
            },
            {
                itemId: 'templateColumn',
                header: 'Rule template',
                xtype: 'templatecolumn',
                tpl: '<tpl if="template">{template.name}</tpl>',
                tdCls: 'isu-grid-description',
                flex: 1
            },
            {
                itemId: 'issueType',
                header: 'Issue type',
                xtype: 'templatecolumn',
                tpl: '<tpl if="issueType">{issueType.name}</tpl>',
                tdCls: 'isu-grid-description',
                flex: 1
            },
            {   itemId: 'action',
                xtype: 'uni-actioncolumn',
                items: 'Isu.view.administration.datacollection.issuecreationrules.ActionMenu'
            }
        ]
    },


    dockedItems: [
        {
            itemId: 'toolbarTop',
            xtype: 'toolbar',
            dock: 'top',
            layout: 'hbox',
            items: [
                {
                    itemId: 'pagingtoolbarTop',
                    xtype: 'pagingtoolbartop',
                    store: 'Isu.store.CreationRule',
                    displayMsg: '{0} - {1} of {2} rules',
                    displayMoreMsg: '{0} - {1} of more than {2} rules',
                    emptyMsg: '0 rules',
                    border: false,
                    flex: 1
                },
                {
                    itemId: 'createRule',
                    xtype: 'button',
                    text: 'Create rule',
                    href: '#/administration/issue/creationrules/create',
                    action: 'create'
                }
            ]
        },
        {
            xtype: 'pagingtoolbarbottom',
            store: 'Isu.store.CreationRule',
            dock: 'bottom'
        }
    ]
});