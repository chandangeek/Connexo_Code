Ext.define('Mdc.controller.setup.CommandLimitationRules', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.commandrules.CommandLimitationRulesOverview'
    ],

    refs: [
        {ref: 'commandRulePreview', selector: 'commandRulesOverview #mdc-command-rule-preview'},
        {ref: 'commandRulePreviewForm', selector: 'commandRulesOverview #mdc-command-rule-preview-form'},
        {ref: 'commandRulePreviewMenu', selector: 'commandRulesOverview #mdc-command-rule-preview commandRuleActionMenu'}

    ],

    init: function () {
        var me = this;
        me.control({
            'commandRulesOverview commandRulesGrid': {
                select: me.loadCommandRuleDetail
            }
        });
    },

    showRulesView: function () {
        var widget = Ext.widget('commandRulesOverview', {
            router: this.getController('Uni.controller.history.Router')
        });
        this.getApplication().fireEvent('changecontentevent', widget);
        this.goToTaskOverview = false;
    },

    loadCommandRuleDetail: function(rowmodel, record, index) {
        var me = this,
            form = me.getCommandRulePreviewForm();
        form.setLoading();
        me.getCommandRulePreview().setTitle( Ext.String.htmlEncode(record.get('name')) );
        me.getCommandRulePreview().commandRuleRecord = record;
        form.loadRecord(record);
        form.setLoading(false);
        me.getCommandRulePreviewMenu().record = record;
    }

});