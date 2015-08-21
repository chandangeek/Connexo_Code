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
                title: Uni.I18n.translate('workspace.issues.title', 'IDV', 'Issues'),
                ui: 'large',
                items: [
                    {
                        xtype: 'isu-view-issues-issuefilter',
                        itemId: 'isu-view-issues-issuefilter',
                        store: 'Idv.store.Issues'
                    },
                    {
                        xtype: 'issues-sorting-toolbar',
                        itemId: 'data-validation-issues-sorting-toolbar',
                        hideEmpty: false,
                        store: 'Idv.store.Issues'
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