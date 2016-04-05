Ext.define('Cal.view.TimeOfUsePreviewContainer', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.tou-preview-container',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Cal.view.Grid',
        'Cal.view.Preview'
    ],

    emptyComponent: {
        xtype: 'no-items-found-panel',
        itemId: 'no-tou-cals',
        title: Uni.I18n.translate('calendars.tou.empty.title', 'CAL', 'No time of use calendars found'),
        reasons: [
            Uni.I18n.translate('calendars.tou.empty.list.item1', 'CAL', 'No time of use calendars have been defined in the system yet.'),
        ]
    },

    initComponent: function () {
        var me = this;
        me.grid = {
            xtype: 'tou-grid',
            itemId: 'grd-time-of-use',
        };

        me.previewComponent = {
            xtype: 'tou-preview',
            itemId: 'pnl-tou-preview',
        };

        me.callParent(arguments);
    }
});