Ext.define('Dcs.controller.history.Schedule', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration_dcs',
    previousTokens: ['administration_dcs'],

    doConversion: function (tokens) {
        if (this.currentTokens !== null) {
            this.previousTokens = this.currentTokens;
        }
        this.currentTokens = tokens;
        if (tokens.length > 1 && tokens[1] === 'scheduling') {
            if (tokens.length < 3) {
                this.showDataCollectionSchedules();
            } else {

            }
        } else {
            this.unknownTokensReturnToOverview();
        }
    },

    tokenizePreviousTokens: function () {
        return this.tokenize(this.previousTokens);
    },

    unknownTokensReturnToOverview: function () {
        this.getApplication().getController('Dcs.controller.Administration').showOverview();
    },

    showDataCollectionSchedules: function () {
        this.getApplication().getController('Dcs.controller.Schedule').showDataCollectionSchedules();
    }





});