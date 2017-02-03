/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.controller.Dashboard', {
    extend: 'Ext.app.Controller',

    init: function () {
        this.initMenu();
        this.callParent();
    },

    initMenu: function () {
        var dashboardMenuItem = Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('general.label.dashboard', 'IMT', 'Dashboard'),
            href: 'dashboard',
            portal: 'dashboard',
            glyph: 'home',
            index: 50
        });

        Uni.store.MenuItems.add(dashboardMenuItem);
    }
})
