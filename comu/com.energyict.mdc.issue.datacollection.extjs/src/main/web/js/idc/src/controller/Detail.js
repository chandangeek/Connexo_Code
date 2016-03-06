Ext.define('Idc.controller.Detail', {
    extend: 'Isu.controller.IssueDetail',
    controllers: [
        'Bpm.monitorissueprocesses.controller.MonitorIssueProcesses'
    ],
    stores: [
        'Isu.store.IssueActions',
        'Isu.store.Clipboard',
        'Idc.store.TimelineEntries',
        'Bpm.monitorissueprocesses.store.IssueProcesses'
    ],

    models: [
        'Idc.model.Issue'
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
        }
    ],

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
        //menuItem.href = me.router.getRoute(me.router.currentRoute.replace('/view', '') + '/view/startProcess')
        // .buildUrl({issueId: issueId} , {details: menuItem.details, issueType: issueType});
        route = router.getRoute(router.currentRoute + '/viewProcesses');
        route.params.process = processId;
        route.forward();

    },
});