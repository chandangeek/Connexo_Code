/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuse.view.AvailableCalendarsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tou-available-cal-setup',
    overflowY: true,
    deviceTypeId: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.timeofuse.view.AvailableCalendarsGrid',
        'Mdc.view.setup.devicetype.SideMenu'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('timeofuse.addTouCalendars', 'MDC', 'Add time of use calendars'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'preview-container',
                    selectByDefault: false,
                    grid: {
                        itemId: 'tou-available-cal-grd',
                        xtype: 'tou-available-cal-grd',
                        deviceTypeId: this.deviceTypeId,
                        plugins: {
                            ptype: 'bufferedrenderer'
                        }
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        margin: '15 0 20 0',
                        title: Uni.I18n.translate('timeofuse.calendars.empty.title', 'MDC', 'No time of use calendars found'),
                        reasons: [
                            Uni.I18n.translate('timeofuse.calendars.empty.list.item2', 'MDC', 'No time of use calendars have been defined yet.')
                        ]
                    }
                },
                {
                    xtype: 'container',
                    itemId: 'buttonsContainer',
                    defaults: {
                        xtype: 'button'
                    },
                    items: [
                        {
                            text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            name: 'add',
                            itemId: 'btn-add-tou-calendars',
                            ui: 'action'
                        },
                        {
                            name: 'cancel',
                            itemId: 'btn-cancel-add-tou-calendars',
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            ui: 'link'
                        }
                    ]
                }
            ]
        }
    ],


    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceTypeSideMenu',
                        itemId: 'deviceTypeSideMenu',
                        deviceTypeId: this.deviceTypeId,
                        toggle: 0
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }

});
