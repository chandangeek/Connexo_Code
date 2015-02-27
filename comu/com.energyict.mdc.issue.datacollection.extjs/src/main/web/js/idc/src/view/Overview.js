Ext.define('Idc.view.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-collection-issues-overview',
    requires: [
        'Uni.component.filter.view.FilterTopPanel',
        'Isu.view.issues.SideFilter',
        'Isu.view.issues.Grid',
        'Isu.view.issues.NoIssuesFoundPanel',
        'Isu.view.issues.GroupingToolbar',
        'Isu.view.issues.GroupGrid',
        'Idc.view.Preview',
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
                itemId: 'data-collection-issues-side-filter'
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
                        itemId: 'data-collection-issues-filter-toolbar',
                        emptyText: Uni.I18n.translate('general.none', 'ISU', 'None'),
                        hideEmpty: false
                    },
                    {
                        xtype: 'menuseparator'
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
                        hideEmpty: false
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