Ext.define('Isu.view.administration.datacollection.issuecreationrules.List', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.layout.container.Column',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    alias: 'widget.issues-creation-rules-list',
    store: 'Isu.store.CreationRule',
    columns: {
        items: [
            {
                itemId: 'Name',
                header: Uni.I18n.translate('general.title.name', 'ISU', 'Name'),
                dataIndex: 'name',
                tdCls: 'isu-grid-description',
                flex: 1
            },
            {
                itemId: 'templateColumn',
                header: Uni.I18n.translate('general.title.ruleTemplate', 'ISU', 'Rule template'),
                xtype: 'templatecolumn',
                tpl: '<tpl if="template">{template.name}</tpl>',
                tdCls: 'isu-grid-description',
                flex: 1
            },
            {
                itemId : 'issueType',
                header: Uni.I18n.translate('general.title.issueType', 'ISU', 'Issue type'),
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
            itemId: 'pagingtoolbartop',
            xtype: 'pagingtoolbartop',
            dock: 'top',
            displayMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbartop.displayMsg', 'ISU', '{0} - {1} of {2} issue creation rules'),
            displayMoreMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbartop.displayMoreMsg', 'ISU', '{0} - {1} of more than {2} issue creation rules'),
            emptyMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbartop.emptyMsg', 'ISU', 'There are no issue creation rules to display'),
            items: [
                '->',
                {
                    itemId: 'createRule',
                    xtype: 'button',
                    text: Uni.I18n.translate('administration.issueCreationRules.add', 'ISU', 'Add rule'),
                    href: '#/administration/creationrules/add',
                    action: 'create'
                }
            ]
        },
        {
            itemId: 'pagingtoolbarbottom',
            xtype: 'pagingtoolbarbottom',
            dock: 'bottom',
            itemsPerPageMsg: Uni.I18n.translate('administration.issueCreationRules.pagingtoolbarbottom.itemsPerPage', 'ISU', 'Issue creation rules per page')
        }
    ],

    initComponent: function () {
        var store = this.store,
            pagingToolbarTop = Ext.Array.findBy(this.dockedItems, function (item) {
                return item.xtype == 'pagingtoolbartop';
            }),
            pagingToolbarBottom = Ext.Array.findBy(this.dockedItems, function (item) {
                return item.xtype == 'pagingtoolbarbottom';
            });

        pagingToolbarTop && (pagingToolbarTop.store = store);
        pagingToolbarBottom && (pagingToolbarBottom.store = store);

        this.callParent(arguments);
    }
});