Ext.define('Mdc.controller.history.Setup', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'setup',

    doConversion: function (tokens) {
        if (tokens.length > 0 && tokens[1] === 'comservers') {
            Mdc.getApplication().getSetupController().showComServers();
        } else if (tokens.length > 0 && tokens[1] === 'devicecommunicationprotocols') {
            Mdc.getApplication().getSetupController().showDeviceCommunicationProtocols();
        } else {
            Mdc.getApplication().getSetupController().showOverview();
        }

    }
});