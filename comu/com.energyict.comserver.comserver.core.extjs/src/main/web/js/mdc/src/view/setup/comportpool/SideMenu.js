/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comportpool.SideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.comportpoolsidemenu',
    title: Uni.I18n.translate('general.comPortPool', 'MDC', 'Communication port pool'),
    objectType: Uni.I18n.translate('general.comPortPool', 'MDC', 'Communication port pool'),
    initComponent: function () {
        var me = this,
            poolId = me.poolId;
        me.menuItems = [
            {
                text: Uni.I18n.translate('comserver.sidemenu.details', 'MDC', 'Details'),
                itemId: 'comportpoolLink',
                href: '#/administration/comportpools/' + poolId
            },
            {
                text: Uni.I18n.translate('comserver.sidemenu.comports', 'MDC', 'Communication ports'),
                itemId: 'commportsLink',
                href: '#/administration/comportpools/' + poolId + '/comports'
            }
        ];
        me.callParent(arguments)
    }
});