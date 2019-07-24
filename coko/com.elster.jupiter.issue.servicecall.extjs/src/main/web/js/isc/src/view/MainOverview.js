/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isc.view.MainOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.servicecall-issue-main-overview',

    requires: [
        'Uni.view.navigation.SubMenu'
    ],

    side: [
        {
            title: Uni.I18n.translate('general.overview','ISC','Overview'),
            xtype: 'menu',
            itemId: 'sideMenu'
        }
    ],

    content: [
        {
            ui: 'large',
            title: Uni.I18n.translate('general.servicecall', 'ISC', 'Service call'),
            flex: 1
        }
    ]
});
