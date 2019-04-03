/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.controller.Issues', {
    extend: 'Isu.controller.IssuesOverview',
    requires: [
        'Itk.view.IssueFilter',
        'Itk.view.NoIssuesFoundPanel',
        'Itk.view.Preview',
        'Itk.store.Issues',
        'Itk.view.Grid',
        'Itk.view.ActionMenu'
    ],

    stores: [
        'Itk.store.Issues',
        'Itk.store.IssueAssignees',
        'Itk.store.IssueReasons',
        'Itk.store.IssueStatuses',
        'Itk.store.IssueWorkgroupAssignees',
        'Itk.store.DueDate',
        'Itk.store.Devices',
        'Itk.store.IssueAssignees'
    ],

    models: [
        'Itk.model.Device',
        'Itk.model.DueDate',
        'Itk.model.IssueAssignee',
        'Itk.model.IssueReason',
        'Itk.model.IssueStatus',
        'Itk.model.Device'
    ],


    constructor: function () {
        var me = this;
        me.refs =
            [
                {
                    ref: 'preview',
                    selector: 'issues-overview #issue-preview'
                },
                {
                    ref: 'previewStatus',
                    selector: 'issues-overview #issues-preview #issue-status'
                },
                {
                    ref: 'filterToolbar',
                    selector: 'issues-overview view-issues-filter'
                },
                {
                    ref: 'groupingToolbar',
                    selector: 'issues-overview #issues-grouping-toolbar'
                },
                {
                    ref: 'groupGrid',
                    selector: 'issues-overview #issues-group-grid'
                },
                {
                    ref: 'previewContainer',
                    selector: 'issues-overview #issues-preview-container'
                },
                {
                    ref: 'groupingTitle',
                    selector: 'issues-overview issues-grouping-title'
                },
                {
                    ref: 'issuesGrid',
                    selector: 'issues-overview #issues-grid'
                },
                {
                    ref: 'previewActionMenu',
                    selector: '#issue-preview issues-action-menu'
                }
            ]
        me.callParent(arguments);
    },

    init: function () {
        var me = this;
        me.control({
            'issues-overview #issues-grid': {
                select: me.showPreview
            },
            'issues-overview #issue-preview #filter-display-button': {
                click: this.setFilterItem
            }
        });
    },

    showOverview: function () {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false);

        if (_.values(queryString).length == 0){
            var latestQueryString = this.getStore('Isu.store.Clipboard').get('latest-issues-filter');
            if (latestQueryString) {
                queryString = latestQueryString;
                window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
            }
        }
        if (queryString.myopenissues) {
            me.getStore('Itk.store.IssueAssignees').load({
                params: {me: true},
                callback: function (records) {
                    queryString.myopenissues = undefined;
                    queryString.userAssignee = records[0].getId();
                    queryString.sort = ['-priorityTotal'];
                    window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                }
            });
        } else if (queryString.myworkgroupissues) {
            Ext.Ajax.request({
                url: '/api/itk/workgroups?myworkgroups=true',
                method: 'GET',
                success: function (response) {
                    var decoded = response.responseText ? Ext.decode(response.responseText, true) : null;
                    if (decoded && decoded.workgroups) {
                        queryString.myworkgroupissues = undefined;
                        queryString.userAssignee = [-1];
                        queryString.workGroupAssignee = decoded.workgroups.length == 0 ? [-1] : decoded.workgroups.map(function (wg) {
                            return wg.id;
                        });
                        queryString.sort = ['-priorityTotal'];
                        window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                    }
                }
            });
        } else if (!queryString.userAssignee && !queryString.myworkgroupissues && !queryString.status) {
            queryString.status = ['status.open', 'status.in.progress'];
            queryString.sort = ['-priorityTotal'];
            window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
        } else if (!queryString.sort) {
            queryString.sort = ['-priorityTotal'];
            window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
        } else {
            me.getStore('Isu.store.Clipboard').set('latest-issues-filter', queryString);
            var widget = Ext.widget('issues-overview', {
                router: me.getController('Uni.controller.history.Router'),
                groupingType: queryString.groupingType,
                filter: {
                    xtype: 'view-issues-filter',
                    itemId: 'view-issues-filter'
                },
                emptyComponent: {
                    xtype: 'no-issues-found-panel',
                    itemId: 'no-issues-found-panel'
                },
                previewComponent: {
                    xtype: 'issue-preview',
                    itemId: 'issue-preview',
                    fieldxtype: 'filter-display'
                },
                grid: {
                    store: 'Itk.store.Issues',
                    xtype: 'issues-grid',
                    itemId: 'issues-grid'
                }
            });

            me.setOverviewIssueComponets(widget);
            me.getApplication().fireEvent('changecontentevent', widget);
        }
    },

    setOverviewIssueComponets: function (widget) {
        widget.down('#issue-panel').setTitle(Uni.I18n.translate('device.issues', 'ITK', 'Issues'));
        widget.down('#issues-grouping-toolbar').setVisible(false);
        widget.down('#issues-group-grid').setVisible(false);
        widget.down('menuseparator').setVisible(false);
        widget.down('#issues-grouping-title').setVisible(false);
    },

    showPreview: function (selectionModel, record) {
        this.callParent(arguments);
        var subEl = new Ext.get('issue-status-field-sub-tpl');
        subEl.setHTML('<div>' + record.get('statusDetailCleared') + '</div>'
            + '<div>' + record.get('statusDetailSnoozed') + '</div>');
    },

    setFilterItem: function (button) {
        var me = this;

        switch (button.filterBy) {
            case 'issueId':
                button.filterBy = 'id';
                break;
            case 'reasonName':
                button.filterBy = 'reason';
                break;
        }
        me.callParent(arguments);
    }
});
