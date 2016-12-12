Ext.define('Isu.view.issues.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issues-overview',
    requires: [
        'Isu.view.issues.NoIssuesFoundPanel',
        'Isu.view.issues.GroupingToolbar',
        'Isu.view.issues.GroupGrid',
        'Isu.view.issues.IssueFilter',
        'Isu.view.issues.Grid',
        'Isu.view.issues.Preview',
        'Isu.view.issues.SortingToolbar',
        'Isu.view.issues.GroupingTitle'
    ],
    router: null,
    groupingType: null,
    emptyComponent: {
        xtype: 'no-issues-found-panel',
        itemId: 'no-issues-found-panel'
    },
    previewComponent: {
        xtype: 'issues-preview',
        itemId: 'issues-preview'
    },
    grid: {
        store: 'Isu.store.Issues',
        xtype: 'issues-grid',
        itemId: 'issues-grid'
    },

    initComponent: function () {
        var me = this;

        me.content = [
            {
                title: Uni.I18n.translate('workspace.issues.title', 'ISU', 'Issues'),
                ui: 'large',
                itemId: 'issue-panel',
                items: [
                    {
                        xtype: 'isu-view-issues-issuefilter',
                        itemId: 'isu-view-issues-issuefilter',
                        store: me.grid.store
                    },
                    {
                        xtype: 'issues-grouping-toolbar',
                        itemId: 'issues-grouping-toolbar',
                        groupingType: me.groupingType
                    },
                    {
                        xtype: 'issues-group-grid',
                        itemId: 'issues-group-grid',
                        store: 'Isu.store.Groups',
                        groupingType: me.groupingType,
                        hidden: true
                    },
                    {
                        xtype: 'menuseparator'
                    },
                    {
                        xtype: 'issues-grouping-title',
                        itemId: 'issues-grouping-title',
                        hidden: true
                    },
                    {
                        xtype: 'issues-sorting-toolbar',
                        itemId: 'issues-sorting-toolbar',
                        hideEmpty: false,
                        store: me.grid.store
                    },
                    {
                        xtype: 'preview-container',
                        itemId: 'issues-preview-container',
                        grid: {
                            xtype: me.grid.xtype,
                            itemId: me.grid.itemId,
                            store: me.grid.store,
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: me.emptyComponent.xtype,
                            itemId: me.emptyComponent.itemId
                        },
                        previewComponent: {
                            xtype: me.previewComponent.xtype,
                            itemId: me.previewComponent.itemId,
                            router: me.router
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});