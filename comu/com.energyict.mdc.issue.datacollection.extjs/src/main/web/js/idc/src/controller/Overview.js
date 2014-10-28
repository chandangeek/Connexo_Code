Ext.define('Idc.controller.Overview', {
    extend: 'Isu.controller.IssuesOverview',

    models: [
        'Isu.model.IssuesFilter',
        'Isu.model.IssueAssignee',
        'Isu.model.IssueReason',
        'Isu.model.Device',
        'Uni.component.sort.model.Sort'
    ],

    stores: [
        'Idc.store.Issues',
        'Isu.store.IssueActions',
        'Isu.store.IssueStatuses',
        'Isu.store.IssueAssignees',
        'Isu.store.IssueReasons',
        'Isu.store.Devices',
        'Isu.store.IssueGrouping',
        'Idc.store.Groups'
    ],

    views: [
        'Idc.view.Overview'
    ],

    refs: [
        {
            ref: 'preview',
            selector: 'data-collection-issues-overview #data-collection-issues-preview'
        },
        {
            ref: 'filterForm',
            selector: 'data-collection-issues-overview #data-collection-issues-side-filter'
        },
        {
            ref: 'filterToolbar',
            selector: 'data-collection-issues-overview #data-collection-issues-filter-toolbar'
        },
        {
            ref: 'groupingToolbar',
            selector: 'data-collection-issues-overview #data-collection-issues-grouping-toolbar'
        },
        {
            ref: 'groupGrid',
            selector: 'data-collection-issues-overview #data-collection-issues-group-grid'
        },
        {
            ref: 'previewContainer',
            selector: 'data-collection-issues-overview #data-collection-issues-preview-container'
        },
        {
            ref: 'groupingTitle',
            selector: 'data-collection-issues-overview issues-grouping-title'
        },
        {
            ref: 'noGroupSelectedPanel',
            selector: 'data-collection-issues-overview no-issues-group-selected-panel'
        },
        {
            ref: 'sortingToolbar',
            selector: 'data-collection-issues-overview #data-collection-issues-sorting-toolbar'
        },
        {
            ref: 'issuesGrid',
            selector: 'data-collection-issues-overview #data-collection-issues-grid'
        }
    ],

    init: function () {
        this.control({
            'data-collection-issues-overview issues-side-filter #filter-by-reason': {
                render: this.setComboTooltip
            },
            'data-collection-issues-overview issues-side-filter #filter-by-meter': {
                render: this.setComboTooltip,
                expand: this.limitNotification
            },
            'data-collection-issues-overview #issues-overview-action-menu': {
                click: this.chooseAction
            },
            'data-collection-issues-overview #data-collection-issues-grid uni-actioncolumn': {
                menuclick: this.chooseAction
            },
            'data-collection-issues-overview #data-collection-issues-grid': {
                select: this.showPreview
            },
            'data-collection-issues-overview #issues-filter-apply': {
                click: this.applyFilter
            },
            'data-collection-issues-overview #issues-filter-reset': {
                click: this.resetFilter
            },
            'data-collection-issues-overview #data-collection-issues-preview #filter-display-button': {
                click: this.setFilterItem
            },
            'data-collection-issues-overview #data-collection-issues-filter-toolbar': {
                removeFilter: this.removeFilterItem,
                clearAllFilters: this.resetFilter
            },
            'data-collection-issues-overview issues-grouping-toolbar #issues-grouping-toolbar-combo': {
                change: this.setGroupingType
            },
            'data-collection-issues-overview issues-group-grid': {
                select: this.setGroupingValue
            },
            'data-collection-issues-overview issues-sorting-toolbar': {
                removeSort: this.removeSortingItem,
                changeSortDirection: this.changeSortDirection
            },
            'data-collection-issues-overview issues-sorting-toolbar #issues-sorting-menu': {
                click: this.addSortingItem
            }
        });
    },

    showOverview: function () {
        this.callParent(['datacollection', 'data-collection-issues-overview']);
    }
});