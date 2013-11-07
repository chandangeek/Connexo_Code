Ext.define('Mdc.controller.history.Setup', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'setup',

    doConversion: function (tokens) {
        if (tokens.length == 2 && tokens[1] === 'comservers') {
            Mdc.getApplication().getSetupSetupOverviewController().showComServers();
        } else if(tokens.length === 3 && tokens[1] === 'comservers'){
            Mdc.getApplication().getSetupComServersController().showEditView(tokens[2]);
        } else {
            Mdc.getApplication().getSetupSetupOverviewController().showOverview();
        }
    },

    tokenizeBrowse: function(item,id) {
        return this.tokenize([this.rootToken,item, id]);
    }
});