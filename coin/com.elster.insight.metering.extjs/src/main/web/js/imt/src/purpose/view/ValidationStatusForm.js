Ext.define('Imt.purpose.view.ValidationStatusForm', {
    extend: 'Ext.container.Container',
    alias: 'widget.output-validation-status-form',
    showSuspectReasonField: true,
    router: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'displayfield',
                name: 'validationActive',
                itemId: 'validation-active-field',
                fieldLabel: Uni.I18n.translate('usagepoint.purpose.output.validation.status', 'IMT', 'Validation status'),
                htmlEncode: false,
                valueToRaw: function (v) {
                    return v;
                },
                renderer: function (value) {
                    var status = '-',
                        icon = '';

                    switch (value) {
                        case true:
                            status = Uni.I18n.translate('usagepoint.purpose.output.validation.status.active', 'IMT', 'Active');
                            icon = '<span class="icon-checkmark-circle" style="color: #33CC33; margin-left: 10px"></span>';
                            break;
                        case false:
                            status = Uni.I18n.translate('usagepoint.purpose.output.validation.status.inactive', 'IMT', 'Inactive');
                            icon = '<span class="icon-blocked" style="color: #eb5642; margin-left: 10px"></span>';
                            break;
                    }

                    return status + icon
                }
            },
            {
                xtype: 'displayfield',
                name: 'allDataValidated',
                itemId: 'all-data-validated-field',
                fieldLabel: Uni.I18n.translate('usagepoint.purpose.output.validation.alldatavalidated', 'IMT', 'All data validated'),
                valueToRaw: function (v) {
                    return v;
                },
                renderer: function (value) {
                    var result = '-';

                    switch (value) {
                        case true:
                            result = Uni.I18n.translate('general.yes', 'IMT', 'Yes');
                            break;
                        case false:
                            result = Uni.I18n.translate('general.no', 'IMT', 'No');
                            break;
                    }

                    return result;
                }
            },
            {
                xtype: 'displayfield',
                name: 'lastChecked',
                itemId: 'last-checked-field',
                fieldLabel: Uni.I18n.translate('usagepoint.purpose.output.validation.lastchecked', 'IMT', 'Last checked'),
                htmlEncode: false,
                renderer: function (value) {
                    return value
                        ? Uni.DateTime.formatDateTimeLong(new Date(value))
                    + '<span class="icon-info" style="margin-left: 10px" data-qtip="'
                    + Uni.I18n.translate('usagepoint.purpose.output.validation.lastchecked.qtip', 'IMT', 'The moment when the last interval was checked in validation process')
                    + '"></span>'
                        : '-';
                }
            },
            {
                xtype: 'displayfield',
                name: 'suspectReason',
                itemId: 'suspect-reason-field',
                fieldLabel: Uni.I18n.translate('usagepoint.purpose.output.validation.suspects', 'IMT', 'Suspects (last month)'),
                htmlEncode: false,
                privileges: me.showSuspectReasonField,
                renderer: function (value) {
                    var result = '';

                    if (Ext.isArray(value)) {
                        Ext.Array.each(value, function (item, index) {
                            var url = me.router.getRoute('administration/rulesets/overview/versions/overview/rules/overview').buildUrl({
                                ruleSetId: item.key.ruleSetVersion.ruleSet.id,
                                versionId: item.key.ruleSetVersion.id,
                                ruleId: item.key.id
                            });

                            if (index) {
                                result += '<br>';
                            }

                            result += '<a href="' + url + '">' + item.key.displayName + '</a>';
                            result += ' - ';
                            result += Uni.I18n.translatePlural('general.suspectX', item.value, 'IMT', '{0} suspects', '{0} suspect', '{0} suspects');
                        });
                    }

                    return result || '-';
                }
            }
        ];
        me.callParent();
    },

    loadValidationInfo: function (validationInfo) {
        var me = this;

        Ext.suspendLayouts();
        Ext.iterate(validationInfo, function (key, value) {
            var field = me.down('[name=' + key + ']');

            if (field) {
                field.setValue(value);
            }
        });
        Ext.resumeLayouts(true);
    }
});