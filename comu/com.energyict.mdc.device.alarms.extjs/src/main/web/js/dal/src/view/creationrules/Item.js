Ext.define('Dal.view.creationrules.Item', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Dal.view.creationrules.ActionMenu',
    ],
    alias: 'widget.alarm-creation-rules-item',
    title: ' ',
    itemId: 'alarm-creation-rules-item',
    frame: true,
    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Dal.privileges.Alarm.createAlarmRule,
            menu: {
                xtype: 'alarm-creation-rule-action-menu'
            }
        }
    ],
    items: {
        xtype: 'form',
        layout: 'column',
        defaults: {
            xtype: 'container',
            layout: 'form',
            columnWidth: 0.5
        },
        items: [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.title.name', 'DAL', 'Name'),
                        name: 'name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.Template', 'DAL', 'Template'),
                        name: 'template_name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.alarmReason', 'DAL', 'Alarm reason'),
                        name: 'reason_name'
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
                        fieldLabel: Uni.I18n.translate('general.title.created', 'DAL', 'Created'),
                        name: 'creationDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.title.lastModified', 'DAL', 'Last modified'),
                        name: 'modificationDate',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                        }
                    }
                ]
            }
        ]
    }
});