Ext.define('Mdc.widget.DateTimeField', {
    extend: 'Ext.form.FieldContainer',
    mixins: {
        field: 'Ext.form.field.Field'
    },
    alias: 'widget.dateTimeField',
    layout: 'hbox',
    msgTarget: 'under',
    submitFormat: 'c',
    dateCfg: null,
    hourCfg:null,
    minuteCfg:null,

    initComponent: function () {
        var me = this;
        if (!me.dateCfg) {
            me.dateCfg = {};
        }
        if (!me.hourCfg) {
            me.hourCfg = {};
        }
        if (!me.minuteCfg) {
            me.minuteCfg = {};
        }
        me.buildField();
        me.callParent();
        me.dateField = me.down('#date');
        me.separator = me.down('#separator');
        me.hourField = me.down('#hourField');
        me.minuteField = me.down('#minuteField');
        me.initField();
    },

    //@private
    buildField: function () {
        var me = this;
        me.items = [
            Ext.apply({
                xtype: 'datefield',
                itemId: 'date',
                format: 'd/m/Y',
                altFormats: 'd.m.Y|d m Y',
                margin: '0 5 5 0',
                required: true,
                submitValue: false
            },me.dateCfg),
            Ext.apply({
                xtype: 'displayfield',
                itemId: 'separator',
                value: Uni.I18n.translate('datetimefield.at', 'UNI', 'at'),
                submitValue: false,
                margin: '0 5 0 0'
            }),
            Ext.apply({
                xtype: 'numberfield',
                itemId: 'hourField',
//                valueToRaw: function (value)
//                {
//                    return (value < 10 ? '0' : '') + value;
//                },
                maxValue: 24,
                minValue: 0,
                submitValue: false,
                margin: '0 5 0 0'
            }, me.hourCfg),
            Ext.apply({
                xtype: 'displayfield',
                value: ':',
                submitValue: false,
                margin: '0 5 0 0'
            }),
            Ext.apply({
                xtype: 'numberfield',
                itemId: 'minuteField',
//                valueToRaw: function (value)
//                {
//                    return (value < 10 ? '0' : '') + value;
//                },
                maxValue: 60,
                minValue: 0,
                submitValue: false,
                margin: '0 5 0 0'
            }, me.minuteCfg)

        ]
    },


    getValue: function () {
        var date =  this.dateField.getValue();
        date.setHours(this.hourField.getValue());
        date.setMinutes(this.minuteField.getValue());
        return date.getTime();
    },

    setValue: function (date) {
//        this.hideOffsetGui();
//        var me = this;
//        if (schedule) {
//            me.valueField.setValue(schedule.temporalExpression.every.count);
//            me.unitField.setValue(schedule.temporalExpression.every.timeUnit);
//
//            var offSet = schedule.temporalExpression.offset;
//            switch(schedule.temporalExpression.every.timeUnit){
//                case 'minutes':
//                    this.secondField.setValue(offSet.count);
//                    break;
//                case 'hours':
//                    this.minuteField.setValue(Math.floor(offSet.count/60));
//                    this.secondField.setValue(offSet.count%60);
//                    break;
//                case 'days':
//                    this.hourField.setValue(Math.floor(offSet.count/3600));
//                    this.minuteField.setValue(Math.floor((offSet.count%3600)/60));
//                    this.secondField.setValue((offSet.count%3600)%60);
//                    break;
//                case 'weeks':
//                    break;
//                case 'months':
//                    if(schedule.temporalExpression.lastDay === true){
//                        this.dayIndexField.setValue(Uni.I18n.translate('schedulefield.last', 'UNI', 'last'));
//                    } else {
//                        this.dayIndexField.setValue(Math.floor(offSet.count/86400));
//                        this.hourField.setValue(Math.floor((offSet.count%86400)/3600));
//                        this.minuteField.setValue(Math.floor(((offSet.count%86400)%3600)/60));
//                        this.secondField.setValue(((offSet.count%86400)%3600)%60);
//                    }
//                    break;
//            }
//        }
    },

    getSubmitData: function () {
        var me = this,
            data = null;
        if (!me.disabled && me.submitValue && !me.isFileUpload()) {
            data = {};
            value = me.getValue();
            data[me.getName()] = '' + value ? value : null;
        }
        return data;
    },

    markInvalid: function(fields){
        this.eachItem(function(field){
            field.markInvalid(fields);
        });
    },

    eachItem: function(fn, scope) {
        if(this.items && this.items.each){
            this.items.each(fn, scope || this);
        }
    }
});

