Ext.define('Mdc.controller.history.DeviceCommunicationProtocol', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'deviceCommunicationProtocols',

    doConversion: function (tokens) {
        Mdc.getApplication().getDeviceCommunicationProtocolController().showOverview();
    }
});