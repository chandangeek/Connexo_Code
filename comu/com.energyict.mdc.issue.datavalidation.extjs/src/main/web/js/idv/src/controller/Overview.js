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
            ref: 'filterToolbar',
            selector: 'data-validation-issues-overview isu-view-issues-issuefilter'
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
            ref: 'issuesGrid',
            selector: 'data-validation-issues-overview #data-validation-issues-grid'
        }
    ],

    init: function () {
        this.control({
            'data-validation-issues-overview #issues-overview-action-menu': {
                click: this.chooseAction
            },
            'data-validation-issues-overview #data-validation-issues-grid uni-actioncolumn': {
                menuclick: this.chooseAction
            },
            'data-validation-issues-overview #data-validation-issues-grid': {
                select: this.showPreview
            },
            'data-validation-issues-overview #data-validation-issues-preview #filter-display-button': {
                click: this.setFilterItem
            }
        });
    },

    showOverview: function () {
        var me = this;

        me.callParent(['datavalidation', 'data-validation-issues-overview', function () {
            me.getFilterToolbar().down('[dataIndex=reason]').hide();
        }]);
    }
});