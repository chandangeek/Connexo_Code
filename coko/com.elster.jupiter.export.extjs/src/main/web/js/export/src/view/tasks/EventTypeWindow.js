Ext.define('Dxp.view.tasks.EventTypeWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.event-type-window',
    itemId: 'eventTypeWindow',
    closable: false,
    width: 800,
    height: 400,
    constrain: true,
    autoShow: true,
    modal: true,
    layout: 'fit',
    floating: true,
    items: {
        xtype: 'form',
        border: false,
        itemId: 'eventTypeForm',
        items: [
            {
                xtype: 'radiogroup',
                itemId: 'eventTypeInputMethod',
                required: true,
                width: 600,
                columns: 1,
                vertical: true,
                items: [
                    {
                        xtype: 'container',
                        layout: 'vbox',
                        items: [
                            {
                                xtype: 'radio',
                                boxLabel: Uni.I18n.translate('export.eventType.specifyEventType', 'DES', 'Specify event type'),
                                name: 'rb',
                                inputValue: '0',
                                checked: true
                            },
                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'specifyEventForm',
                                //width: 800,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        xtype: 'textfield',
                                        fieldLabel: Uni.I18n.translate('export.eventType.eventType', 'DES', 'Event type'),
                                        required: true
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        layout: 'vbox',
                        items: [
                            {
                                xtype: 'radio',
                                boxLabel: Uni.I18n.translate('export.eventType.specifyEventTypeParts', 'DES', 'Specify event type parts'),
                                name: 'rb',
                                inputValue: '1',
                            },
                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'specifyEventPartsForm',
                                //width: 800,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        xtype: 'textfield',
                                        fieldLabel: Uni.I18n.translate('export.eventType.eventType', 'DES', 'Event type')
                                    },
                                    {
                                        xtype: 'combobox',
                                        fieldLabel: Uni.I18n.translate('export.eventType.deviceType', 'DES', 'Device type'),
                                        required: true
                                    },
                                    {
                                        xtype: 'combobox',
                                        fieldLabel: Uni.I18n.translate('export.eventType.deviceDomain', 'DES', 'Device domain'),
                                        required: true
                                    },
                                    {
                                        xtype: 'combobox',
                                        fieldLabel: Uni.I18n.translate('export.eventType.deviceSubDomain', 'DES', 'Device subdomain'),
                                        required: true
                                    },
                                    {
                                        xtype: 'combobox',
                                        fieldLabel: Uni.I18n.translate('export.eventType.deviceEventOrAction', 'DES', 'Device event or action'),
                                        required: true
                                    },
                                ]
                            }
                        ]
                    }
                ]
            }
        ]
    },

    bbar: [
        {
            xtype: 'container',
            flex: 1
        },
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.add','DES','Add'),
            itemId: 'addEventTypeToTask'
        },
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.cancel','DES','Cancel'),
            action: 'cancel',
            ui: 'link',
            listeners: {
                click: {
                    fn: function () {
                        this.up('#eventTypeWindow').destroy();
                    }
                }
            }
        }

    ],


    initComponent: function () {
        var me = this;
        me.callParent(arguments);
    }
});

