Ext.define('Mdc.view.setup.commandrules.CommandRulePreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.commandRulePreview',
    title: '',
    frame: true,
    requires: [
        'Mdc.view.setup.commandrules.CommandRuleActionMenu',
        'Mdc.view.setup.commandrules.CommandRulePreviewForm'
    ],
    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Mdc.privileges.CommandLimitationRules.view,
            menu: {
                xtype: 'commandRuleActionMenu'
            }
        }
    ],
    items: {
        xtype: 'commandRulePreviewForm',
        itemId: 'mdc-command-rule-preview-form'
    }
});