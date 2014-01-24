/**
 * @class Uni.view.user.Menu
 */
Ext.define('Uni.view.user.Menu', {
    extend: 'Ext.button.Button',
    alias: 'widget.userMenu',
    glyph: 'xe004@icomoon',
    scale: 'small',
    cls: 'user-menu',

    menu: [
        {
            text: 'Profile'
        },
        {
            text: 'Settings',
            glyph: 'xe010@icomoon'
        },
        {
            xtype: 'menuseparator'
        },
        {
            text: 'Logout',
            glyph: 'xe00d@icomoon'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});