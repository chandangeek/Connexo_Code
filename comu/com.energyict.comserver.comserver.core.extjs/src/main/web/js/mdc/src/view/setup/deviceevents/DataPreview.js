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
    dynamicallyAddedComponents: [],

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
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('deviceevents.eventAttributes', 'MDC', 'Event attributes'),
                itemId: 'mdc-event-attributes-label',
                hidden: true,
                labelAlign: 'top'
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
                        itemId: 'mdc-events-preview-left-container',
                        items: itemsLeft
                    },
                    {
                        items: itemsRight
                    }
                ]
            }
        ];
        me.callParent(arguments)
    },

    loadData: function(record) {
        var me = this,
            eventAttributes = record.get('eventData'),
            eventAttributesLabel = me.down('#mdc-event-attributes-label'),
            leftContainer = me.down('#mdc-events-preview-left-container'),
            attributesFound = false;

        me.setTitle(Uni.DateTime.formatDateTimeLong(record.get('eventDate')));
        me.down('#deviceLogbookDataPreviewForm').loadRecord(record);

        // 1. Remove the previously added components:
        Ext.Array.forEach(me.dynamicallyAddedComponents, function(component2Remove) {
            leftContainer.remove(component2Remove);
        });
        me.dynamicallyAddedComponents = [];

        // 2. Add the new components:
        for (var propertyName in eventAttributes) {
            attributesFound = true;
            me.dynamicallyAddedComponents.push(
                leftContainer.add(
                    {
                        fieldLabel: propertyName,
                        value: eventAttributes[propertyName]
                    }
                )
            );
        }
        eventAttributesLabel.setVisible(attributesFound);
    }

});
