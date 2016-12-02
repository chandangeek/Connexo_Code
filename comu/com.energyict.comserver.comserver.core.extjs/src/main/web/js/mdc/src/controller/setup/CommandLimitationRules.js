Ext.define('Mdc.controller.setup.CommandLimitationRules', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.commandrules.CommandLimitationRulesOverview',
        'Mdc.view.setup.commandrules.CommandRulePendingChangesOverview'
    ],
    models: [
        'Mdc.model.CommandLimitRule'
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
        //this.goToTaskOverview = false;
    },

    loadCommandRuleDetail: function(rowmodel, record, index) {
        var me = this,
            form = me.getCommandRulePreviewForm();
        form.setLoading();
        me.getCommandRulePreview().setTitle( Ext.String.htmlEncode(record.get('name')) );
        form.loadRecord(record);
        form.setLoading(false);
        me.getCommandRulePreviewMenu().record = record;
    },

    showCommandRuleOverview: function(commandRuleId) {
        var me = this,
            rulesModel = me.getModel('Mdc.model.CommandLimitRule');

        rulesModel.load(commandRuleId, {
            success: function (commandRule) {
                var widget = Ext.widget('comTaskOverview', {
                    router: me.getController('Uni.controller.history.Router'),
                    commandRuleRecord: commandRule
                });
                me.getApplication().fireEvent('loadCommandRule', commandRule);
                me.getApplication().fireEvent('changecontentevent', widget);
                //me.goToTaskOverview = true;
                //me.comTaskBeingEdited = communicationTask;
            }
        });
    },

    showCommandRulePendingChanges: function() {
        var widget = Ext.widget('commandRulesPendingChangesOverview', {
            router: this.getController('Uni.controller.history.Router')
        });
        this.getApplication().fireEvent('changecontentevent', widget);
    }

});