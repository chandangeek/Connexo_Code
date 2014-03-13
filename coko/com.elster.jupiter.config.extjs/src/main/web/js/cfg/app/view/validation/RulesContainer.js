Ext.define('Cfg.view.validation.RulesContainer', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.rulesContainer',
    itemId: 'rulesContainer',
    //cls: 'content-container',
    border: false,
    //overflowY: 'auto',
    requires: [
        'Cfg.view.validation.RuleSetPreview',
        'Cfg.view.validation.RuleBrowse',
        'Uni.view.navigation.SubMenu',
        'Cfg.view.validation.RuleSetOverview'
    ],

    ruleSetId: null,

    layout:'border',
    side: [
        {
            xtype: 'navigationSubMenu',
            itemId: 'stepsMenu'
        }
    ],

    content: [
        {
            xtype: 'container',
            border: false,
            itemId: 'stepsContainer',
            layout: 'card',
            flex: 1,
            items: [
                {
                    xtype: 'ruleSetOverview'
                },
                {
                    xtype: 'container',
                    layout: 'fit',
                    items: [],
                    itemId: 'ruleListContainer'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);

        this.down('#ruleListContainer').add(
            {
                xtype: 'validationruleBrowse',
                ruleSetId: this.ruleSetId
            }
        );

        this.initStepsMenu();
    },

    initStepsMenu: function () {
        var me = this;
        var stepsMenu = this.getStepsMenuCmp();

        var overviewButton = stepsMenu.add({
            text: 'Overview',
            pressed: true,
            itemId: 'ruleSetOverviewLink',
            href: '#administration/validation/overview/' + me.ruleSetId,
            hrefTarget: '_self'
        });

        var rulesButton = stepsMenu.add({
            text: 'Rules',
            pressed: false,
            itemId: 'rulesLink',
            href: '#administration/validation/rules/' + me.ruleSetId,
            hrefTarget: '_self'
        });

        var stepsContainer = this.getStepsContainerCmp();
        stepsContainer.getLayout().setActiveItem(0);
    },

    getStepsMenuCmp: function () {
        return this.down('#stepsMenu');
    },

    getStepsContainerCmp: function () {
        return this.down('#stepsContainer');
    }
});

