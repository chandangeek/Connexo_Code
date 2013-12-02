Ext.define('Uni.view.navigation.Header', {
    extend: 'Ext.container.Container',
    alias: 'widget.navigationHeader',

    requires: [
        'Uni.view.navigation.AppSwitcher',
        'Uni.view.navigation.Logo',
        'Uni.view.search.Quick',
        'Uni.view.notifications.Anchor',
        'Uni.view.navigation.Help',
        'Uni.view.user.Menu'
    ],

    layout: {
        type: 'hbox',
        align: 'stretch'
    },
    cls: 'nav-header',
    height: 40,

    items: [
        {
            xtype: 'navigationAppSwitcher'
        },
        {
            xtype: 'navigationLogo'
        },
        {
            xtype: 'searchQuick',
            flex: 1
        },
        {
            xtype: 'notificationsAnchor'
        },
        {
            xtype: 'navigationHelp'
        },
        {
            xtype: 'userMenu'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});