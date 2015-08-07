Ext.define('Idv.view.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-validation-issues-overview',
    requires: [
        'Isu.view.issues.Grid',
        'Isu.view.issues.NoIssuesFoundPanel',
        'Isu.view.issues.GroupingToolbar',
        'Isu.view.issues.GroupGrid',
        'Idv.view.Preview',
        'Isu.view.issues.GroupingTitle',
        'Isu.view.issues.NoGroupSelectedPanel',
        'Isu.view.issues.SortingToolbar',
        'Isu.view.issues.IssueFilter'
    ],
    router: null,
    groupingType: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                title: Uni.I18n.translate('workspace.issues.title', 'ISU', 'Issues'),
                ui: 'large',
                items: [
                    {
                        xtype: 'isu-view-issues-issuefilter',
                        itemId: 'isu-view-issues-issuefilter',
                        store: 'Idv.store.Issues'
                    },
                    {
                        xtype: 'menuseparator'
                    },
                    {
                        xtype: 'issues-grouping-toolbar',
                        itemId: 'data-validation-issues-grouping-toolbar',
                        groupingType: me.groupingType
                    },
                    {
                        xtype: 'issues-group-grid',
                        itemId: 'data-validation-issues-group-grid',
                        store: 'Idv.store.Groups',
                        groupingType: me.groupingType,
                        hidden: true
                    },
                    {
                        xtype: 'menuseparator'
                    },
                    {
                        xtype: 'issues-grouping-title',
                        itemId: 'data-validation-issues-grouping-title',
                        hidden: true
                    },
                    {
                        xtype: 'issues-sorting-toolbar',
                        itemId: 'data-validation-issues-sorting-toolbar',
                        hideEmpty: false,
                        store: 'Idv.store.Issues'
                    },
                    {
                        xtype: 'no-issues-group-selected-panel',
                        itemId: 'no-data-validation-issues-group-selected-panel',
                        hidden: true
                    },
                    {
                        xtype: 'preview-container',
                        itemId: 'data-validation-issues-preview-container',
                        grid: {
                            xtype: 'issues-grid',
                            itemId: 'data-validation-issues-grid',
                            store: 'Idv.store.Issues',
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'no-issues-found-panel',
                            itemId: 'no-data-validation-issues-found-panel'
                        },
                        previewComponent: {
                            xtype: 'data-validation-issues-preview',
                            itemId: 'data-validation-issues-preview',
                            router: me.router
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});