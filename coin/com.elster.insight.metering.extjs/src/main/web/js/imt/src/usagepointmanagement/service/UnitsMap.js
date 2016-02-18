Ext.define('Imt.usagepointmanagement.service.UnitsMap', {
    singleton: true,


    volts: {
        "0": Uni.I18n.translate('general.measurementunits.volt', 'IMT', 'V'),
        "3": Uni.I18n.translate('general.measurementunits.kiloVolt', 'IMT', 'kV'),
        "6": Uni.I18n.translate('general.measurementunits.megaVolt', 'IMT', 'MV'),
        "9": Uni.I18n.translate('general.measurementunits.gigaVolt', 'IMT', 'GV')

    },

    getUnit: function(unit){
        switch(unit){
            case "V":{
                return this.volts
            } break;
        }
    },

    getActual: function(unit, multiplier){
        return this.getUnit(unit)[multiplier];
    },

    getUnitStore: function(unit){
        switch(unit){
            case "V":{
                return Imt.usagepointmanagement.store.measurementunits.Voltage
            } break;
        }
    },

    getValue: function(unit, multiplier){
        var store = this.getUnitStore(unit),
            record =store.findRecord('multiplier', multiplier);

        return record.get('displayValue');
    }
});
