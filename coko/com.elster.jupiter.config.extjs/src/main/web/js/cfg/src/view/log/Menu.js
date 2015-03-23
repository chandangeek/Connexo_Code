Ext.define('Cfg.view.log.Menu', {
    extend: 'Uni.view.navigation.SubMenu',
    alias: 'widget.log-menu',
    toggle: null,
    router: null,

    initComponent: function () {
        var me = this;
        me.callParent(me);

        me.add(
            {
                text: Uni.I18n.translate('validationTasks.general.log', 'CFG', 'Log'),
                itemId: 'tasks-log-link'
            }
        );

        me.toggleMenuItem(me.toggle);
    }
});


