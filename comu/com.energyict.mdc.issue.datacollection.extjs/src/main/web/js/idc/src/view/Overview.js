Ext.define('Idc.view.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-collection-issues-overview',
    requires: [
        'Isu.view.issues.NoIssuesFoundPanel',
        'Isu.view.issues.GroupingToolbar',
        'Isu.view.issues.GroupGrid',
        'Isu.view.issues.IssueFilter',
        'Isu.view.issues.Grid',
        'Idc.view.Preview',
        'Isu.view.issues.SortingToolbar',
        'Isu.view.issues.GroupingTitle',
        'Isu.view.issues.NoGroupSelectedPanel'
    ],
    router: null,
    groupingType: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                title: Uni.I18n.translate('workspace.issues.title', 'IDC', 'Issues'),
                ui: 'large',
                items: [
                    {
                        xtype: 'isu-view-issues-issuefilter',
                        itemId: 'isu-view-issues-issuefilter',
                        store: 'Idc.store.Issues'
                    },
                    {
                        xtype: 'issues-grouping-toolbar',
                        itemId: 'data-collection-issues-grouping-toolbar',
                        groupingType: me.groupingType
                    },
                    {
                        xtype: 'issues-group-grid',
                        itemId: 'data-collection-issues-group-grid',
                        store: 'Idc.store.Groups',
                        groupingType: me.groupingType,
                        hidden: true
                    },
                    {
                        xtype: 'menuseparator'
                    },
                    {
                        xtype: 'issues-grouping-title',
                        itemId: 'data-collection-issues-grouping-title',
                        hidden: true
                    },
                    {
                        xtype: 'issues-sorting-toolbar',
                        itemId: 'data-collection-issues-sorting-toolbar',
                        hideEmpty: false,
                        store: 'Idc.store.Issues'
                    },
                    {
                        xtype: 'no-issues-group-selected-panel',
                        itemId: 'no-data-collection-issues-group-selected-panel',
                        hidden: true
                    },
                    {
                        xtype: 'preview-container',
                        itemId: 'data-collection-issues-preview-container',
                        grid: {
                            xtype: 'issues-grid',
                            itemId: 'data-collection-issues-grid',
                            store: 'Idc.store.Issues',
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'no-issues-found-panel',
                            itemId: 'no-data-collection-issues-found-panel'
                        },
                        previewComponent: {
                            xtype: 'data-collection-issues-preview',
                            itemId: 'data-collection-issues-preview',
                            router: me.router
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});