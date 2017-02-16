/**
 * @class Uni.form.field.Coordinates
 */
Ext.define('Uni.form.field.Coordinates', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.coordinates',
    mixins: {
        field: 'Ext.form.field.Field'
    },
    layout: 'hbox',
    requires: [
        'Ext.container.Container',
        'Uni.property.view.DefaultButton'
    ],
    displayResetButton: false,
    labelWidth: 150,

    initComponent: function () {
        var me = this,
            noCoordinate = {
                xtype: 'displayfield',
                itemId: 'no-coordinate',
                value: Uni.I18n.translate('coordinate.noInput', 'UNI', 'No coordinates input'),
                fieldStyle: 'font-style:italic; color:#999',
                margin: 0
            },
            displayCoordinateLat = {
                xtype: 'displayfield',
                itemId: 'coordinate-lat',
                width: '39%',
                emptyValueDisplay: ''
            },
            displayCoordinateLong = {
                xtype: 'displayfield',
                itemId: 'coordinate-long',
                width: '39%',
                emptyValueDisplay: ''
            },
            displayCoordinateElev = {
                xtype: 'displayfield',
                itemId: 'coordinate-elev',
                width: '22%',
                emptyValueDisplay: ''
            },
            displayContainer = {
                xtype: 'container',
                itemId: 'ctn-coordinate',
                hidden: true,
                layout: 'hbox',
                flex: 1,
                defaults: {
                    fieldStyle: 'overflow: hidden; text-overflow: ellipsis; white-space: nowrap'
                },
                items: [displayCoordinateLat, displayCoordinateLong, displayCoordinateElev]
            },
            textCoordinateLat = {
                xtype: 'numberfield',
                hideTrigger: true,
                itemId: 'txt-coordinate-lat',
                width: '35%',
                decimalPrecision: 5,
                minValue: -90,
                maxValue: 90,
                listeners: {
                    change: {
                        fn: me.onItemChange,
                        scope: me
                    },
                    blur: {
                        fn: me.recurrenceNumberFieldValidation,
                        scope: me
                    }
                }
            },
            textCoordinateLong = {
                xtype: 'numberfield',
                hideTrigger: true,
                itemId: 'txt-coordinate-long',
                width: '35%',
                decimalPrecision: 5,
                minValue: -180,
                maxValue: 180,
                listeners: {
                    change: {
                        fn: me.onItemChange,
                        scope: me
                    },
                    blur: {
                        fn: me.recurrenceNumberFieldValidation,
                        scope: me
                    }
                }
            },
            textCoordinateElev = {
                xtype: 'numberfield',
                hideTrigger: true,
                itemId: 'txt-coordinate-elev',
                width: '21%',
                decimalPrecision: 3,
                minValue: -10000,
                maxValue: 10000,
                listeners: {
                    change: {
                        fn: me.onItemChange,
                        scope: me
                    },
                    blur: {
                        fn: me.recurrenceNumberFieldValidation,
                        scope: me
                    }
                }
            },
            coordinateSeparator = {
                xtype: 'displayfield',
                value: ':',
                width: '4%',
                fieldStyle: "text-align:center;"
            },
            invalidCoordinate = {
                xtype: 'component',
                itemId: 'pnl-invalid-coordinate',
                style: {
                    color: '#eb5642',
                    margin: '-10px 0 10px 0'
                },
                hidden: true,
                html: ''
            },
            textNote = {
                xtype: 'displayfield',
                itemId: 'txtNote',
                value: Uni.I18n.translate('coordinate.note', 'UNI', 'Edited coordinate values will be displayed also in degrees, minutes, seconds format'),
                fieldStyle: {
                    fontStyle: 'italic',
                    color: '#999'
                },
                width: '100%'
            },
            textContainer = {
                xtype: 'container',
                itemId: 'ctn-txtCoordinates',
                layout: {
                    type: 'hbox',
                    align: 'top'
                },
                width: '100%',
                items: [textCoordinateLat, coordinateSeparator, textCoordinateLong, coordinateSeparator, textCoordinateElev]
            };

        me.items = [
            {
                xtype: 'container',
                flex: 1,
                items: [noCoordinate, displayContainer, textContainer, invalidCoordinate, textNote]
            },
            {
                xtype: 'uni-default-button',
                itemId: 'uni-coordinate-default-button',
                margin: '34 0 0 5',
                listeners: {
                    click: {
                        fn: me.onClickDefault,
                        scope: me
                    }
                }
            }
        ];

        me.callParent(arguments);
        if (me.value) {
            me.setValue(me.value);
        }
        me.updateResetButton();
    },

    updateResetButton: function () {
        var me = this,
            restoreDefault = me.down('uni-default-button');

        if (me.displayResetButton) {
            restoreDefault.setVisible(true);
            restoreDefault.setTooltip(Uni.I18n.translate('coordinate.coordinateResetTooltip', 'UNI', 'Reset to usage point coordinates'));
        }
    },

    onItemChange: function (field, newValue, oldValue) {
        var me = this,
            latTextField = me.down('#txt-coordinate-lat'),
            longTextField = me.down('#txt-coordinate-long'),
            elevTextField = me.down('#txt-coordinate-elev'),
            latField = me.down('#coordinate-lat'),
            longField = me.down('#coordinate-long'),
            elevField = me.down('#coordinate-elev'),
            defaultButton = me.down('#uni-coordinate-default-button'),
            isNoCoordinateVisible = (latTextField.getValue() == null) && (longTextField.getValue() == null) && (elevTextField.getValue() == null);

        me.down('#no-coordinate').setVisible(isNoCoordinateVisible);
        me.down('#ctn-coordinate').setVisible(!isNoCoordinateVisible);
        if (newValue != null) {
            if (field.itemId == 'txt-coordinate-lat') {
                latField.setValue(me.convertDDToDMS(newValue, 'lat'));
            }
            else if (field.itemId == 'txt-coordinate-long') {
                longField.setValue(me.convertDDToDMS(newValue, 'long'));
            }
            else if (field.itemId == 'txt-coordinate-elev') {
                elevField.setValue(newValue > field.maxValue || newValue < field.minValue
                    ? '-'
                    : Ext.String.format('{0} {1}', newValue, Uni.I18n.translate('coordinates.elevationUnit', 'UNI', 'm '))); // Intentionally added space in translation
            }                                                                                                                                     // as I18nAnalyzer has problems with single character translations
            // defaultButton.setDisabled(me.getValue().spatialCoordinates == me.displayValue.usagePointSpatialCoordinates);
        }
        else {
            if (field.itemId == 'txt-coordinate-lat') {
                latField.setValue('');
            }
            else if (field.itemId == 'txt-coordinate-long') {
                longField.setValue('');
            }
            else if (field.itemId == 'txt-coordinate-elev') {
                elevField.setValue('');
            }
        }
        defaultButton.setDisabled(me.usagePointSpatialCoordinates == undefined);
    },

    setValue: function (value) {
        var me = this,
            latTextField = me.down('#txt-coordinate-lat'),
            longTextField = me.down('#txt-coordinate-long'),
            elevTextField = me.down('#txt-coordinate-elev'),
            defaultButton = me.down('#uni-coordinate-default-button'),
            values;

        me.displayValue = value;

        if ((value == null) || (value.spatialCoordinates == null) || (value.spatialCoordinates.length == 0)) {
            me.down('#ctn-coordinate').setVisible(false);
            me.down('#no-coordinate').setVisible(true);
            defaultButton.setDisabled(true);
            return;
        }
        else {
            me.down('#ctn-coordinate').setVisible(true);
            me.down('#no-coordinate').setVisible(false);
        }
        values = value.spatialCoordinates.split(':');
        if (values.length > 0) {
            latTextField.setValue(values[0]);
        }

        if (values.length > 1) {
            longTextField.setValue(values[1]);
        }

        if (values.length > 1) {
            elevTextField.setValue(values[2]);
        }
        defaultButton.setDisabled((value.usagePointSpatialCoordinates == undefined) ||
            ((value.usagePointSpatialCoordinates != undefined) && value.isInherited));
    },

    getValue: function () {
        var me = this,
            latTextField = me.down('#txt-coordinate-lat'),
            longTextField = me.down('#txt-coordinate-long'),
            elevTextField = me.down('#txt-coordinate-elev'),
            defaultButton = me.down('#uni-coordinate-default-button');

        return me.displayResetButton ? {
            spatialCoordinates: (latTextField.getValue() == null && longTextField.getValue() == null && elevTextField.getValue() == null) ? null :
                Ext.String.format('{0}:{1}:{2}', latTextField.getValue(), longTextField.getValue(), elevTextField.getValue()),
            coordinatesDisplay: null,
            isInherited: (me.displayValue && me.displayValue.usagePointSpatialCoordinates == undefined) ? false : defaultButton.disabled,
            usagePointSpatialCoordinates: me.displayValue ? me.displayValue.usagePointSpatialCoordinates : 0
        } :
        {
            spatialCoordinates: (latTextField.getValue() == null && longTextField.getValue() == null && elevTextField.getValue() == null) ? null :
                Ext.String.format('{0}:{1}:{2}', latTextField.getValue(), longTextField.getValue(), elevTextField.getValue()),
            coordinatesDisplay: null
        };
    },

    convertDDToDMS: function (value, type) {
        var sign = 1, Abs = 0;
        var days, minutes, secounds, direction;

        if (value < 0) {
            sign = -1;
        }
        Abs = Math.abs(Math.round(value * 1000000.));

        if (type == "lat" && Abs > (90 * 1000000)) {
            return '-';
        } else if (type == "long" && Abs > (180 * 1000000)) {
            return '-';
        }

        days = Math.floor(Abs / 1000000);
        minutes = Math.floor(((Abs / 1000000) - days) * 60);
        secounds = ( Math.floor((( ((Abs / 1000000) - days) * 60) - minutes) * 100000) * 60 / 100000 );
        secounds = Math.round(secounds * 100) / 100
        days = days * sign;

        if (type == 'lat') {
            direction = days < 0 ? 'S' : 'N';
        }
        if (type == 'long') {
            direction = days < 0 ? 'W' : 'E'
        }

        if (isNaN(days) || isNaN(minutes) || isNaN(secounds)) {
            return '';
        }
        return (days * sign) + 'º ' + minutes + "' " + secounds + "'' " + direction;
    },

    markInvalid: function (fields) {
        var me = this,
            latTextField = me.down('#txt-coordinate-lat'),
            longTextField = me.down('#txt-coordinate-long'),
            elevTextField = me.down('#txt-coordinate-elev');

        if ((latTextField.getValue() == null) || latTextField.getValue().length == 0) {
            latTextField.markInvalid('');
        }
        if ((longTextField.getValue() == null) || longTextField.getValue().length == 0) {
            longTextField.markInvalid('');
        }
        if ((elevTextField.getValue() == null) || elevTextField.getValue().length == 0) {
            elevTextField.markInvalid('');
        }

        var errorMsg = this.down('#pnl-invalid-coordinate');
        Ext.suspendLayouts();
        errorMsg.update(fields);
        errorMsg.show();
        me.down('#txtNote').hide();
        Ext.resumeLayouts(true);

    },
    clearInvalid: function () {
        var me = this;

        Ext.suspendLayouts();
        me.down('#pnl-invalid-coordinate').hide();
        me.down('#txtNote').show();
        Ext.resumeLayouts(true);
    },

    recurrenceNumberFieldValidation: function (field) {
        var value = field.getValue();

        if (value < field.minValue) {
            field.setValue(field.minValue);
        } else if (value > field.maxValue) {
            field.setValue(field.maxValue);
        }
    },

    onClickDefault: function (button) {
        var me = this,
            latTextField = me.down('#txt-coordinate-lat'),
            longTextField = me.down('#txt-coordinate-long'),
            elevTextField = me.down('#txt-coordinate-elev'),
            defaultButton = me.down('#uni-coordinate-default-button'),
            value = me.displayValue, values;

        if ((value == null) || (value.usagePointSpatialCoordinates == null) || (value.usagePointSpatialCoordinates.length == 0)) {
            me.down('#ctn-coordinate').setVisible(false);
            me.down('#no-coordinate').setVisible(true);
            return;
        }
        else {
            me.down('#ctn-coordinate').setVisible(true);
            me.down('#no-coordinate').setVisible(false);
        }
        values = value.usagePointSpatialCoordinates.split(':');
        if (values.length > 0) {
            latTextField.setValue(values[0]);
        }

        if (values.length > 1) {
            longTextField.setValue(values[1]);
        }

        if (values.length > 1) {
            elevTextField.setValue(values[2]);
        }
        defaultButton.setDisabled(true);
    }
});