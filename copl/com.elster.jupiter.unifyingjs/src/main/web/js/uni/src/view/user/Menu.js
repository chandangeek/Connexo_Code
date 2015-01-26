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
            itemId: 'user-log-out',
            listeners: {
                'click': function () {
                    Ext.Ajax.request({
                        url: '/api/apps/apps/logout',
                        method: 'POST',
                        disableCaching: true,
                        scope: this,
                        success: function () {
                            window.location.replace('/apps/login/index.html');
                        }
                    });
                }
            }
        }
    ]
});