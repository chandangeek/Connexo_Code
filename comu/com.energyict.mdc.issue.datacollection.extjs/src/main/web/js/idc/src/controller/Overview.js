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
        'Idc.store.Groups',
        'Isu.store.Clipboard'
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
            ref: 'filterToolbar',
            selector: 'data-collection-issues-overview isu-view-issues-issuefilter'
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
            ref: 'groupEmptyPanel',
            selector: 'data-collection-issues-overview #no-data-collection-issues-group-panel'
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
            ref: 'issuesGrid',
            selector: 'data-collection-issues-overview #data-collection-issues-grid'
        }
    ],

    init: function () {
        this.control({
            'data-collection-issues-overview #issues-overview-action-menu': {
                click: this.chooseAction
            },
            'data-collection-issues-overview #data-collection-issues-grid uni-actioncolumn': {
                menuclick: this.chooseAction
            },
            'data-collection-issues-overview #data-collection-issues-grid': {
                select: this.showPreview
            },
            'data-collection-issues-overview issues-grouping-toolbar #issues-grouping-toolbar-combo': {
                change: this.setGroupingType
            },
            'data-collection-issues-overview issues-group-grid': {
                select: this.setGroupingValue
            },
            'data-collection-issues-overview isu-view-issues-issuefilter': {
                change: this.setGrouping
            },
            'data-collection-issues-overview #data-collection-issues-preview #filter-display-button': {
                click: this.setFilterItem
            }
        });
    },

    showOverview: function () {
        this.callParent(['datacollection', 'data-collection-issues-overview']);
    }
});