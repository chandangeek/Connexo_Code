Ext.define('Mdc.controller.history.Setup', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'setup',
    previousTokens: ['setup'],
    currentTokens: null,

    doConversion: function (tokens) {
        if (this.currentTokens !== null) {
            this.previousTokens = this.currentTokens;
        }
        this.currentTokens = tokens;
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
            } else if (tokens[3] === 'configurations') {
                this.handleConfigurationTokens(tokens);
            } else {
                this.handleDeviceTypeTokens(tokens);
            }
        } else if (tokens.length > 1 && tokens[1] === 'registertypes') {
            this.handleRegisterTypeTokens(tokens);
        } else {
            this.unknownTokensReturnToOverview();
        }
    },

    tokenizePreviousTokens: function () {
        return this.tokenize(this.previousTokens);
    },

    handleComServerTokens: function (tokens) {
        if (tokens.length == 2) {
            this.getApplication().getController('Mdc.controller.setup.SetupOverview').showComServers();
        } else if (tokens.length === 3) {
            if (tokens[2] === 'create') {
                this.getApplication().getController('Mdc.controller.setup.ComServers').showEditView();
            } else {
                this.getApplication().getController('Mdc.controller.setup.ComServers').showEditView(tokens[2]);
            }
        } else {
            this.unknownTokensReturnToOverview();
        }
    },

    handleCommunicationProtocolTokens: function (tokens) {
        if (tokens.length === 2) {
            this.getApplication().getController('Mdc.controller.setup.SetupOverview').showDeviceCommunicationProtocols();
        } else if (tokens.length === 3) {
            if (tokens[2] === 'create') {
                this.getApplication().getController('Mdc.controller.setup.DeviceCommunicationProtocol').showEditView();
            } else {
                this.getApplication().getController('Mdc.controller.setup.DeviceCommunicationProtocol').showEditView(tokens[2]);
            }
        } else {
            this.unknownTokensReturnToOverview();
        }
    },

    handleLicensedProtocolTokens: function (tokens) {
        if (tokens.length === 2) {
            this.getApplication().getController('Mdc.controller.setup.SetupOverview').showLicensedProtocols();
        } else {
            this.unknownTokensReturnToOverview();
        }
    },

    handleComPortPoolTokens: function (tokens) {
        if (tokens.length === 2) {
            this.getApplication().getController('Mdc.controller.setup.SetupOverview').showComPortPools();
        } else if (tokens.length === 3) {
            if (tokens[2] === 'create') {
                this.getApplication().getController('Mdc.controller.setup.ComPortPools').showEditView();
            } else {
                this.getApplication().getController('Mdc.controller.setup.ComPortPools').showEditView(tokens[2]);
            }
        }
    },

    handleDeviceTypeTokens: function (tokens) {

        if (tokens.length === 2) {
            this.getApplication().getController('Mdc.controller.setup.SetupOverview').showDeviceTypes();

        } else if (tokens.length === 3) {
            if (tokens[2] === 'create') {
                this.getApplication().getController('Mdc.controller.setup.DeviceTypes').showDeviceTypeCreateView(null);
            } else {
                this.getApplication().getController('Mdc.controller.setup.DeviceTypes').showDeviceTypeDetailsView(tokens[2]);
            }
        } else if (tokens.length === 4) {
            if (tokens[3] === 'edit') {
                this.getApplication().getController('Mdc.controller.setup.DeviceTypes').showDeviceTypeEditView(tokens[2]);
            }
        }
    },

    handleRegisterTypeTokens: function (tokens) {
            if (tokens.length === 2) {
                this.getApplication().getController('Mdc.controller.setup.RegisterTypes').showRegisterTypes();
            } else if (tokens.length === 3) {
                if (tokens[2] === 'create') {
                    this.getApplication().getController('Mdc.controller.setup.RegisterTypes').showRegisterTypeCreateView(null);
                } else {
                    this.getApplication().getController('Mdc.controller.setup.RegisterTypes').showRegisterTypeDetailsView(tokens[2]);
                }
            } else if (tokens.length === 4) {
                if (tokens[3] === 'edit') {
                    this.getApplication().getController('Mdc.controller.setup.RegisterTypes').showRegisterTypeEditView(tokens[2]);
                }
            }
        },

    handleRegisterMappingTokens: function (tokens) {
        if (tokens.length === 4) {
            this.getApplication().getController('Mdc.controller.setup.RegisterMappings').showRegisterMappings(tokens[2]);
        } else if (tokens.length === 5) {
            if (tokens[4] === 'add') {
                this.getApplication().getController('Mdc.controller.setup.RegisterMappings').addRegisterMappings(tokens[2]);
            }
        }
    },

    handleConfigurationTokens: function (tokens) {
        if (tokens.length === 4) {
            this.getApplication().getController('Mdc.controller.setup.DeviceConfigurations').showDeviceConfigurations(tokens[2]);
        }
    },

    unknownTokensReturnToOverview: function () {
        this.getApplication().getController('Mdc.controller.setup.SetupOverview').showOverview();
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