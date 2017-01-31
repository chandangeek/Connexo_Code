Ext.define('Mdc.view.setup.deviceevents.DataPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLogbookDataPreview',
    itemId: 'deviceLogbookDataPreview',
    layout: 'fit',
    frame: true,

    device: null,
    router: null,
    requires: [
        'Uni.form.field.LastEventTypeDisplay'
    ],
    eventsView: true,
    initComponent: function () {
        var me = this,
            itemsLeft = [
                {
                    fieldLabel: Uni.I18n.translate('deviceevents.eventDate', 'MDC', 'Event date'),
                    name: 'eventDate',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
                    }
                }
            ],
            itemsRight = [
                {
                    fieldLabel: Uni.I18n.translate('general.description', 'MDC', 'Description'),
                    name: 'message',
                    renderer: function(value) {
                        return Ext.isEmpty(value) ? '-' : value;
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('deviceevents.eventAttributes', 'MDC', 'Event attributes'),
                    name: 'eventData',
                    renderer: function (value) {
                        if (Ext.isEmpty(value)) {
                            return '-';
                        }
                        var result = '';
                        for(var propertyName in value) {
                            result += propertyName + ': ' + value[propertyName] + '</br>';
                        }
                        return result;
                    }
                }
            ];

        if (me.eventsView) {
            itemsLeft.push(
                {
                    fieldLabel: Uni.I18n.translate('general.logbook', 'MDC', 'Logbook'),
                    name: 'logBookId',
                    itemId: 'logBookId',
                    renderer: function (value) {
                        var res = '';
                        if (Ext.isNumber(value)) {
                            var logbook = Mdc.model.LogbookOfDevice;
                            logbook.getProxy().setExtraParam('deviceId', me.device.get('name'));
                            logbook.load(value, {
                                success: function (record) {
                                    me.down('#logBookId').setValue(record);
                                }
                            });
                        }
                        value.isModel ? res = '<a href="{url}">{logbookName}</a>'.replace('{url}', me.router.getRoute('devices/device/logbooks/logbookdata').buildUrl({logbookId: value.get('id')})).replace('{logbookName}', Ext.String.htmlEncode(value.get('name'))) : null;
                        return res
                    }
                }
            )
        }

        itemsLeft.push(
            {
                xtype: 'last-event-type-displayfield',
                fieldLabel: Uni.I18n.translate('deviceevents.eventType', 'MDC', 'Event type'),
                name: 'eventType',
                emptyText: '-'
            },
            {
                fieldLabel: Uni.I18n.translate('deviceevents.deviceCode', 'MDC', 'Device code'),
                name: 'deviceCode'
            },
            {
                fieldLabel: Uni.I18n.translate('deviceevents.eventLogId', 'MDC', 'Event log id'),
                name: 'eventLogId'
            },
            {
                fieldLabel: Uni.I18n.translate('deviceevents.readingDate', 'MDC', 'Reading date'),
                name: 'readingDate',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeLong(value) : '-';
                }
            }
        );

        me.items = [
            {
                xtype: 'form',
                itemId: 'deviceLogbookDataPreviewForm',
                layout: 'column',
                defaults: {
                    xtype: 'container',
                    layout: 'form',
                    columnWidth: 0.5,
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    }
                },
                items: [
                    {
                        items: itemsLeft
                    },
                    {
                        items: itemsRight
                    }
                ]
            }
        ];
        me.callParent(arguments)
    }

});
