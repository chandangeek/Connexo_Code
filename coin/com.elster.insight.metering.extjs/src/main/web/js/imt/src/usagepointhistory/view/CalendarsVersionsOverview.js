Ext.define('Imt.usagepointhistory.view.CalendarsVersionsOverview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.calendars-versions-overview',

    store: null,
    selectByDefault: true,

    requires: [
        'Imt.usagepointmanagement.view.calendars.VersionsGrid',
        'Imt.usagepointmanagement.view.calendars.HistoryCalendarPreview'
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'preview-container',
            selectByDefault: me.selectByDefault,
            grid: {
                xtype: 'calendars-versions-grid',
                //type: me.type,
                store: me.store,
                listeners: {
                    select: {
                        fn: Ext.bind(me.onVersionSelect, me)
                    }
                }
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                itemId: 'no-versions-found',
                title: Uni.I18n.translate('calendar.history.title', 'IMT', 'No history available')
            },
            previewComponent: {
                xtype: 'historyCalendarPreview',
                itemId: 'historyCalendarPreview',
                // type: me.type,
                hideAction: true
            }
        };

        me.callParent(arguments);
    },

    onVersionSelect: function (selectionModel, record) {
        this.down('#historyCalendarPreview').loadRecord(record);
    }
});