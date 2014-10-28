/**
 * @class Uni.form.field.ObisDisplay
 */
Ext.define('Uni.form.field.ReadingTypeDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'reading-type-displayfield',
    name: 'readingType',
    fieldLabel: Uni.I18n.translate('readingType.label', 'UNI', 'Reading type'),
    emptyText: '',

    requires: [
        'Ext.button.Button'
    ],

    deferredRenderer: function (value, field, name) {
        var me = this;

        new Ext.button.Button({
            renderTo: field.getEl().down('.x-form-display-field'),
            tooltip: Uni.I18n.translate('readingType.tooltip', 'UNI', 'Reading type info'),
            iconCls: 'icon-info-small',
            cls: 'uni-btn-transparent',
            style: {
                display: 'inline-block',
                "text-decoration": 'none !important'
            },
            handler: function () {
                me.handler(value, name);
            }
        });

        field.updateLayout();
    },

    handler: function (value, name) {
        var widget = Ext.widget('readingTypeDetails');
        widget.setTitle('<span style="margin: 10px 0 0 10px">' + name + '</span>');
        var tpl = new Ext.XTemplate(
            '<table style="width: 100%; margin: 30px 10px">',
            '<tr>',
            '<td colspan="2">',
            '<table style="width: 100%; margin-bottom: 30px">',
            '<tr>',
            '<td style="width: 30%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.name', 'UNI', 'Reading type name') + '</td>',
            '<td style="width: 70%; text-align: left; padding-bottom: 10px">' + name + '</td>',
            '</tr>',
            '<tr>',
            '<td style="width: 30%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.cimCode', 'UNI', 'CIM code') + '</td>',
            '<td style="width: 70%; text-align: left; padding-bottom: 10px">{mrid}</td>',
            '</tr>',
            '<tr>',
            '<td style="width: 30%; text-align: right; font-weight: bold; padding-right: 20px">' + Uni.I18n.translate('readingType.description', 'UNI', 'Description') + '</td>',
            '<td style="width: 70%; text-align: left">{description}</td>',
            '</tr>',
            '</table>',
            '</td>',
            '</tr>',
            '<tr>',
            '<td colspan="2" style="padding-bottom: 20px; font-weight: bold; font-size: 1.5em; color: grey">' + Uni.I18n.translate('readingType.cimCodeDetails', 'UNI', 'CIM code details') + '</td>',
            '</tr>',
            '<tr>',
            '<td style="width: 50%; vertical-align: top">',
            '<table style="width: 100%">',
            '<tr>',
            '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.timePeriodOfInterest', 'UNI', 'Time-period of interest') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{timePeriodOfInterest}</td>',
            '</tr>',
            '<tr>',
            '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.dataQualifier', 'UNI', 'Data qualifier') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{dataQualifier}</td>',
            '</tr>',
            '<tr>',
            '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.timeAttributeEnumerations', 'UNI', 'Time attribute enumerations') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{timeAttributeEnumerations}</td>',
            '</tr>',
            '<tr>',
            '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.accumulationBehaviour', 'UNI', 'Accumulation behavior') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{accumulationBehaviour}</td>',
            '</tr>',
            '<tr>',
            '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.directionOfFlow', 'UNI', 'Direction of flow') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{directionOfFlow}</td>',
            '</tr>',
            '<tr>',
            '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.commodity', 'UNI', 'Commodity') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{commodity}</td>',
            '</tr>',
            '<tr>',
            '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.measurementKind', 'UNI', 'Kind') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{measurementKind}</td>',
            '</tr>',
            '<tr>',
            '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.interharmonics', 'UNI', '(Compound) Interharmonics') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{interharmonics}</td>',
            '</tr>',
            '<tr>',
            '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.argumentReference', 'UNI', '(Compound) Numerator and Denominator Argument Reference') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{argumentReference}</td>',
            '</tr>',
            '</table>',
            '</td>',
            '<td style="width: 50%; vertical-align: top">',
            '<table style="width: 100%">',
            '<tr>',
            '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.timeOfUse', 'UNI', 'Time of use') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{timeOfUse}</td>',
            '</tr>',
            '<tr>',
            '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.criticalPeakPeriod', 'UNI', 'Critical peak period') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{criticalPeakPeriod}</td>',
            '</tr>',
            '<tr>',
            '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.comsumptionTier', 'UNI', 'Consumption tier') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{consumptionTier}</td>',
            '</tr>',
            '<tr>',
            '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.phase', 'UNI', 'Phase') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{phase}</td>',
            '</tr>',
            '<tr>',
            '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.powerOfTenMultiplier', 'UNI', 'Power of ten multiplier') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{powerOfTenMultiplier}</td>',
            '</tr>',
            '<tr>',
            '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.unitOfMeasure', 'UNI', 'Unit of measure') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{unitOfMeasure}</td>',
            '</tr>',
            '<tr>',
            '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.currency', 'UNI', 'Currency') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{currency}</td>',
            '</tr>',
            '</table>',
            '</td>',
            '</tr>',
            '</table>'
        );
        tpl.overwrite(widget.down('panel').body, value);
        widget.show();
    },

    renderer: function (value, field) {
        if (!value) {
            return this.emptyText;
        }

        var assembledName = '';
        if (value.name && Ext.isObject(value.name)) {
            assembledName = value.name.alias + ' ' + value.name.timeOfUse + '(' + value.name.unitOfMeasure + ') [' + value.name.timeAttribute + ']';
        }

        Ext.defer(this.deferredRenderer, 1, this, [value, field, (assembledName || value.mrid)]);
        return '<span style="display: inline-block; width: 230px; float: left;">' + (assembledName || value.mrid) + '</span>';
    }
});