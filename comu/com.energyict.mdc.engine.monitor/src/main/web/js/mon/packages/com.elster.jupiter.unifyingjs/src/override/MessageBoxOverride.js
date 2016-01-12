/**
 * @class Uni.override.MessageBoxOverride
 */
Ext.define('Uni.override.MessageBoxOverride', {
    override: 'Ext.window.MessageBox',

    buttonText: {
        ok: Uni.I18n.translate('window.messabox.ok', 'UNI', 'OK'),
        yes: Uni.I18n.translate('window.messabox.yes', 'UNI', 'Yes'),
        no: Uni.I18n.translate('window.messabox.no', 'UNI', 'No'),
        cancel: Uni.I18n.translate('window.messabox.cancel', 'UNI', 'Cancel')
    }

});