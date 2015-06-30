Ext.define('Idv.controller.Overview', {
    extend: 'Isu.controller.IssuesOverview',

    models: [
        'Isu.model.IssuesFilter',
        'Isu.model.IssueAssignee',
        'Isu.model.IssueReason',
        'Isu.model.Device',
        'Uni.component.sort.model.Sort'
    ],

    stores: [
        'Idv.store.Issues',
        'Isu.store.IssueActions',
        'Isu.store.IssueStatuses',
        'Isu.store.IssueAssignees',
        'Isu.store.IssueReasons',
        'Isu.store.Devices',
        'Isu.store.IssueGrouping',
        'Idv.store.Groups',
        'Isu.store.Clipboard'
    ],

    views: [
        'Idv.view.Overview'
    ],

    refs: [
        {
            ref: 'preview',
            selector: 'data-validation-issues-overview #data-validation-issues-preview'
        },
        {
            ref: 'filterForm',
            selector: 'data-validation-issues-overview #data-validation-issues-side-filter'
        },
        {
            ref: 'filterToolbar',
            selector: 'data-validation-issues-overview #data-validation-issues-filter-toolbar'
        },
        {
            ref: 'groupingToolbar',
            selector: 'data-validation-issues-overview #data-validation-issues-grouping-toolbar'
        },
        {
            ref: 'groupGrid',
            selector: 'data-validation-issues-overview #data-validation-issues-group-grid'
        },
        {
            ref: 'previewContainer',
            selector: 'data-validation-issues-overview #data-validation-issues-preview-container'
        },
        {
            ref: 'groupingTitle',
            selector: 'data-validation-issues-overview issues-grouping-title'
        },
        {
            ref: 'noGroupSelectedPanel',
            selector: 'data-validation-issues-overview no-issues-group-selected-panel'
        },
        {
            ref: 'sortingToolbar',
            selector: 'data-validation-issues-overview #data-validation-issues-sorting-toolbar'
        },
        {
            ref: 'issuesGrid',
            selector: 'data-validation-issues-overview #data-validation-issues-grid'
        }
    ],

    init: function () {
        this.control({
            'data-validation-issues-overview issues-side-filter #filter-by-reason': {
                render: this.setComboTooltip
            },
            'data-validation-issues-overview issues-side-filter #filter-by-meter': {
                render: this.setComboTooltip,
                expand: this.limitNotification
            },
            'data-validation-issues-overview #issues-overview-action-menu': {
                click: this.chooseAction
            },
            'data-validation-issues-overview #data-validation-issues-grid uni-actioncolumn': {
                menuclick: this.chooseAction
            },
            'data-validation-issues-overview #data-validation-issues-grid': {
                select: this.showPreview
            },
            'data-validation-issues-overview #issues-filter-apply': {
                click: this.applyFilter
            },
            'data-validation-issues-overview #issues-filter-reset': {
                click: this.resetFilter
            },
            'data-validation-issues-overview #data-validation-issues-preview #filter-display-button': {
                click: this.setFilterItem
            },
            'data-validation-issues-overview #data-validation-issues-filter-toolbar': {
                removeFilter: this.removeFilterItem,
                clearAllFilters: this.resetFilter
            },
            'data-validation-issues-overview issues-grouping-toolbar #issues-grouping-toolbar-combo': {
                change: this.setGroupingType
            },
            'data-validation-issues-overview issues-group-grid': {
                select: this.setGroupingValue
            },
            'data-validation-issues-overview issues-sorting-toolbar': {
                removeSort: this.removeSortingItem,
                changeSortDirection: this.changeSortDirection
            },
            'data-validation-issues-overview issues-sorting-toolbar #issues-sorting-menu': {
                click: this.addSortingItem
            }
        });
    },

    showOverview: function () {
        var me = this;

        me.callParent(['datavalidation', 'data-validation-issues-overview', function () {
            me.getFilterForm().down('#filter-by-reason').hide();
        }]);
    }
});