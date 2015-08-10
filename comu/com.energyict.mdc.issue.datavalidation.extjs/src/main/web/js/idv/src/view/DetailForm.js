Ext.define('Idv.view.DetailForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Mdc.privileges.Device'
    ],
    alias: 'widget.data-validation-issue-detail-form',
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
                        itemId: 'data-validation-issue-detail-usage-point',
                        fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'IDV', 'Usage point'),
                        name: 'usage_point'
                    },
                    {
                        itemId: 'data-validation-issue-detail-device',
                        fieldLabel: Uni.I18n.translate('general.title.device', 'IDV', 'Device'),
                        name: 'device',
                        renderer: function (value) {
                            var url = '',
                                result = '';

                            if (value) {
                                if (value.serialNumber && Mdc.privileges.Device.canViewDeviceCommunication()) {
                                    url = me.router.getRoute('devices/device').buildUrl({mRID: value.serialNumber});
                                    result = '<a href="' + url + '">' + value.name + ' ' + value.serialNumber + '</a>';
                                } else {
                                    result = value.name + ' ' + value.serialNumber;
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
                        itemId: 'data-validation-issue-detail-status',
                        fieldLabel: Uni.I18n.translate('general.title.status', 'IDV', 'Status'),
                        name: 'status',
                        renderer: function (value) {
                            return value.name ? value.name : '';
                        }
                    },
                    {
                        itemId: 'data-validation-issue-detail-due-date',
                        fieldLabel: Uni.I18n.translate('general.title.dueDate', 'IDV', 'Due date'),
                        name: 'dueDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateLong(value) : '';
                        }
                    },
                    {
                        itemId: 'data-validation-issue-detail-modification-date',
                        fieldLabel: Uni.I18n.translate('general.title.modificationDate', 'IDV', 'Modification date'),
                        name: 'modTime',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateLong(value) : '';
                        }
                    },
                    {
                        itemId: 'data-validation-issue-detail-assignee',
                        fieldLabel: Uni.I18n.translate('general.title.assignee', 'IDV', 'Assignee'),
                        name: 'assignee',
                        renderer: function (value) {
                            return value.name ? value.name : Uni.I18n.translate('general.none', 'IDV', 'None');
                        }
                    },
                    {
                        itemId: 'data-validation-issue-detail-creation-date',
                        fieldLabel: Uni.I18n.translate('general.title.creationDate', 'IDV', 'Creation date'),
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