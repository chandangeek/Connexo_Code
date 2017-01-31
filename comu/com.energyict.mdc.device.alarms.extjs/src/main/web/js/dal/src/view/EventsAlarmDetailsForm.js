/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.view.EventsAlarmDetailsForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Mdc.privileges.Device',
        'Mdc.privileges.DeviceType',
        'Dal.view.LogGrid',
    ],
    alias: 'widget.events-alarm-details-form',
    router: null,
    store: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'alarm-preview',
                itemId: 'alarm-detail-form',
                router: me.router,
                showTools: false,
                frame: false
            },
            {
                xtype: 'alarm-details-log-grid',
                title: Uni.I18n.translate('general.relatedEvents', 'DAL', 'Related events'),
                itemId: 'alarm-log-grid',
                store: me.store
            }
        ];

        me.callParent(arguments);
    }
});
