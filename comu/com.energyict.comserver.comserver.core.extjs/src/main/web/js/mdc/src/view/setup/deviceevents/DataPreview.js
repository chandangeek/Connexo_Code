Ext.define('Mdc.view.setup.deviceevents.DataPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLogbookDataPreview',
    itemId: 'deviceLogbookDataPreview',
    layout: 'fit',
    frame: true,
    title: '&nbsp;',
    device: null,
    router: null,
    requires: [
        'Uni.form.field.DisplayFieldWithInfoIcon'
    ],
    eventsView: true,
    initComponent: function () {
        var me = this,
            items = [{
                fieldLabel: Uni.I18n.translate('deviceevents.message', 'MDC', 'Message'),
                name: 'message'
            }];
        if (me.eventsView) {
            items.push({
                fieldLabel: Uni.I18n.translate('deviceevents.logbook', 'MDC', 'Logbook'),
                name: 'logBookId',
                itemId: 'logBookId',
                renderer: function (value) {
                    var res = '';
                    if (Ext.isNumber(value)) {
                        var logbook = Mdc.model.LogbookOfDevice;
                        logbook.getProxy().setUrl(me.device.get('mRID'));
                        logbook.load(value, {
                            success: function (record) {
                                me.down('#logBookId').setValue(record)
                            }
                        })
                    }
                    value.isModel ? res = '<a href="{url}">{logbookName}</a>'.replace('{url}', me.router.getRoute('devices/device/logbooks/logbookdata').buildUrl({logbookId: value.get('id')})).replace('{logbookName}', Ext.String.htmlEncode(value.get('name'))) : null;
                    return res
                }
            })
        }

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
                        items: [
                            {
                                fieldLabel: Uni.I18n.translate('deviceevents.eventDate', 'MDC', 'Event date'),
                                name: 'eventDate',
                                renderer: function (value) {
                                    return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                                }
                            },
                            {
                                fieldLabel: Uni.I18n.translate('deviceevents.eventType', 'MDC', 'Event type'),
                                name: 'code'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('deviceevents.deviceType', 'MDC', 'Device type'),
                                name: 'deviceType',
                                renderer: function (value) {
                                    return value ? value.name + ' ' + '(' + value.id + ')' : '';
                                }
                            },
                            {
                                fieldLabel: Uni.I18n.translate('deviceevents.domain', 'MDC', 'Domain'),
                                name: 'domain',
                                renderer: function (value) {
                                    return value ? value.name + ' ' + '(' + value.id + ')' : '';
                                }
                            },
                            {
                                fieldLabel: Uni.I18n.translate('deviceevents.subDomain', 'MDC', 'Subdomain'),
                                name: 'subDomain',
                                renderer: function (value) {
                                    return value ? value.name + ' ' + '(' + value.id + ')' : '';
                                }
                            },
                            {
                                fieldLabel: Uni.I18n.translate('deviceevents.eventOrAction', 'MDC', 'Event or action'),
                                name: 'eventOrAction',
                                renderer: function (value) {
                                    return value ? value.name + ' ' + '(' + value.id + ')' : '';
                                }
                            },
                            {
                                xtype: 'displayfield-with-info-icon',
                                fieldLabel: Uni.I18n.translate('deviceevents.deviceCode', 'MDC', 'Device code'),
                                name: 'deviceCode',
                                infoTooltip: Uni.I18n.translate('deviceevents.deviceCode.tooltip', 'MDC', 'Identifier of the event type on the device')
                            },
                            {
                                xtype: 'displayfield-with-info-icon',
                                fieldLabel: Uni.I18n.translate('deviceevents.eventLogId', 'MDC', 'Event log id'),
                                name: 'eventLogId',
                                infoTooltip: Uni.I18n.translate('deviceevents.eventLogId.tooltip', 'MDC', 'Identifier of the event on the device')
                            },
                            {
                                fieldLabel: Uni.I18n.translate('deviceevents.readingDate', 'MDC', 'Reading date'),
                                name: 'readingDate',
                                renderer: function (value) {
                                    return value ? Uni.DateTime.formatDateTimeLong(value) : '';
                                }
                            }
                        ]
                    },
                    {
                        items: items
                    }
                ]
            }
        ];
        me.callParent(arguments)
    }

});
