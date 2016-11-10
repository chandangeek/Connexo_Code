Ext.define('Imt.usagepointlifecycle.view.Clone', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usagepoint-life-cycles-clone',
    router: null,
    route: null,
    title: null,
    requires: ['Imt.usagepointlifecycle.view.AddForm'],

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'usagepoint-life-cycles-add-form',
                title: me.title,
                router: me.router,
                infoText: me.infoText,
                route: me.route,
                btnAction: 'clone',
                btnText: Uni.I18n.translate('general.clone', 'IMT', 'Clone'),
                hideInfoMsg: false
            }
        ];
        me.callParent(arguments);
    }
});