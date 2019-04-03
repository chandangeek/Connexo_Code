/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.controller.Detail', {
    extend: 'Isu.controller.IssueDetail',
    controllers: [
        'Bpm.monitorissueprocesses.controller.MonitorIssueProcesses'
    ],
    stores: [
        'Isu.store.Clipboard',
        'Itk.store.Issues',
        'Itk.store.RelatedEventsStore'
    ],

    models: [
        'Itk.model.Issue'
    ],

    views: [
        'Itk.view.Detail'
    ],

    widgetXtype: 'issue-detail',
    issueModel: 'Itk.model.Issue',
    itemUrl: '/api/itk/issues/',

    constructor: function () {
        var me = this;
        me.refs =
            [
                {
                    ref: 'page',
                    selector: 'issue-detail'
                },
                {
                    ref: 'commentsPanel',
                    selector: 'issue-detail #issue-comments'
                },
                {
                    ref: 'issueDetailForm',
                    selector: 'issue-detail #issue-detail-form'
                },
                {
                    ref: 'actionMenu',
                    selector: 'issue-detail #issues-action-menu'
                }
            ]
        me.callParent(arguments);
    },

    init: function () {
        this.control({
            'issue-detail #issue-comments #issue-comments-add-comment-button': {
                click: this.showCommentForm
            },
            'issue-detail #issue-comments #empty-message-add-comment-button': {
                click: this.showCommentForm
            },
            'issue-detail #issue-comments #issue-comment-cancel-adding-button': {
                click: this.hideCommentForm
            },
            'issue-detail #issue-comments #issue-comment-save-button': {
                click: this.addComment
            },
            'issue-detail #issue-comments #issue-add-comment-area': {
                change: this.validateCommentForm
            },
            'issue-detail #issue-comments #issue-comment-cancel-editing-button': {
                click: this.hideEditCommentForm
            },
            'issue-detail #issue-comments #issue-comment-edit-button': {
                click: this.editComment
            },
            'issue-detail #issue-comments #issue-edit-comment-area': {
                change: this.validateEditCommentForm
            },
            'issue-detail #issue-detail-action-menu': {
                click: this.chooseAction
            },
            'issue-detail #issue-timeline-view': {
                onClickLink: this.showProcesses
            },
            'issue-detail #issue-process-view': {
                onClickLink: this.showProcesses,
                onClickTaskLink: this.showTask
            }
        });
    },

    showOverview: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            store = me.getStore('Itk.store.Issues'),
            processStore = me.getStore('Bpm.monitorissueprocesses.store.IssueProcesses'),
            widgetXtype,
            model,
            widget;

        widgetXtype = me.widgetXtype;
        issueModel = me.issueModel;

        processStore.getProxy().setUrl(id);
        processStore.load(function (records) {
        });

        widget = Ext.widget(widgetXtype, {
            router: router,
            issuesListLink: me.makeLinkToList(router)
        });
        me.widget = widget;
        me.getApplication().fireEvent('changecontentevent', widget);
        me.issueModel = issueModel;
        widget.setLoading(true);

        me.getModel(issueModel).load(id, {
            callback: function () {
                widget.setLoading(false);
            },
            success: function (record) {
                if (!widget.isDestroyed) {
                    Ext.getStore('Isu.store.Clipboard').set('issue', record);
                    me.getApplication().fireEvent('issueLoad', record);
                    Ext.suspendLayouts();
                    widget.down('#issue-detail-top-title').setTitle(record.get('title'));

                    me.getIssueDetailForm().loadRecord(record);
                    var subEl = new Ext.get('issue-status-field-sub-tpl');

                    subEl.setHTML('<div>' + record.get('statusDetailCleared') + '</div>'
                        + '<div>' + record.get('statusDetailSnoozed') + '</div>');

                    Ext.resumeLayouts(true);
                    if ((typeof me.getActionMenu === "function") && me.getActionMenu()) {
                        me.getActionMenu().record = record;
                    }
                    else if (widget.down('#issue-detail-action-menu')) {
                        widget.down('#issue-detail-action-menu').record = record;
                    }
                    me.loadComments(record, 'issue');
                    me.loadRelatedEvents(widget, record);
                }
            },
            failure: function () {
                router.getRoute(router.currentRoute.replace('/view', '')).forward();
            }
        });
    },

    makeLinkToList: function (router) {
        var link = '<a href="{0}">' + Uni.I18n.translate('workspace.issues.title', 'ITK', 'Issues').toLowerCase() + '</a>',
            filter = this.getStore('Isu.store.Clipboard').get('latest-issues-filter'),
            queryParams = filter ? filter : null;

        return Ext.String.format(link, router.getRoute('workspace/issues').buildUrl(null, queryParams));
    },

    loadRelatedEvents: function (widget, record) {
        var me = this,
            grid = widget.down('#issue-log-grid');

        if (record.raw.relatedEvents && grid) {
            var data = [],
                store;

            record.raw.relatedEvents.map(function (item) {
                data.push(Ext.apply({}, {
                    eventDate: item.eventDate,
                    deviceType: item.deviceType,
                    deviceCode: item.deviceCode,
                    domain: item.domain,
                    subDomain: item.subDomain,
                    eventOrAction: item.eventOrAction,
                    message: item.message
                }))
            });

            if (data.length) {
                store = Ext.create('Itk.store.RelatedEventsStore', {data: data});
                grid.reconfigure(store);
            }
        }
    },

    canViewProcesses: function () {
        return Itk.privileges.Issue.canViewProcesses();
    },

    canComment: function () {
        return Itk.privileges.Issue.canComment();
    },

    showProcesses: function (processId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            route;

        route = router.getRoute(router.currentRoute + '/viewProcesses');
        route.params.process = processId;
        route.forward(router.arguments, router.queryParams);

    },
    showTask: function (task) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            route;
        router.arguments.taskId = task;
        route = 'workspace/tasks/task/performTask';
        route && (route = router.getRoute(route));
        route && route.forward(router.arguments);
    }
});