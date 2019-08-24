/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Iws.view.MainOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.webservice-issue-main-overview',

    requires: [
        'Uni.view.navigation.SubMenu'
    ],

    side: [
        {
            title: Uni.I18n.translate('general.overview','IWS','Overview'),
            xtype: 'menu',
            itemId: 'sideMenu'
        }
    ],

    content: [
        {
            ui: 'large',
            title: Uni.I18n.translate('general.webservice', 'IWS', 'Web service'),
            flex: 1
        }
    ]
});
