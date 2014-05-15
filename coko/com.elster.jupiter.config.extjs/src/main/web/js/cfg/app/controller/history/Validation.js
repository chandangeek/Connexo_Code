Ext.define('Cfg.controller.history.Validation', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'validation',
    previousTokens: ['administration'],

    doConversion: function (tokens) {
        if (this.currentTokens !== null) {
            this.previousTokens = this.currentTokens;
        }
        this.currentTokens = tokens;
        if (tokens.length > 1 && tokens[1] === 'validation') {
            if (tokens.length < 3) {
                this.showRuleSets();
            } else {
                if (tokens[2] === 'rules') {
                    var id = parseInt(tokens[3]);
                    this.showRules(id);
                } else if (tokens[2] === 'overview') {
                    var id = parseInt(tokens[3]);
                    this.showRuleSetOverview(id);
                }  else if (tokens[2] === 'createset') {
                    this.newRuleSet();
                } else if (tokens[2] === 'addRule') {
                    var id = parseInt(tokens[3]);
                    this.addRule(id);
                }
            }
        } else {
            //this.unknownTokensReturnToOverview();
        }
    },

    tokenizePreviousTokens: function () {
        return this.tokenize(this.previousTokens);
    },

    unknownTokensReturnToOverview: function () {
        this.getApplication().getController('Cfg.controller.Administration').showOverview();
    },

    addRule: function(ruleSetId) {
        this.getApplication().getController('Cfg.controller.Validation').addRule(ruleSetId);
    },

    showRules: function (ruleSetId) {
        this.getApplication().getController('Cfg.controller.Validation').showRules(ruleSetId);
    },

    showRuleSets: function () {
        this.getApplication().getController('Cfg.controller.Validation').showRuleSets();
    },

    showRuleSetOverview: function (ruleSetId) {
        this.getApplication().getController('Cfg.controller.Validation').showRuleSetOverview(ruleSetId);
    },

    newRuleSet: function () {
        this.getApplication().getController('Cfg.controller.Validation').newRuleSet();
    }





});