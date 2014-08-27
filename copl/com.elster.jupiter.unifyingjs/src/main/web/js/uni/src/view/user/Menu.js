/**
 * @class Uni.view.user.Menu
 */
Ext.define('Uni.view.user.Menu', {
    extend: 'Ext.button.Button',
    alias: 'widget.userMenu',
    scale: 'small',
    cls: 'user-menu',
    iconCls: 'icon-user',

    menu: [
        /*{
            text: 'Profile'
        },
        {
            text: 'Settings',
            glyph: 'xe010@icomoon'
        },
        {
            xtype: 'menuseparator'
        },*/
        {
            text: 'Logout',
            action: 'logout'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});