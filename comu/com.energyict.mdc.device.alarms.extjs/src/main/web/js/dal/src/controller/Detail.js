Ext.define('Dal.controller.Detail', {
    extend: 'Isu.controller.IssueDetail',

    stores: [
        'Isu.store.Clipboard',
        'Dal.store.Alarms',
        'Dal.store.RelatedEventsStore'
    ],

    models: [
        'Dal.model.Alarm'
    ],

    views: [
        'Dal.view.Detail'
    ],

    widgetXtype: 'alarm-detail',
    issueModel: 'Dal.model.Alarm',
    itemUrl: '/api/dal/alarms/',

    constructor: function () {
        var me = this;
        me.refs =
            [
                {
                    ref: 'page',
                    selector: 'alarm-detail'
                },
                {
                    ref: 'commentsPanel',
                    selector: 'alarm-detail #alarm-comments'
                },
                {
                    ref: 'issueDetailForm',
                    selector: 'alarm-detail #alarm-detail-form'
                },
                {
                    ref: 'actionMenu',
                    selector: 'alarm-detail #issues-action-menu'
                }
            ]
        me.callParent(arguments);
    },

    init: function () {
        this.control({
            'alarm-detail #alarm-comments #issue-comments-add-comment-button': {
                click: this.showCommentForm
            },
            'alarm-detail #alarm-comments #empty-message-add-comment-button': {
                click: this.showCommentForm
            },
            'alarm-detail #alarm-comments #issue-comment-cancel-adding-button': {
                click: this.hideCommentForm
            },
            'alarm-detail #alarm-comments #issue-comment-save-button': {
                click: this.addComment
            },
            'alarm-detail #alarm-comments #issue-add-comment-area': {
                change: this.validateCommentForm
            },
            'alarm-detail #alarm-detail-action-menu': {
                click: this.chooseAction
            },
            'alarm-detail #alarm-timeline-view': {
                onClickLink: this.showProcesses
            },
            'alarm-detail #alarm-process-view': {
                onClickLink: this.showProcesses,
                onClickTaskLink: this.showTask
            }
        });
    },

    showOverview: function (id) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            store = me.getStore('Dal.store.Alarms'),
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
                    var subEl = new Ext.get('alarm-status-field-sub-tpl');
                    subEl.setHTML(record.get('statusDetail'));

                    Ext.resumeLayouts(true);
                    if ((typeof me.getActionMenu === "function") && me.getActionMenu()) {
                        me.getActionMenu().record = record;
                    }
                    else if (widget.down('#alarm-detail-action-menu')) {
                        widget.down('#alarm-detail-action-menu').record = record;
                    }
                    me.loadComments(record, 'alarm');
                    me.loadRelatedEvents(widget, record);
                }
            },
            failure: function () {
                router.getRoute(router.currentRoute.replace('/view', '')).forward();
            }
        });
    },

    makeLinkToList: function (router) {
        var link = '<a href="{0}">' + Uni.I18n.translate('workspace.issues.title', 'DAL', 'Alarms').toLowerCase() + '</a>',
            filter = this.getStore('Isu.store.Clipboard').get('latest-issues-filter'),
            queryParams = filter ? filter : null;

        return Ext.String.format(link, router.getRoute('workspace/alarms').buildUrl(null, queryParams));
    },

    loadRelatedEvents: function (widget, record) {
        var me = this,
            grid = widget.down('#alarm-log-grid');

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
                store = Ext.create('Dal.store.RelatedEventsStore', {data: data});
                grid.reconfigure(store);
            }
        }
    },

    canViewProcesses: function () {
        return Dal.privileges.Alarm.canViewProcesses();
    },

    canComment: function () {
        return Dal.privileges.Alarm.canComment();
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