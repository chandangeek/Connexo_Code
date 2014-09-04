Ext.define('Mdc.view.setup.deviceregisterdata.ValidationPreview', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.deviceregisterreportpreview-validation',
    itemId: 'deviceregisterreportpreviewvalidation',

    fieldLabel: Uni.I18n.translate('deviceloadprofiles.validation', 'MDC', 'Validation'),
    labelAlign: 'top',
    layout: 'vbox',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 200
    },
    items: [
        {
            fieldLabel: Uni.I18n.translate('device.registerData.validationStatus', 'MDC', 'Validation status'),
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
            fieldLabel: Uni.I18n.translate('device.registerData.dataValidated', 'MDC', 'Data validated'),
            name: 'dataValidated',
            renderer: function (value) {
                if (value === true) {
                    return Uni.I18n.translate('general.yes', 'MDC', 'Yes');
                } else if (value === false) {
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
                        return Uni.I18n.translate('device.registerData.notValidated', 'MDC', 'Not validated') + '&nbsp;&nbsp;&nbsp;<img style="vertical-align: middle; height: 13px" src="../mdc/resources/images/Not-validated.png"/>';
                        break;
                    case 'validationStatus.ok':
                        return Uni.I18n.translate('validationStatus.ok', 'MDC', 'OK');
                        break;
                    case 'validationStatus.suspect':
                        return Uni.I18n.translate('validationStatus.suspect', 'MDC', 'Suspect') + '&nbsp;&nbsp;&nbsp;<img style="vertical-align: middle; height: 13px" src="../mdc/resources/images/Suspect.png"/>';
                        break;
                    default:
                        return '';
                        break;
                }
            }
        },
        {
            fieldLabel: Uni.I18n.translate('device.registerData.suspectReason', 'MDC', 'Suspect reason'),
            name: 'suspect_rules'
        }
    ]
});
