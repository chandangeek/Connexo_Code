/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.issue.controller.IssuesOverview', {
    extend: 'Isu.controller.IssuesOverview',

    requires: [
        'Imt.issue.view.IssueFilter',
        'Imt.issue.view.Preview'
    ],
    /* requires: [
     'Dal.view.AlarmFilter',
     'Dal.view.NoAlarmsFoundPanel',
     'Dal.view.Preview',
     'Dal.store.Alarms',
     'Dal.view.Grid',
     'Dal.view.ActionMenu'
     ],*/

    models: [

        'Isu.model.IssuesFilter',
        'Isu.model.IssueAssignee',
        'Isu.model.IssueWorkgroupAssignee',
        'Isu.model.IssueReason',
        'Isu.model.Device',
        'Imt.datavalidation.model.Issue',
        'Uni.component.sort.model.Sort',
        'Imt.usagepointgroups.model.UsagePointGroup'
    ],

    stores: [
        'Imt.datavalidation.store.Issues',
        'Isu.store.Issues',
        'Isu.store.IssueActions',
        'Isu.store.IssueStatuses',
        'Isu.store.IssueAssignees',
        'Isu.store.IssueWorkgroupAssignees',
        'Isu.store.IssueReasons',
        'Isu.store.Devices',
        'Isu.store.IssueGrouping',
        'Isu.store.Groups',
        'Isu.store.Clipboard',
        'Imt.usagepointgroups.store.UsagePointGroups'
    ],

    constructor: function () {
        var me = this;
        me.refs =
            [
                {
                    ref: 'preview',
                    selector: '#validation-issues-overview #issue-view-preview'
                },
                {
                    ref: 'filterToolbar',
                    selector: '#validation-issues-overview issue-view-filter'
                },
                {
                    ref: 'groupingToolbar',
                    selector: '#validation-issues-overview #issues-grouping-toolbar'
                },
                {
                    ref: 'groupGrid',
                    selector: '#validation-issues-overview #issues-group-grid'
                },
                {
                    ref: 'previewContainer',
                    selector: '#validation-issues-overview #issues-preview-container'
                },
                {
                    ref: 'groupingTitle',
                    selector: '#validation-issues-overview issues-grouping-title'
                },
                {
                    ref: 'issuesGrid',
                    selector: '#validation-issues-overview #issues-view-grid'
                },
                {
                    ref: 'previewActionMenu',
                    selector: '#issue-view-preview issues-action-menu'
                },
                {
                    ref: 'countButton',
                    selector: '#validation-issues-overview #issues-count-action'
                },
            ];
        me.callParent(arguments);
    },

    init: function () {
        var me = this;
        extendedBy = 'imt';
        this.control({
            '#validation-issues-overview #issues-overview-action-menu': {
                click: this.chooseAction
            },
            '#validation-issues-overview #issues-view-grid uni-actioncolumn': {
                menuclick: this.chooseAction
            },
            '#validation-issues-overview #issues-view-grid': {
                select: this.showPreview
            },
            '#validation-issues-overview issues-grouping-toolbar #issues-grouping-toolbar-combo': {
                change: this.setGroupingType
            },
            '#validation-issues-overview issues-group-grid': {
                select: this.setGroupingValue
            },
            '#validation-issues-overview isu-view-issues-issuefilter': {
                change: this.setGrouping
            },
            '#validation-issues-overview #issue-view-preview #filter-display-button': {
                click: this.setFilterItem
            },
            '#validation-issues-overview #issues-count-action': {
                click: this.countIssues
            }
        });
    },

    showOverview: function () {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false);

        if (queryString.myopenissues) {
            me.getStore('Isu.store.IssueAssignees').load({
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
                url: '/api/isu/workgroups?myworkgroups=true',
                method: 'GET',
                success: function (response) {
                    var decoded = response.responseText ? Ext.decode(response.responseText, true) : null;
                    if (decoded && decoded.workgroups) {
                        me.getStore('Isu.store.IssueAssignees').load({
                            params: {me: true},
                            callback: function (records) {
                                queryString.myworkgroupissues = undefined;
                                queryString.userAssignee = records && !Ext.isEmpty(records) ? [-1, records[0].getId()] : [-1];
                                queryString.workGroupAssignee = decoded.workgroups.length == 0 ? [-1] : decoded.workgroups.map(function (wg) {
                                    return wg.id;
                                });
                                queryString.sort = ['-priorityTotal'];
                                window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                            }
                        });
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
                itemId: 'validation-issues-overview',
                router: me.getController('Uni.controller.history.Router'),
                groupingType: queryString.groupingType,
                filter: {
                    xtype: 'issue-view-filter',
                    itemId: 'issue-view-filter'
                },
                previewComponent: {
                    xtype: 'issue-view-preview',
                    itemId: 'issue-view-preview',
                    fieldxtype: 'filter-display'
                },
                grid: {
                    store: 'Imt.datavalidation.store.Issues',
                    xtype: 'issues-grid',
                    itemId: 'issues-view-grid'
                },
            });

            me.getApplication().fireEvent('changecontentevent', widget);
        }
    },

    countIssues: function(){
        var me = this;
        me.fireEvent('loadingcount');
        Ext.Ajax.suspendEvent('requestexception');
        me.getCountButton().up('panel').setLoading(true);
        var filters = [];
        var queryStringValues = Uni.util.QueryString.getQueryStringValues(false);
        for (property in queryStringValues){
            if (property !== 'sort'){
                filters.push({'property' : property, value: queryStringValues[property] })
            }
        };
        filters.push({'property' : 'application', value: 'INS' })

        Ext.Ajax.request({
            url: Ext.getStore('Imt.datavalidation.store.Issues').getProxy().url + '/count',
            timeout: 120000,
            method: 'GET',
            params: {
                 filter: JSON.stringify(filters)
            },
            success: function (response) {
                me.getCountButton().setText(response.responseText);
                me.getCountButton().setDisabled(true);
                me.getCountButton().up('panel').setLoading(false);
            },
            failure: function (response, request) {
                var box = Ext.create('Ext.window.MessageBox', {
                    buttons: [
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.close', 'IMT', 'Close'),
                            action: 'close',
                            name: 'close',
                            ui: 'remove',
                            handler: function () {
                                box.close();
                            }
                        }
                    ],
                    listeners: {
                        beforeclose: {
                            fn: function () {
                                me.getCountButton().setDisabled(true);
                                me.getCountButton().up('panel').setLoading(false);
                            }
                        }
                    }
                });

                box.show({
                    title: Uni.I18n.translate('general.timeOut', 'IMT', 'Time out'),
                    msg: Uni.I18n.translate('general.timeOutMessageIssues', 'IMT', 'Counting the issues took too long.'),
                    modal: false,
                    ui: 'message-error',
                    icon: 'icon-warning2',
                    style: 'font-size: 34px;'
                });
            }
        });
    }

});
