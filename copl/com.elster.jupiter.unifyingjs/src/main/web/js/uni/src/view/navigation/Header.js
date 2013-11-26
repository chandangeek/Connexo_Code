Ext.define('Uni.view.navigation.Header', {
    extend: 'Ext.container.Container',
    alias: 'widget.navigationHeader',

    requires: [
        'Uni.view.navigation.AppSwitcher',
        'Uni.view.navigation.Toggler',
        'Uni.view.navigation.Logo',
        'Uni.view.search.Quick',
        'Uni.view.notifications.Counter',
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
//            xtype: 'navigationAppSwitcher'
            xtype: 'navigationToggler' // Temporary until the navigation toggler isn't necessary anymore.
        },
        {
            xtype: 'navigationLogo'
        },
        {
            xtype: 'searchQuick',
            flex: 1
        },
        {
            xtype: 'notificationsCounter'
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