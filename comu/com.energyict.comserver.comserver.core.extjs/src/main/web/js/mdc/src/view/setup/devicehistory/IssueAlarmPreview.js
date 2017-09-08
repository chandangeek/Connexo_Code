/**
 * Created by H251853 on 8/28/2017.
 */
Ext.define('Mdc.view.setup.devicehistory.IssueAlarmPreview', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.FilterDisplay',
        'Isu.view.issues.ActionMenu',
        'Isu.privileges.Issue',
        'Isu.privileges.Device'
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
                menu1: {
                    xtype: 'issues-action-menu',
                 itemId: 'issues-overview-action-menu',
                 router: me.router
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
                        renderer: function (value) {
                            if (value && me.getRecord()) {
                                me.down('#issue-preview-reason').setVisible(me.getRecord().get('issueType').uid != 'datavalidation');
                                return Ext.String.htmlEncode(value.name);
                            }
                            return '-';
                        }
                    },
                    {
                        itemId: 'issue-preview-usage-point',
                        fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'MDC', 'Usage point'),
                        name: 'usage_point'
                    },
                    {

                        itemId: 'issue-location',
                        fieldLabel: Uni.I18n.translate('general.location', 'MDC', 'Location'),
                        name: 'issueLocation',
                        renderer: function (value) {
                            return value ? value.name : '-';
                        }
                    },
                    {

                        itemId: 'issue-logbook',
                        fieldLabel: Uni.I18n.translate('general.logbook', 'MDC', 'Logbook'),
                        name: 'device',
                        renderer: function (value) {
                            return value ? value.name : '-';
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
                        name: 'status',
                        renderer: function (value) {
                            this.setVisible(!this.isVisible());

                            return value.name ? value.name : '-';
                        }
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
                        name: 'priority'
                    },
                    {

                        itemId: 'issue-preview-workgroup',
                        fieldLabel: Uni.I18n.translate('general.workgroup', 'MDC', 'Workgroup'),
                        name: 'workGroupAssignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'MDC', 'Unassigned');
                        }
                    },
                    {

                        itemId: 'issue-preview-assignee',
                        fieldLabel: Uni.I18n.translate('general.title.USER', 'MDC', 'User'),
                        name: 'userAssignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'MDC', 'Unassigned');
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
    }
});
