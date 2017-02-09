/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicegroup.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.devicegroups-menu',

    deviceGroupId: null,

    title: Uni.I18n.translate('general.deviceGroup', 'MDC', 'Device group'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
                itemId: 'devicegroups-view-link',
                href:  '#/devices/devicegroups/' + me.deviceGroupId
            }
        ];

        me.callParent(arguments);
    }
});


