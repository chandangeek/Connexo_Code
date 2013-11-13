Ext.define('Mdc.controller.history.Setup', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'setup',

    doConversion: function (tokens) {
        if (tokens.length == 2 && tokens[1] === 'comservers') {
            Mdc.getApplication().getSetupSetupOverviewController().showComServers();
        } else if (tokens.length === 3 && tokens[1] === 'comservers') {
            if(tokens[2]==='create'){
                Mdc.getApplication().getSetupComServersController().showEditView();
            } else {
                Mdc.getApplication().getSetupComServersController().showEditView(tokens[2]);
            }
        } else if (tokens.length === 2 && tokens[1] === 'devicecommunicationprotocols') {
            Mdc.getApplication().getSetupSetupOverviewController().showDeviceCommunicationProtocols();
        } else if (tokens.length === 3 && tokens[1] === 'devicecommunicationprotocols') {
            Mdc.getApplication().getSetupDeviceCommunicationProtocolController().showEditView(tokens[2]);
        } else {
            Mdc.getApplication().getSetupSetupOverviewController().showOverview();
        }
    },

    tokenizeBrowse: function (item, id) {
        if (id === undefined) {
            return this.tokenize([this.rootToken, item]);
        } else {
            return this.tokenize([this.rootToken, item, id]);
        }
    },

    tokenizeAddComserver: function(){
        return this.tokenize([this.rootToken, 'comservers','create']);
    }
});