/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.view.ActionMenu', {
    extend: 'Isu.view.issues.ActionMenu',
    requires: [
        'Dal.privileges.Alarm'
    ],
    alias: 'widget.alarms-action-menu',
    urlStoreProxy: '/api/dal/alarms/{0}/actions',
    onBeforeShow: Ext.emptyFn,

    addDynamicActions: function () {
        var me = this,
            itemId = me.record.getId();

        // add dynamic actions
        me.store.each(function (record) {
            var privileges,
                section;
            switch (record.get('name')) {
                case 'Assign alarm':
                    privileges = Dal.privileges.Alarm.canDoAction() && Dal.privileges.Alarm.assign;
                    section = this.SECTION_ACTION;
                    break;
                case 'Close alarm':
                    privileges = Dal.privileges.Alarm.canDoAction() && Dal.privileges.Alarm.close;
                    section = this.SECTION_REMOVE;
                    break;
                case 'Set priority for alarm(s)':
                    privileges = Dal.privileges.Alarm.canDoAction() && Dal.privileges.Alarm.setPriority;
                    section = this.SECTION_REMOVE;
                    break;
                case 'Snooze':
                    privileges = Dal.privileges.Alarm.canDoAction();
                    section = this.SECTION_ACTION;
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
                menuItem.href = me.router.getRoute(me.router.currentRoute.replace('/view', '') + '/view/action').buildUrl(
                    {
                        alarmId: itemId,
                        actionId: record.getId()
                    },
                    {
                        fromOverview: me.router.currentRoute.match('view') != null,
                    }
                );
            }
            me.add(menuItem);
        });
    },

    onCheck: function (getConfirmationWindow) {
        var me = this,
            confWindow = getConfirmationWindow();
        me.doOperation(confWindow, Uni.I18n.translate('snooze.acknowledgment.snoozed', 'DAL', 'Alarm snoozed'), 'snooze');
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
            url: '/api/dal/alarms/snooze',
            method: 'PUT',
            jsonData: Ext.encode(updatedData),
            success: function (response) {
                confirmationWindow.close();
                router.getApplication().fireEvent('acknowledge', successMessage);
                router.getRoute().forward(null, Ext.Object.fromQueryString(router.getQueryString()));
            },
            failure: function (response) {
                var json = Ext.decode(response.responseText, true);
                if (json && json.errors) {
                    confirmationWindow.down('#issue-snooze-until-date').markInvalid(json.errors[0].msg);

                }
            }
        });
    },

    snoozeCheck: function (item) {
        if (item.action === "snooze") {
            var me = this,
                issueId = me.record.getId(),
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
                confirmText: Uni.I18n.translate('issue.snooze', 'DAL', 'Snooze'),
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
                title: Uni.I18n.translate('issue.snoozeNow', 'DAL', "Snooze '{0}'?",
                    this.record.getData().title, false)
            });

            function getConfirmationWindow() {
                return confirmationWindow;
            }
        }
    },

    addPredefinedActions: function () {
        var me = this,
            itemId = me.record.getId(),
            detail = Ext.ComponentQuery.query('alarm-detail-top')[0],
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
        snoozeVisible.hidden = ((me.record.getData().status.id == 'status.resolved') || (me.record.getData().status.id == 'status.wont.fix') || (me.record.getData().status.id == 'status.forwarded'));
        snoozeVisible.record = me.record;

        // add predefined actions
        if (predefinedItems && predefinedItems.length) {
            Ext.Array.each(predefinedItems, function (menuItem) {
                switch (menuItem.action) {
                    case 'assignAlarm':
                        menuItem.href = me.router.getRoute(me.router.currentRoute.replace('/view', '') + '/view/assignIssue').buildUrl(
                            {
                                alarmId: itemId
                            },
                            {
                                addComment: true,
                                fromOverview: me.router.currentRoute.match('view') != null
                            }
                        );
                        break;
                    case 'addComment':
                        menuItem.href = me.router.getRoute(me.router.currentRoute.replace('/view', '') + '/view').buildUrl(
                            {
                                alarmId: itemId
                            },
                            {
                                addComment: true
                            }
                        );
                        break;
                    case 'startProcess':
                        menuItem.href = me.router.getRoute(me.router.currentRoute.replace('/view', '') + '/view/startProcess').buildUrl(
                            {
                                alarmId: itemId
                            },
                            {
                                details: menuItem.details
                            }
                        );
                        break;
                    case 'setPriority':
                        menuItem.href = me.router.getRoute(me.router.currentRoute.replace('/view', '') + '/view/setpriority').buildUrl(
                            {
                                alarmId: itemId
                            },
                            {
                                details: (detail) ? true : false,
                                issueType:'alarm'
                            }
                        );
                        break;
                }
            });
            me.add(predefinedItems);
        }
        if (Dal.privileges.Alarm.viewAdminProcesses) {
            me.add({

                text: Uni.I18n.translate('alarms.actionMenu.startProcess', 'DAL', 'Start process'),
                action: 'startProcess',
                section: this.SECTION_ACTION,
                href: me.router.getRoute(me.router.currentRoute.replace('/view', '') + '/view/startProcess').buildUrl({alarmId: itemId}, {details: (detail) ? true : false}),
                details: false,
                section: me.SECTION_ACTION
            });
        }
    },

    addSpecificActions: function () {
    },

    getPredefinedItems: function () {
        var me = this;
        return [
            {
                text: Uni.I18n.translate('issues.actionMenu.assignToMe', 'DAL', 'Assign to me'),
                privileges: Dal.privileges.Alarm.assign,
                action: 'assignIssueToMe',
                itemId: 'assign-alarm-to-me',
                section: me.SECTION_ACTION,
                hidden: true
            },
            {
                text: Uni.I18n.translate('issues.actionMenu.unassign', 'DAL', 'Unassign'),
                privileges: Dal.privileges.Alarm.assign,
                action: 'unassign',
                itemId: 'unassign-alarm',
                section: me.SECTION_ACTION,
                hidden: true
            },
            {
                text: Uni.I18n.translate('issues.actionMenu.snooze', 'DAL', 'Snooze'),
                privileges: Dal.privileges.Alarm.action,
                action: 'snooze',
                itemId: 'snooze-date',
                section: me.SECTION_ACTION,
            },
            {
                text: Uni.I18n.translate('issues.actionMenu.addComment', 'DAL', 'Add comment'),
                privileges: Dal.privileges.Alarm.comment,
                section: me.SECTION_ACTION,
                action: 'addComment'
            },
            {
                text: Uni.I18n.translate('issues.actionMenu.setPriority', 'DAL', 'Set priority'),
                privileges: Dal.privileges.Alarm.viewAdminAlarm,
                section: me.SECTION_EDIT,
                action: 'setPriority'
            }
        ];
    }
});
