/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.history.UsagePointHistoryDevices', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.usage-point-history-devices',
    requires: [
        'Mdc.usagepointmanagement.view.history.UsagePointHistoryDevicesGrid',
        'Mdc.usagepointmanagement.view.history.UsagePointHistoryDevicesPreview'
    ],
    router: null,

    emptyComponent: {
        xtype: 'no-items-found-panel',
        itemId: 'usage-point-history-devices-empty',
        title: Uni.I18n.translate('usagePoint.history.devices.emptyCmp.title', 'MDC', 'No devices have been linked to this usage point yet')
    },

    initComponent: function () {
        var me = this;

        me.grid = {
            xtype: 'usage-point-history-devices-grid',
            itemId: 'usage-point-history-devices-grid',
            router: me.router
        };

        me.previewComponent = {
            xtype: 'usage-point-history-devices-preview',
            itemId: 'usage-point-history-devices-preview',
            router: me.router
        };

        me.on('afterrender', function () {
            me.grid.getStore().fireEvent('load');
        }, me, {single: true});

        me.callParent(arguments);
    }
});

