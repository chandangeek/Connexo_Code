/**
 * @class Uni.form.field.Coordinates
 */
Ext.define('Uni.form.field.Coordinates', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.coordinates',
    mixins: {
        field: 'Ext.form.field.Field'
    },
    layout: 'vbox',
    requires: [
        'Ext.container.Container',
        'Uni.property.view.DefaultButton'
    ],
    displayResetButton: false,

    initComponent: function () {

        var me = this,
            completeWidth = me.width - me.labelWidth - me.labelPad,
            itemsWidth = me.displayResetButton ? me.width - me.labelWidth - me.labelPad - 40 : me.width - me.labelWidth - me.labelPad,
            noCoordinate = {
                xtype: 'displayfield',
                itemId: 'no-coordinate',
                value: Uni.I18n.translate('coordinate.noInput', 'UNI', 'No coordinates input'),
                fieldStyle: {
                    fontStyle: 'italic',
                    color: '#999'
                },
                width: itemsWidth
            },
            displayCoordinateLat = {
                xtype: 'displayfield',
                itemId: 'coordinate-lat',
                width: '39%',
                renderer: function(value) { return Ext.isEmpty(value) ? '' : value }
            },
            displayCoordinateLong = {
                xtype: 'displayfield',
                itemId: 'coordinate-long',
                width: '39%',
                renderer: function(value) { return Ext.isEmpty(value) ? '' : value }
            },
            displayCoordinateElev = {
                xtype: 'displayfield',
                itemId: 'coordinate-elev',
                width: '22%',
                renderer: function(value) { return Ext.isEmpty(value) ? '' : value }
            },
            displayContainer = {
                xtype: 'container',
                itemId: 'ctn-coordinate',
                hidden: true,
                width: itemsWidth,
                layout: {
                    type: 'hbox',
                    align: 'middle'
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
                xtype: 'panel',
                itemId: 'pnl-invalid-coordinate',
                width: '100%',
                bodyStyle: {
                    color: '#eb5642'
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
                width: itemsWidth
            },
            textContainer = {
                xtype: 'container',
                itemId: 'ctn-coordinates',
                width: completeWidth,
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'container',
                        itemId: 'ctn-txtCoordinates',
                        width: itemsWidth,
                        layout: {
                            type: 'hbox',
                            align: 'top'
                        },
                        items: [textCoordinateLat, coordinateSeparator, textCoordinateLong, coordinateSeparator, textCoordinateElev]
                    },
                    {
                        xtype: 'container',
                        itemId: 'ctn-restore',
                        width: 40,
                        layout: {
                            type: 'hbox',
                            align: 'top'
                        },
                        items: [
                            {
                                xtype: 'uni-default-button',
                                itemId: 'uni-coordinate-default-button',
                                listeners: {
                                    click: {
                                        fn: me.onClickDefault,
                                        scope: me
                                    }
                                }
                            }
                        ]
                    }
                ]
            };

        me.items = [noCoordinate, displayContainer, textContainer, invalidCoordinate, textNote];

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
        if (field.getValue() != null) {
            if (field.itemId == 'txt-coordinate-lat') {
                latField.setValue(me.convertDDToDMS(field.getValue(), 'lat'));
            }
            else if (field.itemId == 'txt-coordinate-long') {
                longField.setValue(me.convertDDToDMS(field.getValue(), 'long'));
            }
            else if (field.itemId == 'txt-coordinate-elev') {
                elevField.setValue(Ext.String.format('{0} {1}', field.getValue(), Uni.I18n.translate('coordinates.elevationUnit', 'UNI', 'm '))); // Intentionally added space in translation
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
        defaultButton.setDisabled(me.displayValue.usagePointSpatialCoordinates == undefined);
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
            return false;
        } else if (type == "long" && Abs > (180 * 1000000)) {
            return false;
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
        errorMsg.update(fields);
        errorMsg.show();

    },
    clearInvalid: function () {
        this.down('#pnl-invalid-coordinate').hide();
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