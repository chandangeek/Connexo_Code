/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.add-usage-point-navigation',
    jumpForward: false,
    jumpBack: true,
    ui: 'medium',
    padding: '0 0 0 0',

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'navigation-general-info',
                text: Uni.I18n.translate('usagepoint.navigation.step1', 'IMT', 'General information')
            },
            {
                itemId: 'navigation-technical-info',
                text: Uni.I18n.translate('usagepoint.navigation.step2', 'IMT', 'Technical information')
            }
        ];

        me.callParent(arguments);
    }
});