Ext.define('Cfg.view.validation.RulesContainer', {
    extend: 'Ext.container.Container',
    alias: 'widget.rulesContainer',
    itemId: 'rulesContainer',
    //cls: 'content-container',
    border: false,
    //overflowY: 'auto',
    requires: [
        'Cfg.view.validation.RuleSetPreview',
        'Cfg.view.validation.RuleBrowse'
    ],

    layout:'border',
    items: [{
        region:'west',
        width: 150,
        minSize: 150,
        maxSize: 150,
        dockedItems: [{
            xtype: 'toolbar',
            dock : 'left',
            width: 150,
            border: false,
            layout: {
                type: 'vbox',
                align: 'left'
            },
            items: [{
                xtype: 'button',
                enableToggle: true,
                toggleGroup: 'ratings',
                itemId: 'ruleSetOverviewLink',
                href: 'www.google.com',
                text: Uni.I18n.translate('general.overview', 'CFG', 'Overview'),
                hrefTarget: '_self',
                width: 130,
                style: {
                    borderColor: '#007dc3',
                    borderStyle: 'solid'
                }
            }, {
                xtype: 'button',
                enableToggle: true,
                toggleGroup: 'ratings',
                itemId: 'rulesLink',
                href: 'www.google.com',
                text: Uni.I18n.translate('validation.rules', 'CFG', 'Rules'),
                hrefTarget: '_self',
                width: 130,
                style: {
                    borderColor: '#007dc3',
                    borderStyle: 'solid'
                }
            }]
        }]
    },{
        xtype: 'container',
        border: false,
        region: 'center',
        itemId: 'rulesListContainer',
        layout: 'fit'
    }],

    initComponent: function () {
        this.callParent(arguments);
    }
});

