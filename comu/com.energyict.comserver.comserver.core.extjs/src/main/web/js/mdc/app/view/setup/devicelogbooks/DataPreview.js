Ext.define('Mdc.view.setup.devicelogbooks.DataPreview', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.form.field.DisplayFieldWithInfoIcon'
    ],
    alias: 'widget.deviceLogbookDataPreview',
    itemId: 'deviceLogbookDataPreview',
    layout: 'fit',
    frame: true,
    title: '&nbsp;',
    items: [
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
                            fieldLabel: Uni.I18n.translate('devicelogbooks.eventDate', 'MDC', 'Event date'),
                            name: 'eventDate',
                            renderer: function (value) {
                                return value ? Uni.I18n.formatDate('devicelogbooks.eventDate.dateFormat', value, 'MDC', 'M d, Y H:i:s') : '';
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('devicelogbooks.eventType', 'MDC', 'Event type'),
                            name: 'code'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('devicelogbooks.deviceType', 'MDC', 'Device type'),
                            name: 'deviceType',
                            renderer: function (value) {
                                return value ? value.name + ' ' + '(' + value.id + ')' : '';
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('devicelogbooks.domain', 'MDC', 'Domain'),
                            name: 'domain',
                            renderer: function (value) {
                                return value ? value.name + ' ' + '(' + value.id + ')' : '';
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('devicelogbooks.subDomain', 'MDC', 'Subdomain'),
                            name: 'subDomain',
                            renderer: function (value) {
                                return value ? value.name + ' ' + '(' + value.id + ')' : '';
                            }
                        },
                        {
                            fieldLabel: Uni.I18n.translate('devicelogbooks.eventOrAction', 'MDC', 'Event or action'),
                            name: 'eventOrAction',
                            renderer: function (value) {
                                return value ? value.name + ' ' + '(' + value.id + ')' : '';
                            }
                        },
                        {
                            xtype: 'displayfield-with-info-icon',
                            fieldLabel: Uni.I18n.translate('devicelogbooks.deviceCode', 'MDC', 'Device code'),
                            name: 'deviceCode',
                            infoTooltip: Uni.I18n.translate('devicelogbooks.deviceCode.tooltip', 'MDC', 'Identifier of the event type on the device')
                        },
                        {
                            xtype: 'displayfield-with-info-icon',
                            fieldLabel: Uni.I18n.translate('devicelogbooks.eventLogId', 'MDC', 'Event log id'),
                            name: 'eventLogId',
                            infoTooltip: Uni.I18n.translate('devicelogbooks.eventLogId.tooltip', 'MDC', 'Identifier of the event on the device')
                        },
                        {
                            xtype: 'displayfield-with-info-icon',
                            fieldLabel: Uni.I18n.translate('devicelogbooks.readingDate', 'MDC', 'Reading date'),
                            name: 'readingDate',
                            infoTooltip: Uni.I18n.translate('devicelogbooks.readingDate.tooltip', 'MDC', 'The moment when the data was read out'),
                            beforeRenderer: function (value) {
                                return value ? Uni.I18n.formatDate('devicelogbooks.readingDate.dateFormat', value, 'MDC', 'M d, Y H:i') : '';
                            }
                        }
                    ]
                },
                {
                    items: [
                        {
                            fieldLabel: Uni.I18n.translate('devicelogbooks.message', 'MDC', 'Message'),
                            name: 'message'
                        }
                    ]
                }
            ]
        }
    ]
});
