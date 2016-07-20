Ext.define('Imt.purpose.view.ValidationStatusForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.output-validation-status-form',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'displayfield',
                name: 'validationActive',
                fieldLabel: Uni.I18n.translate('usagepoint.purpose.output.validation.status', 'IMT', 'Validation status'),
                htmlEncode: false,
                renderer: function (isActive) {
                    var icon = '&nbsp;&nbsp;<i class="icon ' +
                            (isActive ? 'icon-warning2' : 'icon-checkmark-circle2') +
                            '" style="display: inline-block; width: 16px; height: 16px;"></i>',
                        status = isActive ? Uni.I18n.translate('usagepoint.purpose.output.validation.status.active', 'IMT', 'Active') : Uni.I18n.translate('usagepoint.purpose.output.validation.status.inactive', 'IMT', 'Inactive');
                    return status + icon
                }
            },
            {
                xtype: 'displayfield',
                name: 'allDataValidated',
                fieldLabel: Uni.I18n.translate('usagepoint.purpose.output.validation.alldatavalidated', 'IMT', 'All data validated'),
                htmlEncode: false,
                renderer: function (validated) {
                    return validated ? Uni.I18n.translate('general.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.no', 'IMT', 'No');
                }
            },
            {
                xtype: 'displayfield',
                name: 'lastChecked',
                fieldLabel: Uni.I18n.translate('usagepoint.purpose.output.validation.lastchecked', 'IMT', 'Last checked'),
                htmlEncode: false,
                renderer: function (lastChecked) {
                    return lastChecked
                }
            },
            {
                xtype: 'displayfield',
                name: 'suspectReason',
                fieldLabel: Uni.I18n.translate('usagepoint.purpose.output.validation.suspects', 'IMT', 'Suspects (last mounth)'),
                htmlEncode: false,
                renderer: function (value) {
                    return value
                }
            }
        ];
        me.callParent();
    }
});