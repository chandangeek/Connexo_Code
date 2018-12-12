/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.navigation.Header
 */
Ext.define('Uni.view.navigation.Header', {
    extend: 'Ext.container.Container',
    alias: 'widget.navigationHeader',

    ui: 'navigationheader',

    requires: [
        'Uni.view.navigation.AppCenter',
        'Uni.view.navigation.Logo',
        'Uni.view.search.Basic',
        'Uni.view.search.Quick',
        'Uni.view.notifications.Anchor',
        'Uni.view.navigation.Help',
        'Uni.view.user.Menu'
    ],

    layout: {
        type: 'hbox',
        align: 'stretch'
    },

    height: 48,

    /**
     * Most items here have been disabled until their respective stories are fully developed.
     * Also see: http://jira.eict.vpdc/browse/JP-651
     */
    items: [
        {
            xtype: 'uni-nav-appcenter'
        },
        {
            xtype: 'uni-nav-logo'
        },
        {
            xtype: 'component',
            flex: 1
        },
//        {
//            xtype: 'searchBasic'
//        }
//        {
//            xtype: 'searchQuick',
//            flex: 1
//        }
//        {
//            xtype: 'notificationsAnchor'
//        },
        {
            xtype: 'button',
            itemId: 'globalSearch',
            text: Uni.I18n.translate('navigation.header.search', 'UNI', 'Search'),
            cls: 'search-button',
            iconCls: 'icon-search3',
            scale: 'medium',
            action: 'search',
            href: '#/search',
            hidden: true
        },
        {
            xtype: 'navigationHelp',
            itemId: 'global-help-menu-btn'
        },
        {
            xtype: 'userMenu',
            itemId: 'user-menu'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});