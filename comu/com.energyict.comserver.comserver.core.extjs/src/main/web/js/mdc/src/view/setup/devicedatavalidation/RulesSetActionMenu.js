Ext.define('Mdc.view.setup.devicedatavalidation.RulesSetActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.deviceDataValidationRulesSetActionMenu',
    itemId: 'deviceDataValidationRulesSetActionMenu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'changeRuleSetStateActionMenuItem'
            }
        ];
        this.callParent(arguments);
    }

});