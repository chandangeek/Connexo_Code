/**
 * @class Uni.form.field.ReadingTypeDisplay
 */
Ext.define('Uni.form.field.ReadingTypeDisplay', {
    extend: 'Ext.form.field.Display',
    alias: 'widget.reading-type-displayfield',
    name: 'readingType',
    fieldLabel: Uni.I18n.translate('general.readingType', 'UNI', 'Reading type'),
    emptyText: '',
    showTimeAttribute: true,
    link: null,

    requires: [
        'Ext.button.Button'
    ],

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
            '<td style="width: 70%; text-align: left; padding-bottom: 10px">{mRID}</td>',
            '</tr>',
            '<tr>',
                '<td style="width: 30%; text-align: right; font-weight: bold; padding-right: 20px">' + Uni.I18n.translate('readingType.description', 'UNI', 'Description') + '</td>',
            '<td style="width: 70%; text-align: left">{name}</td>',
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
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{macroPeriod}</td>',
            '</tr>',
            '<tr>',
                '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.dataQualifier', 'UNI', 'Data qualifier') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{aggregate}</td>',
            '</tr>',
            '<tr>',
                '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.time', 'UNI', 'Time') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{measuringPeriod}</td>',
            '</tr>',
            '<tr>',
                '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.accumulation', 'UNI', 'Accumulation') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{accumulation}</td>',
            '</tr>',
            '<tr>',
                '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.directionOfFlow', 'UNI', 'Direction of flow') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{flowDirection}</td>',
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
                '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.interharmonicNumerator', 'UNI', 'Interharmonic numerator') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{interHarmonicNumerator}</td>',
            '</tr>',
            '<tr>',
                '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.interharmonicDenominator', 'UNI', 'Interharmonic denominator') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{interHarmonicDenominator}</td>',
            '</tr>',
            '</table>',
            '</td>',
            '<td style="width: 50%; vertical-align: top">',
            '<table style="width: 100%">',
            '<tr>',
                '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.argumentNumerator', 'UNI', 'Argument numerator') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{argumentNumerator}</td>',
            '</tr>',
            '<tr>',
                '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.argumentDenominator', 'UNI', 'Argument denominator') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{argumentDenominator}</td>',
            '</tr>',
            '<tr>',
                '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.timeOfUse', 'UNI', 'Time of use') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{tou}</td>',
            '</tr>',
            '<tr>',
                '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.criticalPeakPeriod', 'UNI', 'Critical peak period') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{cpp}</td>',
            '</tr>',
            '<tr>',
                '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.comsumptionTier', 'UNI', 'Consumption tier') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{consumptionTier}</td>',
            '</tr>',
            '<tr>',
                '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.phase', 'UNI', 'Phase') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{phases}</td>',
            '</tr>',
            '<tr>',
                '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.multiplier', 'UNI', 'Multiplier') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{metricMultiplier}</td>',
            '</tr>',
            '<tr>',
                '<td style="width: 50%; text-align: right; font-weight: bold; padding: 0 20px 10px 0">' + Uni.I18n.translate('readingType.unitOfMeasure', 'UNI', 'Unit of measure') + '</td>',
            '<td style="width: 50%; text-align: left; padding-bottom: 10px">{unit}</td>',
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

    renderer: function (value, field, view, record) {
        if (!value) return this.emptyText;

        var me = this,
            assembledName = value.fullAliasName,
            //alias = value.aliasName ? (' ' + value.aliasName) : '',
            icon = '<span class="uni-icon-info-small" style="cursor: pointer; display: inline-block; width: 16px; height: 16px; float: left;" data-qtip="' + Uni.I18n.translate('readingType.tooltip', 'UNI', 'Reading type info') + '"></span>';

        /*if (value.names && Ext.isObject(value.names)) {
            assembledName +=
                    ((value.names.timeAttribute && me.showTimeAttribute) ? ('[' + value.names.timeAttribute + ']') : '')
                    + alias
                    + (value.names.unitOfMeasure ? (' (' + value.names.unitOfMeasure + ')') : '')
                    + (value.names.phase ? (' ' + value.names.phase ) : '')
                    + (value.names.timeOfUse ? (' ' + value.names.timeOfUse) : '');
        } else {
            assembledName += alias;
        }*/

        setTimeout(function () {
            var parent,
                iconEl;

            if (Ext.isDefined(view) && Ext.isDefined(record)) {
                try {
                    parent = view.getCell(record, me);

                    if (Ext.isDefined(parent)) {
                        iconEl = parent.down('.uni-icon-info-small');
                    }
                } catch (ex) {
                    // Fails for some reason sometimes.
                }
            } else {
                parent = field.getEl();

                if (Ext.isDefined(parent)) {
                    iconEl = parent.down('.uni-icon-info-small');
                }
            }

            if (Ext.isDefined(iconEl)) {
                iconEl.clearListeners();
                iconEl.on('click', function () {
                    field.handler(value, assembledName || value.mRID);
                });
            }
        }, 1);

        return '<span style="display: inline-block; float: left; margin: 0px 10px 0px 0px">' +
            (me.link ? ('<a href="' + me.link + '">' + (Ext.String.htmlEncode(assembledName) || Ext.String.htmlEncode(value.mRID)) + '</a>') :
                (Ext.String.htmlEncode(assembledName) || Ext.String.htmlEncode(value.mRID))) + '</span>' + icon;
    }
});