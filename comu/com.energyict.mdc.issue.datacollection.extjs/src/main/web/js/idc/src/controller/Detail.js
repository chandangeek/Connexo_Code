Ext.define('Idc.controller.Detail', {
    extend: 'Isu.controller.IssueDetail',

    controllers: [
        'Bpm.monitorissueprocesses.controller.MonitorIssueProcesses'
    ],
    stores: [
        'Idc.store.Issues',
        'Isu.store.IssueActions',
        'Isu.store.Clipboard',
        'Idc.store.TimelineEntries',
        'Bpm.monitorissueprocesses.store.IssueProcesses'

    ],

    views: [
        'Idc.view.Detail'

    ],

    refs: [
        {
            ref: 'page',
            selector: 'data-collection-issue-detail'
        },
        {
            ref: 'detailForm',
            selector: 'data-collection-issue-detail #data-collection-issue-detail-form'
        },
        {
            ref: 'commentsPanel',
            selector: 'data-collection-issue-detail #data-collection-issue-comments'
        },
        {
            ref: 'timelinePanel',
            selector: 'data-collection-issue-detail #data-collection-issue-timeline'
        }

    ],

    issueId: null,

    init: function () {
        this.control({
            'data-collection-issue-detail #data-collection-issue-comments #issue-comments-add-comment-button': {
                click: this.showCommentForm
            },
            'data-collection-issue-detail #data-collection-issue-comments #empty-message-add-comment-button': {
                click: this.showCommentForm
            },
            'data-collection-issue-detail #data-collection-issue-comments #issue-comment-cancel-adding-button': {
                click: this.hideCommentForm
            },
            'data-collection-issue-detail #data-collection-issue-comments #issue-comment-save-button': {
                click: this.addComment
            },
            'data-collection-issue-detail #data-collection-issue-comments #issue-add-comment-area': {
                change: this.validateCommentForm
            },
            'data-collection-issue-detail #issue-detail-action-menu': {
                click: this.chooseAction
            },
            'data-collection-issue-detail #issue-timeline-view': {
                onClickLink: this.showProcesses
            },
            'data-collection-issue-detail #issue-process-view': {
                onClickLink: this.showProcesses
            }
        });
    },

    showProcesses: function(processId){

        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            route;

        route = router.getRoute(router.currentRoute + '/viewProcesses');
        route.params.issueId = me.issueId;
        route.params.process = processId;
        route.forward();

    },
    showOverview: function (id) {
        var me = this,
            processStore = me.getStore('Bpm.monitorissueprocesses.store.IssueProcesses');

        me.issueId = id;
        processStore.getProxy().setUrl(id);
        processStore.load(function (records) {
                });
        this.callParent([id, 'Idc.model.Issue', 'Idc.store.Issues', 'data-collection-issue-detail', 'workspace/datacollectionissues', 'datacollection']);
    },

    loadTimeline: function(commentsStore){
        var me = this,
            timelineView = this.getPage().down('#issue-timeline-view'),
            processView = this.getPage().down('#issue-process-view'),
            timelineStore = me.getStore('Idc.store.TimelineEntries'),
            procesStore = me.getStore('Bpm.monitorissueprocesses.store.IssueProcesses'),
            data=[];

        timelineStore.data.clear();

        commentsStore.each(function(rec)
            {
                data.push({
                    user: rec.data.author.name,
                    actionText: Uni.I18n.translate('issue.workspace.datacollection.added.comment','IDC','added a comment'),
                    creationDate: rec.data.creationDate,
                    contentText: rec.data.splittedComments
                });
            }
        );
        procesStore.each(function(rec)
            {
                data.push({
                    user: rec.data.startedBy,
                    actionText: Uni.I18n.translate('issue.workspace.datacollection.processStarted','IDC','started a process'),
                    creationDate: rec.data.startDate,
                    contentText: rec.data.name,
                    forProcess: true,
                    processId: rec.data.processId,
                    status: ' (' + rec.data.statusDisplay + ')'
                });
            }
        );

        Ext.Array.each(data, function (item) {
            timelineStore.add(item);
        })

        timelineStore.sort('creationDate', 'DESC');
        timelineView.bindStore(timelineStore);
        timelineView.previousSibling('#no-issue-timeline').setVisible(timelineStore.data.items.length <= 0);

        procesStore.sort('startDate', 'DESC');
        processView.bindStore(procesStore);
        processView.previousSibling('#no-issue-processes').setVisible(procesStore.data.items.length <= 0);
    },

});