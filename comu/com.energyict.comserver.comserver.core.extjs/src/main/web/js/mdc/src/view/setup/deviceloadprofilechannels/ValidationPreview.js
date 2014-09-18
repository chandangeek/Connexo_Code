Ext.define('Mdc.view.setup.deviceloadprofilechannels.ValidationPreview', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.deviceloadprofilechannelspreview-validation',
    itemId: 'deviceloadprofilechannelspreviewvalidation',

    fieldLabel: Uni.I18n.translate('deviceloadprofiles.validation', 'MDC', 'Validation'),
    labelAlign: 'top',
    layout: 'vbox',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },
    items: [
        {
            fieldLabel: Uni.I18n.translate('device.channelData.validationStatus', 'MDC', 'Validation status'),
            name: 'validationStatus',
            renderer: function (value) {
                if (value) {
                    return Uni.I18n.translate('communicationtasks.task.active', 'MDC', 'Active');
                } else {
                    return Uni.I18n.translate('communicationtasks.task.inactive', 'MDC', 'Inactive');
                }
            }
        },
        {
            fieldLabel: Uni.I18n.translate('device.channelData.dataValidated', 'MDC', 'Data validated'),
            name: 'dataValidated',
            renderer: function (value) {
                if (value == true) {
                    return Uni.I18n.translate('general.yes', 'MDC', 'Yes');
                } else if (value == false) {
                    return Uni.I18n.translate('general.no', 'MDC', 'No');
                } else {
                    return '';
                }
            }
        },
        {
            fieldLabel: Uni.I18n.translate('device.dataValidation.validationResult', 'MDC', 'Validation result'),
            name: 'validationResult',
            renderer: function (value) {
                switch (value) {
                    case 'validationStatus.notValidated':
                        return Uni.I18n.translate('device.channelData.notValidated', 'MDC', 'Not validated') + '&nbsp;&nbsp;<span class="icon-validation icon-validation-black"></span>';
                        break;
                    case 'validationStatus.ok':
                        return Uni.I18n.translate('validationStatus.ok', 'MDC', 'OK');
                        break;
                    case 'validationStatus.suspect':
                        return Uni.I18n.translate('validationStatus.suspect', 'MDC', 'Suspect') + ' ' + '&nbsp;&nbsp;<span class="icon-validation icon-validation-red"></span>';
                        break;
                    default:
                        return '';
                        break;
                }
            }
        },
        {
            fieldLabel: Uni.I18n.translate('device.channelData.suspectReason', 'MDC', 'Suspect reason'),
            name: 'suspect_rules'
        }
    ]
});
