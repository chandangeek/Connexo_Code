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
                            //clear token from local storage

                            //invalidate cookie also by setting it in the past
                            //Ext.util.Cookies.clear('X-CONNEXO-TOKEN');
                            //cannot remove cookie in js since it is httpOnly
                            /* if (Ext.util.Cookies.get('X-CONNEXO-TOKEN'))
                             document.cookie = "X-CONNEXO-TOKEN" + "=" + "; expires=Thu, 01-Jan-70 00:00:01 GMT";
                             */
                            window.localStorage.removeItem('X-AUTH-TOKEN');

                            //Ext.util.Cookies.set('X-CONNEXO-TOKEN','',new Date(0),'/');
                            // document.cookie= 'X-CONNEXO-TOKEN=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
                            window.location.replace('/apps/login/index.html');

                        }
                    });
                }
            }
        }
    ]
});