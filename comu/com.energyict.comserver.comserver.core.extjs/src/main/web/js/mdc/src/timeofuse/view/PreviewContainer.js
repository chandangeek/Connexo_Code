Ext.define('Mdc.timeofuse.view.PreviewContainer', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.tou-devicetype-preview-container',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.timeofuse.view.CalendarsGrid',
        'Mdc.timeofuse.view.Preview'
    ],

    emptyComponent: {
        xtype: 'no-items-found-panel',
        itemId: 'no-tou-cals',
        title: Uni.I18n.translate('timeofuse.calendars.empty.title', 'MDC', 'No time of use calendars found'),
        reasons: [
            Uni.I18n.translate('timeofuse.calendars.empty.list.item1', 'MDC', 'No time of use calendars have been added yet.'),
            Uni.I18n.translate('timeofuse.calendars.empty.list.item2', 'MDC', 'No time of use calendars have been defined in the system yet.'),
            Uni.I18n.translate('timeofuse.calendars.empty.list.item3', 'MDC', 'Time of use is not allowed.')
        ],
        stepItems: [
            {
                text: Uni.I18n.translate('timeofuse.addTouCalendars', 'MDC', 'Add time of use calendars'),
                //privileges: Apr.privileges.AppServer.admin,
                itemId: 'tou-no-cal-add-btn',
                disabled: true
            },
            {
                text: Uni.I18n.translate('timeofuse.activate', 'MDC', 'Activate time of use'),
                //privileges: Apr.privileges.AppServer.admin,
                itemId: 'tou-no-cal-activate-btn',
                disabled: true
            }
        ]
    },

    initComponent: function () {
        var me = this;
        me.grid = {
            xtype: 'tou-calendars-grid',
            itemId: 'grid-tou-calendars'
        };

        me.previewComponent = {
            xtype: 'tou-preview-panel',
            itemId: 'pnl-tou-preview-devicetype',
        };

        me.callParent(arguments);
    }
});