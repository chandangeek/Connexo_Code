/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.navigation.Help
 */
Ext.define('Uni.view.navigation.Help', {
    extend: 'Ext.button.Button',
    requires: [
        'Uni.view.navigation.HelpBtn'
    ],
    alias: 'widget.navigationHelp',
    scale: 'medium',
    cls: 'nav-help',
    iconCls: 'icon-question3',
    menu: {
        itemId: 'global-help-menu',
        plain: true,
        border: false,
        defaults: {
            isMenuItem: false // workaround to avoid firing click event twice
        },
        items: [
            {
                itemId: 'global-about',
                text: Uni.I18n.translate('general.aboutConnexo', 'UNI', 'About Connexo'),
                href: '#/about',
                hrefTarget: '_blank'
            },
            {
                itemId: 'global-online-help',
                xtype: 'online-help-btn'
            }
        ]
    }
});