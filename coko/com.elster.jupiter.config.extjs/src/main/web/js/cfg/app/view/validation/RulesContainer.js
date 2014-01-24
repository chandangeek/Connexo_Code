Ext.define('Cfg.view.validation.RulesContainer', {
    extend: 'Ext.container.Container',
    alias: 'widget.rulesContainer',
    itemId: 'rulesContainer',
    cls: 'content-container',
    overflowY: 'auto',
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
                text: 'Overview',
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
                text: 'Rules',
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
        region: 'center',
        itemId: 'rulesListContainer',
        layout: 'fit'
    }],

    initComponent: function () {
        this.callParent(arguments);
    }
});

