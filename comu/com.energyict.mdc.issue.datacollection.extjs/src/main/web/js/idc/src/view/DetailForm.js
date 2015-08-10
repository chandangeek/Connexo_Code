Ext.define('Idc.view.DetailForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Mdc.privileges.Device'
    ],
    alias: 'widget.data-collection-issue-detail-form',
    layout: 'column',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 0.5
    },
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        itemId: 'data-collection-issue-detail-reason',
                        fieldLabel: Uni.I18n.translate('general.title.reason', 'IDC', 'Reason'),
                        name: 'reason',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : '';
                        }
                    },
                    {
                        itemId: 'data-collection-issue-detail-usage-point',
                        fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'IDC', 'Usage point'),
                        name: 'usage_point'
                    },
                    {
                        itemId: 'data-collection-issue-detail-device',
                        fieldLabel: Uni.I18n.translate('general.title.device', 'IDC', 'Device'),
                        name: 'device',
                        renderer: function (value) {
                            var url = '',
                                result = '';

                            if (value) {
                                if (value.serialNumber && Mdc.privileges.Device.canViewDeviceCommunication()) {
                                    url = me.router.getRoute('devices/device').buildUrl({mRID: value.serialNumber});
                                    result = '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + ' ' + value.serialNumber + '</a>';
                                } else {
                                    result = Ext.String.htmlEncode(value.name) + ' ' + value.serialNumber;
                                }
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
                        itemId: 'data-collection-issue-detail-status',
                        fieldLabel: Uni.I18n.translate('general.title.status', 'IDC', 'Status'),
                        name: 'status',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : '';
                        }
                    },
                    {
                        itemId: 'data-collection-issue-detail-due-date',
                        fieldLabel: Uni.I18n.translate('general.title.dueDate', 'IDC', 'Due date'),
                        name: 'dueDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateLong(value) : '';
                        }
                    },
                    {
                        itemId: 'data-collection-issue-detail-assignee',
                        fieldLabel: Uni.I18n.translate('general.title.assignee', 'IDC', 'Assignee'),
                        name: 'assignee',
                        renderer: function (value) {
                            return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.none', 'IDC', 'None');
                        }
                    },
                    {
                        itemId: 'data-collection-issue-detail-creation-date',
                        fieldLabel: Uni.I18n.translate('general.title.creationDate', 'IDC', 'Creation date'),
                        name: 'creationDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});