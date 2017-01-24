Ext.define('Mdc.view.setup.devicedataestimation.RulesSetActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.deviceDataEstimationRulesSetActionMenu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'changeEstimationRuleSetStateActionMenuItem'
            }
        ];
        this.callParent(arguments);
    }
});