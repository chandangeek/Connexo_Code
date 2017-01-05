Ext.define('Dal.view.Preview', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.FilterDisplay',
        'Dal.privileges.Alarm',
        'Uni.store.Apps',
        'Dal.view.ActionMenu'
    ],
    alias: 'widget.alarm-preview',
    layout: 'column',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 0.5
    },
    frame: true,
    router: null,
    fieldxtype: 'displayfield',
    showTools: true,

    initComponent: function () {
        var me = this;

        if (me.showTools) {
            me.tools = [
                {
                    xtype: 'uni-button-action',
                    itemId: 'alarm-preview-actions-button',
                    privileges: Dal.privileges.Alarm.adminDevice,
                    menu: {
                        xtype: 'alarms-action-menu',
                        itemId: 'alarm-overview-action-menu',
                        router: me.router
                    },
                    listeners: {
                        click: function () {
                            this.showMenu();
                        }
                    }
                }
            ];
        }

        me.items = [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        xtype: 'displayfield',
                        itemId: 'alarm-id',
                        fieldLabel: Uni.I18n.translate('general.title.alarmId', 'DAL', 'Id'),
                        name: 'alarmId'
                    },
                    {
                        xtype: me.fieldxtype,
                        itemId: 'alarm-reason',
                        fieldLabel: Uni.I18n.translate('general.title.reason', 'DAL', 'Reason'),
                        name: 'reason',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : '-';
                        }
                    },
                    {
                        itemId: 'alarm-usage-point',
                        fieldLabel: Uni.I18n.translate('general.title.Usage point', 'DAL', 'Usage point'),
                        name: 'usagePointMRID',
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
                        xtype: me.fieldxtype,
                        itemId: 'alarm-device',
                        fieldLabel: Uni.I18n.translate('general.title.device', 'DAL', 'Device'),
                        name: 'device',
                        renderer: function (value) {
                            var result = '';

                            if (value && value.name && Dal.privileges.Alarm.canViewDeviceCommunication()) {
                                var url = me.router.getRoute('devices/device').buildUrl({deviceId: value.name});
                                result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                            } else if (value) {
                                result = value.name;
                            }

                            return result;
                        }
                    },
                    {
                        itemId: 'alarm-location',
                        fieldLabel: Uni.I18n.translate('general.title.location', 'DAL', 'Location'),
                        name: 'location',
                        renderer: function (value) {
                            if (!Ext.isEmpty(value)) {
                                return Ext.String.htmlEncode(value).replace(/(?:\\r\\n|\\r|\\n)/g, '<br>');
                            } else {
                                return '-'
                            }
                        }
                    },
                    {
                        itemId: 'alarm-logbook',
                        fieldLabel: Uni.I18n.translate('general.title.logbook', 'DAL', 'Logbook'),
                        name: 'logbook',
                        renderer: function (value, metaData, record) {
                            var url = '',
                                result = '-';

                            if (value && Dal.privileges.Alarm.canViewLogbook()) {
                                url = me.router.getRoute('devices/device/logbooks/logbookdata').buildUrl({deviceId: record.get('id'), logbookId: value.id});
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
                        xtype: me.fieldxtype,
                        itemId: 'alarm-status',
                        fieldLabel: Uni.I18n.translate('general.title.status', 'DAL', 'Status'),
                        name: 'status',
                        afterSubTpl: '<span id="alarm-status-field-sub-tpl" class="field-additional-info" style="color: #686868;"></span>',
                        renderer: function (value, field) {
                            return value ? value.name : '-';
                        }
                    },
                    {
                        xtype: 'displayfield',
                        itemId: 'alarm-due-date',
                        fieldLabel: Uni.I18n.translate('general.title.dueDate', 'DAL', 'Due date'),
                        name: 'dueDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateShort(value) : '-';
                        }
                    },
                    {
                        xtype: me.fieldxtype,
                        itemId: 'alarm-workgroup',
                        fieldLabel: Uni.I18n.translate('general.title.workgroup', 'DAL', 'Workgroup'),
                        name: 'workGroupAssignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'DAL', 'Unassigned');
                        }
                    },
                    {
                        xtype: me.fieldxtype,
                        itemId: 'alarm-user',
                        fieldLabel: Uni.I18n.translate('general.title.user', 'DAL', 'User'),
                        name: 'userAssignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'DAL', 'Unassigned');
                        }
                    },
                    {
                        itemId: 'alarm-creation-date',
                        fieldLabel: Uni.I18n.translate('general.title.creationDate', 'DAL', 'Creation date'),
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