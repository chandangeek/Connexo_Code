/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idl.view.MainOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-validation-main-overview',

    requires: [
        'Uni.view.navigation.SubMenu'
    ],

    side: [
        {
            title: Uni.I18n.translate('general.overview','IDL','overview'),
            xtype: 'menu',
            itemId: 'sideMenu'
        }
    ],

    content: [
        {
            ui: 'large',
            title: Uni.I18n.translate('issue.workspace.devicelifecycle', 'IDL', 'Device lifecycle'),
            flex: 1
        }
    ]
});