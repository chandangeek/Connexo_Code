/**
 * @class Uni.view.navigation.Header
 */
Ext.define('Uni.view.navigation.Header', {
    extend: 'Ext.container.Container',
    alias: 'widget.navigationHeader',

    ui: 'navigationheader',

    requires: [
        'Uni.view.navigation.AppSwitcher',
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
//        {
//            xtype: 'navigationAppSwitcher'
//        },
        {
            xtype: 'navigationLogo'
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
//        {
//            xtype: 'navigationHelp'
//        },
        {
            xtype: 'button',
            itemId: 'globalSearch',
            text: 'Search',
            cls: 'search-button',
            glyph: 'xe021@icomoon',
            scale: 'small',
            action: 'search'
        },
        {
            xtype: 'userMenu'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});