/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.controller.IssueDetail', {
    extend: 'Ext.app.Controller',
    requires: [
        'Isu.privileges.Issue',
        'Isu.store.TimelineEntries',
        'Bpm.monitorissueprocesses.store.IssueProcesses',
        'Bpm.monitorissueprocesses.store.AlarmProcesses',
        'Uni.util.FormEmptyMessage',
        'Isu.view.issues.EditCommentForm',
        'Isu.view.issues.ManualIssueDetail',
        'Isu.model.ManualIssue'
    ],

    stores: [
        'Isu.store.IssueActions',
        'Isu.store.Issues',
        'Isu.store.TimelineEntries',
        'Bpm.monitorissueprocesses.store.IssueProcesses',
        'Bpm.monitorissueprocesses.store.AlarmProcesses'
    ],

    itemUrl: '/api/isu/issues/',

    showOverview: function (issueId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            issueType = queryString.issueType,
            store = me.getIssueStore(),
            widgetXtype,
            widget;

        Ext.Ajax.request({

            url: '/api/isu/issues/' + issueId +'?variableid=' + issueId + '&variablevalue=' + issueId,
            method: 'GET',
            params: {
                filters: [{property: 'meter',  value : queryString.meter}]
            },
            success: function (operation) {
                var record = Ext.decode(operation.responseText, true);//Ext.JSON.decode(operation.responseText);

                store.loadRawData([record]);
                store.each(function (record) {
                    if (record) {
                        var issueActualType = record.get('issueType').uid;
                        if (issueActualType != issueType) {
                            queryString.issueType = issueActualType;
                            window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                            issueType = issueActualType;
                        }
                    }

                    widgetXtype = me.settingsForCurrentIssueType(issueType);

                    widget = Ext.widget(widgetXtype, {
                        router: router,
                        issuesListLink: me.makeLinkToList(router)
                    });

                    me.widget = widget;
                    me.getApplication().fireEvent('changecontentevent', widget);
                    me.setAdditionalSettingsForCurrentIssue(issueType, widget);

                    Ext.getStore('Isu.store.Clipboard').set('issue', record);
                    widget.down('#issue-detail-top-title').setTitle(record.get('title'));
                    if (issueType === 'datacollection') {
                        me.loadDataCollectionIssueDetails(widget, record);
                    } else {
                        widget.down('#issue-detail-form').loadRecord(record);
                    }
                    Ext.resumeLayouts(true);
                    var subEl = new Ext.get('issue-status-field-sub-tpl');
                    subEl.setHTML(record.get('statusDetail'));

                    if ((typeof me.getActionMenu === "function") && me.getActionMenu()) {
                        me.getActionMenu().record = record;
                    }
                    else if (widget.down('#issue-detail-action-menu')) {
                        widget.down('#issue-detail-action-menu').record = record;
                    }
                    me.loadComments(record, issueType);
                    me.loadProcesses(record, issueType, id);
                });
            }
        });
    },

    settingsForCurrentIssueType: function (issueType) {
        var me = this,
            widgetXtype;
        switch (issueType) {
            case 'datacollection':
                widgetXtype = 'data-collection-issue-detail';
                me.issueModel = 'Idc.model.Issue';
                break;
            case 'datavalidation':
                if (Ext.Ajax.defaultHeaders['X-CONNEXO-APPLICATION-NAME'] == 'MDC') {
                    widgetXtype = 'data-validation-issue-detail';
                    me.issueModel = 'Idv.model.Issue';
                    me.nonEstimatedDataStore = 'Idv.store.NonEstimatedDataStore';
                }
                break;
            case 'devicelifecycle':
                widgetXtype = 'device-lifecycle-issue-detail';
                me.issueModel = 'Idl.model.Issue';
                me.transitionStore = 'Idl.store.TransitionStore';
                break;
            case 'task':
                widgetXtype = 'task-issue-detail';
                me.issueModel = 'Itk.model.Issue';
                me.taskStore = 'Itk.store.OccurrenceStore';
                break;
            case 'servicecall':
                widgetXtype = 'servicecall-issue-detail';
                me.issueModel = 'Isc.model.Issue';
                me.serviceCallLogStore = 'Isc.store.Logs';
                break;
            case 'manual':
                widgetXtype = 'manual-issue-detail';
                me.issueModel = 'Isu.model.ManualIssue';
                break;
            case 'webservice':
                widgetXtype = 'webservice-issue-detail';
                me.issueModel = 'Iws.model.Issue';
                me.webServiceLogStore = 'Iws.store.Logs';
                break;
            default:
                widgetXtype = me.widgetXtype;
                me.issueModel = me.issueModel;
                break;
        }
        return widgetXtype;
    },

    setAdditionalSettingsForCurrentIssue: function (issueType, widget) {
        var me = this;

        switch (issueType) {
            case 'datavalidation':
            case 'usagepointdatavalidation':
                me.addValidationBlocksWidget(widget);
                break;
            case 'devicelifecycle':
                me.addTransitionBlocksWidget(widget);
                break;
            case 'task':
                me.addTaskOccurrenceWidget(widget);
                break;
            case 'servicecall':
                me.addServiceCallIssueLogs(widget);
                break;
            case 'webservice':
                me.addWebServiceIssueLogs(widget);
                break;
            default:
                break;
        }
    },

    loadTimeline: function (commentsStore) {
        var me = this,
            timelineView = this.widget ? this.widget.down('#issue-timeline-view') : this.getPage().down('#issue-timeline-view'),
            processView = this.widget ? this.widget.down('#issue-process-view') : this.getPage().down('#issue-process-view'),
            timelineStore = me.getStore('Isu.store.TimelineEntries'),
            alarm = Ext.ComponentQuery.query('alarm-timeline')[0];
        procesStore = (alarm) ? me.getStore('Bpm.monitorissueprocesses.store.AlarmProcesses') : me.getStore('Bpm.monitorissueprocesses.store.IssueProcesses'),
            router = me.getController('Uni.controller.history.Router'),
            data = [];

        timelineStore.data.clear();
        commentsStore.each(function (rec) {
                data.push({
                    user: rec.data.author.name,
                    actionText: Uni.I18n.translate('issue.workspace.datacollection.added.comment', 'ISU', 'added a comment'),
                    creationDate: rec.data.creationDate,
                    contentText: rec.data.splittedComments
                });
            }
        );
        if (me.canViewProcesses()) {
            procesStore.each(function (rec) {
                    data.push({
                        user: rec.data.startedBy,
                        actionText: Uni.I18n.translate('issue.workspace.datacollection.processStarted', 'ISU', 'started a process'),
                        creationDate: rec.data.startDate,
                        contentText: rec.data.name,
                        forProcess: true,
                        processId: rec.data.processId,
                        status: ' (' + rec.data.statusDisplay + ')'
                    });
                }
            );
        }

        if (timelineView) {
            timelineStore.suspendEvents(false);
            Ext.Array.each(data, function (item) {
                timelineStore.add(item);
            });
            timelineStore.resumeEvents();
            timelineStore.sort('creationDate', 'DESC');

            timelineView.bindStore(timelineStore);
            timelineView.previousSibling('#no-issue-timeline').setVisible(timelineStore.data.items.length <= 0);
        }
    },

    makeLinkToList: function (router) {
        var link = '<a href="{0}">' + Uni.I18n.translate('workspace.issues.title', 'ISU', 'Issues').toLowerCase() + '</a>',
            filter = this.getStore('Isu.store.Clipboard').get('latest-issues-filter'),
            queryParams = filter ? filter : null;

        return Ext.String.format(link, router.getRoute('workspace/issues').buildUrl(null, queryParams));
    },

    loadProcesses: function (record, issueType, id) {
        alarm = Ext.ComponentQuery.query('alarm-timeline')[0];
        var me = this,
            processView = this.widget ? this.widget.down('#issue-process-view') : this.getPage().down('#issue-process-view'),
            processList = processView.up('issue-process-list'),
            processStore = (alarm) ? me.getStore('Bpm.monitorissueprocesses.store.AlarmProcesses') : me.getStore('Bpm.monitorissueprocesses.store.IssueProcesses');
        if (me.canViewProcesses()) {
            switch (issueType) {
                case 'datavalidation':
                case 'datacollection':
                case 'devicelifecycle':
                case 'task':
                case 'servicecall':
                case 'webservice':
                    processStore.getProxy().setUrl(issueId);
                    Ext.Ajax.suspendEvent('requestexception');
                    processView.setLoading();
                    processStore.load({
                        callback: function (records, options, success) {
                            if (success) {
                                processStore.sort('startDate', 'DESC');
                                processView.bindStore(processStore);
                                processList.down('#no-issue-processes')&&processList.down('#no-issue-processes').setVisible(processStore.getCount() <= 0);
                            } else {
                                processList.down('#issue-processes-has-error')&&processList.down('#issue-processes-has-error').show();
                            }
                            processView.setLoading(false);
                            Ext.Ajax.resumeEvent('requestexception');
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    },

    loadComments: function (record, issueType) {
        var
            me = this,
            commentsView = this.widget ? this.widget.down('#issue-comments-list') : this.getPage().down('#issue-comments-list'),
            commentsStore = record.comments(),
            router = this.getController('Uni.controller.history.Router');

        commentsStore.getProxy().url = me.itemUrl + record.getId() + '/comments';
        commentsStore.sort('creationDate', 'DESC');
        commentsView.setLoading(true);
        commentsStore.load(function (records) {
            if (!commentsView.isDestroyed) {
                Ext.suspendLayouts();
                commentsStore.add(records);
                commentsView.store = commentsStore;
                commentsView.show();
                commentsView.previousSibling('#no-issue-comments').setVisible(!records.length && !router.queryParams.addComment);
                commentsView.up('issue-comments').down('#issue-comments-add-comment-button').setVisible(records.length && !router.queryParams.addComment && me.canComment());
                if ((issueType === 'datavalidation') || (issueType === 'datacollection') || (issueType === 'alarm') || (issueType === 'devicelifecycle') || (issueType === 'task') || (issueType === 'servicecall') || (issueType === 'webservice')) {
                    me.loadTimeline(commentsStore);
                }
                me.constructComments(commentsView, commentsStore);
                Ext.resumeLayouts(true);
                commentsView.setLoading(false);
            }
        });
        if (router.queryParams.addComment) {
            if ((issueType === 'datavalidation') || (issueType === 'datacollection') || (issueType === 'alarm') || (issueType === 'devicelifecycle') || (issueType === 'task') || (issueType === 'servicecall') || (issueType === 'webservice')) {
                this.showCommentForm();
            } else {
                this.showCommentFormValidation();
            }
        }
    }
    ,

    showCommentForm: function () {
        var me = this,
            page = this.widget ? this.widget : this.getCommentsPanel(),
            editButtons = Ext.ComponentQuery.query("button[itemId^=btn-edit-]"),
            removeButtons = Ext.ComponentQuery.query("button[itemId^=btn-remove-]"),
            buttons = Ext.Array.merge(editButtons, removeButtons);

        Ext.suspendLayouts();
        page.down('#issue-add-comment-form').show();
        page.down('#issue-add-comment-area').focus();
        page.down('#no-issue-comments').hide();
        page.down('#issue-comments-add-comment-button').hide();

        if (Ext.isArray(buttons)) {
            Ext.Array.each(buttons, function (button) {
                button.setDisabled(true);
            })
        }

        if (page.down('#tab-issue-context')) {
            page.down('#tab-issue-context').setActiveTab(1);
        } else {
            page.up('#tab-issue-context').setActiveTab(1);
        }
        Ext.resumeLayouts(true);
    }
    ,

    showCommentFormValidation: function () {
        var me = this,
            page = this.widget ? this.widget : this.getCommentsPanel(),
            editButtons = Ext.ComponentQuery.query("button[itemId^=btn-edit-]"),
            removeButtons = Ext.ComponentQuery.query("button[itemId^=btn-remove-]"),
            buttons = Ext.Array.merge(editButtons, removeButtons);

        Ext.suspendLayouts();
        page.down('#issue-add-comment-form').show();
        page.down('#issue-add-comment-area').focus();
        page.down('#no-issue-comments').hide();
        page.down('#issue-comments-add-comment-button').hide();
        if (Ext.isArray(buttons)) {
            Ext.Array.each(buttons, function (button) {
                button.setDisabled(true);
            })
        }

        Ext.resumeLayouts(true);
    }
    ,

    hideCommentForm: function () {
        var me = this,
            commentsPanel = this.getCommentsPanel(),
            hasComments = commentsPanel.down('#issue-comments-list').store.getCount() ? true : false,
            editButtons = Ext.ComponentQuery.query("button[itemId^=btn-edit-]"),
            removeButtons = Ext.ComponentQuery.query("button[itemId^=btn-remove-]"),
            router = this.getController('Uni.controller.history.Router'),
            buttons = Ext.Array.merge(editButtons, removeButtons);

        Ext.suspendLayouts();
        commentsPanel.down('#issue-add-comment-form').hide();
        commentsPanel.down('#issue-add-comment-area').reset();
        commentsPanel.down('#issue-comments-add-comment-button').setVisible(hasComments && this.canComment());
        commentsPanel.down('#no-issue-comments').setVisible(!hasComments);
        if (Ext.isArray(buttons)) {
            Ext.Array.each(buttons, function (button) {
                button.setDisabled(false);
            })
        }

        if (router.queryParams.addComment) {
            delete router.queryParams.addComment;
            url = router.getRoute().buildUrl(router.arguments, router.queryParams);
            Uni.util.History.setParsePath(false);
            Uni.util.History.suspendEventsForNextCall();
            window.location.replace(url);
        }

        Ext.resumeLayouts(true);

    }
    ,

    validateCommentForm: function (textarea, newValue) {
        this.getCommentsPanel().down('#issue-comment-save-button').setDisabled(!newValue.trim().length);
    }
    ,

    validateEditCommentForm: function (textarea, newValue) {
        textarea.up('panel').down('#issue-comment-edit-button').setDisabled(!newValue.trim().length);
    }
    ,

    addComment: function () {
        var me = this,
            commentsPanel = me.getCommentsPanel(),
            commentsView = commentsPanel.down('#issue-comments-list'),
            commentsStore = commentsView.store;

        commentsView.setLoading();
        commentsStore.add(commentsPanel.down('#issue-add-comment-form').getValues());
        commentsStore.sync({
            callback: function () {
                commentsStore.load(function (records) {
                    this.add(records);
                    me.constructComments(commentsView, commentsStore);
                    commentsView.setLoading(false);
                    me.loadTimeline(commentsStore);
                })
            }
        });

        me.hideCommentForm();
    }
    ,

    addCommentValidation: function () {
        var me = this,
            commentsPanel = me.getCommentsPanel(),
            commentsView = commentsPanel.down('#issue-comments-list'),
            commentsStore = commentsView.store;

        commentsView.setLoading();
        commentsStore.add(commentsPanel.down('#issue-add-comment-form').getValues());
        commentsStore.sync({
            callback: function () {
                commentsStore.load(function (records) {
                    this.add(records);
                    me.constructComments(commentsView, commentsStore);
                    commentsView.setLoading(false);
                })
            }
        });

        me.hideCommentForm();
    }
    ,

    chooseAction: function (menu, menuItem) {
        if (!Ext.isEmpty(menuItem.actionRecord)) {
            this.applyActionImmediately(menu.record, menuItem.actionRecord);
        }
    }
    ,

    applyActionImmediately: function (issue, action) {
        var me = this,
            actionModel = Ext.create(issue.actions().model);

        actionModel.setId(action.getId());
        actionModel.set('parameters', {});
        actionModel.getProxy().url = me.itemUrl + issue.getId() + '/actions';
        actionModel.save({
            callback: function (model, operation, success) {
                var responseText = Ext.decode(operation.response.responseText, true);

                if (responseText) {
                    if (responseText.data.actions[0].success) {
                        me.getApplication().fireEvent('acknowledge', responseText.data.actions[0].message);
                        me.getModel('Isu.model.Issue').load(issue.getId(), {
                            success: function (record) {
                                var form = me.getDetailForm();

                                if (form) {
                                    form.loadRecord(record);
                                }
                            }
                        });
                    } else {
                        me.getApplication().getController('Uni.controller.Error').showError(Uni.I18n.translate('administration.issue.apply.action.failed.title', 'ISU', 'Couldn\'t perform your action'), model.get('name') + '.' + responseText.data.actions[0].message, responseText.data.actions[0].errorCode);
                    }
                }
            }
        });
    }
    ,

    loadDataCollectionIssueDetails: function (widget, issue) {
        var me = this,
            container = widget.down('#data-collection-issue-detail-container'),
            router = me.getController('Uni.controller.history.Router'),
            store = null,
            journals,
            form;

        switch (issue.get('reason').id) {
            case 'reason.connection.failed':
                journals = issue.get('connectionTask_journals');
                if (journals && !_.isEmpty(journals)) {
                    store = Ext.create('Idc.store.ConnectionLogs', {data: journals});
                }
                form = Ext.widget('connection-issue-details-form', {
                    itemId: 'connection-issue-details-form',
                    store: store,
                    router: router
                });
                break;
            case 'reason.connection.setup.failed':
                journals = issue.get('connectionTask_journals');
                if (journals && !_.isEmpty(journals)) {
                    store = Ext.create('Idc.store.ConnectionLogs', {data: issue.get('connectionTask_journals')});
                }
                form = Ext.widget('connection-issue-details-form', {
                    itemId: 'connection-setup-issue-details-form',
                    store: store,
                    router: router
                });
                break;
            case 'reason.failed.to.communicate':
                journals = issue.get('communicationTask_journals');
                if (journals && !_.isEmpty(journals)) {
                    store = Ext.create('Idc.store.CommunicationLogs', {data: issue.get('communicationTask_journals')});
                }
                form = Ext.widget('communication-issue-details-form', {
                    itemId: 'communication-issue-details-form',
                    store: store,
                    router: router
                });
                break;
            case 'reason.unknown.inbound.device':
                form = Ext.widget('inbound-issue-details-form', {
                    itemId: 'inbound-issue-details-form'
                });
                break;
            case 'reason.unknown.outbound.device':
                form = Ext.widget('outbound-issue-details-form', {
                    itemId: 'outbound-issue-details-form',
                    router: router
                });
                break;
            case 'reason.unregistered.device':
                form = Ext.widget('meter-registration-issue-details-form', {
                    itemId: 'meter-registration-issue-details-form',
                    router: router
                });
                break;
            default:
                form = Ext.widget('default-issue-details-form', {
                    itemId: 'default-issue-details-form',
                    router: router
                });
                break;
        }
        form.loadRecord(issue);
        container.add(form);
    }
    ,

    addValidationBlocksWidget: function (widget) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        me.getApplication().on('issueLoad', function (rec) {
            var panel = widget.down('#no-estimated-data-panel');
            if (rec.raw.notEstimatedData && panel) {
                var data = [],
                    store, validationBlocksWidget;

                rec.raw.notEstimatedData.map(function (item) {
                    item.notEstimatedBlocks.map(function (block) {
                        data.push(Ext.apply({}, {
                            mRID: item.readingType.mRID,
                            channelId: item.channelId,
                            registerId: item.registerId,
                            readingType: item.readingType
                        }, block))
                    });
                });

                if (data.length) {
                    store = Ext.create(me.nonEstimatedDataStore, {data: data});
                    validationBlocksWidget = Ext.widget('no-estimated-data-grid', {
                        itemId: 'validation-no-estimated-data-grid',
                        store: store,
                        router: router,
                        issue: rec
                    });
                } else {
                    validationBlocksWidget = Ext.widget('uni-form-empty-message', {
                        text: Uni.I18n.translate('issues.validationBlocks.empty.reason1', 'ISU', 'No open validation issues.')
                    });
                }

                panel.removeAll();
                panel.add(validationBlocksWidget);
            }
        }, me, {
            single: true
        });
    }
    ,

    addTransitionBlocksWidget: function (widget) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        me.getApplication().on('issueLoad', function (rec) {
            var panel = widget.down('#device-lifecycle-issue-detail-container');

            if (rec.raw.failedTransitionData && panel) {
                var data = [],
                    store, validationBlocksWidget;

                rec.raw.failedTransitionData.map(function (item) {
                    item.failedTransitions.map(function (block) {
                        data.push(Ext.apply({}, {
                            deviceType: rec.raw.device.name,
                            cause: block.cause,
                            from: block.from.name,
                            failedStateChange: 'From ' + block.from.name + ' to ' + block.to.name,
                            deviceLifecycle: block.lifecycle.name,
                            transition: block.transition.name
                        }, block))
                    });
                });

                if (data.length) {
                    store = Ext.create(me.transitionStore, {data: data});
                    panel.getView().bindStore(store);
                }
            }
        }, me, {
            single: true
        });
    }
    ,

    addTaskOccurrenceWidget: function (widget) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        me.getApplication().on('issueLoad', function (rec) {
            var panel = widget.down('#task-issue-detail-container');

            if (rec.raw.taskOccurrences && panel) {
                var data = [],
                    store, taskOccurenceWidget;

                rec.raw.taskOccurrences.map(function (taskOccurrence) {
                    data.push(Ext.apply({}, {
                        triggerTime: taskOccurrence.triggerTime,
                        startDate: taskOccurrence.startDate,
                        enddate: taskOccurrence.enddate,
                        status: taskOccurrence.status,
                        errorMessage: taskOccurrence.errorMessage,
                        failureTime: taskOccurrence.failureTime
                    }, taskOccurrence))
                });
                if (data.length) {
                    store = Ext.create(me.taskStore, {
                        data: data,
                        sorters: [
                            {
                                property: 'startDate',
                                direction: 'DESC'
                            }
                        ],
                    });
                    panel.getView().bindStore(store);
                }
            }
        }, me, {
            single: true
        });
    }
    ,

    addServiceCallIssueLogs: function (widget) {
        var me = this;

        me.getApplication().on('issueLoad', function (rec) {
            var panel = widget.down('#servicecall-issue-detail-log'),
                detailsForm = widget.down('#servicecall-details-form');

            if (rec.raw.serviceCallInfo && rec.raw.serviceCallInfo.logs && panel) {
                var data = [];

                rec.raw.serviceCallInfo.logs.map(function (log) {
                    data.push(Ext.apply({}, {
                        timestamp: log.timestamp,
                        details: log.details,
                        logLevel: log.logLevel,
                    }, log))
                });

                Ext.getStore('Isc.store.Logs').loadData(data);
                panel.bindStore(Ext.getStore('Isc.store.Logs'), true);
                panel.addDocked({
                    xtype: 'toolbar',
                    itemId: 'components-list-top-toolbar',
                    items: [
                        '->',
                        {
                            xtype: 'exporterbutton',
                            itemId: 'components-exporter-button',
                            ui: 'icon',
                            iconCls: 'icon-file-download',
                            text: '',
                            component: 'servicecall-issue-detail-log'
                        }
                    ]
                })
            }

            detailsForm && detailsForm.loadRecord(rec);

        }, me, {
            single: true
        });
    }
    ,

    addWebServiceIssueLogs: function (widget) {
        var me = this;

        me.getApplication().on('issueLoad', function (rec) {
            var panel = widget.down('#webservice-issue-detail-log'),
                detailsForm = widget.down('#webservice-details-form');

            if (rec.raw.webServiceCallOccurrence && rec.raw.webServiceCallOccurrence.logs && panel) {
                var data = [];

                rec.raw.webServiceCallOccurrence.logs.map(function (log) {
                    data.push(Ext.apply({}, {
                        timestamp: log.timestamp,
                        message: log.message,
                        logLevel: log.logLevel,
                    }, log))
                });
                Ext.getStore('Iws.store.Logs').loadData(data);
                panel.bindStore(Ext.getStore('Iws.store.Logs'), true);
                panel.addDocked({
                    xtype: 'toolbar',
                    itemId: 'components-list-top-toolbar',
                    items: [
                        '->',
                        {
                            xtype: 'exporterbutton',
                            itemId: 'components-exporter-button',
                            ui: 'icon',
                            iconCls: 'icon-file-download',
                            text: '',
                            component: 'webservice-issue-detail-log'
                        }
                    ]
                })
            }

            detailsForm && detailsForm.loadRecord(rec);

        }, me, {
            single: true
        });
    }
    ,

    refreshGrid: function (widget) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            issueType = queryString.issueType,
            issueId = router.arguments.issueId,
            issueModel;


        if (issueType === 'datacollection') {
            issueModel = 'Idc.model.Issue';
        } else if (issueType === 'datavalidation') {
            issueModel = 'Idv.model.Issue';
        } else if (issueType === 'devicelifecycle') {
            issueModel = 'Idl.model.Issue';
        } else if (issueType === 'task') {
            issueModel = 'Itk.model.Issue';
        } else if (issueType === 'manual') {
            issueModel = 'Isu.model.ManualIssue';
        } else if (issueType === 'servicecall') {
            issueModel = 'Isc.model.Issue';
        } else if (issueType === 'webservice') {
            issueModel = 'Iws.model.Issue';
        }
        else {
            issueModel = me.issueModel;
        }

        widget.setLoading(true);

        me.getModel(issueModel).load(issueId, {
            callback: function () {
                widget.setLoading(false);
            },
            success: function (record) {
                if (!widget.isDestroyed) {
                    Ext.getStore('Isu.store.Clipboard').set('issue', record);
                    me.getApplication().fireEvent('issueLoad', record);
                    Ext.suspendLayouts();
                    widget.down('#issue-detail-top-title').setTitle(record.get('title'));
                    if (issueType === 'datacollection') {
                        me.loadDataCollectionIssueDetails(widget, record);
                    } else {
                        widget.down('#issue-detail-form').loadRecord(record);
                    }
                    Ext.resumeLayouts(true);
                    if (me.getActionMenu()) {
                        me.getActionMenu().record = record;
                    }
                    me.loadComments(record, issueType);
                }
            }
        });

        if ((issueType === 'datavalidation') || (issueType == 'usagepointdatavalidation')) {
            me.addValidationBlocksWidget(widget);
        }

        if (issueType === 'devicelifecycle') {
            me.addTransitionBlocksWidget(widget);
        }
        if (issueType === 'task') {
            me.addTaskOccurrenceWidget(widget);
        }
        if (issueType === 'servicecall') {
            me.addServiceCallIssueLogs(widget);
        }
        if (issueType === 'webservice') {
            me.addWebServiceIssueLogs(widget);
        }
    }
    ,

    canViewProcesses: function () {
        return Isu.privileges.Issue.canViewProcesses();
    }
    ,

    canComment: function () {
        return Isu.privileges.Issue.canComment();
    }
    ,

    containerclick: function (view, record) {
        var className = record && record.getTarget() && record.getTarget().className;

        if (className == 'icon-pencil2') {
            this.editComment(view, record);
        }
        else if (className == 'icon-cancel-circle2') {
            this.removeComment(view, record)
        }
    }
    ,

    editComment: function (view, record) {
        var me = this,
            commentId = record && record.getTarget() && record.getTarget().id,
            comment = me.widget.getEl().down('p[id=comment' + commentId + ']'),
            staticComment = comment.down('span[id=staticComment]'),
            editedComment = comment.down('span[id=editedComment]');

        staticComment && staticComment.el.setStyle('display', 'none');
        Ext.create('Isu.view.issues.EditCommentForm', {
            renderTo: editedComment,
            comment: staticComment.dom.innerText
        });


    }
    ,

    constructComments: function (issueCommentsView, commentsStore) {
        var me = this,
            itemsAdded = 0;

        // load current user
        Ext.Ajax.request({
            url: '/api/usr/currentuser',
            success: function (response) {
                me.currentUserId = Ext.decode(response.responseText, true).id;

                Ext.suspendLayouts();
                issueCommentsView.removeAll();
                issueCommentsView.add({
                    xtype: 'container',
                    html: '<br>'
                });
                commentsStore.each(function (record) {
                    var creationDate = record.get('creationDate');
                    var items = [];

                    creationDate = creationDate ? me.formatCreationDate(creationDate) : "";

                    // add new row
                    itemsAdded++;
                    html = '<span class="isu-icon-USER"></span><b> ' + record.get('author').name + '</b> ' +
                        Uni.I18n.translate('general.addedcomment.lowercase', 'ISU', 'added a comment') + ' - ' + creationDate;

                    // title
                    items.push(Ext.create('Ext.form.FieldContainer', {
                        html: html
                    }));

                    // comments
                    var splittedComments = [];
                    if (Ext.isArray(record.get('splittedComments'))) {
                        Ext.Array.each(record.get('splittedComments'), function (splittedComment) {
                            splittedComments.push(Ext.String.htmlEncode(splittedComment));
                        })
                    }
                    var comments = Ext.create('Ext.container.Container', {
                        html: splittedComments.join('<br>')
                    });
                    var editComments = Ext.create('Isu.view.issues.EditCommentForm', {
                        hidden: true,
                        cntComment: comments
                    });

                    // edit and remove actions
                    if ((record.get('author').id == me.currentUserId) && me.canComment()) {
                        var edit = Ext.create('Ext.button.Button', {
                            margin: '0 0 0 10',
                            ui: 'plain',
                            itemId: 'btn-edit-' + itemsAdded,
                            iconCls: 'icon-pencil2',
                            tooltip: Uni.I18n.translate('general.editComment', 'ISU', 'Edit'),
                            handler: function () {
                                if (comments.isVisible()) {
                                    me.setDisableEditButtons(true);
                                    comments.setVisible(false);
                                    editComments.setVisible(true);
                                    editComments.record = record;
                                    editComments.setComment(record.get('splittedComments').join('\n'));
                                }
                            }
                        });

                        var remove = Ext.create('Ext.button.Button', {
                            margin: '0 0 0 7',
                            ui: 'plain',
                            itemId: 'btn-remove-' + itemsAdded,
                            iconCls: 'icon-cancel-circle2',
                            tooltip: Uni.I18n.translate('general.removeComment', 'ISU', 'Remove'),
                            record: record,
                            handler: function () {
                                me.removeComment(issueCommentsView, this);
                            }
                        });
                        items.push(edit);
                        items.push(remove);
                    }
                    issueCommentsView.add(Ext.create('Ext.container.Container', {
                        layout: 'hbox',
                        items: items
                    }));

                    issueCommentsView.add([{
                        xtype: 'container',
                        html: '<br>'
                    },
                        comments,
                        editComments,
                        {
                            xtype: 'box',
                            autoEl: {tag: 'hr'}
                        }
                    ]);
                });

                if (me.getController('Uni.controller.history.Router').queryParams.addComment) {
                    var buttons = Ext.Array.merge(Ext.ComponentQuery.query("button[itemId^=btn-edit-]"),
                        Ext.ComponentQuery.query("button[itemId^=btn-remove-]"));
                    if (Ext.isArray(buttons)) {
                        Ext.Array.each(buttons, function (button) {
                            button.setDisabled(true);
                        })
                    }
                }
                Ext.resumeLayouts();
                issueCommentsView.doLayout();
            }
        });
    }
    ,

    formatCreationDate: function (date) {
        date = Ext.isDate(date) ? date : new Date(date);
        return Uni.DateTime.formatDateTimeLong(date);
    }
    ,

    hideEditCommentForm: function (button) {
        var me = this,
            commentsPanel = me.getCommentsPanel(),
            hasComments = commentsPanel.down('#issue-comments-list').store.getCount() ? true : false;

        Ext.suspendLayouts();
        button.editCommentForm.setVisible(false);
        button.commentPanel.setVisible(true);
        me.setDisableEditButtons(false);

        commentsPanel.down('#issue-comments-add-comment-button').setVisible(hasComments && me.canComment());
        commentsPanel.down('#no-issue-comments').setVisible(!hasComments);
        Ext.resumeLayouts(true);
    }
    ,

    editComment: function (editButton, b, c) {
        var me = this,
            commentsView = editButton.up('#issue-comments-list'),
            record = editButton.up('panel').record,
            commentsStore = editButton.up('panel').record.store,
            value = editButton.up('panel').down('#issue-edit-comment-area').getValue(),
            oldValue = record.get('comment');

        if (value != oldValue) {
            record.beginEdit();
            record.set('comment', value);
            record.endEdit();

            commentsView.setLoading();
            commentsStore.sync({
                callback: function () {
                    commentsStore.load(function (records) {
                        this.add(records);
                        me.constructComments(commentsView, commentsStore);
                        me.loadTimeline(commentsStore);
                        me.setDisableEditButtons(false);
                        commentsView.setLoading(false);
                    })
                }
            });
        }
        else {
            commentsView.setLoading();
            me.setDisableEditButtons(false);
            commentsStore.load(function (records) {
                this.add(records);
                commentsView.setLoading(false);
                me.constructComments(commentsView, commentsStore);
                me.loadTimeline(commentsStore);
            })
        }
    }
    ,

    removeComment: function (commentsView, button) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            commentsStore = commentsView.store;

        Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('remove.comment.confirmation.yes', 'ISU', 'Remove'),
            cancelText: Uni.I18n.translate('remove.comment.confirmation.no', 'ISU', 'Cancel'),
            confirmation: function () {
                this.close();
                commentsView.setLoading();
                commentsStore.remove(button.record);
                commentsStore.sync({
                    callback: function () {
                        commentsStore.load(function (records) {
                            this.add(records);
                            commentsView.previousSibling('#no-issue-comments').setVisible(!records.length && !router.queryParams.addComment);
                            commentsView.up('issue-comments').down('#issue-comments-add-comment-button').setVisible(records.length && !router.queryParams.addComment && me.canComment());
                            me.constructComments(commentsView, commentsStore);
                            me.loadTimeline(commentsStore);
                            commentsView.setLoading(false);
                        })
                    }
                });
            }
        }).show({
            msg: Uni.I18n.translate('remove.comment.msg', 'ISU', 'You will no longer see this comment'),
            title: Uni.I18n.translate('remove.comment.title', 'ISU', 'Remove this comment?')

        });
    }
    ,

    setDisableEditButtons: function (enable) {
        var me = this,
            editButtons = Ext.ComponentQuery.query("button[itemId^=btn-edit-]"),
            removeButtons = Ext.ComponentQuery.query("button[itemId^=btn-remove-]"),
            addCommentButton = Ext.ComponentQuery.query("button[itemId^=issue-comments-add-comment-button]"),
            buttons = Ext.Array.merge(editButtons, removeButtons, addCommentButton);

        if (Ext.isArray(buttons)) {
            Ext.Array.each(buttons, function (button) {
                button.setDisabled(enable);
            })
        }
    }
    ,

    getIssueStore: function () {
        return this.getStore('Isu.store.Issues');
    }


});
