Ext.define('Cfg.view.validation.RuleSetBrowseFilter', {
    extend: 'Uni.component.filter.view.Filter',
    border: false,
    alias: 'widget.rulesetbrowsefilter',
    itemId: 'rulesetbrowsefilter',
    title: 'Filter',
    region: 'west',
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
                    xtype: 'label',
                    text: Uni.I18n.translate('general.name', 'CFG', 'Name'),
                    margin: '0 0 5 2'
                },
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
