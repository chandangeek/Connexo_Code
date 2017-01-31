/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cal.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tou-setup',
    router: null,
    store: null,

    requires: [
        'Cal.view.TimeOfUsePreviewContainer'
    ],

    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.calendars', 'CAL', 'Calendars'),
            items: [
                {
                    xtype: 'tou-preview-container',
                }
            ]
        };


        me.callParent(arguments);
    }
});