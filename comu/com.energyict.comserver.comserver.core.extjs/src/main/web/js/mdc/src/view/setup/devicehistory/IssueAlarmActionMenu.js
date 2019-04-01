/**
 * Created by H251853 on 9/21/2017.
 */
Ext.define('Mdc.view.setup.devicehistory.IssueAlarmActionMenu', {
    extend: 'Isu.view.issues.ActionMenu',
    requires: [
        'Isu.privileges.Issue',
        'Isu.privileges.Device',
        'Dal.privileges.Alarm',
        'Isu.store.IssueActions'
    ],
    alias: 'widget.issues-alarms-action-menu',
    store: 'Isu.store.IssueActions',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    router: null,
    mixins: {
        bindable: 'Ext.util.Bindable'
    },
    currentUserId: -1,
    urlIssueStoreProxy: '/api/isu/issues/{0}/actions',
    urlAlarmStoreProxy: '/api/dal/alarms/{0}/actions',
    onBeforeShow: Ext.emptyFn,
    listeners: {
        show: {
            fn: function () {
                var me = this;

                me.removeAll();

                if ((me.record.get('issueType').uid === "datacollection") || (me.record.get('issueType').uid === "datavalidation") || (me.record.get('issueType').uid === "devicelifecycle")) {
                    me.setLoading(true);
                    me.store.getProxy().url = Ext.String.format(me.urlIssueStoreProxy, me.record.getId());
                    me.store.load(function () {
                        me.onLoad();
                        me.setLoading(false);
                    });
                } else if (me.record.get('issueType').uid === 'devicealarm') {
                    me.setLoading(true);
                    me.store.getProxy().url = Ext.String.format(me.urlAlarmStoreProxy, me.record.getId());
                    me.store.load(function () {
                        me.onLoad();
                        me.setLoading(false);
                    });
                } else {
                    //<debug>
                    console.error('Record for \'' + me.xtype + '\' is not defined');
                    //</debug>
                }
            }
        }
    },
    initComponent: function () {
        var me = this;

        //<debug>
        if (!me.router) {
            console.error('Router for \'' + me.xtype + '\' is not defined');
        }
        //</debug>

        me.bindStore(me.store || 'ext-empty-store', true);

        this.callParent(arguments);
    },

    prepare: function () {
        var me = this;

        // load current user
        (me.currentUserId != -1) && Ext.Ajax.request({
            url: '/api/usr/currentuser',
            success: function (response) {
                var currentUser = Ext.decode(response.responseText, true);
                me.currentUserId = currentUser.id;
            }
        });
    },

    // this method overwritten to avoid firing click event twice in menu
    onClick: function (e) {
        var me = this,
            item;

        if (me.disabled) {
            e.stopEvent();
            return;
        }

        item = (e.type === 'click') ? me.getItemFromEvent(e) : me.activeItem;
        if (item && item.isMenuItem) {
            if (!item.menu || !me.ignoreParentClicks) {
                item.onClick(e);
                me.snoozeCheck(item);
            } else {
                e.stopEvent();
            }
        }
        // Click event may be fired without an item, so we need a second check
        if (!item || item.disabled) {
            item = undefined;
        }
        me.fireEvent('click', me, item, e);
        me.hide();
    },

    onLoad: function () {
        var me = this;

        if (!me.router) {
            return
        }

        Ext.suspendLayouts();
        me.removeAll();
        me.addDynamicActions();
        me.addPredefinedActions();
        me.addSpecificActions();
        me.reorderItems();
        Ext.resumeLayouts(true);
    },

    addDynamicActions: function () {
        var me = this,
            issueId = me.record.getId(),
            issueType = me.record.get('issueType').uid;

        // add dynamic actions
        me.store.each(function (record) {
            var privileges;
            switch (record.get('name')) {
                case 'Assign issue':
                    privileges = Isu.privileges.Issue.canDoAction() && Isu.privileges.Issue.assign;
                    break;
                case 'Assign alarm':
                    privileges = Dal.privileges.Alarm.canDoAction() && Dal.privileges.Alarm.assign;
                    break;
                case 'Close issue':
                    privileges = Isu.privileges.Issue.canDoAction() && Isu.privileges.Issue.close;
                    break;
                case 'Close alarm':
                    privileges = Dal.privileges.Alarm.canDoAction() && Dal.privileges.Alarm.close;
                    break;
                case 'Retry now':
                    privileges = Isu.privileges.Device.canOperateDeviceCommunication() && Isu.privileges.Issue.canDoAction();
                    break;
                case 'Retry estimation':
                    privileges = Isu.privileges.Issue.runTask;
                    break;
                case 'Set priority':
                    privileges = me.isIssueType() ? Isu.privileges.Issue.canDoAction() : (Dal.privileges.Alarm.canDoAction() && Dal.privileges.Alarm.setPriority);
                    break;
                case 'Snooze':
                    privileges = me.isIssueType() ? Isu.privileges.Issue.canDoAction() : Dal.privileges.Alarm.canDoAction();
                    break;
            }

            var menuItem = {
                text: record.get('name'),
                section: record.get('actionType'),
                privileges: privileges
            };

            if (Ext.isEmpty(record.properties().count())) {
                menuItem.actionRecord = record;
            } else {
                menuItem.href = me.router.getRoute(me.router.currentRoute + '/view/action').buildUrl(
                    {
                        issueId: issueId,
                        actionId: record.getId()
                    },
                    {
                        fromOverview: me.router.currentRoute.match('view') != null,
                        issueType: issueType
                    }
                );
            }
            me.add(menuItem);
        });
    },

    onCheck: function (getConfirmationWindow) {
        var me = this,
            confWindow = getConfirmationWindow(),
            progressbar = confWindow.insert(2, {
                xtype: 'progressbar',
                itemId: 'snooze-progressbar',
                margin: '5 0 15 0'
            });

        progressbar.wait({
            duration: 10000,
            fn: Ext.bind(me.onTooLongOperation, me, [confWindow, {
                title: Uni.I18n.translate('issue.snooze.timeout.title1', 'MDC', 'Snooze request takes longer than expected'),
                msg: Uni.I18n.translate('issue.snooze.timeout.message', 'MDC', 'Snooze request takes longer than expected and will continue in the background.')
            }])
        });

        me.doOperation(confWindow, Uni.I18n.translate('snooze.successMsg', 'MDC', 'Snooze successful'), 'snooze');
    },

    doOperation: function (confirmationWindow, successMessage, action) {
        var me = this,
            router = me.router,
            updatedData;

        updatedData = {
            issue: {
                id: me.record.getData().id,
                version: me.record.getData().version
            },
            snoozeDateTime: confirmationWindow.down('#issue-snooze-until-date').getValue().getTime()
        };

        Ext.Ajax.request({
            url: me.isIssueType() ? '/api/isu/issues/snooze' : '/api/dal/alarms/snooze',
            method: 'PUT',
            jsonData: Ext.encode(updatedData),
            success: function (response) {
                confirmationWindow.close();
                router.getApplication().fireEvent('acknowledge', successMessage);
                router.getRoute().forward(null, {activeTab: 'issues'});
            },
            failure: function (response) {
                var json = Ext.decode(response.responseText, true);
                if (json && json.errors && json.errors.length > 0) {
                    confirmationWindow.down('#snooze-progressbar').reset(true);
                    confirmationWindow.down('#issue-snooze-until-date').markInvalid(json.errors[0].msg);

                }
            }
        });
    },

    onTooLongOperation: function (confirmationWindow, errorMessageConfig) {
        var errorMessage = Ext.widget('messagebox', {
            closeAction: 'destroy',
            buttons: [
                {
                    text: Uni.I18n.translate('general.close', 'MDC', 'Close'),
                    ui: 'remove',
                    handler: function () {
                        this.up('window').close();
                    }
                }
            ]
        });

        Ext.suspendLayouts();
        confirmationWindow.close();
        errorMessage.show(Ext.apply({
            ui: 'notification-error',
            modal: false,
            icon: Ext.MessageBox.ERROR
        }, errorMessageConfig));
        Ext.resumeLayouts(true);
    },


    snoozeCheck: function (item) {
        if (item.action === "snooze") {
            var me = this,
                issueId = me.record.getId(),
                issueType = me.record.get('issueType').uid,
                router = this.router,
                snoozedDateTime;

            if (me.record.get('status').id == 'status.snoozed') {
                snoozedDateTime = new Date(me.record.get('snoozedDateTime'));
            }
            else {
                var tomorrowMidnight = new Date();
                tomorrowMidnight.setHours(24, 0, 0, 1);
                snoozedDateTime = tomorrowMidnight;
            }

            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'snooze-snoozeConfirmationWindow',
                confirmText: Uni.I18n.translate('issue.snooze', 'MDC', 'Snooze'),
                closeAction: 'destroy',
                green: true,
                confirmation: Ext.bind(this.onCheck, this, [getConfirmationWindow])
            })
            ;

            confirmationWindow.insert(1, {
                xtype: 'snooze-date',
                itemId: 'issue-sel-snooze-run',
                defaultDate: snoozedDateTime,
                padding: '-10 0 0 45'
            });
            confirmationWindow.insert(1, {
                itemId: 'snooze-now-window-errors',
                xtype: 'label',
                margin: '0 0 10 50',
                hidden: true
            });
            confirmationWindow.show({
                title: Uni.I18n.translate('issue.snoozeNow', 'MDC', "Snooze '{0}'?",
                    this.record.get('title'), false)
            });

            function getConfirmationWindow() {
                return confirmationWindow;
            }
        }
    },

    addPredefinedActions: function () {
        var me = this,
            issueId = me.record.getId(),
            issueType = me.record.get('issueType').uid,
            fromDetails = Ext.ComponentQuery.query('issue-detail-top')[0],
            predefinedItems = me.getPredefinedItems();

        var me = this,
            predefinedItems = me.getPredefinedItems();

        // show/hide 'Assign to me and' and 'Unassign' menu items
        var assignIssueToMe = predefinedItems.filter(function (menu) {
            return menu.action === 'assignIssueToMe';
        })[0];
        assignIssueToMe.hidden = (me.record.get('userId') == me.currentUserId);
        assignIssueToMe.record = me.record;


        var unassign = predefinedItems.filter(function (menu) {
            return menu.action === 'unassign';
        })[0];
        unassign.hidden = (me.record.get('userId') != me.currentUserId);
        unassign.record = me.record;

        var snoozeVisible = predefinedItems.filter(function (menu) {
            return menu.action === 'snooze';
        })[0];
        snoozeVisible.hidden = ((me.record.getData().status.id == 'status.resolved') || (me.record.getData().status.id == 'status.wont.fix') || (me.record.get('status').id == 'status.in.progress') || (me.record.getData().status.id == 'status.forwarded'));
        snoozeVisible.record = me.record;

        // add predefined actions
        if (predefinedItems && predefinedItems.length) {
            Ext.Array.each(predefinedItems, function (menuItem) {
                switch (menuItem.action) {
                    case 'assignIssue':
                        menuItem.href = me.router.getRoute(me.router.currentRoute + '/view/assignIssue').buildUrl(
                            {
                                issueId: issueId
                            },
                            {
                                addComment: true,
                                issueType: issueType,
                                fromOverview: me.router.currentRoute.match('view') != null
                            }
                        );
                        break;
                    case 'addComment':
                        menuItem.href = me.router.getRoute(me.router.currentRoute + '/view').buildUrl(
                            {
                                issueId: issueId
                            },
                            {
                                addComment: true,
                                issueType: issueType
                            }
                        );
                        break;
                    case 'startProcess':
                        menuItem.href = me.router.getRoute(me.router.currentRoute + '/view/startProcess').buildUrl(
                            {
                                issueId: issueId
                            },
                            {
                                details: menuItem.details,
                                issueType: issueType
                            }
                        );
                        break;
                    case 'setPriority':
                        menuItem.href = me.router.getRoute(me.router.currentRoute + '/view/setpriority').buildUrl(
                            {
                                issueId: issueId
                            },
                            {
                                details: (fromDetails) ? true : false,
                                issueType: issueType
                            }
                        );
                        break;
                }
            });
            me.add(predefinedItems);
        }

        if (Isu.privileges.Issue.canViewProcessMenu() && issueType == 'datacollection') {
            me.add({
                text: Uni.I18n.translate('issues.actionMenu.startProcess', 'MDC', 'Start process'),
                action: 'startProcess',
                section: this.SECTION_ACTION,
                href: me.router.getRoute(me.router.currentRoute + '/view/startProcess').buildUrl({issueId: issueId}, {
                    details: false,
                    issueType: issueType
                }),
                details: false
            });
        }

        if ((Dal.privileges.Alarm.canViewProcesses()) && issueType == 'devicealarm') {
            var detail = Ext.ComponentQuery.query('alarm-detail-top')[0];
            me.add({
                text: Uni.I18n.translate('alarms.actionMenu.startProcess', 'MDC', 'Start process'),
                action: 'startProcess',
                section: this.SECTION_ACTION,
                href: me.router.getRoute(me.router.currentRoute.replace('/view', '') + '/view/startProcess').buildUrl({issueId: issueId}, {details: (detail) ? true : false}),
                details: false
            });
        }
    },

    addSpecificActions: function () {
        var me = this,
            issueId = me.record.getId(),
            issueType = me.record.get('issueType').uid,
            deviceName,
            comTaskId,
            comTaskSessionId,
            connectionTaskId,
            comSessionId;
        // add specific actions
        if (Isu.privileges.Device.viewDeviceCommunication && (issueType === "datacollection") || (issueType === "datavalidation")) {
            deviceName = me.record.get('deviceName');
            if (deviceName) {
                comTaskId = me.record.get('comTaskId');
                comTaskSessionId = me.record.get('comTaskSessionId');
                connectionTaskId = me.record.get('connectionTaskId');
                comSessionId = me.record.get('comSessionId');
                if (comTaskId && comTaskSessionId) {
                    me.add({
                        text: Uni.I18n.translate('issues.actionMenu.viewCommunicationLog', 'MDC', 'View communication log'),
                        href: me.router.getRoute('devices/device/communicationtasks/history/viewlog').buildUrl(
                            {
                                deviceId: deviceName,
                                comTaskId: comTaskId,
                                historyId: comTaskSessionId
                            },
                            {
                                logLevels: ['Error', 'Warning', 'Information']
                            }
                        ),
                        section: me.SECTION_VIEW,
                        hrefTarget: '_blank'
                    });
                }
                if (connectionTaskId && comSessionId) {
                    me.add({
                        text: Uni.I18n.translate('issues.actionMenu.viewConnectionLog', 'MDC', 'View connection log'),
                        href: me.router.getRoute('devices/device/connectionmethods/history/viewlog').buildUrl(
                            {
                                deviceId: deviceName,
                                connectionMethodId: connectionTaskId,
                                historyId: comSessionId
                            },
                            {
                                logLevels: ['Error', 'Warning', 'Information'],
                                communications: ['Connections', 'Communications']
                            }
                        ),
                        section: me.SECTION_VIEW,
                        hrefTarget: '_blank'
                    });
                }
            }
        }
    },

    isIssueType: function () {
        var issueType = this.record.get('issueType').uid;
        return (issueType === "datacollection") || (issueType === "datavalidation") || (issueType === "devicelifecycle");
    },

    isAlarmType: function () {
        var issueType = this.record.get('issueType').uid;
        return (issueType === "devicealarm");
    },

    getPredefinedItems: function () {
        var me = this,
            issueType = me.record.get('issueType').uid;

        if ((issueType === "datacollection") || (issueType === "datavalidation") || (issueType === "devicelifecycle")) {
            return me.getPredefinedItemsForIssues();
        }
        else if (issueType == 'devicealarm') {
            return me.getPredefinedItemsForAlarms();
        }
    },

    getPredefinedItemsForIssues: function () {
        var me = this;
        return [
            {
                text: Uni.I18n.translate('issues.actionMenu.assignToMe', 'MDC', 'Assign to me'),
                privileges: Isu.privileges.Issue.assign,
                action: 'assignIssueToMe',
                itemId: 'assign-to-me',
                section: me.SECTION_ACTION,
                hidden: true
            },
            {
                text: Uni.I18n.translate('issues.actionMenu.unassign', 'MDC', 'Unassign'),
                privileges: Isu.privileges.Issue.assign,
                action: 'unassign',
                itemId: 'unassign',
                section: me.SECTION_ACTION,
                hidden: true
            },
            {
                text: Uni.I18n.translate('issues.actionMenu.snooze', 'MDC', 'Snooze'),
                privileges: Isu.privileges.Issue.action,
                action: 'snooze',
                itemId: 'snooze-date',
                section: me.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('issues.actionMenu.addComment', 'MDC', 'Add comment'),
                privileges: Isu.privileges.Issue.comment,
                section: me.SECTION_ACTION,
                action: 'addComment'
            },
            {
                text: Uni.I18n.translate('issues.actionMenu.setPriority', 'MDC', 'Set priority'),
                privileges: Isu.privileges.Issue.action,
                section: me.SECTION_EDIT,
                action: 'setPriority'
            }
        ];
    },

    getPredefinedItemsForAlarms: function () {
        var me = this;
        return [
            {
                text: Uni.I18n.translate('issues.actionMenu.assignToMe', 'MDC', 'Assign to me'),
                privileges: Dal.privileges.Alarm.assign,
                action: 'assignIssueToMe',
                itemId: 'assign-alarm-to-me',
                section: me.SECTION_ACTION,
                hidden: true
            },
            {
                text: Uni.I18n.translate('issues.actionMenu.unassign', 'MDC', 'Unassign'),
                privileges: Dal.privileges.Alarm.assign,
                action: 'unassign',
                itemId: 'unassign-alarm',
                section: me.SECTION_ACTION,
                hidden: true
            },
            {
                text: Uni.I18n.translate('issues.actionMenu.snooze', 'MDC', 'Snooze'),
                privileges: Dal.privileges.Alarm.action,
                action: 'snooze',
                itemId: 'snooze-date',
                section: me.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('issues.actionMenu.addComment', 'MDC', 'Add comment'),
                privileges: Dal.privileges.Alarm.comment,
                section: me.SECTION_ACTION,
                action: 'addComment'
            },
            {
                text: Uni.I18n.translate('issues.actionMenu.setPriority', 'MDC', 'Set priority'),
                privileges: Dal.privileges.Alarm.viewAdminAlarm,
                section: me.SECTION_EDIT,
                action: 'setPriority'
            }
        ];
    }
});