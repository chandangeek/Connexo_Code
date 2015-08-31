/**
 * @class Uni.view.error.Window
 */
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
    closeAction: 'hide',

    title: 'Error message',

    items: [
        {
            xtype: 'textareafield',
            itemId: 'messagefield',
            margin: 10
        }
    ],

    initComponent: function () {
        this.buttons = [
            {
                text: Uni.I18n.translate('general.reportIssue','UNI','Report issue'),
                action: 'report',
                disabled: true
            },
            {
                text: Uni.I18n.translate('general.close','UNI','Close'),
                scope: this,
                handler: this.close
            }
        ];

        this.callParent(arguments);
    },

    setErrorMessage: function (message) {
        var errorMessageField = this.down('#messagefield');
        errorMessageField.setValue(message);
    }

});