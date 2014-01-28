Ext.define('Cfg.controller.history.Validation', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'validation',

    showOverview: function () {
        Cfg.getApplication().getValidationController().showOverview();
    },


    doConversion: function (tokens) {
        if (tokens.length > 1) {
            if (tokens[1] === 'rules') {
                var id = parseInt(tokens[2]);
                this.showRules(id);
            } else if (tokens[1] === 'overview') {
                var id = parseInt(tokens[2]);
                this.showRuleSetOverview(id);
            }  else if (tokens[1] === 'createset') {
                this.newRuleSet();
            }
        } else {
            this.unknownTokensReturnToOverview();
        }
    },

    unknownTokensReturnToOverview: function () {
        this.showOverview();
    },

    showRules: function (ruleSetId) {
        Cfg.getApplication().getValidationController().showRules(ruleSetId);
    },

    showRuleSetOverview: function (ruleSetId) {
        Cfg.getApplication().getValidationController().showRuleSetOverview(ruleSetId);
    },

    newRuleSet: function (ruleSetId) {
        Cfg.getApplication().getValidationController().newRuleSet();
    }





});