/**
 * @class Uni.view.user.Menu
 */
Ext.define('Uni.view.user.Menu', {
    extend: 'Ext.button.Button',
    xtype: 'userMenu',
    scale: 'medium',
    cls: 'user-menu',
    iconCls: 'icon-user',

    menu: [
        {
            text: 'Logout',
            action: 'logout',
            href: '/apps/login/index.html?logout=true'
        }
    ]
});