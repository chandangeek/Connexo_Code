/**
 * @class Uni.override.MessageBoxOverride
 */
Ext.define('Uni.override.MessageBoxOverride', {
    override: 'Ext.window.MessageBox',

    buttonText: {
        ok: Uni.I18n.translate('general.ok', 'UNI', 'OK'),
        yes: Uni.I18n.translate('general.yes', 'UNI', 'Yes'),
        no: Uni.I18n.translate('general.no', 'UNI', 'No'),
        cancel: Uni.I18n.translate('general.cancel', 'UNI', 'Cancel')
    }

});