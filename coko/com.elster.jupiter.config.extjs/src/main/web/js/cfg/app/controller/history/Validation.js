Ext.define('Cfg.controller.history.Validation', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'validation',

    showOverview: function () {
        Cfg.getApplication().getValidationController().showOverview();
    },


    doConversion: function (tokens) {
        if (tokens.length > 1 && tokens[1] === 'rulesforset') {
            var id = parseInt(tokens[2]);
            this.showRulesContainer(id);
        } else {
            this.unknownTokensReturnToOverview();
        }
    },

    unknownTokensReturnToOverview: function () {
        this.showOverview();
    },

    showRulesContainer: function (ruleSetId) {
        Cfg.getApplication().getValidationController().showRulesContainer(ruleSetId);
    }





});