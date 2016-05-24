Ext.define('Cal.view.TimeOfUsePreviewContainer', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.tou-preview-container',

    requires: [
        'Uni.util.FormEmptyMessage',
        'Cal.view.Grid',
        'Cal.view.Preview'
    ],

    emptyComponent: {
        xtype: 'uni-form-empty-message',
        itemId: 'no-tou-cals',
        text: Uni.I18n.translate('calendars.tou.empty', 'CAL', 'No time of use calendars have been defined yet.')
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