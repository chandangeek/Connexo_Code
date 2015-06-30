Ext.define('Idv.view.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-validation-issues-overview',
    requires: [
        'Uni.component.filter.view.FilterTopPanel',
        'Isu.view.issues.SideFilter',
        'Isu.view.issues.Grid',
        'Isu.view.issues.NoIssuesFoundPanel',
        'Isu.view.issues.GroupingToolbar',
        'Isu.view.issues.GroupGrid',
        'Idv.view.Preview',
        'Isu.view.issues.GroupingTitle',
        'Isu.view.issues.NoGroupSelectedPanel',
        'Isu.view.issues.SortingToolbar'
    ],
    router: null,
    groupingType: null,
    side: {
        xtype: 'panel',
        ui: 'medium',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                xtype: 'issues-side-filter',
                itemId: 'data-validation-issues-side-filter'
            }
        ]
    },
    initComponent: function () {
        var me = this;

        me.content = [
            {
                title: Uni.I18n.translate('workspace.issues.title', 'ISU', 'Issues'),
                ui: 'large',
                items: [
                    {
                        xtype: 'filter-top-panel',
                        itemId: 'data-validation-issues-filter-toolbar',
                        emptyText: Uni.I18n.translate('general.none', 'ISU', 'None'),
                        hideEmpty: false
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
                        hideEmpty: false
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