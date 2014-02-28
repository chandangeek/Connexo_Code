Ext.define('Mtr.controller.history.UsagePoint', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'usagepoints',

    doConversion: function (tokens) {
        if (tokens.length > 1) {
            var id = parseInt(tokens[1]);
            if (!isNaN(id)) {
                // TODO Map to specific actions.
                this.browse(id);
            } else {
                this.showOverview();
            }
        } else {
            this.showOverview();
        }
    },

    showOverview: function () {
        Mtr.getApplication().getUsagePointController().showOverview();
    },
    browse: function (id) {
        Mtr.getApplication().getUsagePointController().browse(id);
    },

    tokenizeBrowse: function(id) {
        return this.tokenize([this.rootToken, id]);
    }
});