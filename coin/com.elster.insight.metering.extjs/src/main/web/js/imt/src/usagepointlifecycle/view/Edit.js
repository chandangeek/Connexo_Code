Ext.define('Imt.usagepointlifecycle.view.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usagepoint-life-cycles-edit',
    xtype: 'usagepoint-life-cycles-edit',
    router: null,
    route: null,
    requires: ['Imt.usagepointlifecycle.view.AddForm'],

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'usagepoint-life-cycles-add-form',
                router: me.router,
                route: me.route,
                btnAction: 'edit',
                btnText: Uni.I18n.translate('general.save', 'IMT', 'Save'),
                hideInfoMsg: true
            }
        ];
        me.callParent(arguments);
    }
});
