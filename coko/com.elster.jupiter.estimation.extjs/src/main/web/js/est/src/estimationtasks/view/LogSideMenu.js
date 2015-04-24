Ext.define('Est.estimationtasks.view.LogSideMenu', {
    extend: 'Uni.view.navigation.SubMenu',
    alias: 'widget.estimationtasks-log-menu',
    toggle: null,
    router: null,

    initComponent: function () {
        var me = this;
        me.callParent(me);

        me.add(
            {
                text: Uni.I18n.translate('estimationtasks.general.log', 'EST', 'Log'),
                itemId: 'estimationtasks-log-link'
            }
        );

        me.toggleMenuItem(me.toggle);
    }
});


