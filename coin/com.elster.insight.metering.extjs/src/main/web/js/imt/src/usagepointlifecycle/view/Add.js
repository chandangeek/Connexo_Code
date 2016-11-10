Ext.define('Imt.usagepointlifecycle.view.Add', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usagepoint-life-cycles-add',
    xtype: 'usagepoint-life-cycles-add',
    router: null,
    requires: ['Imt.usagepointlifecycle.view.AddForm'],

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'usagepoint-life-cycles-add-form',
                title: Uni.I18n.translate('general.addUsagePointLifeCycle', 'IMT', 'Add usage point life cycle'),
                infoText: Uni.I18n.translate('usagePointLifeCycles.add.templateMsg', 'IMT', 'The new usage point life cycle is based on the standard template and will use the same states and transitions.'),
                router: me.router,
                route: 'administration/usagepointlifecycles',
                btnAction: 'add',
                btnText: Uni.I18n.translate('general.add', 'IMT', 'Add'),
                hideInfoMsg: false
            }
        ];
        me.callParent(arguments);
    }
});
