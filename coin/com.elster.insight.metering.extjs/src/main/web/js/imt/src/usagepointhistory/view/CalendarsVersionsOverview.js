/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                xtype: 'uni-form-empty-message',
                text: Uni.I18n.translate('calendars.history.empty.list', 'IMT', 'There is no history available for calendars')
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