Ext.define('Mdc.controller.history.Setup', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'setup',

    doConversion: function (tokens) {
        if (tokens.length > 1 && tokens[1] === 'comservers') {
            this.handleComServerTokens(tokens);
        } else if (tokens.length > 1 && tokens[1] === 'devicecommunicationprotocols') {
            this.handleCommunicationProtocolTokens(tokens);
        } else if (tokens.length > 1 && tokens[1] === 'comportpools') {
            this.handleComPortPoolTokens(tokens);
        } else if (tokens.length > 1 && tokens[1] === 'licensedprotocols') {
            this.handleLicensedProtocolTokens(tokens);
        } else if (tokens.length > 1 && tokens[1] === 'devicetypes') {
            if (tokens[3] === 'registertypes') {
                this.handleRegisterMappingTokens(tokens);
            } else if (tokens[3] === 'configurations'){
                this.handleConfigurationTokens(tokens);
            } else {
                this.handleDeviceTypeTokens(tokens);
            }
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

    handleLicensedProtocolTokens: function (tokens) {
        if (tokens.length === 2) {
            Mdc.getApplication().getSetupSetupOverviewController().showLicensedProtocols();
        } else {
            this.unknownTokensReturnToOverview();
        }
    },

    handleComPortPoolTokens: function (tokens) {
        if (tokens.length === 2) {
            Mdc.getApplication().getSetupSetupOverviewController().showComPortPools();
        } else if (tokens.length === 3) {
            if (tokens[2] === 'create') {
                Mdc.getApplication().getSetupComPortPoolsController().showEditView();
            } else {
                Mdc.getApplication().getSetupComPortPoolsController().showEditView(tokens[2]);
            }
        }
    },

    handleDeviceTypeTokens: function (tokens) {
        if (tokens.length === 2) {
            Mdc.getApplication().getSetupSetupOverviewController().showDeviceTypes();
        } else if (tokens.length === 3) {
            if (tokens[2] === 'create') {
                Mdc.getApplication().getSetupDeviceTypesController().showDeviceTypeCreateView(null);
            } else {
                Mdc.getApplication().getSetupDeviceTypesController().showDeviceTypeDetailsView(tokens[2]);
            }
        } else if (tokens.length === 4){
            if (tokens[3] === 'edit'){
                Mdc.getApplication().getSetupDeviceTypesController().showDeviceTypeEditView(tokens[2]);
            }
        }
    },

    handleRegisterMappingTokens: function (tokens) {
        if (tokens.length === 4) {
            Mdc.getApplication().getSetupRegisterMappingsController().showRegisterMappings(tokens[2]);
        } else if (tokens.length === 5) {
            if (tokens[4] === 'add') {
                Mdc.getApplication().getSetupRegisterMappingsController().addRegisterMappings(tokens[2]);
            }
        }
    },

    handleConfigurationTokens: function (tokens) {
        if (tokens.length === 4) {
            Mdc.getApplication().getSetupDeviceConfigurationsController().showDeviceConfigurations(tokens[2]);
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
    },

    tokenizeAddComPortPool: function () {
        return this.tokenize([this.rootToken, 'comportpools', 'create']);
    }
});