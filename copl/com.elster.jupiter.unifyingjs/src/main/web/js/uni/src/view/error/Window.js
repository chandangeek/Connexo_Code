Ext.define('Uni.view.error.Window', {
    extend: 'Ext.window.Window',
    alias: 'widget.errorWindow',

    requires: [
    ],

    width: 600,
    height: 350,

    layout: 'fit',

    modal: true,
    constrain: true,

    title: 'Error message',

    items: [
        {
            xtype: 'textareafield',
            itemId: 'message',
            margin: 10
        }
    ],

    buttons: [
        {
            text: 'Report issue'
        },
        {
            text: 'Close' // TODO Close the window.
        }
    ],

    setErrorMessage: function (message) {
        var messageField = this.down('#message');
        messageField.setValue(message);
    }

});