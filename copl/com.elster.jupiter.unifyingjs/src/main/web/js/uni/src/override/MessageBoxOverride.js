/**
 * @class Uni.override.MessageBoxOverride
 */
Ext.define('Uni.override.MessageBoxOverride', {
    override: 'Ext.window.MessageBox',

    buttonText: {
        ok: I18n.translate('window.messabox.ok', 'UNI', 'OK'),
        yes: I18n.translate('window.messabox.yes', 'UNI', 'Yes'),
        no: I18n.translate('window.messabox.no', 'UNI', 'No'),
        cancel: I18n.translate('window.messabox.cancel', 'UNI', 'Cancel')
    }

});