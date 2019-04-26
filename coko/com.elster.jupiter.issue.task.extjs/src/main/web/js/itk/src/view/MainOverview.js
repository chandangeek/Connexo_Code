/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.MainOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.task-main-overview',

    requires: [
        'Uni.view.navigation.SubMenu'
    ],

    side: [
        {
            title: Uni.I18n.translate('general.overview','ITK','Overview'),
            xtype: 'menu',
            itemId: 'sideMenu'
        }
    ],

    content: [
        {
            ui: 'large',
            title: Uni.I18n.translate('issue.workspace.task', 'ITK', 'Task'),
            flex: 1
        }
    ]
});