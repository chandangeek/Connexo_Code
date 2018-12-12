/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuse.view.PreviewContainer', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.tou-devicetype-preview-container',
    deviceTypeId: null,
    timeOfUseAllowed: null,
    timeOfUseSupported: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.timeofuse.view.CalendarsGrid',
        'Mdc.timeofuse.view.Preview'
    ],


    initComponent: function () {
        var me = this;
        me.grid = {
            xtype: 'tou-calendars-grid',
            itemId: 'grid-tou-calendars',
            deviceTypeId: me.deviceTypeId,
            timeOfUseAllowed: me.timeOfUseAllowed
        };

        me.previewComponent = {
            xtype: 'tou-preview-panel',
            itemId: 'pnl-tou-preview-devicetype',
            timeOfUseAllowed: me.timeOfUseAllowed
        };

        me.emptyComponent = {
            xtype: 'no-items-found-panel',
            itemId: 'no-tou-cals',
            title: Uni.I18n.translate('timeofuse.calendars.empty.title', 'MDC', 'No time of use calendars found'),
            reasons: [
                Uni.I18n.translate('timeofuse.calendars.empty.list.item1', 'MDC', 'No time of use calendars have been added yet.'),
                Uni.I18n.translate('timeofuse.calendars.empty.list.item2', 'MDC', 'No time of use calendars have been defined yet.'),
                Uni.I18n.translate('timeofuse.calendars.empty.list.item3', 'MDC', 'Time of use is not allowed.')
            ],
            stepItems: [
                {
                    text: Uni.I18n.translate('timeofuse.addTouCalendars', 'MDC', 'Add time of use calendars'),
                    privileges: Mdc.privileges.DeviceType.admin,
                    itemId: 'tou-no-cal-add-btn',
                    disabled: !me.timeOfUseAllowed
                },
                {
                    text: Uni.I18n.translate('timeofuse.allowTimeOfUse', 'MDC', 'Allow time of use'),
                    privileges: Mdc.privileges.DeviceType.admin,
                    itemId: 'tou-no-cal-activate-btn',
                    disabled: me.timeOfUseAllowed || !me.timeOfUseSupported
                }
            ]
        },

            me.callParent(arguments);
    }
});