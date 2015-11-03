/**
 * @class Uni.view.navigation.HelpBtn
 */
Ext.define('Uni.view.navigation.HelpBtn', {
    extend: 'Ext.menu.Item',
    alias: 'widget.online-help-btn',
    text: Uni.I18n.translate('general.help', 'UNI', 'Help'),
    hrefTarget: '_blank',
    href: 'help/index.html',

    initComponent: function () {
        var me = this;

        Ext.Ajax.request({
            url: '/api/usr/currentuser',
            success: function (response) {
                var currentUser = Ext.decode(response.responseText, true),
                    url;

                if (currentUser && currentUser.language && currentUser.language.languageTag) {
                    url = 'help/' + currentUser.language.languageTag + '/index.html';
                    Ext.Ajax.request({
                        url: url,
                        method: 'HEAD',
                        success: function (response) {
                            me.href = url;
                        }
                    });
                }
            }
        });

        me.callParent(arguments);
    }
});