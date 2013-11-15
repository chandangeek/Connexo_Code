Ext.define('Mdc.controller.history.Setup', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'setup',

    doConversion: function (tokens) {
        if (tokens.length > 1 && tokens[1] === 'comservers') {
            this.handleComServerTokens(tokens);
        } else if (tokens.length > 1 && tokens[1] === 'devicecommunicationprotocols') {
            this.handleCommunicationProtocolTokens(tokens);
        } else if (tokens.length > 1 && tokens[1] === 'comports'){
            this.handleComPortTokens(tokens);
        } else {
            this.unknownTokensReturnToOverview();
        }
    },

    handleComServerTokens: function (tokens) {
        if (tokens.length == 2) {
            Mdc.getApplication().getSetupSetupOverviewController().showComServers();
        } else if (tokens.length === 3) {
            if (tokens[2] === 'create') {
                Mdc.getApplication().getSetupComServersController().showEditView();
            } else {
                Mdc.getApplication().getSetupComServersController().showEditView(tokens[2]);
            }
        } else {
            this.unknownTokensReturnToOverview();
        }
    },

    handleComPortTokens: function (tokens) {
        if (tokens.length === 3) {
                Mdc.getApplication().getSetupComPortsController().showEditView(tokens[2]);
        } else {
            this.unknownTokensReturnToOverview();
        }
    },

    handleCommunicationProtocolTokens: function (tokens) {
        if (tokens.length === 2) {
            Mdc.getApplication().getSetupSetupOverviewController().showDeviceCommunicationProtocols();
        } else if (tokens.length === 3) {
            if (tokens[2] === 'create') {
                Mdc.getApplication().getSetupDeviceCommunicationProtocolController().showEditView();
            } else {
                Mdc.getApplication().getSetupDeviceCommunicationProtocolController().showEditView(tokens[2]);
            }
        } else {
            this.unknownTokensReturnToOverview();
        }
    },

    unknownTokensReturnToOverview: function () {
        Mdc.getApplication().getSetupSetupOverviewController().showOverview();
    },

    tokenizeBrowse: function (item, id) {
        if (id === undefined) {
            return this.tokenize([this.rootToken, item]);
        } else {
            return this.tokenize([this.rootToken, item, id]);
        }
    },

    tokenizeAddComserver: function () {
        return this.tokenize([this.rootToken, 'comservers', 'create']);
    },

    tokenizeAddDeviceCommunicationProtocol: function () {
        return this.tokenize([this.rootToken, 'devicecommunicationprotocols', 'create']);
    }
});