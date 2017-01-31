/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.view.ActionMenu', {
    extend: 'Isu.view.issues.ActionMenu',
    requires: [
        'Dal.privileges.Alarm',
        //'Isu.privileges.Device'
    ],
    alias: 'widget.alarms-action-menu',
    //store: 'Isu.store.IssueActions',
    urlStoreProxy: '/api/dal/alarms/{0}/actions',
    predefinedItems: [
        {
            text: Uni.I18n.translate('issues.actionMenu.assignToMe', 'DAL', 'Assign to me'),
            privileges: Dal.privileges.Alarm.assign,
            action: 'assignIssueToMe',
            itemId: 'assign-alarm-to-me',
            hidden: true
        },
        {
            text: Uni.I18n.translate('issues.actionMenu.unassign', 'DAL', 'Unassign'),
            privileges: Dal.privileges.Alarm.assign,
            action: 'unassign',
            itemId: 'unassign-alarm',
            hidden: true
        },
        {
            text: Uni.I18n.translate('issues.actionMenu.addComment', 'DAL', 'Add comment'),
            privileges: Dal.privileges.Alarm.comment,
            action: 'addComment'
        },
        {
            text: Uni.I18n.translate('issues.actionMenu.setPriority', 'DAL', 'Set priority'),
            privileges: Dal.privileges.Alarm.viewAdminAlarm,
            action: 'setPriority'
        }

    ],

    addDynamicActions: function () {
        var me = this,
            itemId = me.record.getId();

        // add dynamic actions
        me.store.each(function (record) {
            var privileges;
            switch (record.get('name')) {
                case 'Assign alarm':
                    privileges = Dal.privileges.Alarm.canDoAction() && Dal.privileges.Alarm.assign;
                    break;
                case 'Close alarm':
                    privileges = Dal.privileges.Alarm.canDoAction() && Dal.privileges.Alarm.close;
                    break;
            }

            var menuItem = {
                text: record.get('name'),
                section: this.SECTION_ACTION,
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

    addPredefinedActions: function () {
        var me = this,
            itemId = me.record.getId(),
            detail = Ext.ComponentQuery.query('alarm-detail-top')[0];

        // show/hide 'Assign to me and' and 'Unassign' menu items
        var assignIssueToMe = me.predefinedItems.filter(function (menu) {
            return menu.action === 'assignIssueToMe';
        })[0];
        assignIssueToMe.hidden = (me.record.get('userId') == me.currentUserId);
        assignIssueToMe.record = me.record;

        var unassign = me.predefinedItems.filter(function (menu) {
            return menu.action === 'unassign';
        })[0];
        unassign.hidden = (me.record.get('userId') != me.currentUserId);
        unassign.record = me.record;

        // add predefined actions
        if (me.predefinedItems && me.predefinedItems.length) {
            Ext.Array.each(me.predefinedItems, function (menuItem) {
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
            me.add(me.predefinedItems);
        }
        if (Dal.privileges.Alarm.viewAdminProcesses) {
            me.add({

                text: Uni.I18n.translate('alarms.actionMenu.startProcess', 'DAL', 'Start process'),
                action: 'startProcess',
                href: me.router.getRoute(me.router.currentRoute.replace('/view', '') + '/view/startProcess').buildUrl({alarmId: itemId}, {details: (detail) ? true : false}),
                details: false
            });
        }
    },

    addSpecificActions: function () {
    }


});
