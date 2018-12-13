/**
 * Created by H251853 on 8/28/2017.
 */
Ext.define('Mdc.view.setup.devicehistory.IssueAlarmPreview', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.FilterDisplay',
        'Isu.view.issues.ActionMenu',
        'Dal.view.ActionMenu',
        'Isu.privileges.Issue',
        'Isu.privileges.Device',
        'Dal.privileges.Alarm'
    ],
    alias: 'widget.issues-alarms-preview',
    layout: 'column',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 0.5
    },
    frame: true,
    router: null,

    initComponent: function () {
        var me = this;

        me.tools = [
            {
                xtype: 'uni-button-action',
                itemId: 'issues-preview-actions-button',
                privileges: Ext.Array.merge(Isu.privileges.Issue.adminDevice, Isu.privileges.Device.viewDeviceCommunication),
                menu: {
                    xtype: 'issues-alarms-action-menu',
                    itemId: 'issues-overview-action-menu',
                    router: me.router,
                    record: me.record,
                    currentUserId: me.currentUserId
                },
                listeners: {
                    click: function () {
                        this.showMenu();
                    }
                 }
            }
        ];

        me.items = [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        itemId: 'issue-id',
                        fieldLabel: Uni.I18n.translate('general.title.issueId', 'MDC', 'ID'),
                        name: 'issueId'
                    },
                    {

                        itemId: 'issue-preview-reason',
                        fieldLabel: Uni.I18n.translate('general.title.reason', 'MDC', 'Reason'),
                        name: 'reason',
                    },
                    {
                        itemId: 'issue-preview-usage-point',
                        fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'MDC', 'Usage point'),
                        name: 'usagePoint',
                        renderer: function (value) {
                            var appName = 'Insight';
                            if (value && Dal.privileges.Alarm.canViewUsagePoint()) {
                                if (Uni.store.Apps.checkApp(appName)) {
                                    if (Mdc.privileges.UsagePoint.canViewInInsight()) {
                                        var url = Ext.String.format('{0}/usagepoints/{1}', Uni.store.Apps.getAppUrl(appName), encodeURIComponent(value));
                                        return Ext.String.format('<a href="{0}">{1}</a>', url, Ext.String.htmlEncode(value));
                                    }
                                } else if (Mdc.privileges.UsagePoint.canView()) {
                                    var url = me.router.getRoute('usagepoints/usagepoint').buildUrl({usagePointId: value});
                                    return Ext.String.format('<a href="{0}">{1}</a>', url, Ext.String.htmlEncode(value));
                                }
                                else {
                                    return '-';
                                }
                            } else {
                                return '-';
                            }
                        }
                    },
                    {
                        name: 'location',
                        itemId: 'issue-location',
                        fieldLabel: Uni.I18n.translate('general.location', 'MDC', 'Location'),
                        renderer: function (value) {
                            if (me.record && me.record.get('device') && !Ext.isEmpty(me.record.get('device').location)) {
                                return Ext.String.htmlEncode(me.record.get('device').location).replace(/(?:\\r\\n|\\r|\\n)/g, '<br>');
                            }
                            return '-';
                        }
                    },
                    {
                        itemId: 'issue-logbook',
                        fieldLabel: Uni.I18n.translate('general.logbook', 'MDC', 'Logbook'),
                        name: 'logBook',
                        renderer: function (value) {
                            var url = '',
                                result = '-';

                            if (value && value.id && me.record && Dal.privileges.Alarm.canViewLogbook()) {
                                url = me.router.getRoute('devices/device/logbooks/logbookdata').buildUrl({deviceId: me.record.get('device').name, logbookId: value.id});
                                result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                            } else if (value) {
                                result = value.name;
                            }
                            return result;
                        }

                    }
                ]
            },
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {

                        itemId: 'issue-preview-status',
                        fieldLabel: Uni.I18n.translate('general.title.status', 'MDC', 'Status'),
                        name: 'statusName'
                    },
                    {
                        itemId: 'issue-preview-due-date',
                        fieldLabel: Uni.I18n.translate('general.title.dueDate', 'MDC', 'Due date'),
                        name: 'dueDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateShort(value) : '-';
                        }
                    },
                    {
                        itemId: 'issue-preview-priority',
                        fieldLabel: Uni.I18n.translate('general.title.priority', 'MDC', 'Priority'),
                        name: 'priorityValue'
                    },
                    {

                        itemId: 'issue-preview-workgroup',
                        fieldLabel: Uni.I18n.translate('general.workgroup', 'MDC', 'Workgroup'),
                        name: 'workGroupAssignee',
                        renderer: function (value) {
                            return value ? Ext.String.htmlEncode(value) : Uni.I18n.translate('general.unassigned', 'MDC', 'Unassigned');
                        }
                    },
                    {

                        itemId: 'issue-preview-assignee',
                        fieldLabel: Uni.I18n.translate('general.title.USER', 'MDC', 'User'),
                        name: 'userAssigneeName',
                        renderer: function (value) {
                            return value ? Ext.String.htmlEncode(value) : Uni.I18n.translate('general.unassigned', 'MDC', 'Unassigned');
                        }
                    },
                    {
                        itemId: 'issue-preview-creation-date',
                        fieldLabel: Uni.I18n.translate('general.title.creationDate', 'MDC', 'Creation date'),
                        name: 'creationDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);

        me.on('afterrender', function () {
            me.record && me.loadRecord(me.record);
        }, me, {single: true});
    }
});
