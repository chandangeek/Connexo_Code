Ext.define('Cfg.view.validation.RuleSetBrowseFilter', {
    extend: 'Ext.panel.Panel',
    border: false,
    alias: 'widget.rulesetbrowsefilter',
    itemId: 'rulesetbrowsefilter',
    region: 'center',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    items: [
        {
            xtype: 'form',
            border: false,
            padding: '10 10 0 10',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },

            items: [
                {
                    xtype: 'textfield'
                }
            ]
        }
    ],





    initComponent: function () {
        this.callParent(arguments);
    }
});
