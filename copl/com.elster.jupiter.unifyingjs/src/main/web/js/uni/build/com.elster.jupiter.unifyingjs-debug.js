/**
 * @class Uni.override.ApplicationOverride
 */
Ext.define('Uni.override.ApplicationOverride', {
    override: 'Ext.app.Application',

    unifyingControllers: [
        'Uni.controller.Acknowledgements',
        'Uni.controller.Configuration',
        'Uni.controller.Error',
        'Uni.controller.Navigation',
        'Uni.controller.Portal',
        'Uni.controller.Notifications',
        'Uni.controller.Search'
    ],

    /**
     *
     */
    initControllers: function () {
        this.callParent(arguments);
        this.loadUnifyingControllers();
    },

    loadUnifyingControllers: function () {
        var me = this;

        for (var i = 0, ln = me.unifyingControllers.length; i < ln; i++) {
            me.getController(me.unifyingControllers[i]);
        }
    }

});

Ext.define('Uni.override.panel.Panel', {
    override: 'Ext.panel.Panel',

    beforeRender: function() {
        var me = this;
        this.callParent(arguments);

        if (me.subtitle) {
            this.setSubTitle(me.subtitle);
        }
    },

    /**
     * Set a title for the panel's header. See {@link Ext.panel.Header#title}.
     * @param {String} subtitle
     */
    setSubTitle: function(subtitle) {
        var me = this,
            header = me.header;

        me.subtitle = subtitle;

        if (header) {
            if (header.isHeader) {
                header.setSubTitle(subtitle);
            } else {
                header.subtitle = subtitle;
            }
        } else if (me.rendered) {
            me.updateHeader();
        }
    }
});

Ext.define('Uni.override.panel.Header', {
    override: 'Ext.panel.Header',

    headingTpl: [
        // unselectable="on" is required for Opera, other browsers inherit unselectability from the header
        '<span id="{id}-textEl" class="{headerCls}-text {cls}-text {cls}-text-{ui}" unselectable="on"',
        '<tpl if="headerRole">',
        ' role="{headerRole}"',
        '</tpl>',
        '>{title}</span>',
        '<span id="{id}-subTextEl" class="{headerCls}-sub-text {cls}-sub-text {cls}-sub-text-{ui}" unselectable="on"',
        '>{subtitle}</span>'
    ],

    initComponent: function() {
        var me = this;

        this.callParent(arguments);
        me.titleCmp.childEls.push("subTextEl");
    },

    /**
     * Sets the subtitle of the header.
     * @param {String} subtitle The title to be set
     */
    setSubTitle: function(subtitle) {
        var me = this,
            titleCmp = me.titleCmp;

        me.subtitle = subtitle;

        if (titleCmp.rendered) {
            titleCmp.subTextEl.update(me.subtitle || '&#160;');
            titleCmp.updateLayout();
        } else {
            me.titleCmp.on({
                render: function() {
                    me.setSubTitle(subtitle);
                },
                single: true
            });
        }
    }
});

/**
 * @class Uni.override.ButtonOverride
 */
Ext.define('Uni.override.ButtonOverride', {
    override: 'Ext.button.Button',

    /**
     * Changes the default value from '_blank' to '_self'.
     */
    hrefTarget: '_self'

});

/**
 * @class Uni.override.JsonWriterOverride
 */
Ext.define('Uni.override.JsonWriterOverride', {
    override: 'Ext.data.writer.Json',

    /**
     * Adds the associated data to the returned record.
     * @param record
     * @returns {*}
     */
    getRecordData: function (record, operation) {
     //   Ext.apply(record.data, record.getAssociatedData());
        return record.getWriteData(true, true);
    }

});

/**
 * @class Uni.override.StoreOverride
 *
 * Stops ExtJS from overriding the storeId property when using the stores property in a controller.
 */
Ext.define('Uni.override.StoreOverride', {
    override: 'Ext.data.Store',

    pageSize: 10,

    getStore: function (name) {
        if (Ext.isEmpty(this.self.storeIdMap)) {
            this.self.storeIdMap = {};
        }

        var storeName = this.self.storeIdMap[name],
            store = null;

        if (storeName) {
            store = Ext.StoreManager.get(storeName);
        }
        if (!store) {
            store = Ext.create(this.getModuleClassName(name, 'store'));
            this.self.storeIdMap[name] = store.storeId;
        }

        return store;
    }
});

Ext.define('Uni.override.ServerOverride', {
    override: 'Ext.data.proxy.Server',
    processResponse: function(success, operation, request, response, callback, scope) {
        var me = this,
            reader,
            result;

        if (success === true) {
            reader = me.getReader();

            // Apply defaults to incoming data only for read operations.
            // For create and update, there will already be a client-side record
            // to match with which will contain any defaulted in values.
            reader.applyDefaults = operation.action === 'read';

            result = reader.read(me.extractResponseData(response));

            if (result.success !== false) {
                //see comment in buildRequest for why we include the response object here
                Ext.apply(operation, {
                    response: response,
                    resultSet: result
                });

                operation.commitRecords(result.records);
                operation.setCompleted();
                operation.setSuccessful();
            } else {
                Ext.apply(operation, {
                    response: response,
                    resultSet: result
                });
                operation.setException(result.message);
                me.fireEvent('exception', this, response, operation);
            }
        } else {
            Ext.apply(operation, {
                response: response,
                resultSet: result
            });
            me.setException(operation, response);
            me.fireEvent('exception', this, response, operation);
        }

        //this callback is the one that was passed to the 'read' or 'write' function above
        if (typeof callback == 'function') {
            callback.call(scope || me, operation);
        }

        me.afterRequest(request, success);
    }
});

/**
 * @class Uni.override.ModelOverride
 */
Ext.define('Uni.override.ModelOverride', {
    override: 'Ext.data.Model',

    getWriteData: function(includeAssociated,excludeNotPersisted){
        var me = this,
            fields = me.fields.items,
            fLen = fields.length,
            data = {},
            name, f, persistent;

        persistent = (typeof excludeNotPersisted === 'undefined')?false:excludeNotPersisted;

        for (f = 0; f < fLen; f++) {
            if (!persistent)
            {
                name = fields[f].name;
                data[name] = me.get(name);
            }
            else
            {
                if(fields[f].persist)
                {
                    name = fields[f].name;
                    data[name] = me.get(name);
                }
            }
        }

        if (includeAssociated === true) {
            Ext.apply(data, me.getAssociatedData(persistent));
        }
        return data;
    },

    getAssociatedData: function(persistedFields){
        return this.prepareAssociatedData({}, 1,persistedFields);
    },

    /**
     * @private
     * This complex-looking method takes a given Model instance and returns an object containing all data from
     * all of that Model's *loaded* associations. See {@link #getAssociatedData}
     * @param {Object} seenKeys A hash of all the associations we've already seen
     * @param {Number} depth The current depth
     * @return {Object} The nested data set for the Model's loaded associations
     */
    prepareAssociatedData: function(seenKeys, depth,persistedFields) {
        /**
         * In this method we use a breadth first strategy instead of depth
         * first. The reason for doing so is that it prevents messy & difficult
         * issues when figuring out which associations we've already processed
         * & at what depths.
         */
        var me = this,
            associations = me.associations.items,
            associationCount = associations.length,
            associationData = {},
// We keep 3 lists at the same index instead of using an array of objects.
// The reasoning behind this is that this method gets called a lot
// So we want to minimize the amount of objects we create for GC.
            toRead = [],
            toReadKey = [],
            toReadIndex = [],
            associatedStore, associatedRecords, associatedRecord, o, index, result, seenDepth,
            associationId, associatedRecordCount, association, i, j, type, name;

        for (i = 0; i < associationCount; i++) {
            association = associations[i];
            associationId = association.associationId;

            seenDepth = seenKeys[associationId];
            if (seenDepth && seenDepth !== depth) {
                continue;
            }
            seenKeys[associationId] = depth;

            type = association.type;
            name = association.name;
            if (type == 'hasMany') {
//this is the hasMany store filled with the associated data
                associatedStore = me[association.storeName];

//we will use this to contain each associated record's data
                associationData[name] = [];

//if it's loaded, put it into the association data
                if (associatedStore && associatedStore.getCount() > 0) {
                    associatedRecords = associatedStore.data.items;
                    associatedRecordCount = associatedRecords.length;

//now we're finally iterating over the records in the association. Get
// all the records so we can process them
                    for (j = 0; j < associatedRecordCount; j++) {
                        associatedRecord = associatedRecords[j];
                        associationData[name][j] = associatedRecord.getWriteData(false,persistedFields);
                        toRead.push(associatedRecord);
                        toReadKey.push(name);
                        toReadIndex.push(j);
                    }
                }
            } else if (type == 'belongsTo' || type == 'hasOne') {
                associatedRecord = me[association.instanceName];
// If we have a record, put it onto our list
                if (associatedRecord !== undefined) {
                    associationData[name] = associatedRecord.getWriteData(false,persistedFields);
                    toRead.push(associatedRecord);
                    toReadKey.push(name);
                    toReadIndex.push(-1);
                }
            }
        }

        for (i = 0, associatedRecordCount = toRead.length; i < associatedRecordCount; ++i) {
            associatedRecord = toRead[i];
            o = associationData[toReadKey[i]];
            index = toReadIndex[i];
            result = associatedRecord.prepareAssociatedData(seenKeys, depth + 1, persistedFields);
            if (index === -1) {
                Ext.apply(o, result);
            } else {
                Ext.apply(o[index], result);
            }
        }

        return associationData;
    }
});

/**
 * @class Uni.override.RestOverride
 *
 * Formats reserved HTML tokens in the url builder.
 */
Ext.define('Uni.override.RestOverride', {
    override: 'Ext.data.proxy.Rest',

    buildUrl: function (request) {
        var me = this,
            operation = request.operation,
            records = operation.records || [],
            record = records[0],
            id = record ? record.getId() : operation.id;

        // Encodes HTML characters such as '/' and '@'.
        if (typeof id !== 'undefined') {
            id = encodeURIComponent(id);

            if (record) {
                record.setId(id);
            } else {
                operation.id = id;
            }
        }

        var url = me.callParent(arguments);

        var urlTemplate = new Ext.Template(url),
            params = request.proxy.extraParams,
            newUrl = urlTemplate.apply(params);


        //Remove variables embedded into URL
        Ext.Object.each(params, function (key, value) {
            var regex = new RegExp('{' + key + '.*?}');
       /*     if (regex.test(url)) {
                delete params[key];
            }*/
        });

        request.url = url;

        return newUrl;
    }
});

/**
 * @class Uni.override.GridPanelOverride
 * override allows you so setup hydrator to the form.
 * You can pass hydrator class to the configyration:
 * ...
 * hydrator: 'App.example.Hydrator'
 * ...
 * or via setter:
 * form.setHydrator(hydrator);
 *
 * Once hydrator is set, data binding between form and bounded record goes through provided hydrator.
 */
Ext.define('Uni.override.FormOverride', {
    override: 'Ext.form.Basic',
    hydrator: null,

//
//    updateProperties: function () {
//        var view = this.getPropertyEdit();
//        var properties = this.propertiesStore;
//        if (properties != null) {
//            properties.each(function (property, id) {
//                    var propertyValue = Ext.create('Mdc.model.PropertyValue');
//                    var value;
//                    if (view.down('#' + property.data.key) != null) {
//                        var field = view.down('#' + property.data.key);
//                        value = field.getValue();
//
//                    if (property.getPropertyType().data.simplePropertyType === 'NULLABLE_BOOLEAN') {
//                        value = view.down('#' + property.data.key).getValue().rb;
//                    }
//                    if (property.getPropertyType().data.simplePropertyType === 'DATE') {
//                        value = view.down('#date' + property.data.key).getValue();
//                        if (value !== null && value !== '') {
//                            var newDate = new Date(value.getFullYear(), value.getMonth(), value.getDate(),
//                                0, 0, 0, 0);
//                            value = value.getTime();
//                        }
//                    }
//                    if (property.getPropertyType().data.simplePropertyType === 'TIMEOFDAY') {
//                        value = view.down('#time' + property.data.key).getValue();
//                        if (value !== null && value !== '') {
//                            var newDate = new Date(1970, 0, 1, value.getHours(), value.getMinutes(), value.getSeconds(), 0);
//                            value = newDate.getTime() / 1000;
//                        }
//                    }
//                    if (property.getPropertyType().data.simplePropertyType === 'CLOCK') {
//                        var timeValue = view.down('#time' + property.data.key).getValue();
//                        var dateValue = view.down('#date' + property.data.key).getValue();
//                        if (timeValue !== null && timeValue !== '' && dateValue !== null && dateValue !== '') {
//                            var newDate = new Date(dateValue.getFullYear(), dateValue.getMonth(), dateValue.getDate(),
//                                timeValue.getHours(), timeValue.getMinutes(), timeValue.getSeconds(), 0);
//                            value = newDate.getTime();
//                        }
//                    }
//
//                    if (property.data.isInheritedOrDefaultValue === true) {
//                        property.setPropertyValue(null);
//                    } else {
//                        propertyValue.set('value', value);
//                        property.setPropertyValue(propertyValue);
//                    }
//                }
//            );
//        }
//        return properties;
//    },

    constructor: function(owner) {
        this.callParent(arguments);
        if (owner.hydrator) {
            this.setHydrator(Ext.create(owner.hydrator))
        }
    },

    setHydrator: function(hydrator) {
        this.hydrator = hydrator
    },

    loadRecord: function(record) {
        if (!this.hydrator) {
            this.callParent(arguments)
        } else {
            this._record = record;
            return this.setValues(this.hydrator.extract(record));
        }
    },

    updateRecord: function(record) {
        record = record || this._record;

        if (this.hydrator) {
            var data = this.getFieldValues();

            record.beginEdit();
            this.hydrator.hydrate(data, record);
            record.endEdit();
            return this;
        } else {
            return this.callParent(arguments);
        }
    }
});

/**
 * @class Uni.I18n
 *
 * Internationalization (I18N) class that can be used to retrieve translations from the translations
 * REST interface. It uses the {@link Ldr.store.Translations} store to retrieve all the available
 * translations for certain components when loading an application.
 *
 * # How to initialize the component translations
 *
 * You need to initialize what translation components should be loaded before you start up the
 * application. Otherwise your translations will not be available. This can be done before calling
 * {@link Uni.Loader#onReady} with the {@link Uni.Loader#initI18n} function. Be sure to include
 * an array of component aliases you want to have available in your application.
 *
 *      @example
 *      Ext.require('Uni.Loader');
 *      Ext.onReady(function () {
 *          var loader = Ext.create('Uni.Loader');
 *          loader.initI18n(['MTR', 'USR', 'PRT']); // Component UNI automatically included.
 *
 *          loader.onReady(function () {
 *              // Start up the application.
 *          });
 *      });
 *
 * Note that the UnifyingJS (alias **UNI**) component translations are always loaded in as well.
 * This is to make sure that the components render correctly for all languages.
 *
 * # General notation
 *
 * In order to use the internationalization object you need to call the  fully qualified name of
 * {@link Uni.I18n}, as shown below.
 *
 *     @example
 *     Ext.create('Ext.panel.Panel' {
 *         title: Uni.I18n.translate('my.key', 'CMP', 'Fallback')
 *     });
 *
 * Be mindful that you will need to add {@link Uni.I18n} as a requirement every time, which can be
 * easily forgotten for something as frequently used as internationalization.
 *
 * # Translating simple string values
 *
 * For simple translations you can directly ask the {@link #translate} function to return the translation
 * for a component. Optionally, yet recommended, is to add a fallback translation in case no translation
 * is found.
 *
 *     @example
 *     Ext.create('Ext.panel.Panel' {
 *         title: Uni.I18n.translate('my.key', 'CMP', 'Fallback')
 *     });
 *
 * More information and examples can be found at the {@link #translate} function.
 *
 * # Translating plural string values
 *
 * There is built-in support for having simple plural versions of a string translation. Summarized, this
 * means that you can have compounded keys that mention which translation should be used for what
 * specific amount.
 *
 * A use case would be having a separate translation for 'no items', '1 item' and '2 items'.
 *
 * More information and examples can be found at the {@link #translatePlural} function.
 *
 * # Formatting dates
 *
 * When you want to format dates a similar template applies as with string formatting. You need a key,
 * a value to format, a component for which it applies, and a fallback format. The date format needs
 * to conform to the Moment.js library.
 *
 * A full list of supported formats can be found at the {Ext.Date} documentation.
 *
 *     @example
 *     var formattedNow = Uni.I18n.formatDate('long.date.format', new Date(), 'CMP', ''F j Y g:i A'');
 *     console.log(formattedNow); // January 28 2014 11:14 AM
 *
 * More information and examples can be found at the {@link #formatDate} function.
 *
 * # Formatting numbers
 *
 * To format numbers in a simple way, there is the {@link #formatNumber} function which requires
 * only a few parameters to work. First of all, a number to format, and secondly, the component for
 * which to format the number. There's also an optional parameter for the number of decimals that
 * should be used.
 *
 *     @example
 *     var formattedNumber = Uni.I18n.formatNumber(130000.037, 'CMP');
 *     console.log(formattedNumber); // 130,000.04
 *
 * More information and examples can be found at the {@link #formatNumber} function.
 *
 * # Formatting currency
 *
 * Currency formatting relates to number formatting, in a way that the number representing is
 * formatted first by the {@link #formatNumber} function. That formatted number is then used to
 * create a complete formatted currency string.
 *
 *     @example
 *     var formattedCurrency = Uni.I18n.formatCurrency(130000.037, 'CMP');
 *     console.log(formattedCurrency); // €130,000.04
 *
 * More information and examples can be found at the {@link #formatCurrency} function.
 *
 */
Ext.define('Uni.I18n', {
    singleton: true,

    requires: [
        'Ldr.store.Translations'
    ],

    /**
     * Default currency format key to perform translation look-ups with.
     *
     * @property {String} [currencyFormatKey='currencyFormat']
     */
    currencyFormatKey: 'currencyFormat',
    /**
     * Default decimal separator format key to perform translation look-ups with.
     *
     * @property {String} [decimalSeparatorKey='decimalSeparator']
     */
    decimalSeparatorKey: 'decimalSeparator',
    /**
     * Default thousands separator format key to perform translation look-ups with.
     *
     * @property {String} [thousandsSeparatorKey='thousandsSeparator']
     */
    thousandsSeparatorKey: 'thousandsSeparator',

    // Used to only show missing translation messages once.
    blacklist: [],

    /**
     * Initializes the internationalization components that should be used during loading.
     *
     * @param {String} components Components to load
     */
    init: function (components) {
        // TODO Refactor.
        Ldr.store.Translations.setComponents(components);
    },

    /**
     * Looks up the translation for a certain key. If there is a missing translation, the key
     * will be returned surrounded by square brackets like [this]. In debug there will also
     * be an extra warning that is logged in the debug console.
     *
     * @param {String} key Key to look up the translation for
     * @param {String} component Component to filter on
     * @returns {String} Translation
     */
    lookupTranslation: function (key, component) {
        var translation,
            index;

        if (typeof component !== 'undefined' && component) {
            index = Ldr.store.Translations.findBy(function (record) {
                return record.data.key === key && record.data.cmp === component;
            });
            translation = Ldr.store.Translations.getAt(index);
        } else {
            translation = Ldr.store.Translations.getById(key);
        }

        if (typeof translation !== 'undefined' && translation !== null) {
            translation = translation.data.value;
        } else {
            if (!this.blacklist[key + component]) {
                this.blacklist[key + component] = true;
                var warning = 'Missing translation for key \'' + key + '\'';
                if (component) {
                    warning += ' in component \'' + component + '\'.';
                } else {
                    warning += '.';
                }
                console.warn(warning);
            }
        }

        return translation;
    },

    /**
     * Uses a regular expression to find and replace all instances of a translation parameter.
     *
     * @param {String} translation Translation to find and replace the index parameters
     * @param {Number} searchIndex Index value to replace with the value
     * @param {String} replaceValue Value to replace search results with
     * @returns {String} Replaced translation
     */
    replaceAll: function (translation, searchIndex, replaceValue) {
        var lookup = '\{[' + searchIndex + ']\}';
        return translation.replace(new RegExp(lookup, 'g'), replaceValue);
    },

    /**
     * Returns the text translation of the key, looking for the key in a certain .
     *
     * @param {String} key Translation key to look up
     * @param {String} component Component on which to filter
     * @param {String} fallback Fallback value in case the translation was not found
     * @param {String[]} [values] Values to replace in the translation
     * @returns {String} Translation
     */
    translate: function (key, component, fallback, values) {
        var translation = this.lookupTranslation(key, component);

        if ((typeof translation === 'undefined' || translation === null)
            && typeof fallback === 'undefined' && fallback === null) {
            translation = key;
        }

        if ((typeof translation === 'undefined' || translation === null)
            && typeof fallback !== 'undefined' && fallback !== null) {
            translation = fallback;
        }

        if (typeof translation !== 'undefined' && translation !== null
            && typeof values !== 'undefined') {
            for (var i = 0; i < values.length; i++) {
                translation = this.replaceAll(translation, i, values[i]);
            }
        }

        return translation;
    },

    /**
     * Looks up the plural translation of a number, e.g. for 0 items the translation could be
     * 'There no items', for 1 item 'There is 1 item', or for 7 items 'There are 7 items'.
     * If your key is named 'itemCount' then for the amount 0 will look up 'itemCount[0]',
     * for the amount 1 'itemCount[1]', and so on. It falls back on the generic 'itemCount' key.
     *
     * @param {String} key Translation key to look up
     * @param {Number/String} amount Amount to translate with
     * @param {String} component Component to look up the translation for
     * @param {String} fallback Fallback value in case the translation was not found
     */
    translatePlural: function (key, amount, component, fallback) {
        var lookup = key + '[' + amount + ']',
            translation = this.lookupTranslation(lookup, component);

        if (typeof translation === 'undefined') {
            translation = this.lookupTranslation(key, component) || fallback;
        }

        if (typeof amount !== 'undefined') {
            translation = this.replaceAll(translation, 0, amount);
        }

        return translation;
    },

    /**
     * Formats a date based on a translation key. If no date has been given, the current date is used.
     *
     * The used parse syntax is that of ExtJS which can be found at the {Ext.Date} documentation.
     *
     * @param {String} key Translation key to format the date with
     * @param {Date} [date=new Date()] Date to format
     * @param {String} [component] Component to look up the format for
     * @param {String} [fallback] Fallback format
     * @returns {String} Formatted date as a string value
     */
    formatDate: function (key, date, component, fallback) {
        date = date || new Date();

        var format = this.translate(key, component, fallback);

        return Ext.Date.format(date, format);
    },

    /**
     * Formats a number based on parameters for the number of trailing decimal places, what decimal
     * separator should be used, and what the thousands separator is. If the number of trailing
     * decimals is not specified, 2 decimals are used. By default the decimal separator is '.' and
     * the thousands separator is ','.
     *
     * Adapted from: [http://stackoverflow.com/a/149099/682311](http://stackoverflow.com/a/149099/682311)
     *
     * @param {Number} number Number to format
     * @param {Number} [decimals=2] Number of required decimal places
     * @param {String} [decimalSeparator=.] Required decimal separator
     * @param {String} [thousandsSeparator=,] Required thousand separator
     * @returns {String} Formatted number
     */
    formatNumberWithSeparators: function (number, decimals, decimalSeparator, thousandsSeparator) {
        var n = parseFloat(number),
            c = isNaN(decimals) ? 2 : Math.abs(decimals),
            d = decimalSeparator || '.',
            t = (typeof thousandsSeparator === 'undefined') ? ',' : thousandsSeparator,
            sign = (n < 0) ? '-' : '',
            i = parseInt(n = Math.abs(n).toFixed(c)) + '',
            j = ((j = i.length) > 3) ? j % 3 : 0;

        return sign + (j ? i.substr(0, j) + t : '') + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d + Math.abs(n - i).toFixed(c).slice(2) : '');
    },

    /**
     * Internationalizes a number based on the number of trailing decimals, decimal separator, and thousands
     * separator for the currently active locale. If the number of trailing decimals is not specified,
     * 2 decimals are used. The translation lookup key for the decimal separator is 'decimalSeparator,
     * while the thousands separator has the lookup key 'thousandsSeparator'.
     *
     * @param {Number} number Number to internationalize
     * @param {String} [component] Component to look up the format for
     * @param {Number} [decimals] Number of required decimal places
     * @returns {String} Internationalized number
     */
    formatNumber: function (number, component, decimals) {
        var decimalSeparator = this.translate(this.decimalSeparatorKey, component, '.'),
            thousandsSeparator = this.translate(this.thousandsSeparatorKey, component, ',');

        return this.formatNumberWithSeparators(number, decimals, decimalSeparator, thousandsSeparator);
    },

    /**
     * Internationalizes a value into its correct currency format based on the active locale. If the number of
     * trailing decimals is not specified, 2 decimals are used. The lookup key for the currency format
     * is 'currencyFormat'. If the currency format is not found, the formatted numeric value is used.
     *
     * @param {Number} value Currency value to internationalize
     * @param {String} [component] Component to look up the format for
     * @param {Number} [decimals] Number of required decimal places
     * @returns {String} Internationalized currency value
     */
    formatCurrency: function (value, component, decimals) {
        var formattedValue = this.formatNumber(value, component, decimals);

        return this.translate(this.currencyFormatKey, component, formattedValue, [formattedValue]);
    }

});

/**
 * @class Uni.override.MessageBoxOverride
 */
Ext.define('Uni.override.MessageBoxOverride', {
    override: 'Ext.window.MessageBox',

    buttonText: {
        ok: Uni.I18n.translate('window.messabox.ok', 'UNI', 'OK'),
        yes: Uni.I18n.translate('window.messabox.yes', 'UNI', 'Yes'),
        no: Uni.I18n.translate('window.messabox.no', 'UNI', 'No'),
        cancel: Uni.I18n.translate('window.messabox.cancel', 'UNI', 'Cancel')
    }

});

Ext.define('Uni.override.window.MessageBox', {
    override: 'Ext.window.MessageBox',
    shadow: false,

    reconfigure: function (cfg) {
        if (((typeof cfg) != 'undefined') && cfg.ui) {
            this.ui = cfg.ui;
        }
        this.callParent(arguments);
    },

    initComponent: function () {
        var me = this,
            title = me.title;

        me.title = null;
        this.callParent(arguments);
        this.topContainer.padding = 0;

        me.titleComponent = new Ext.panel.Header({
            title: title
        });
        me.promptContainer.insert(0, me.titleComponent);
    },

    /**
     * Set a title for the panel's header. See {@link Ext.panel.Header#title}.
     * @param {String} newTitle
     */
    setTitle: function (newTitle) {
        var me = this,
            header = me.titleComponent;

        if (header) {
            var oldTitle = header.title;
        }

        if (header) {
            if (header.isHeader) {
                header.setTitle(newTitle);
            } else {
                header.title = newTitle;
            }
        }
        else if (me.rendered) {
            me.updateHeader();
        }

        me.fireEvent('titlechange', me, newTitle, oldTitle);
    }
}, function () {
    /**
     * @class Ext.MessageBox
     * @alternateClassName Ext.Msg
     * @extends Ext.window.MessageBox
     * @singleton
     * Singleton instance of {@link Ext.window.MessageBox}.
     */
    Ext.MessageBox = Ext.Msg = new this();
});

Ext.define('Uni.override.form.field.Text', {
    override: "Ext.form.field.Text",
    labelAlign: 'right',
    labelPad: 15,
    msgTarget: 'under',
    blankText: 'This is a required field'
});

/**
 * @class Uni.override.FieldBaseOverride
 */
Ext.define('Uni.override.FieldBaseOverride', {
    override: 'Ext.form.field.Base',

    /**
     * Changes the default value ':'.
     */
    labelSeparator: '',

    /**
     * Changes the default value 'qtip'.
     */
    msgTarget: 'under'

});

Ext.define('Uni.override.form.field.Base', {
    override: "Ext.form.field.Base",
    labelAlign: 'right',
    labelPad: 15,
    msgTarget: 'under',
    blankText: 'This is a required field',
    validateOnChange: false,
    validateOnBlur: false,

    getLabelCls: function () {
        var labelCls = this.labelCls;
        if (this.required) {
            labelCls += ' ' + 'uni-form-item-label-required';
        }

        return labelCls;
    },

    initComponent: function() {
        this.callParent(arguments);
    }
});



/**
 * @class Uni.override.CheckboxOverride
 */
Ext.define('Uni.override.CheckboxOverride', {
    override: 'Ext.form.field.Checkbox',

    /**
     * Changes the default value from 'on' to 'true'.
     */
    inputValue: true

});

/**
 * @class Uni.override.FieldContainerOverride
 */
Ext.define('Uni.override.FieldContainerOverride', {
    override: 'Ext.form.FieldContainer',

    /**
     * Changes the default value ':'.
     */
    labelSeparator: '',

    /**
     * Changes the default value 'qtip'.
     */
    msgTarget: 'side',

    /**
     * Changes the default label alignment.
     */
    labelAlign: 'right',

    initComponent: function () {
        this.callParent();
        this.form = new Ext.form.Basic(this);
    },

    getValues: function () {
        return this.form.getValues();
    },

    setValues: function (data) {
        this.form.setValues(data);
    }
});

Ext.define('Uni.override.form.field.FieldContainer', {
    override: "Ext.form.FieldContainer",
    labelPad: 15,
    getLabelCls: function () {
        var labelCls = this.labelCls;
        if (this.required) {
            labelCls += ' ' + 'uni-form-item-label-required';
        }
        return labelCls;
    },
    initComponent: function() {
        this.callParent(arguments);
    }
});

/**
 * @class Uni.override.FieldSetOverride
 */
Ext.define('Uni.override.FieldSetOverride', {
    override: 'Ext.form.FieldSet',

    initComponent: function () {
        this.callParent();
        this.form = new Ext.form.Basic(this);
        this.form.monitor.selector = "[isFormField]:not([excludeForm]){ownerCt.getId() === '" + this.getId() + "'}";
    },

    getValues: function () {
        var values = this.form.getValues();
        this.items.each(function (item) {
            if (_.isFunction(item.getValues)) {
                _.isEmpty(item.name) ? Ext.merge(values, item.getValues()) : values[item.name] = item.getValues();
            }
        });
        return values;
    },

    setValues: function (data) {
        this.form.setValues(data);
        this.items.each(function (item) {
            if (_.isFunction(item.setValues)) {
                _.isEmpty(item.name) ? item.setValues(data) : item.setValues(data[item.name]);
            }
        });
    }
});

Ext.define('Uni.override.form.Label', {
    override: 'Ext.form.Label',
    cls: 'x-form-item-label'
});

Ext.define('Uni.override.form.Panel', {
    override: 'Ext.form.Panel',
    buttonAlign: 'left',

    initComponent: function() {
        var me = this;
        var width = 100;

        if (me.defaults && me.defaults.labelWidth) {
            width = me.defaults.labelWidth;
        }
        // the case when label align is defined and not left. Than don't move the buttons.
        if (me.defaults
         && me.defaults.labelAlign
         && me.defaults.labelAlign != 'left') {
            width = 0;
        }
        if (me.buttons) {
            me.buttons.splice(0, 0, {
                xtype: 'tbspacer',
                width: width,
                cls: 'x-form-item-label-right'
            })
        }

        me.callParent(arguments);
    }
});

Ext.define('Uni.override.form.field.ComboBox', {
    override: 'Ext.form.field.ComboBox',

    anyMatch: true,

    initComponent: function() {
        this.callParent(arguments);
    }
});



/**
 * @class Uni.override.ApplicationOverride
 */
Ext.define('Uni.override.form.field.ComboBox', {
    override: 'Ext.form.field.ComboBox',

    anyMatch: true,

    listeners: {
        // force re-validate on combo change
        change: function (combo) {
            combo.validate();
        }
    },

    initComponent: function () {
        this.callParent(arguments);
    }
});

/**
 * @class Uni.override.NumberFieldOverride
 */
Ext.define('Uni.override.NumberFieldOverride', {
    override: 'Ext.form.field.Number',

    /**
     * Changes the default alignment of numberfields.
     */
    fieldStyle: 'text-align:right;',

    minText : "The minimum value is {0}",

    maxText : "The maximum value is {0}"



});

Ext.define('Uni.override.form.field.Date', {
    override: 'Ext.form.field.Date',

    format: 'd/m/Y',

    initComponent: function () {
        this.callParent(arguments);
    }
});



/**
 * @class Uni.grid.plugin.ShowConditionalToolTip
 */
Ext.define('Uni.grid.plugin.ShowConditionalToolTip', {
    extend: 'Ext.AbstractPlugin',
    requires: [
        'Ext.util.Format',
        'Ext.tip.ToolTip'
    ],
    alias: 'plugin.showConditionalToolTip',

    /**
     * @private
     */
    init: function (grid) {
        var gridView = grid.getView();

        gridView.on('refresh', this.setTooltip);
        gridView.on('resize', this.setTooltip);
        gridView.on('beforerefresh', this.destroyTooltips);
        gridView.on('beforedestroy', this.destroyTooltips, this, {single: true});
        grid.on('beforedestroy', this.destroyHeaderTooltips, this, {single: true});
    },

    /**
     * @private
     */
    setTooltip: function (grid) {
        var gridPanel = grid.up('gridpanel');
        Ext.Array.each(gridPanel.columns, function (column) {
            var header = Ext.get(gridPanel.getEl().query('#' + column.id + '-titleEl')[0]);

            header.tooltip && header.tooltip.destroy();

            if (column.text && (header.getWidth(true) < header.getTextWidth())) {
                header.tooltip = Ext.create('Ext.tip.ToolTip', {
                    target: header,
                    html: column.text
                });
            }

            if (column.$className === 'Ext.grid.column.Column' || column.$className === 'Ext.grid.column.Date') {
                Ext.Array.each(grid.getEl().query('.x-grid-cell-headerId-' + column.id), function (item) {
                    var cell = Ext.get(item),
                        inner = cell.down('.x-grid-cell-inner'),
                        text = inner ? Ext.util.Format.stripTags(inner.getHTML()) : false;

                    cell.tooltip && cell.tooltip.destroy();

                    if (text && (cell.getWidth(true) < cell.getTextWidth())) {
                        cell.tooltip = Ext.create('Ext.tip.ToolTip', {
                            target: cell,
                            html: text
                        });
                    }
                });
            }
        });
    },

    /**
     * @private
     */
    destroyHeaderTooltips: function(grid) {
        Ext.Array.each(grid.columns, function (column) {
            var header = Ext.get(grid.getEl().query('#' + column.id + '-titleEl')[0]);
            header.tooltip && header.tooltip.destroy();
        });
    },

    /**
     * @private
     */
    destroyTooltips: function (grid) {
        Ext.Array.each(grid.getEl().query('.x-grid-cell'), function (item) {
            var cell = Ext.get(item);

            cell.tooltip && cell.tooltip.destroy();
        });
    }
});

/**
 * @class Uni.override.GridPanelOverride
 */
Ext.define('Uni.override.GridPanelOverride', {
    override: 'Ext.grid.Panel',
    requires: [
        'Uni.grid.plugin.ShowConditionalToolTip'
    ],
    plugins: [
        'showConditionalToolTip'
    ],

    /**
     * Do not select row when column is of type actioncolumn.
     */
    listeners: {
        cellclick: function (gridView, htmlElement, columnIndex, dataRecord) {
            var type = gridView.getHeaderCt().getHeaderAtIndex(columnIndex).getXType();
            if (type === 'actioncolumn') {
                return true;
            }
        }
    }
});

Ext.define('Uni.override.grid.Panel', {
    override: 'Ext.grid.Panel',
    bodyBorder: true,
    enableColumnHide: false,
    enableColumnMove: false,
    enableColumnResize: false,
    sortableColumns: false,
    collapsible: false,
    overflowY: 'auto',
    selModel: {
        mode: 'SINGLE'
    }
});

Ext.define('Uni.override.view.Table', {
    override: 'Ext.view.Table',
    bodyBorder: true
//    ,
//    scroll: true,
//    scrollbarTpl: '<div class="scrollbar"><div class="up"><span class="arrow-up"></span></div><div class="down"><span class="arrow-down"></span></div><div class="track"><div class="thumb"></div></div></div>',
//
//    // todo: refactor this
//    listeners: {
////        refresh: function () {
////            var body = this.getEl().parent('.x-grid-body');
////            body.update(body.getHTML() + this.scrollbarTpl);
////            if (body.down('.x-grid-view')) {
////                body.down('.x-grid-view').addCls('viewport');
////
////                this.getEl().down('.x-grid-table').addCls('overview');
////
////                var $scrollbar = body.dom,
////                    scrollbar  = tinyscrollbar($scrollbar, {
////                        trackSize: body.down('.track').dom.offsetHeight
////                    });
////            }
////
////        }
//    }
});

Ext.define('Uni.override.grid.plugin.BufferedRenderer', {
    override: 'Ext.grid.plugin.BufferedRenderer',
    rowHeight: 29, // comes from skyline theme

    init: function(grid) {
        this.callParent(arguments);

        // grid height calculated before the toolbar is on layouts, it causes the bug: JP-3817
        grid.on('boxready', function() {
            grid.view.refresh();
        })
    },

    bindStore: function(store) {
        var me = this;
        me.trailingBufferZone = 0;
        me.leadingBufferZone = store.pageSize;
        this.callParent(arguments);
    }
});

Ext.define('Uni.override.menu.Item', {
    override: 'Ext.menu.Item',

    setHref: function (href, target) {
        this.href = !Ext.isDefined(href) ? '#' : href;
        this.hrefTarget = !Ext.isDefined(target) ? '_self' : target || this.hrefTarget;

        if (Ext.isDefined(this.itemEl)) {
            this.itemEl.set({
                href: this.href,
                hrefTarget: this.hrefTarget
            });
        }
    }
});

/**
 * @class Uni.About
 */
Ext.define('Uni.About', {
    singleton: true,

    version: '1.0.0',
    startup: new Date(),
    baseCssPrefix: 'uni-'
});

/**
 * @class Uni.Auth
 *
 * Authorization class that checks whether the currently logged-in user has privileges or not.
 */
Ext.define('Uni.Auth', {
    singleton: true,

    requires: [
        'Ldr.store.Privileges'
    ],

    hasPrivilege: function (privilege) {
        for (var i = 0; i < Ldr.store.Privileges.getCount(); i++) {
            if (privilege === Ldr.store.Privileges.getAt(i).get('name')) {
                return true;
            }
        }
        return false;
    },

    hasNoPrivilege: function (privilege) {
        return !this.hasPrivilege(privilege);
    },

    hasAnyPrivilege: function (privileges) {
        if (Ext.isArray(privileges)) {
            for (var i = 0; i < privileges.length; i++) {
                var privilege = privileges[i];
                if (this.hasPrivilege(privilege)) {
                    return true;
                }
            }
        }
        return false;
    }
});

/**
 * @class Uni.view.window.Acknowledgement
 */
Ext.define('Uni.view.window.Acknowledgement', {
    extend: 'Ext.window.Window',
    xtype: 'acknowledgement-window',

    autoShow: true,
    resizable: false,
    bodyBorder: false,
    shadow: false,
    animCollapse: true,
    border: false,
    header: false,
    cls: Uni.About.baseCssPrefix + 'window-acknowledgement',

    layout: {
        type: 'hbox',
        align: 'center'
    },

    setMessage: function (message) {
        var msgPanel = this.down('#msgmessage');

        msgPanel.removeAll();
        msgPanel.add({
            xtype: 'label',
            html: message
        });
    },

    initComponent: function () {
        var me = this;

        me.items = [
            // Icon.
            {
                xtype: 'component',
                cls: 'icon'
            },
            // Message.
            {
                xtype: 'panel',
                itemId: 'msgmessage',
                cls: 'message',
                layout: {
                    type: 'vbox',
                    align: 'left'
                }
            },
            {
                xtype: 'component',
                html: '&nbsp;',
                flex: 1
            },
            // Close button.
            {
                xtype: 'button',
                iconCls: 'close',
                ui: 'close',
                width: 28,
                height: 28,
                handler: function () {
                    me.close();
                }
            }
        ];

        me.callParent(arguments);
    }
});

/**
 * @class Uni.controller.Acknowledgements
 *
 * Acknowledgements controller that is responsible for displaying acknowledgements
 * and removing them from the screen when required.
 */
Ext.define('Uni.controller.Acknowledgements', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.view.window.Acknowledgement'
    ],

    init: function () {
        this.getApplication().on('acknowledge', this.showAcknowledgement);
    },

    /**
     *
     * @param {String} message Message to show as acknowledgement.
     */
    showAcknowledgement: function (message) {
        var msgWindow = Ext.widget('acknowledgement-window'),
            task = new Ext.util.DelayedTask(function () {
                msgWindow.close();
            });

        msgWindow.setMessage(message);
        msgWindow.center();
        msgWindow.setPosition(msgWindow.x, 116, false);

        task.delay(5000);
    }
});

/**
 * @class Uni.controller.Configuration
 */
Ext.define('Uni.controller.Configuration', {
    extend: 'Ext.app.Controller',

    refs: [
        {
            ref: 'logo',
            selector: 'navigationLogo'
        },
        {
            ref: 'appSwitcher',
            selector: 'navigationAppSwitcher'
        }
    ],

    init: function () {
        this.getApplication().on('changeapptitleevent', this.changeAppTitle, this);
        this.getApplication().on('changeappglyphevent', this.changeAppGlyph, this);
    },

    changeAppTitle: function (title) {
        var logo = this.getLogo();
        logo.setLogoTitle(title);
    },

    changeAppGlyph: function (glyph) {
        var logo = this.getLogo();
        logo.setLogoGlyph(glyph);
    }

});

/**
 * @class Uni.view.error.Window
 */
Ext.define('Uni.view.error.Window', {
    extend: 'Ext.window.Window',
    alias: 'widget.errorWindow',

    requires: [
    ],

    width: 600,
    height: 350,

    layout: 'fit',

    modal: true,
    constrain: true,
    closeAction: 'hide',

    title: 'Error message',

    items: [
        {
            xtype: 'textareafield',
            itemId: 'messagefield',
            margin: 10
        }
    ],

    initComponent: function () {
        this.buttons = [
            {
                text: 'Report issue',
                action: 'report',
                disabled: true
            },
            {
                text: 'Close',
                scope: this,
                handler: this.close
            }
        ];

        this.callParent(arguments);
    },

    setErrorMessage: function (message) {
        var errorMessageField = this.down('#messagefield');
        errorMessageField.setValue(message);
    }

});

/* 
 *	Notification extension for Ext JS 4.0.2+
 *	Version: 2.1.3
 *
 *	Copyright (c) 2011 Eirik Lorentsen (http://www.eirik.net/)
 *
 *	Follow project on GitHub: https://github.com/EirikLorentsen/Ext.ux.window.Notification
 *
 *	Dual licensed under the MIT (http://www.opensource.org/licenses/mit-license.php) 
 *	and GPL (http://opensource.org/licenses/GPL-3.0) licenses.
 *
 */

Ext.define('Ext.ux.window.Notification', {
	extend: 'Ext.window.Window',
	alias: 'widget.uxNotification',

	cls: 'ux-notification-window',
	autoClose: true,
	autoHeight: true,
	plain: false,
	draggable: false,
	shadow: false,
	focus: Ext.emptyFn,

	// For alignment and to store array of rendered notifications. Defaults to document if not set.
	manager: null,

	useXAxis: false,

	// Options: br, bl, tr, tl, t, l, b, r
	position: 'br',

	// Pixels between each notification
	spacing: 6,

	// Pixels from the managers borders to start the first notification
	paddingX: 30,
	paddingY: 10,

	slideInAnimation: 'easeIn',
	slideBackAnimation: 'bounceOut',
	slideInDuration: 1500,
	slideBackDuration: 1000,
	hideDuration: 500,
	autoCloseDelay: 7000,
	stickOnClick: true,
	stickWhileHover: true,

	// Private. Do not override!
	isHiding: false,
	isFading: false,
	destroyAfterHide: false,
	closeOnMouseOut: false,

	// Caching coordinates to be able to align to final position of siblings being animated
	xPos: 0,
	yPos: 0,

	statics: {
		defaultManager: {
			el: null
		}
	},

	initComponent: function() {
		var me = this;

		// Backwards compatibility
		if (Ext.isDefined(me.corner)) {
			me.position = me.corner;
		}
		if (Ext.isDefined(me.slideDownAnimation)) {
			me.slideBackAnimation = me.slideDownAnimation;
		}
		if (Ext.isDefined(me.autoDestroyDelay)) {
			me.autoCloseDelay = me.autoDestroyDelay;
		}
		if (Ext.isDefined(me.autoHideDelay)) {
			me.autoCloseDelay = me.autoHideDelay;
		}
		if (Ext.isDefined(me.autoHide)) {
			me.autoClose = me.autoHide;
		}
		if (Ext.isDefined(me.slideInDelay)) {
			me.slideInDuration = me.slideInDelay;
		}
		if (Ext.isDefined(me.slideDownDelay)) {
			me.slideBackDuration = me.slideDownDelay;
		}
		if (Ext.isDefined(me.fadeDelay)) {
			me.hideDuration = me.fadeDelay;
		}

		// 'bc', lc', 'rc', 'tc' compatibility
		me.position = me.position.replace(/c/, '');

		me.updateAlignment(me.position);

		me.setManager(me.manager);

		me.callParent(arguments);
	},

	onRender: function() {
		var me = this;
		me.callParent(arguments);

		me.el.hover(
			function () {
				me.mouseIsOver = true;
			},
			function () {
				me.mouseIsOver = false;
				if (me.closeOnMouseOut) {
					me.closeOnMouseOut = false;
					me.close();
				}
			},
			me
		);

	},
	
	updateAlignment: function (position) {
		var me = this;

		switch (position) {
			case 'br':
				me.paddingFactorX = -1;
				me.paddingFactorY = -1;
				me.siblingAlignment = "br-br";
				if (me.useXAxis) {
					me.managerAlignment = "bl-br";
				} else {
					me.managerAlignment = "tr-br";
				}
				break;
			case 'bl':
				me.paddingFactorX = 1;
				me.paddingFactorY = -1;
				me.siblingAlignment = "bl-bl";
				if (me.useXAxis) {
					me.managerAlignment = "br-bl";
				} else {
					me.managerAlignment = "tl-bl";
				}
				break;
			case 'tr':
				me.paddingFactorX = -1;
				me.paddingFactorY = 1;
				me.siblingAlignment = "tr-tr";
				if (me.useXAxis) {
					me.managerAlignment = "tl-tr";
				} else {
					me.managerAlignment = "br-tr";
				}
				break;
			case 'tl':
				me.paddingFactorX = 1;
				me.paddingFactorY = 1;
				me.siblingAlignment = "tl-tl";
				if (me.useXAxis) {
					me.managerAlignment = "tr-tl";
				} else {
					me.managerAlignment = "bl-tl";
				}
				break;
			case 'b':
				me.paddingFactorX = 0;
				me.paddingFactorY = -1;
				me.siblingAlignment = "b-b";
				me.useXAxis = 0;
				me.managerAlignment = "t-b";
				break;
			case 't':
				me.paddingFactorX = 0;
				me.paddingFactorY = 1;
				me.siblingAlignment = "t-t";
				me.useXAxis = 0;
				me.managerAlignment = "b-t";
				break;
			case 'l':
				me.paddingFactorX = 1;
				me.paddingFactorY = 0;
				me.siblingAlignment = "l-l";
				me.useXAxis = 1;
				me.managerAlignment = "r-l";
				break;
			case 'r':
				me.paddingFactorX = -1;
				me.paddingFactorY = 0;
				me.siblingAlignment = "r-r";
				me.useXAxis = 1;
				me.managerAlignment = "l-r";
				break;
			}
	},
	
	getXposAlignedToManager: function () {
		var me = this;

		var xPos = 0;

		// Avoid error messages if the manager does not have a dom element
		if (me.manager && me.manager.el && me.manager.el.dom) {
			if (!me.useXAxis) {
				// Element should already be aligned vertically
				return me.el.getLeft();
			} else {
				// Using getAnchorXY instead of getTop/getBottom should give a correct placement when document is used
				// as the manager but is still 0 px high. Before rendering the viewport.
				if (me.position == 'br' || me.position == 'tr' || me.position == 'r') {
					xPos += me.manager.el.getAnchorXY('r')[0];
					xPos -= (me.el.getWidth() + me.paddingX);
				} else {
					xPos += me.manager.el.getAnchorXY('l')[0];
					xPos += me.paddingX;
				}
			}
		}

		return xPos;
	},

	getYposAlignedToManager: function () {
		var me = this;

		var yPos = 0;

		// Avoid error messages if the manager does not have a dom element
		if (me.manager && me.manager.el && me.manager.el.dom) {
			if (me.useXAxis) {
				// Element should already be aligned horizontally
				return me.el.getTop();
			} else {
				// Using getAnchorXY instead of getTop/getBottom should give a correct placement when document is used
				// as the manager but is still 0 px high. Before rendering the viewport.
				if (me.position == 'br' || me.position == 'bl' || me.position == 'b') {
					yPos += me.manager.el.getAnchorXY('b')[1];
					yPos -= (me.el.getHeight() + me.paddingY);
				} else {
					yPos += me.manager.el.getAnchorXY('t')[1];
					yPos += me.paddingY;
				}
			}
		}

		return yPos;
	},

	getXposAlignedToSibling: function (sibling) {
		var me = this;

		if (me.useXAxis) {
			if (me.position == 'tl' || me.position == 'bl' || me.position == 'l') {
				// Using sibling's width when adding
				return (sibling.xPos + sibling.el.getWidth() + sibling.spacing);
			} else {
				// Using own width when subtracting
				return (sibling.xPos - me.el.getWidth() - me.spacing);
			}
		} else {
			return me.el.getLeft();
		}

	},

	getYposAlignedToSibling: function (sibling) {
		var me = this;

		if (me.useXAxis) {
			return me.el.getTop();
		} else {
			if (me.position == 'tr' || me.position == 'tl' || me.position == 't') {
				// Using sibling's width when adding
				return (sibling.yPos + sibling.el.getHeight() + sibling.spacing);				
			} else {
				// Using own width when subtracting
				return (sibling.yPos - me.el.getHeight() - sibling.spacing);
			}
		}
	},

	getNotifications: function (alignment) {
		var me = this;

		if (!me.manager.notifications[alignment]) {
			me.manager.notifications[alignment] = [];
		}

		return me.manager.notifications[alignment];
	},

	setManager: function (manager) {
		var me = this;

		me.manager = manager;

		if (typeof me.manager == 'string') {
//			me.manager = Ext.getCmp(me.manager);
			me.manager = Ext.ComponentQuery.query(me.manager)[0];
		}

		// If no manager is provided or found, then the static object is used and the el property pointed to the body document.
		if (!me.manager) {
			me.manager = me.statics().defaultManager;

			if (!me.manager.el) {
				me.manager.el = Ext.getBody();
			}
		}
		
		if (typeof me.manager.notifications == 'undefined') {
			me.manager.notifications = {};
		}
	},
	
	beforeShow: function () {
		var me = this;

		if (me.stickOnClick) {
			if (me.body && me.body.dom) {
				Ext.fly(me.body.dom).on('click', function () {
					me.cancelAutoClose();
					me.addCls('notification-fixed');
				}, me);
			}
		}

		if (me.autoClose) {
			me.task = new Ext.util.DelayedTask(me.doAutoClose, me);
			me.task.delay(me.autoCloseDelay);
		}

		// Shunting offscreen to avoid flicker
		me.el.setX(-10000);
		me.el.setOpacity(0.9);
		
	},

	afterShow: function () {
		var me = this;

		me.callParent(arguments);

		var notifications = me.getNotifications(me.managerAlignment);

		if (notifications.length) {
			me.el.alignTo(notifications[notifications.length - 1].el, me.siblingAlignment, [0, 0]);
			me.xPos = me.getXposAlignedToSibling(notifications[notifications.length - 1]);
			me.yPos = me.getYposAlignedToSibling(notifications[notifications.length - 1]);
		} else {
			me.el.alignTo(me.manager.el, me.managerAlignment, [(me.paddingX * me.paddingFactorX), (me.paddingY * me.paddingFactorY)], false);
			me.xPos = me.getXposAlignedToManager();
			me.yPos = me.getYposAlignedToManager();
		}

		Ext.Array.include(notifications, me);

		// Repeating from coordinates makes sure the windows does not flicker into the center of the viewport during animation
		me.el.animate({
			from: {
				x: me.el.getX(),
				y: me.el.getY()
			},
			to: {
				x: me.xPos,
				y: me.yPos,
				opacity: 0.9
			},
			easing: me.slideInAnimation,
			duration: me.slideInDuration,
			dynamic: true
		});

	},
	
	slideBack: function () {
		var me = this;

		var notifications = me.getNotifications(me.managerAlignment);
		var index = Ext.Array.indexOf(notifications, me)

		// Not animating the element if it already started to hide itself or if the manager is not present in the dom
		if (!me.isHiding && me.el && me.manager && me.manager.el && me.manager.el.dom && me.manager.el.isVisible()) {

			if (index) {
				me.xPos = me.getXposAlignedToSibling(notifications[index - 1]);
				me.yPos = me.getYposAlignedToSibling(notifications[index - 1]);
			} else {
				me.xPos = me.getXposAlignedToManager();
				me.yPos = me.getYposAlignedToManager();
			}

			me.stopAnimation();

			me.el.animate({
				to: {
					x: me.xPos,
					y: me.yPos
				},
				easing: me.slideBackAnimation,
				duration: me.slideBackDuration,
				dynamic: true
			});
		}
	},

	cancelAutoClose: function() {
		var me = this;

		if (me.autoClose) {
			me.task.cancel();
		}
	},

	doAutoClose: function () {
		var me = this;

		if (!(me.stickWhileHover && me.mouseIsOver)) {
			// Close immediately
			me.close();
		} else {
			// Delayed closing when mouse leaves the component.
			me.closeOnMouseOut = true;
		}
	},

	removeFromManager: function () {
		var me = this;

		if (me.manager) {
			var notifications = me.getNotifications(me.managerAlignment);
			var index = Ext.Array.indexOf(notifications, me);
			if (index != -1) {
				// Requires Ext JS 4.0.2
				Ext.Array.erase(notifications, index, 1);

				// Slide "down" all notifications "above" the hidden one
				for (;index < notifications.length; index++) {
					notifications[index].slideBack();
				}
			}
		}
	},

	hide: function () {
		var me = this;

		if (me.isHiding) {
			if (!me.isFading) {
				me.callParent(arguments);
				// Must come after callParent() since it will pass through hide() again triggered by destroy()
				me.isHiding = false;
			}
		} else {
			// Must be set right away in case of double clicks on the close button
			me.isHiding = true;
			me.isFading = true;

			me.cancelAutoClose();

			if (me.el) {
				me.el.fadeOut({
					opacity: 0,
					easing: 'easeIn',
					duration: me.hideDuration,
					remove: me.destroyAfterHide,
					listeners: {
						afteranimate: function () {
							me.isFading = false;
							me.removeCls('notification-fixed');
							me.removeFromManager();
							me.hide(me.animateTarget, me.doClose, me);
						}
					}
				});
			}
		}

		return me;
	},

	destroy: function () {
		var me = this;
		if (!me.hidden) {
			me.destroyAfterHide = true;
			me.hide(me.animateTarget, me.doClose, me);
		} else {
			me.callParent(arguments);
		}
	}

});

/**
 * @class Uni.controller.Error
 *
 * General error controller that is responsible to log and show uncaught errors
 * that are not dealt with in a separate failure handle case.
 */
Ext.define('Uni.controller.Error', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.view.error.Window',
        'Ext.ux.window.Notification'
    ],

    config: {
        window: null
    },

    refs: [
        {
            ref: 'contentPanel',
            selector: 'viewport > #contentPanel'
        }
    ],

    init: function () {
        var me = this;

        Ext.Error.handle = me.handleGenericError;
        Ext.Ajax.on('requestexception', me.handleRequestError, me);
    },

    handleGenericError: function (error) {
        console.log(error);

        var title = Uni.I18n.translate('error.requestFailed', 'UNI', 'Request failed');
        this.showError(title, error);
    },

    handleRequestError: function (conn, response, options) {
        var title = Uni.I18n.translate('error.requestFailed', 'UNI', 'Request failed'),
            message = response.responseText || response.statusText,
            decoded = Ext.decode(message, true);

        if (Ext.isDefined(decoded) && decoded !== null) {
            if (!Ext.isEmpty(decoded.message)) {
                message = decoded.message;
            } else if (Ext.isDefined(decoded.errors) && Ext.isArray(decoded.errors)) {
                if (1 === decoded.errors.length) {
                    message = decoded.errors[0].msg;
                } else if (1 < decoded.errors.length) {
                    message = '<ul>';
                    for (var i = 0; i < decoded.errors.length; i++) {
                        message += '<li>' + decoded.errors[i].msg + '</li>';
                    }
                    message += '</ul>';
                } else {
                    message = Uni.I18n.translate(
                        'error.unknownErrorOccurred',
                        'UNI',
                        'An unknown error occurred.'
                    );
                }
            }

            if (!Ext.isEmpty(decoded.error)) {
                console.log('Error code: ' + decoded.error);
            }
        }

        if (Ext.isEmpty(message)) {
            title = Uni.I18n.translate(
                'error.connectionProblemsTitle',
                'UNI',
                'Unexpected connection problems'
            );

            message = Uni.I18n.translate(
                'error.connectionProblemsMessage',
                'UNI',
                'Unexpected connection problems. Please check that server is available.'
            );
        }

        switch (response.status) {
            case 400: // Bad request.
                if (decoded && decoded.message) {
                    title = Uni.I18n.translate(
                        'error.requestFailed',
                        'UNI',
                        'Request failed'
                    );
                    this.showError(title, message);
                }
                break;
            case 500: // Internal server error.
                title = Uni.I18n.translate(
                    'error.internalServerError',
                    'UNI',
                    'Internal server error'
                );
                message = Uni.I18n.translate(
                    'error.internalServerErrorMessage',
                    'UNI',
                    'Please contact your system administrator.'
                );
                this.showError(title, message);
                break;
            case 404: // Not found.
                title = Uni.I18n.translate(
                    'error.requestFailed',
                    'UNI',
                    'Request failed'
                );
                message = Uni.I18n.translate(
                    'error.notFoundErrorMessage',
                    'UNI',
                    'Please contact your system administrator.'
                );
                this.showError(title, message);
                break;
            case 401: // Unauthorized.
                this.getApplication().fireEvent('sessionexpired');
                break;
            case 403: // Forbidden.
            // Fallthrough.
            case 418: // I'm a teapot.
            // Fallthrough.
            default:
                this.showError(title, message);
                break;
        }
    },

    /**
     * Shows an error window with a title and a message to the user.
     *
     * @param {String} title Window title to show
     * @param {String} message Error message to show
     * @param {String} [config={}] Optional {@link Ext.window.MessageBox} configuration if tweaks are required
     */
    showError: function (title, message, config) {
        config = config ? config : {};
        Ext.apply(config, {
            title: title,
            msg: message,
            modal: false,
            ui: 'message-error',
            icon: Ext.MessageBox.ERROR
        });

        var box = Ext.create('Ext.window.MessageBox', {
            buttons: [
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('general.close', 'UNI', 'Close'),
                    action: 'close',
                    name: 'close',
                    ui: 'action',
                    handler: function () {
                        box.close();
                    }
                }
            ]
        });

        box.show(config);
    }
});

/**
 * @class Uni.controller.history.EventBus
 */
Ext.define('Uni.controller.history.EventBus', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.util.History'
    ],

    config: {
        defaultToken: '',
        previousPath: null,
        currentPath: null
    },

    onLaunch: function () {
        this.initHistory();
    },

    initHistory: function () {
        var me = this;

        Ext.util.History.init(function () {
            Ext.util.History.addListener('change', function (token) {
                me.onHistoryChange(token);
            });

            me.checkHistoryState();
        });
    },

    checkHistoryState: function () {
        var me = this,
            token = Ext.util.History.getToken();

        if (token === null || token === '') {
            token = me.getDefaultToken();
            Ext.util.History.add(token);
        }

        me.onHistoryChange(token);
    },

    onHistoryChange: function (token) {
        var queryStringIndex = token.indexOf('?');
        if (queryStringIndex > 0) {
            token = token.substring(0, queryStringIndex);
        }

        if (this.getCurrentPath() !== null) {
            this.setPreviousPath(this.getCurrentPath());
        }
        this.setCurrentPath(token);

        crossroads.parse(token);
    }
});

/**
 * @class Uni.model.MenuItem
 */
Ext.define('Uni.model.MenuItem', {
    extend: 'Ext.data.Model',
    fields: [
        'text',
        'portal',
        'href',
        'glyph',
        'index',
        'hidden'
    ],
    proxy: {
        type: 'memory'
    }
});

/**
 * @class Uni.store.MenuItems
 */
Ext.define('Uni.store.MenuItems', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.MenuItem',
    storeId: 'menuItems',
    singleton: true,
    autoLoad: false,
    clearOnPageLoad: false,
    clearRemovedOnLoad: false,

    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
            root: 'items'
        }
    },

    sorters: [
        {
            property: 'index',
            direction: 'DESC'
        }
    ]
});

/**
 * @class Uni.model.AppItem
 */
Ext.define('Uni.model.AppItem', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'basePath',
        'startPage',
        'icon',
        'mainController',
        'scripts',
        'translationComponents',
        'styleSheets',
        'dependencies'
    ]
});

/**
 * @class Uni.store.AppItems
 */
Ext.define('Uni.store.AppItems', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.AppItem',
    storeId: 'appitems',
    singleton: true,
    autoLoad: true,

    proxy: {
        type: 'ajax',
        url: '/api/apps/pages',
        reader: {
            type: 'json',
            root: ''
        }
    }
});

/**
 * @class Uni.model.App
 */
Ext.define('Uni.model.App', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        {
            name: 'url',
            convert: function (value, record) {
                if (value.indexOf('#') === -1 && value.indexOf('http') === -1) {
                    value += '#';
                }
                return value;
            }
        },
        'icon',
        {
            name: 'isActive',
            persist: false,
            convert: function (value, record) {
                var href = window.location.href,
                    pathname = window.location.pathname,
                    fullPath = pathname + window.location.hash;

                return href.indexOf(record.data.url, 0) === 0
                    || fullPath.indexOf(record.data.url, 0) === 0;
            }
        },
        {
            name: 'isExternal',
            persist: false,
            convert: function (value, record) {
                var url = record.get('url');
                return url.indexOf('http') === 0;
            }
        }
    ]
});

/**
 * @class Uni.store.Apps
 */
Ext.define('Uni.store.Apps', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.App',
    storeId: 'apps',
    singleton: true,
    autoLoad: false,

    proxy: {
        type: 'ajax',
        url: '/api/apps/apps',
        reader: {
            type: 'json',
            root: ''
        }
    }
});

/**
 * @class Uni.view.container.ContentContainer
 *
 * Common content container that supports to set breadcrumbs, content in the center, and a
 * component beside the content.
 *
 * Styling will also be applied automatically to anything that is in the component.
 *
 * # Example usage
 *
 *     @example
 *     Ext.create('Uni.view.container.ContentContainer', {
 *         // Other container properties.
 *
 *         side: [
 *             // Placed beside the content, used as if it was a 'items' configuration.
 *         ],
 *
 *         content: [
 *             // What you would normally place in the 'items' property.
 *         ]
 *     }
 *
 * # Visual guide
 *
 * {@img view/container/ContentContainer.png Visual guide to the container component}
 *
 * # Breadcrumbs
 *
 * You can use the built-in breadcrumbs component by either fetching it via query selector with
 * the id '#breadcrumbTrail' or call the method #getBreadcrumbTrail.
 *
 * # Changing the side or content dynamically
 *
 * If your screen has already been rendered and you want to change the visible side or content
 * component you will have to refer to it as you would with any component. There are methods to request
 * each separate wrapper:
 *
 *     * North container #getNorthContainer
 *     * Center container #getCenterContainer
 *     * West container #getWestContainer
 *
 * Try to get as much done before rendering in the {#side} and {#content} properties. Otherwise future changes
 * to the content container might impact your application.
 */
Ext.define('Uni.view.container.ContentContainer', {
    extend: 'Ext.container.Container',
    alias: 'widget.contentcontainer',
    ui: 'contentcontainer',
    overflowY: 'auto',

    requires: [
    ],

    layout: {
        type: 'hbox'
    },

    /**
     * @cfg {Object/Ext.Component}
     *
     * Configuration of the side panel. Used just as if you would use the items configuration.
     */
    side: null,

    /**
     * @cfg {Object/Ext.Component}
     *
     * Configuration of the content panel. Used just as if you would use the items configuration.
     */
    content: null,

    items: [
        {
            xtype: 'container',
            itemId: 'westContainer',
            overflowY: 'auto',
            cls: 'west'
        },
        {
            xtype: 'container',
            itemId: 'centerContainer',
            overflowY: 'auto',
            cls: 'center',
            flex: 1,
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: []
        }
    ],

    initComponent: function () {
        var side = this.side,
            content = this.content;

        if (!(side instanceof Ext.Component)) {
            // Never modify a passed config object, that could break the expectations of the using code.
            side = Ext.clone(side);
        }

        this.items[0].items = side;

        if (!(content instanceof Ext.Component)) {
            // Never modify a passed config object, that could break the expectations of the using code.
            content = Ext.clone(content);
        }

        this.items[1].items = content;

        // Else use the default config already in place.

        this.callParent(arguments);
    },

    /**
     *
     * @returns {Ext.container.Container}
     */
    getNorthContainer: function () {
        return this.down('#northContainer');
    },

    /**
     *
     * @returns {Ext.container.Container}
     */
    getWestContainer: function () {
        return this.down('#westContainer');
    },

    /**
     *
     * @returns {Ext.container.Container}
     */
    getCenterContainer: function () {
        return this.down('#centerContainer');
    }

});

/**
 * The router component is responsible for gathering and handling route configuration
 *
 * @class Uni.controller.history.Router
 *
 * Route confipuration parameters:
 *
 * title: string or function
 * route: the part of route, srr crossroads for example.
 * controller: specify the controller which will handle the route
 * action: (optional) action of the controller which will be fired on route match. If not specified defaultAction will be used.
 * disabled: (optional) if true the route will not be registered with crossroads
 * items: (optional) the set of child routes
 * params: (optional) the set of optional parameters which will be passed to the action
 * redirect: string|object specify string route for redirect or an object in following format:
 * {
 *      route: 'route/to/redirect/{paramId}',
 *      params: {paramId: 42}
 * }
 *
 * For creating URL in the application use the following example:
 *
 * router.getRoute('/app/item/view').getTitle() # for title
 * router.getRoute('app/item/view').buildUrl({id: 1}) # example output: /application/item/1
 *
 * Router will fire event 'routematch' with the Router component as parameter when the route is activated.
 * Example of route configuration:
 *
 * router.addConfig({
 *      administration : {
 *          title: 'Administration',
 *          route: 'administration',
 *          disabled: true,
 *          items: {
 *              issue: {
 *                  title: 'Issue',
 *                  route: 'issue',
 *                  items: {
 *                      assignmentrules: {
 *                          title: 'Assignment Rules',
 *                          route: 'assignmentrules',
 *                          controller: 'Isu.controller.IssueAssignmentRules'
 *                      },
 *                      creationrules: {
 *                          title: 'Creation Rules',
 *                          route: 'creationrules',
 *                          controller: 'Isu.controller.IssueCreationRules',
 *                          items: {
 *                              create: {
 *                                  title: 'Create',
 *                                  route: '/create',
 *                                  controller: 'Isu.controller.IssueCreationRulesEdit'
 *                              },
 *                              edit: {
 *                                  title: 'Edit',
 *                                  route: '{id}/edit',
 *                                  controller: 'Isu.controller.IssueCreationRulesEdit'
 *                              }
 *                          }
 *                      }
 *                  }
 *              },
 *              communicationtasks: {
 *                  title: 'Communication Tasks',
 *                  route: 'communicationtasks',
 *                  controller: 'Isu.controller.CommunicationTasksView',
 *                  items: {
 *                      create: {
 *                          title: 'Create',
 *                          route: '/create',
 *                          controller: 'Isu.controller.CommunicationTasksEdit'
 *                      },
 *                      edit: {
 *                          title: 'Edit',
 *                          route: '{id}',
 *                          controller: 'Isu.controller.CommunicationTasksEdit'
 *                      }
 *                  }
 *              }
 *          }
 *      }
 *  })
 *
 */
Ext.define('Uni.controller.history.Router', {
    extend: 'Ext.app.Controller',

    config: {},

    /**
     * @private
     */
    routes: {},

    defaultAction: 'showOverview',
    currentRoute: null,

    /**
     * List of route arguments
     */
    arguments: {},

    /**
     * List of query params
     */
    queryParams: {},

    /**
     * Filter instance
     */
    filter: null,

    /**
     * Add router configuration
     * @param config
     */
    addConfig: function (config) {
        _.extend(this.config, config);

        var me = this;
        _.each(config, function (item, key) {
            me.initRoute(key, item);
        });
    },

    getQueryString: function () {
        var token = Ext.util.History.getToken() || document.location.href.split('?')[1],
            queryStringIndex = token.indexOf('?');
        return queryStringIndex < 0 ? '' : token.substring(queryStringIndex + 1);
    },

    getQueryStringValues: function () {
        var queryString = this.getQueryString();
        if (typeof queryString !== 'undefined') {
            return Ext.Object.fromQueryString(this.getQueryString());
        }
        return {};
    },

    queryParamsToString: function (obj) {
        return Ext.urlEncode(_.object(_.keys(obj), _.map(obj, function (i) {
            return _.isString(i) ? i : Ext.JSON.encodeValue(i);
        })));
    },

    /**
     * @private
     * @param key string
     * @param config Object
     * @param prefix string|null
     */
    initRoute: function (key, config, prefix) {
        var me = this;
        prefix = typeof prefix !== 'undefined' ? prefix : '';
        var route = prefix + '/' + config.route;
        var action = typeof config.action !== 'undefined' ? config.action : me.defaultAction;
        var params = typeof config.params !== 'undefined' ? config.params : {};

        // register route within controller
        // todo: move route class to external entity.
        me.routes[key] = _.extend(config, {
            path: route,

            /**
             * Return title of the route
             * @returns string
             */
            getTitle: function () {
                var route = this;
                return _.isFunction(this.title)
                    ? this.title.apply(me, [route])
                    : this.title;
            },

            setTitle: function (title) {
                this.title = title;
                me.fireEvent('routechange', this);
            },

            /**
             * returns URL builded with provided arguments and query string parameters
             *
             * @param arguments
             * @param queryParams
             *
             * @returns {string}
             */
            buildUrl: function (arguments, queryParams) {
                arguments = Ext.applyIf(arguments || {}, me.arguments);
                var url = this.crossroad ?
                    '#' + this.crossroad.interpolate(arguments) :
                    '#' + this.path;
                return _.isEmpty(queryParams) ? url : url + '?' + me.queryParamsToString(queryParams);
            },

            /**
             * @param arguments
             * @param queryParams
             */
            forward: function (arguments, queryParams) {
                window.location.href = this.buildUrl(arguments, queryParams);
            }
        });

        if (me.routes[key].callback) {
            me.routes[key].callback.apply(me, [me.routes[key]])
        }

        // register route with crossroads if not disabled
        if (!config.disabled) {
            me.routes[key].crossroad = crossroads.addRoute(route, function () {
                me.currentRoute = key;

                // todo: this will not work with optional params
                me.queryParams = Ext.Object.fromQueryString(me.getQueryString());
                me.arguments = _.object(
                    me.routes[key].crossroad._paramsIds,
                    arguments
                );

                var routeArguments = _.values(_.extend(me.arguments, params));

                if (Ext.isDefined(config.redirect)) {
                    // perform redirect on route match
                    if (Ext.isObject(config.redirect)) {
                        var redirectParams = _.extend(me.arguments, config.redirect.params);
                        me.getRoute(config.redirect.route).forward(redirectParams);
                    } else if (Ext.isString(config.redirect)) {
                        me.getRoute(config.redirect).forward(me.arguments);
                    } else {
                        throw 'config redirect must be a string or an object';
                    }
                } else {
                    // fire the controller action with this route params as arguments
                    var controller = me.getController(config.controller);

                    var dispatch = function () {
                        me.fireEvent('routematch', me);
                        controller[action].apply(controller, routeArguments);
                    };

                    // load filter
                    if (config.filter) {
                        Ext.ModelManager.getModel(config.filter).load(null, {
                            callback: function (record) {
                                me.filter = record || Ext.create(config.filter);
                                dispatch();
                            }
                        });
                    } else {
                        dispatch();
                    }
                }
            });
        }

        // handle child items
        if (config.items) {
            _.each(config.items, function (item, itemKey) {
                if (Ext.isArray(item.privileges) && !Uni.Auth.hasAnyPrivilege(item.privileges)) {
                    return;
                }

                var path = key + '/' + itemKey;
                me.initRoute(path, item, route);
            });
        }
    },

    /**
     * Builds breadcrumbs data based on path
     * @param path
     * @returns [Route]
     */
    buildBreadcrumbs: function (path) {
        var me = this;
        path = typeof path === 'undefined'
            ? me.currentRoute.split('/')
            : path.split('/');

        var items = [];
        do {
            var route = me.getRoute(path.join('/'));
            items.push(route);
            path.pop();
        } while (path.length);

        return items;
    },

    /**
     * return the route via alias
     * Route object have following api:
     * getTitle() - returns the title of the route
     * buildUrl(arguments) - builds URl with provided arguments
     * @param path
     * @returns Route
     */
    getRoute: function (path) {
        var me = this;

        if (!Ext.isDefined(path)) {
            path = me.currentRoute;
        }

        return me.routes[path];
    },

    getRouteConfig: function (path) {
        var route = me.routeConfig;
        path = path.split('/');

        do {
            var item = path.shift();
            route = route[item];
            if (item !== 'items' && path.length) path.splice(0, 0, 'items');
        } while (path.length);

        return route;
    }
});

/**
 * @class Uni.controller.Navigation
 */
Ext.define('Uni.controller.Navigation', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.EventBus',
        'Uni.store.MenuItems',
        'Uni.store.AppItems',
        'Uni.store.Apps',
        'Uni.view.container.ContentContainer',
        'Uni.controller.history.Router'
    ],

    views: [
    ],

    refs: [
        {
            ref: 'navigationMenu',
            selector: 'navigationMenu'
        },
        {
            ref: 'contentWrapper',
            selector: 'viewport > #contentPanel'
        },
        {
            ref: 'breadcrumbs',
            selector: 'breadcrumbTrail'
        },
        {
            ref: 'searchButton',
            selector: 'navigationHeader #globalSearch'
        }
    ],

    applicationTitle: 'Connexo Multi Sense',
    applicationTitleSeparator: '-',
    searchEnabled: Uni.Auth.hasAnyPrivilege(['privilege.administrate.device', 'privilege.view.device']),

    init: function () {
        var me = this;

        Ext.util.History.addListener('change', function () {
            me.selectMenuItemByActiveToken();
        });

        this.initApps();
        this.initMenuItems();

        this.control({
            'navigationMenu': {
                afterrender: this.onAfterRenderNavigationMenu
            },
            'navigationAppSwitcher': {
                afterrender: this.resetAppSwitcherState
            },
            'navigationHeader #globalSearch': {
                afterrender: this.initSearch
            }
        });

        this.getApplication().on('changemaincontentevent', this.showContent, this);
        this.getApplication().on('changemainbreadcrumbevent', this.initTitle, this);
        this.getApplication().on('changemainbreadcrumbevent', this.setBreadcrumb, this);

        this.getController('Uni.controller.history.Router').on('routematch', this.initBreadcrumbs, this);
        this.getController('Uni.controller.history.Router').on('routechange', this.initBreadcrumbs, this);
    },

    initApps: function () {
        Uni.store.Apps.load();
    },

    initTitle: function (breadcrumbItem) {
        var me = this,
            text = '';

        if (Ext.isObject(breadcrumbItem)) {
            text = breadcrumbItem.get('text');

            while (Ext.isDefined(breadcrumbItem.getAssociatedData()['Uni.model.BreadcrumbItem'])) {
                breadcrumbItem = breadcrumbItem.getChild();
                text = breadcrumbItem.get('text');
            }
        }

        if (!Ext.isEmpty(text)) {
            Ext.getDoc().dom.title = text + ' '
                + me.applicationTitleSeparator + ' '
                + me.applicationTitle;
        } else {
            Ext.getDoc().dom.title = me.applicationTitle;
        }
    },

    initBreadcrumbs: function () {
        var me = this;
        var router = me.getController('Uni.controller.history.Router');
        var breadcrumbs = me.getBreadcrumbs();
        var child, breadcrumb;

        breadcrumbs.removeAll();
        _.map(router.buildBreadcrumbs(), function (route) {
            var title = route.getTitle();

            breadcrumb = Ext.create('Uni.model.BreadcrumbItem', {
                text: Ext.isString(title) ? title : '',
                href: route.buildUrl(),
                relative: false
            });

            if (child) {
                breadcrumb.setChild(child);
            }
            child = breadcrumb;
        });

        me.initTitle(breadcrumb);
        breadcrumbs.setBreadcrumbItem(breadcrumb);
    },

    initSearch: function () {
        var me = this;
        me.getSearchButton().setVisible(me.searchEnabled);
    },

    onAfterRenderNavigationMenu: function () {
        this.refreshNavigationMenu();
        this.selectMenuItemByActiveToken();
    },

    initMenuItems: function () {
        Uni.store.MenuItems.on({
            add: this.refreshNavigationMenu,
            load: this.refreshNavigationMenu,
            update: this.refreshNavigationMenu,
            remove: this.refreshNavigationMenu,
            bulkremove: this.refreshNavigationMenu,
            scope: this
        });
    },

    initAppItems: function () {
        Uni.store.AppItems.on({
            add: this.resetAppSwitcherState,
            load: this.resetAppSwitcherState,
            update: this.resetAppSwitcherState,
            remove: this.resetAppSwitcherState,
            bulkremove: this.resetAppSwitcherState,
            scope: this
        });
        Uni.store.AppItems.load();
    },

    resetAppSwitcherState: function () {
        var count = Uni.store.AppItems.getCount();

        if (count > 0) {
            this.getAppSwitcher().enable();
        } else {
            this.getAppSwitcher().disable();
        }
    },

    refreshNavigationMenu: function () {
        var menu = this.getNavigationMenu(),
            store = Uni.store.MenuItems;

        this.removeDuplicatesFromStore(store);

        if (menu !== undefined) {
            if (menu.rendered) {
                Ext.suspendLayouts();
            }

            menu.removeAllMenuItems();
            store.each(function (record) {
                menu.addMenuItem(record);
            });

            if (menu.rendered) {
                Ext.resumeLayouts(true);
            }
        }
    },

    removeDuplicatesFromStore: function (store) {
        var hits = [],
            duplicates = [];

        store.each(function (record) {
            var text = record.get('text'),
                portal = record.get('portal');

            if (hits[text + portal]) {
                duplicates.push(record);
            } else {
                hits[text + portal] = true;
            }
        });

        // Delete the duplicates.
        store.remove(duplicates);
    },

    addMenuItem: function (title, href, glyph) {
        href = portal ? '#/' + portal : href;
        var item = {
            text: title,
            tooltip: title,
            href: href,
            glyph: glyph
        };

        this.getNavigationMenu().addMenuItem(item);
    },

    selectMenuItemByActiveToken: function () {
        var me = this,
            token = Ext.util.History.getToken(),
            tokens = me.stripAndSplitToken(token);

        me.getNavigationMenu().deselectAllMenuItems();

        Uni.store.MenuItems.each(function (model) {
            modelTokens = me.stripAndSplitToken(model.get('href'));
            if (tokens[0] === modelTokens[0] || tokens[0] === model.get('portal')) {
                me.getNavigationMenu().selectMenuItem(model);
                return;
            }
        });
    },

    stripAndSplitToken: function (token) {
        if (token) {
            token = token.indexOf(Uni.controller.history.Settings.tokenDelimiter) === 0 ? token.substring(1) : token;
            token = token.replace(/#\/|#/g, ''); // Regex to replace all '#' or '#/'.

            // Strip the query parameters if necessary.
            if (token.indexOf('?') >= 0) {
                token = token.slice(0, token.indexOf('?'));
            }

            return token.split(Uni.controller.history.Settings.tokenDelimiter);
        } else {
            return [];
        }
    },

    showContent: function (content, side) {
        this.getContentWrapper().removeAll();

        if (content instanceof Uni.view.container.ContentContainer) {
            side = content.side;
            content = content.content;
        }

        var contentContainer = new Ext.widget('contentcontainer', {
            content: content,
            side: side
        });

        this.getContentWrapper().add(contentContainer);
        this.getContentWrapper().doComponentLayout();
    },

    setBreadcrumb: function (breadcrumbItem) {
        var trail = this.getBreadcrumbs();
        trail.setBreadcrumbItem(breadcrumbItem);
    }
});

/**
 * @class Uni.view.notifications.Anchor
 */
Ext.define('Uni.view.notifications.Anchor', {
    extend: 'Ext.button.Button',
    alias: 'widget.notificationsAnchor',

    text: '',
    action: 'preview',
    glyph: 'xe012@icomoon',
    scale: 'small',
    cls: 'notifications-anchor',
    disabled: true,

    menu: [
        {
            xtype: 'dataview',
            tpl: [
                '<tpl for=".">',
                '<div class="notification-item">',
                '<p>{message}</p>',
                '</div>',
                '</tpl>'
            ],
            itemSelector: 'div.notification-item',
            store: 'notifications'
        }
    ]
});

/**
 * @class Uni.model.Notification
 */
Ext.define('Uni.model.Notification', {
    extend: 'Ext.data.Model',

    fields: [
        'message',
        'type',
        'timeadded',
        'timeseen',
        'callback'
    ],

    constructor: function () {
        var data = arguments[0] || {};

        if (!data['timeadded']) {
            data['timeadded'] = new Date();
        }

        if (arguments.length === 0) {
            this.callParent([data]);
        } else {
            this.callParent(arguments);
        }
    },

    proxy: {
        type: 'memory'
    }
});

/**
 * @class Uni.store.Notifications
 */
Ext.define('Uni.store.Notifications', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.Notification',
    storeId: 'notifications',
    singleton: true,
    autoLoad: false,
    clearOnPageLoad: false,
    clearRemovedOnLoad: false,

    // TODO Sort the store according on timeadded (most recent first).

    proxy: {
        type: 'memory'
    }
});

/**
 * @class Uni.controller.Notifications
 */
Ext.define('Uni.controller.Notifications', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.view.notifications.Anchor',
        'Uni.store.Notifications'
    ],

    refs: [
        {
            ref: 'anchor',
            selector: 'notificationsAnchor'
        }
    ],

    init: function () {
        this.getApplication().on('addnotificationevent', this.addNotification, this);

        Uni.store.Notifications.on({
            add: this.resetAnchorCount,
            load: this.resetAnchorCount,
            update: this.resetAnchorCount,
            remove: this.resetAnchorCount,
            bulk: this.resetAnchorCount,
            scope: this
        });

        this.control({
            'notificationsAnchor': {
                afterrender: this.resetAnchorCount
            }
        });
    },

    addNotification: function (notification) {
        Uni.store.Notifications.add(notification);
    },

    resetAnchorCount: function () {
        var unseenCount = 0;

        Uni.store.Notifications.each(function (record) {
            if (!record.data.timeseen) {
                unseenCount++;
            }
        });

        if (unseenCount > 0) {
            this.getAnchor().enable();
        } else {
            this.getAnchor().disable();
        }

        this.getAnchor().setText(this.getUnseenText(unseenCount));
        // TODO Slightly animate it when the count increases.
    },

    getUnseenText: function (count) {
        var unseenText = '';

        if (count > 10) {
            unseenText = '10+';
        } else if (count > 0) {
            unseenText = count;
        }

        return unseenText;
    }
});

/**
 * @class Uni.model.PortalItem
 *
 * If you have several widgets that need to be shown on a common page, you will need
 * to create a {@link Uni.model.PortalItem} for each widget on that page. E.g. there is
 * an administration page that is filled with several portal widgets.
 *
 * This model class provides the portal item configuration, including title and component.
 *
 * See {@link Uni.controller.Portal} for more detailed information.
 *
 */
Ext.define('Uni.model.PortalItem', {
    extend: 'Ext.data.Model',
    fields: [
        'title',
        'portal',
        'index',
        'items'
    ],
    proxy: {
        type: 'memory'
    }
});

/**
 * @class Uni.store.PortalItems
 *
 * If you have several widgets that need to be shown on a common page, you will need
 * to create a {@link Uni.model.PortalItem} for each widget on that page. E.g. there is
 * an administration page that is filled with several portal widgets.
 *
 * This store is used to keep track of the portal items and listen to changes to them in
 * {@link Uni.controller.Portal}.
 *
 * See {@link Uni.controller.Portal} for more detailed information.
 *
 */
Ext.define('Uni.store.PortalItems', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.PortalItem',
    storeId: 'portalItems',
    singleton: true,
    autoLoad: false,
    clearOnPageLoad: false,
    clearRemovedOnLoad: false,

    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
            root: 'items'
        }
    }
});

Ext.define('Uni.view.container.PortalContainer', {
    extend: 'Ext.panel.Panel',
    xtype: 'portal-container',
    ui: 'large',

    padding: '16px 0 0 0 ',
    layout: 'column',
    columnCount: 3,

    addPortalItem: function (model) {
        var me = this,
            component = this.createPortalWidgetFromItem(model),
            index = model.get('index');

        if (index === '' || index === null || typeof index === 'undefined') {
            this.add(component);
        } else {
            this.insert(index, component);
        }

        var count = this.items.items.length,
            remainder = count % me.columnCount;

        switch (remainder) {
            case 1:
                component.addCls('first');
                break;
            case 2:
                component.addCls('middle');
                break;
            default:
                component.addCls('last');
                break;
        }
    },

    createPortalWidgetFromItem: function (model) {
        var me = this,
            title = model.get('title'),
            items = model.get('items'),
            widget;

        if (typeof items === 'undefined') {
            return widget;
        }

        widget = Ext.create('Ext.panel.Panel', {
            title: title,
            ui: 'tile',
            columnWidth: 1 / me.columnCount,
            height: 256,
            items: [
                {
                    xtype: 'menu',
                    ui: 'tilemenu',
                    floating: false,
                    items: items
                }
            ]
        });

        return widget;
    }
});

/**
 * @class Uni.controller.Portal
 *
 * The portal controller ({@link Uni.controller.Portal}) is responsible for combining several
 * widgets that belong to the same portal category. Each widget gets placed on the same overview
 * component. The history is handled by this controller as well.
 *
 * For now, the styling is quite minimized. Every component that is specified in a
 * {@link Uni.model.PortalItem} is placed inside of a {@link Ext.panel.Panel}.
 *
 * The breadcrumb also just consists of the portal name as a header.
 *
 * # Example usage
 *
 *     var portalMenuItem = Ext.create('Uni.model.MenuItem', {
 *                 text: 'Administration',
 *                 href: '#/administration',
 *                 portal: 'administration',
 *                 glyph: 'settings',
 *                 index: 10
 *     });
 *
 *     Uni.store.MenuItems.add(menuItem);
 *
 *     var portalItem1 = Ext.create('Uni.model.PortalItem', {
 *                 title: 'Portal item 1',
 *                 component: Ext.create('My.view.Component'),
 *                 portal: 'administration'
 *             });
 *
 *      var portalItem2 = Ext.create('Uni.model.PortalItem', {
 *                title: 'Portal item 2',
 *                component: Ext.create('Ext.Component', {
 *                    html: '<h1>Test</h1>'
 *                }),
 *                portal: 'administration'
 *            });
 *
 *     Uni.store.PortalItems.add(
 *         portalItem1, portalItem2, portalItem3,
 *         portalItem4, portalItem5, portalItem6
 *     );
 *
 */
Ext.define('Uni.controller.Portal', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.store.MenuItems',
        'Uni.store.PortalItems',
        'Uni.view.container.PortalContainer'
    ],

    portalViews: [],

    init: function () {
        this.initMenuItems();
        this.refreshPortals();

        this.addEvents(
            /**
             * @event changemaincontentevent
             *
             * Fires after a portal view needs to be shown.
             *
             * @param {Ext.Component} Content component
             * @param {Ext.Component} Side component
             */
            'changemaincontentevent',
            /**
             * @event changemainbreadcrumbevent
             *
             * Fires after a portal breadcrumb needs to be shown.
             *
             * @param {Uni.model.BreadcrumbItem} Breadcrumb to show
             */
            'changemainbreadcrumbevent'
        );
    },

    initMenuItems: function () {
        Uni.store.MenuItems.on({
            add: this.refreshPortals,
            load: this.refreshPortals,
            update: this.refreshPortals,
            remove: this.refreshPortals,
            bulkremove: this.refreshPortals,
            scope: this
        });
    },

    refreshPortals: function () {
        var me = this,
            store = Uni.store.MenuItems;

        store.each(function (item) {
            var portal = item.get('portal'),
                title = item.get('text');

            if (!Ext.isEmpty(portal)) {
                crossroads.addRoute('/' + portal, function () {
                    me.showPortalOverview(portal, title);
                });
            }
        });
    },

    showPortalOverview: function (portal, title) {
        var store = Uni.store.PortalItems,
            portalView = this.portalViews[portal];

        if (Ext.isDefined(portalView)) {
            portalView.removeAll();
        }

        this.portalViews[portal] = Ext.create('Uni.view.container.PortalContainer', {
            title: title
        });
        portalView = this.portalViews[portal];

        store.clearFilter();
        store.filter('portal', portal);
        var portalItemsToDisplay = {};
        store.each(function (portalItem) {
            if (portalItemsToDisplay.hasOwnProperty(portalItem.get('title'))) {
                Ext.each(portalItem.get('items'), function (item) {
                    portalItemsToDisplay[portalItem.get('title')].get('items').push(item);
                });
                store.remove(portalItem);
            } else {
                portalItemsToDisplay[portalItem.get('title')] = portalItem;
            }
            portalItemsToDisplay[portalItem.get('title')].get('items').sort(function (item1, item2) {
                if (item1.text < item2.text) {
                    return -1;
                } else if (item1.text > item2.text) {
                    return 1;
                } else {
                    return 0;
                }
            });
        });

        var sortArrayForPortalItems = [];
        for (portalItemToDisplay in portalItemsToDisplay) {
            if (portalItemsToDisplay.hasOwnProperty(portalItemToDisplay)) {
                sortArrayForPortalItems.push(portalItemToDisplay);
            }
        }
        sortArrayForPortalItems.sort();
        for (var i = 0; i < sortArrayForPortalItems.length; i++) {
            portalView.addPortalItem(portalItemsToDisplay[sortArrayForPortalItems[i]]);
        }

        this.getApplication().fireEvent('changemaincontentevent', portalView);

        var portalBreadcrumb = Ext.create('Uni.model.BreadcrumbItem', {
            text: title
        });

        this.getApplication().fireEvent('changemainbreadcrumbevent', portalBreadcrumb);
    }
});

/**
 * @class Uni.view.search.Quick
 */
Ext.define('Uni.view.search.Quick', {
    extend: 'Ext.container.Container',
    alias: 'widget.searchQuick',
    cls: 'search-quick',
    layout: {
        type: 'hbox',
        align: 'stretch',
        pack: 'end'
    },
    items: [
        {
            xtype: 'container',
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            items: [
                {
                    xtype: 'textfield',
                    itemId: 'searchField',
                    cls: 'search-field',
                    emptyText: 'Search'
                }
            ]
        },
        {
            xtype: 'button',
            itemId: 'searchButton',
            cls: 'search-button',
            glyph: 'xe021@icomoon',
            scale: 'small'
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    }
});

/**
 * @class Uni.controller.Search
 */
Ext.define('Uni.controller.Search', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.view.search.Quick'
    ],

    refs: [
        {
            ref: 'searchField',
            selector: 'searchQuick #searchField'
        }
    ],

    init: function () {
        this.control({
            'searchQuick #searchButton': {
                click: this.onClickSearchButton
            },
            'searchButton': {
                click: this.onClickBasicSearchButton
            },
            'searchQuick #searchField': {
                specialkey: this.onEnterSearchField
            }
        });
    },

    onClickBasicSearchButton: function () {
        this.getApplication().fireEvent('showadvancedsearchevent');
    },

    onClickSearchButton: function () {
        this.validateInputAndFireEvent();
    },

    onEnterSearchField: function (field, e) {
        if (e.getKey() === e.ENTER) {
            this.validateInputAndFireEvent();
        }
    },

    validateInputAndFireEvent: function () {
        var query = this.getSearchField().getValue().trim();

        if (query.length > 0) {
            this.fireSearchQueryEvent(query);
        }
    },

    fireSearchQueryEvent: function (query) {
        this.getApplication().fireEvent('searchqueryevent', query);
    }
});

/**
 * @class Uni.view.form.field.Vtypes
 */
Ext.define('Uni.view.form.field.Vtypes', {

    requires: ['Ext.form.field.VTypes'],

    hexstringRegex: /^[a-f_A-F_0-9]*$/,

    init: function () {
        this.validateNonEmptyString();
        this.validateHexString();
        this.validateEan13String();
        this.validateEan18String();
        this.validateReadingtype();
    },


    validateReadingtype: function () {
        var me = this;
        var message = null;
        Ext.apply(Ext.form.field.VTypes, {
            readingtype:  function(v) {
                return /^\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+$/.test(v);
            },
            readingtypeText: 'Invalid reading type syntax',
            readingtypeMask: /[\d\.]/i
        });
    },


    validateNonEmptyString: function () {
        var me = this;
        var message = null;
        Ext.apply(Ext.form.field.VTypes, {
            nonemptystring: function (val) {
                message = null;
                //check value
                if ((val==null || val==undefined || val=='')) {
                    return false;
                }
                if (val.trim().length == 0) {
                    return false;
                }
            },
            nonemptystringText: 'This field is required'
        });
    },

    validateHexString: function () {
        var me = this;
        Ext.apply(Ext.form.field.VTypes, {
            hexstring: function (val) {
                //check value
                return me.hexstringRegex.test(val);
            },
            hexstringText: 'Wrong Hexadecimal number!'
        });
    },

    validateEan13String: function () {
        var me = this;

        Ext.apply(Ext.form.field.VTypes, {
            ean13: function (val) {
                //check value
                if (val.length != 13) {
                    return false;
                } else if (me.validateNumeric(val) === false) {
                    return false;
                } else if (val.substr(12) !== me.validateCheckDigit(val.substring(0, 12))) {
                    return false;
                } else {
                    return true;
                }
            },
            ean13stringText: 'Wrong Ean13!'
        });
    },

    numericregex: /^[0-9]$/,
    validateEan18String: function () {
        var me = this;
        Ext.apply(Ext.form.field.VTypes, {
            ean18: function (val) {
                //check value
                if (val.length !== 18) {
                    return false;
                } else if (me.validateNumeric(val) === false) {
                    return false;
                } else if (val.substr(17) !== me.validateCheckDigit(val.substring(0, 17))) {
                    return false;
                } else {
                    return true;
                }
            },
            ean18stringText: 'Wrong Ean18!'
        });
    },

    validateNumeric: function (value) {
        return this.numericregex.test(value);
    },

    validateCheckDigit: function (value) {
        var multiplier = 3;
        var sum = 0;

        for (var i = value.length - 1; i >= 0; i--) {
            var digit = value.substring(i, i + 1);
            sum += digit * multiplier;
            multiplier = (multiplier === 3) ? 1 : 3;
        }
        var next10 = (((sum - 1) / 10) + 1) * 10;
        return next10 - sum;
    }


});

Ext.define('Uni.view.panel.FilterToolbar', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.filter-toolbar',
    titlePosition: 'left',
    layout: {
        type: 'hbox'
    },
    header: false,
    ui: 'filter-toolbar',
    showClearButton: true,

    items: [
        {
            xtype: 'container',
            itemId: 'itemsContainer',
            defaults: {
                margin: '0 8 0 0'
            },
            items: []
        },
        {
            xtype: 'label',
            itemId: 'emptyLabel',
            hidden: true
        },
        {
            xtype: 'component',
            flex: 1,
            html: '&nbsp;'
        },
        {
            xtype: 'container',
            itemId: 'toolsContainer',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            dock: 'left'
        }
    ],

    dockedItems: [
        {
            xtype: 'header',
            dock: 'left'
        },
        {
            xtype: 'container',
            dock: 'right',
            minHeight: 150,
            items: {
                itemId: 'Reset',
                xtype: 'button',
                text: 'Clear all',
                action: 'clear'
            }
        }
    ],

    updateContainer: function (container) {
        var hasItems = container.items.getCount() ? true : false;

        if (!this.emptyText) {
            this.setVisible(hasItems);
        } else {
            this.getEmptyLabel().setVisible(!hasItems);
            this.getClearButton().setDisabled(!hasItems);
        }
    },

    initComponent: function () {
        var me = this;

        this.dockedItems[0].title = me.title;
        this.items[0].items = me.content;
        this.items[1].text = me.emptyText;
        this.items[3].items = me.tools;

        this.callParent(arguments);

        this.getClearButton().on('click', function () {
            me.fireEvent('clearAllFilters');
        });

        if (!this.showClearButton) {
            this.getClearButton().hide();
        }

        this.getContainer().on('afterlayout', 'updateContainer', this);
    },

    getContainer: function () {
        return this.down('#itemsContainer')
    },

    getTools: function () {
        return this.down('#toolsContainer')
    },

    getClearButton: function () {
        return this.down('button[action="clear"]')
    },

    getEmptyLabel: function () {
        return this.down('#emptyLabel')
    }
});

/**
 * todo: move out!
 */
Ext.define('Uni.component.filter.view.FilterTopPanel', {
    extend: 'Uni.view.panel.FilterToolbar',
    alias: 'widget.filter-top-panel',
    title: 'Filters',
    setFilter: function (key, name, value, hideIcon) {
        var me = this,
            btnsContainer = me.getContainer(),
            btn = btnsContainer.down('button[name=' + key + ']');
        if (!_.isEmpty(btn)) {
            btn.setText(name + ': ' + value);
        } else if (!hideIcon) {
            btnsContainer.add(Ext.create('Uni.view.button.TagButton', {
                text: name + ': ' + value,
                name: key,
                listeners: {
                    closeclick: function () {
                        me.fireEvent('removeFilter', key);
                    }
                }
            }));
        } else {
            btnsContainer.add(Ext.create('Ext.button.Button', {
                text: name + ': ' + value,
                name: key,
                ui: 'tag'
            }));
        }
        this.updateContainer(this.getContainer());
    }
});

/**
 * @class Uni.form.NestedForm
 * TODO: Move functionality to Basic
 */
Ext.define('Uni.form.NestedForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.nested-form',

    initComponent: function () {
        this.callParent();
        this.getForm().monitor.selector = "[isFormField]:not([excludeForm]){ownerCt.getId() === '" + this.getId() + "'}";
    },

    getValues: function () {
        var values = this.callParent();
        this.items.each(function (item) {
            if (_.isFunction(item.getValues)) values[item.name] = item.getValues();
        });
        return values;
    },

    setValues: function (data) {
        this.form.setValues(data);
        this.items.each(function (item) {
            if (!_.isEmpty(item.name) && _.has(data, item.name)) {
                if (_.isFunction(item.setValues) && _.isObject(data[item.name])) {
                    item.setValues(data[item.name]);
                }
            }
        });
    },

    loadRecord: function (record) {
        this.form._record = record;
        var data = this.form.hydrator ? this.form.hydrator.extract(record) : record.getData();
        return this.setValues(data);
    },

    updateRecord: function (record) {
        record = record || this.getRecord();
        var data = this.getValues();
        record.beginEdit();
        this.form.hydrator ? this.form.hydrator.hydrate(data, record) : record.set(data);
        record.endEdit();
        return this;
    }
});

Ext.define('Uni.override.ux.window.Notification', {
    override: 'Ext.ux.window.Notification',
    title: false,
    position: 't',
    stickOnClick: false,
    closable: false,
    ui: 'notification'
});

/**
 * @class Uni.Loader
 *
 * Loader class for the Unifying JS project, it makes sure every dependency is
 * ready to use.
 */
Ext.define('Uni.Loader', {
    scriptLoadingCount: 0,

    requires: [
        'Ext.tip.QuickTipManager',
        'Ext.layout.container.Absolute',
        'Ext.data.proxy.Rest',
        'Ext.state.CookieProvider',

        'Uni.About',
        'Uni.I18n',
        'Uni.Auth',

        'Uni.controller.Acknowledgements',
        'Uni.controller.Configuration',
        'Uni.controller.Error',
        'Uni.controller.Navigation',
        'Uni.controller.Notifications',
        'Uni.controller.Portal',
        'Uni.controller.Search',

        'Uni.view.form.field.Vtypes',
        'Uni.component.filter.view.FilterTopPanel',
        'Uni.form.NestedForm',

        'Uni.override.ServerOverride',
        'Uni.override.ApplicationOverride',
        'Uni.override.ButtonOverride',
        'Uni.override.CheckboxOverride',
        'Uni.override.FieldBaseOverride',
        'Uni.override.FieldContainerOverride',
        'Uni.override.NumberFieldOverride',
        'Uni.override.JsonWriterOverride',
        'Uni.override.RestOverride',
        'Uni.override.StoreOverride',
        'Uni.override.GridPanelOverride',
        'Uni.override.FormOverride',
        'Uni.override.form.field.ComboBox',
        'Uni.override.ModelOverride',
        'Uni.override.FieldSetOverride',

        'Uni.override.form.field.Base',
        'Uni.override.form.field.ComboBox',
        'Uni.override.form.field.Date',
        'Uni.override.form.field.FieldContainer',
        'Uni.override.form.Label',
        'Uni.override.form.Panel',
        'Uni.override.grid.plugin.BufferedRenderer',
        'Uni.override.grid.Panel',
        'Uni.override.menu.Item',
        'Uni.override.panel.Header',
        'Uni.override.panel.Panel',
        'Uni.override.ux.window.Notification',
        'Uni.override.view.Table',
        'Uni.override.window.MessageBox'
    ],

    /**
     * Initializes the internationalization components that should be used during loading.
     *
     * @param {String} components Components to load
     */
    initI18n: function (components) {
        // The I18n singleton is not initialized here because there is no guarantee
        // this method will be called since it is optional.
        Uni.I18n.init(components);
    },

    /**
     * Used during development to load in paths for packages.
     *
     * @example
     *     [
     *         {
     *             name: 'Cfg',
     *             controller: 'Cfg.controller.Main',
     *             path: '../../apps/cfg/app'
     *         },
     *         {
     *             name: 'Mdc',
     *             controller: 'Mdc.controller.Main',
     *             path: '../../apps/mdc/app'
     *         }
     *     ]
     *
     * @param {Object[]} packages Packages to initialize
     */
    initPackages: function (packages) {
        for (var i = 0; i < packages.length; i++) {
            var pkg = packages[i];
            Ext.Loader.setPath(pkg.name, pkg.path);
        }
    },

    onReady: function (callback) {
        var me = this;

        me.loadFont();
        me.loadTooltips();
        me.loadStateManager();
        me.loadStores();
        me.loadVtypes();

        callback();
    },

    loadFont: function () {
        Ext.setGlyphFontFamily('icomoon');
    },

    loadTooltips: function () {
        Ext.tip.QuickTipManager.init();
    },

    loadStateManager: function () {
        Ext.state.Manager.setProvider(Ext.create('Ext.state.CookieProvider'));
    },

    loadStores: function () {
        Ext.require('Uni.store.Apps');
        Ext.require('Uni.store.AppItems');
        Ext.require('Uni.store.Notifications');
    },

    loadVtypes: function () {
        Ext.create('Uni.view.form.field.Vtypes').init();
    },

    loadStyleSheet: function (href) {
        var fileref = document.createElement('link');
        fileref.setAttribute('rel', 'stylesheet');
        fileref.setAttribute('type', 'text/css');
        fileref.setAttribute('href', href);

        document.getElementsByTagName('head')[0].appendChild(fileref);
    }

});

/**
 * @class Uni.component.filter.model.Filter
 * @deprecated
 *
 * @use Uni.data.proxy.QueryStringProxy together with Uni.util.Hydrator instead
 *
 * Filter model extends Ext.data.Model.
 * Model allows you to retrieve model data as plain object {one level key-value pair}.
 *
 */
Ext.define('Uni.component.filter.model.Filter', {
    extend: 'Ext.data.Model',

    /**
     * Returns array of fields and association names
     *
     * @returns {String[]}
     */
    getFields: function() {
        var fields = [];
        this.fields.each(function(field){
            fields.push(field.name);
        });

        this.associations.each(function(association){
            fields.push(association.name);
        });

        return fields;
    },

    /**
     * Returns plain object with the associated data
     *
     * @returns {Object}
     */
    getPlainData: function() {
        var me = this,
            data = this.getData(true);

        this.associations.each(function (association) {
            switch (association.type) {
                case 'hasOne':
                    data[association.name] = me.extractHasOne(me[association.getterName](), association);
                    break;
                case 'hasMany':
                    data[association.name] = me.extractHasMany(me[association.name](), association);
                    break;
            }
        });

        // filter out empty values
        _.each(data, function(elm, key){
            if (!elm) {
                delete data[key];
            }
        });

        return data;
    },

    /**
     * Extracts data from the association object to the Integer
     *
     * @param record The associated record
     *
     * @returns {Number}
     */
    extractHasOne: function(record) {
        return record ? record.getId() : false;
    },

    /**
     * Extracts data from the store to the array
     *
     * @param store The associated store
     *
     * @returns {Number[]}
     */
    extractHasMany: function(store) {
        var result = [];
        store.each(function(record){
            result.push(record.getId());
        });

        return result;
    },

    removeFilterParam: function(key, id) {
        if (id) {
            var store = this[key]();
            var rec = store.getById(id);
            if (rec) {
                store.remove(rec);
            }
        } else if (!_.isUndefined(this.data[key])){
            delete this.data[key];
        }
    }
});

/**
 * @class Uni.component.filter.store.Filterable
 * @deprecated
 *
 * Filterable store is a mixin that allow you to bind Filter model (See: {@Link Uni.component.filter.model.Filter})
 * to the store, and retrieve plain (ready for sending via configured proxy) data from this model;
 *
 * Class fires "updateProxyFilter" event with an filter model parameter.
 */
Ext.define('Uni.component.filter.store.Filterable', {

    proxyFilter: null,

    /**
     * @param filter Uni.component.filter.model.Filter
     */
    setProxyFilter: function(filter) {
        if (!filter instanceof Uni.component.filter.model.Filter) {
            Ext.Error.raise('!filter instanceof Uni.component.filter.model.Filter');
        }

        this.proxyFilter = filter;
        this.updateProxyFilter();
    },

    /*
     * @returns {Uni.component.filter.model.Filter}
     */
    getProxyFilter: function() {
        return this.proxyFilter;
    },

    updateProxyFilter: function() {
        this.load();
        this.fireEvent('updateProxyFilter', this.proxyFilter);
    },

    getFilterParams: function() {
        return this.proxyFilter.getPlainData();
    }
});

/**
 * @class Uni.component.filter.view.Filter
 * @deprecated
 *
 * Filter panel is an extension over Ext js form panel See {@link Ext.form.Panel}.
 *
 * Filter panel fixes form data binding (loading data from model to the form and update model from form's data).
 * Panel allows you to bind models that contains associations (hasOne, hasMany). Note that form should contain
 * components with the binded stores (See: {@link Ext.util.Bindable}) to properly fetch data between models and have name
 * the same as association name.
 *
 * @Example
 *
 *    Ext.define('App.view.Filter', {
 *      extend: 'Uni.component.filter.view.Filter',
 *      alias: 'widget.filter',
 *      title: 'Filter',
 *
 *      items: [{
 *          xtype: 'combobox',
 *          name: 'reason',
 *          fieldLabel: 'Reason',
  *         displayField: 'name',
 *          valueField: 'id',
 *          store: 'App.store.Reason',
 *      }]
 *   });
 *
 * Ext.define('App.model.IssueFilter', {
 *     extend: 'Uni.component.filter.model.Filter',
 *
 *     requires: [
 *         'App.model.Reason'
 *     ],
 *
 *     hasOne: [{
 *         model: 'App.model.Reason',
 *         associationKey: 'reason',
 *         name: 'reason'
 *     }],
 * });
 *
 * // Now model App.model.IssueFilter can be binded to Filter panel as usual:
 *
 * var model = new App.model.IssueFilter();
 * var filter = new App.view.Filter();
 *
 * filter.loadRecord(model);
 *
 * // After form chandes by user you can update binded filter model by calling^
 *
 * var filterModel = form.getRecord();
 * form.updateRecord(filterModel);
 *
 */
Ext.define('Uni.component.filter.view.Filter', {
    extend: 'Ext.form.Panel',
    alias: 'widget.filter-form',
    applyKey: 13,
    /**
     * @override
     *
     * Load data to the form of the model with associations.
     *
     * @param filter Uni.component.filter.model.Filter
     * @returns {Ext.form.Basic}
     */
    loadRecord: function(filter) {
        var me = this,
            data = filter.getData(true);

        this.callParent([filter]);
        filter.associations.each(function (association) {
            switch (association.type) {
                case 'hasOne':
                    data[association.name] = me.extractHasOne(filter[association.getterName].call(filter));
                    break;
                case 'hasMany':
                    data[association.name] = me.extractHasMany(filter[association.name]());
                    break;
            }
        });

        return this.getForm().setValues(data);
    },

    /**
     * Extracts data from the association object to the Integer
     *
     * @param record The associated record
     *
     * @returns {Number}
     */
    extractHasOne: function (record) {
        return record ? record.getId() : null;
    },

    /**
     * Extracts data from the store to the array
     *
     * @param store The associated store
     *
     * @returns {Number[]}
     */
    extractHasMany: function (store) {
        var result = [];

        store.each(function(record){
            result.push(record.getId());
        });

        return result;
    },

    /**
     * @override
     *
     * Extracts data from the form and bind to the filter model
     *
     * @param record The associated filter record
     * @returns {Uni.component.filter.view.Filter}
     */
    updateRecord: function (record) {
        this.callParent([record]);
        var me = this,
            values = this.getValues();

        record = record || this._record;
        record.associations.each(function (association) {
            switch (association.type) {
                case 'hasOne':
                    me.hydrateHasOne(record, association, values);
                    break;
                case 'hasMany':
                    me.hydrateHasMany(record, association, values);
                    break;
            }
        });

        return this;
    },

    /**
     * Hydrates array data to the associated model field
     *
     * @param record Filter model
     * @param association selected association
     * @param values data array
     */
    hydrateHasOne: function (record, association, values) {
        var name = association.name,
            cmp = this.down('[name="' + name + '"]');

        if (!values[name]) {
            record[association.setterName](Ext.create(association.model));
        } else if (cmp && cmp.mixins.bindable) {
            var store = cmp.getStore();
            var rec = store.getById(values[name]);
            record[association.setterName](rec);
        }

        return this;
    },

    /**
     * Hydrates array data to the associated model store
     *
     * @param record Filter model
     * @param association selected association
     * @param values data array
     */
    hydrateHasMany: function (record, association, values) {
        var name = association.name,
            store = record[name](),
            cmp = this.down('[name="' + name + '"]');

        if (!values[name]) {
            store.removeAll();

        } else if (cmp && cmp.mixins.bindable && values[name]) {
            var cmpStore = cmp.getStore();

            if (!_.isArray(values[name])) {
                values[name] = [values[name]];
            }

            var records = _.map(values[name], function (value) {
                return cmpStore.getById(value);
            });
            store.loadRecords(records, {});
        }

        return this;
    },

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
        me.on('afterrender', function (form) {
            var el = form.getEl();
            el.on('keypress', function (e, t) {
                (e.getKey() == me.applyKey) && (me.fireEvent('applyfilter', {me: me, key: me.applyKey, t: t}));
            })
        })
    }
});

/**
 * @class Uni.component.sort.model.Sort
 *
 * Model of sorting params.
 * params should be set via model fields:
 *
 * @Example
 *
 *  Ext.define('App.model.Sort', {
 *      extend: 'Uni.component.sort.model.Sort',
 *      fields: [{
 *           name: 'dueDate',
 *           displayValue: 'Due date'
 *      }, {
 *          name: 'created',
 *          displayValue: 'Created time'
 *      }]
 *  });
 *
 *  var sort = new App.model.Sort();
 *
 *  sort.addSortParam('dueDate');
 *  sort.addSortParam('created', App.model.Sort.ASC);
 *
 *  Model allows you to get plain data
 *
 *  @Example
 *
 *  {
 *      'sort': ['dueDate', '-created']
 *  }
 *
 */
Ext.define('Uni.component.sort.model.Sort', {
    extend: 'Ext.data.Model',

    inheritableStatics: {
        /**
         * @property
         * @static
         */
        ASC : 'asc',

        /**
         * @property
         * @static
         * @private
         */
        DESC: 'desc'
    },

    defaultOrder: 'ASC',

    /**
     * returned object key property
     */
    key: 'sort',

    /**
     * Returns array of fields and association names
     *
     * @returns {String[]}
     */
    getFields: function() {
        return [this.key];
    },

    /**
     * Add sorting param to the model. If sort order is not present it uses default value.
     *
     * @param key sorting key
     * @param order sorting order
     */
    addSortParam: function(key, order) {
        order = order || this.statics()[this.defaultOrder];

        var field = this.fields.getByKey(key);
        if (field) {
            this.set(key, order);
        }
    },

    /**
     * Toddles sorting direction of the specified param
     *
     * @param key sorting key
     */
    toggleSortParam: function(key) {
        var field = this.fields.getByKey(key);

        if (field) {
            var order = this.get(key) == this.statics().ASC
                ? this.statics().DESC
                : this.statics().ASC
            ;

            this.set(key, order);
        }
    },

    /**
     * Removes param from sorting
     *
     * @param key sorting key
     */
    removeSortParam: function(key) {
        delete this.data[key];
    },

    /**
     * Returns plain object of sorting params
     *
     * @returns {Object}
     */
    getPlainData: function() {
        var data = this.getData(),
            map = {};

        map[this.statics().ASC] = '';
        map[this.statics().DESC] = '-';

        var params = [];
        _.each(data, function(item, key) {
            if (_.contains(_.keys(map), item)) {
                params.push(map[item] + key);
            }
        });

        var result = {};
        result[this.key] = params;

        return result;
    }
});

/**
 * @class Uni.component.sort.store.Sortable
 *
 * Sortable store is a mixin that allow you to bind Sort model (See: {@Link Uni.component.sort.model.Sort})
 * to the store, and retrieve plain (ready for sending via configured proxy) data from this model;
 *
 * Class fires "updateProxySort" event with an sort model parameter.
 */
Ext.define('Uni.component.sort.store.Sortable', {

    proxySort: null,

    /**
     * @param sortModel Uni.component.filter.model.Filter
     */
    setProxySort: function(sortModel) {
        if (!sortModel instanceof Uni.component.filter.model.Filter) {
            Ext.Error.raise('!sortModel instanceof Uni.component.filter.model.Filter');
        }

        this.proxySort = sortModel;
        this.updateProxySort();
    },

    /*
     * @returns {Uni.component.filter.model.Filter}
     */
    getProxySort: function() {
        return this.proxySort;
    },

    updateProxySort: function() {
        this.load();
        this.fireEvent('updateProxySort', this.proxySort);
    },

    getSortParams: function() {
        return this.proxySort.getPlainData();
    }
});

/**
 * @class Uni.controller.AppController
 */
Ext.define('Uni.controller.AppController', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        },
        {
            ref: 'contentPanel',
            selector: 'viewport > #contentPanel'
        },
        {
            ref: 'logo',
            selector: 'viewport uni-nav-logo'
        }
    ],

    /**
     * @cfg {String} applicationTitle
     *
     * The title to be used across the application.
     */
    applicationTitle: 'Connexo',

    /**
     * @cfg {String} defaultToken
     *
     * The default history token the application needs to use.
     */
    defaultToken: '',

    /**
     * @cfg {Boolean} searchEnabled
     *
     * Whether the search button shows or not in the application header.
     * True by default.
     */
    searchEnabled: true,

    /**
     * @cfg {String[]} privileges
     * The privileges that allow user to access application.
     * Empty by default.
     */
    privileges: [],

    /**
     * @cfg {Object[]} packages
     *
     * The packages that need to be loaded in by the application.
     *
     */
    packages: [],

    init: function () {

        var me = this;
        if (Uni.Auth.hasAnyPrivilege(me.privileges)){
            me.initCrossroads();

            me.getController('Uni.controller.Navigation').applicationTitle = me.applicationTitle;
            me.getController('Uni.controller.Navigation').searchEnabled = me.searchEnabled;
            me.getController('Uni.controller.history.EventBus').setDefaultToken(me.defaultToken);
            me.getApplication().on('changecontentevent', me.showContent, me);
            me.getApplication().on('sessionexpired', me.redirectToLogin, me);

            me.loadControllers();
            me.callParent(arguments);
        }
    },

    /**
     * Makes crossroads ignore state so that applications that don't use crossroads
     * have no influence on crossroads' behavior.
     */
    initCrossroads: function () {
        crossroads.ignoreState = true;
    },

    onLaunch: function () {
        var me = this,
            logo = me.getLogo();

        if (logo.rendered) {
            logo.setText(me.applicationTitle);
        } else {
            logo.text = me.applicationTitle;
        }

        me.callParent(arguments);
    },

    showContent: function (widget) {
        this.getContentPanel().removeAll();
        this.getContentPanel().add(widget);
        this.getContentPanel().doComponentLayout();
    },

    redirectToLogin: function () {
        window.location = '/apps/login/index.html?expired&page='
            + window.location.pathname
            + window.location.hash;
    },

    loadControllers: function () {
        for (var i = 0; i < this.controllers.length; i++) {
            var controller = this.controllers[i];

            try {
                this.getController(controller);
            } catch (ex) {
                console.error('Could not load the \'' + controller + '\' controller.');
            }
        }
    }
});

/**
 * @class Uni.controller.history.Converter
 */
Ext.define('Uni.controller.history.Converter', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.EventBus'
    ],

    rootToken: null, // Implemented by extending classes.

    init: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
        this.callParent(arguments);
    },

    tokenize: function (tokens, includeHash) {
        includeHash = includeHash !== undefined ? includeHash : true;

        var token = '',
            delimiter = Uni.controller.history.Settings.tokenDelimiter;

        for (var i = 0; i < tokens.length; i++) {
            token += delimiter + tokens[i];
        }

        if (includeHash) {
            token = '#' + token;
        }

        return token;
    },

    tokenizePath: function (path, includeHash) {
        includeHash = includeHash !== undefined ? includeHash : true;
        if (includeHash) {
            path = '#' + path;
        }
        return path;
    },

    /**
     * Default tokenize method for an overview.
     * @returns String History token.
     */
    tokenizeShowOverview: function () {
        return this.tokenize([this.rootToken]);
    }
});

/**
 * @class Uni.controller.history.Settings
 *
 * History settings that mainly holds static history conversion settings.
 */
Ext.define('Uni.controller.history.Settings', {
    statics: {
        tokenDelimiter: '/'
    }
});

/**
 * @deprecated
 *
 * @class Uni.data.model.Filter
 * @use Ext.data.Model instead
 */
Ext.define('Uni.data.model.Filter', {
    extend: 'Ext.data.Model'
});

/**
 * @class Uni.util.Application
 */
Ext.define('Uni.util.Application', {
    singleton: true,

    appPath: 'app',

    getAppNamespace: function () {
        var paths = Ext.Loader.getConfig().paths;

        for (var name in paths) {
            if (paths.hasOwnProperty(name) && paths[name] === this.appPath) {
                return name;
            }
        }

        return undefined;
    }
});

/**
 * @class Uni.util.History
 */
Ext.define('Uni.util.History', {
    singleton: true,

    routerController: 'Uni.controller.history.Router',

    requires: [
        'Uni.util.Application'
    ],

    suspendEventsForNextCall: function () {
        var currentHref = location.href;

        Ext.util.History.suspendEvents();

        new Ext.util.DelayedTask(function () {
            if (location.href !== currentHref) {
                Ext.util.History.resumeEvents();
                this.stopped = true;
            }
        }).delay(100);
    },

    getRouterController: function () {
        var me = this,
            appPath = Ext.String.htmlEncode(Uni.util.Application.appPath),// Better safe than sorry, so encoding these.
            namespace = Ext.String.htmlEncode(Uni.util.Application.getAppNamespace()),
            evalCode = namespace + '.' + appPath + '.getController(\'' + me.routerController + '\')';

        if (typeof namespace !== 'undefined') {
            try {
                return eval(evalCode + ';');
            } catch (ex) {
                return evalCode;
            }
        }

        return Ext.create(me.routerController);
    }
});

/**
 * @class Uni.data.proxy.QueryStringProxy
 *
 * Uses URL query string as data storage.
 * The model is serialized and deserialized over JSON encode/decode

 * # Example Usage
 *
 *     Ext.define('User', {
 *         extend: 'Ext.data.Model',
 *         fields: ['firstName', 'lastName'],
 *         proxy: {
 *             type: 'querystring',
 *             root: 'filter'
 *         }
 *     });
 */
Ext.define('Uni.data.proxy.QueryStringProxy', {
    extend: 'Ext.data.proxy.Proxy',
    alias: 'proxy.querystring',

    /**
     * @cfg {String} root
     * The root from which to read and save data
     */
    root: '',

    router: null,

    requires: [
        'Uni.util.History'
    ],

    writer: {
        type: 'json',
        writeRecordId: false
    },

    constructor: function (config) {
        config = config || {};
        this.callParent(arguments);
        if (config.hydrator) {
            this.hydrator = Ext.create(config.hydrator);
        }
        this.router = config.router || Uni.util.History.getRouterController();
    },

    create: function () {
        this.setQueryParams.apply(this, arguments);
    },

    update: function () {
        this.setQueryParams.apply(this, arguments);
    },

    /**
     * Deserializes model from the URL via router
     *
     * @param operation
     * @param callback
     * @param scope
     */
    read: function (operation, callback, scope) {
        var me = this,
            router = me.router,
            Model = me.model;

        operation.setStarted();

        if (!_.isUndefined(router.queryParams[me.root])) {
            var data = Ext.decode(router.queryParams[me.root], true);

            if (this.hydrator) {
                var record = Ext.create(Model);
                this.hydrator.hydrate(data, record);

                operation.resultSet = Ext.create('Ext.data.ResultSet', {
                    records: [record],
                    total: 1,
                    loaded: true,
                    success: true
                });
            } else {
                operation.resultSet = me.reader.read(data);
            }

            operation.setSuccessful();
        }

        operation.setCompleted();

        if (!operation.wasSuccessful()) {
            me.fireEvent('exception', me, null, operation);
        }

        Ext.callback(callback, scope || me, [operation]);
    },

    /**
     * removes params
     */
    destroy: function () {
        var router = this.router;
        delete router.queryParams[this.root];

        //should redirect be performed via proxy? How it will work with another models, like sorting?
        router.getRoute().forward();
    },

    /**
     * Serializes model to the URL via router
     *
     * @param operation
     * @param callback
     * @param model
     */
    setQueryParams: function (operation, callback, model) {
        var router = this.router,
            queryParams = {};

        operation.setStarted();

        var data = this.hydrator
            ? this.hydrator.extract(model)
            : this.writer.getRecordData(model);

        //todo: clean empty data!
        model.commit();

        operation.setCompleted();
        operation.setSuccessful();

        queryParams[this.root] = Ext.encode(data);
        router.getRoute().forward(null, queryParams);
    }
});

/**
 * Extend from this store if you want to use model for filtering
 */
Ext.define('Uni.data.store.Filterable', {
    extend: 'Ext.data.Store',

    remoteFilter: true,
    hydrator: null,

    /**
     * Initialises filters from filter model
     * @param config
     */
    constructor: function (config) {
        var me = this;

        config = config || {};

        me.callParent(arguments);

        var router = me.router = config.router || Uni.util.History.getRouterController();

        if (me.hydrator && Ext.isString(me.hydrator)) {
            me.hydrator = Ext.create(me.hydrator);
        }

        router.on('routematch', function () {
            if (router.filter) {
                me.setFilterModel(router.filter);
            }
        });
    },

    /**
     * returns data in a format of filter:
     * [{property: key, value: item}]
     */
    setFilterModel: function (model) {
        var me = this,
            data = me.hydrator ? me.hydrator.extract(model) : model.getData(),
            filters = [];

        _.map(data, function (item, key) {
            if (item) {
                filters.push({property: key, value: item});
            }
        });

        me.clearFilter(true);
        me.addFilter(filters, false);
    }
});


/**
 * @class Uni.form.field.StartPeriod
 */
Ext.define('Uni.form.field.StartPeriod', {
    extend: 'Ext.form.RadioGroup',
    xtype: 'uni-form-field-startperiod',

    fieldLabel: Uni.I18n.translate('form.field.startPeriod.label', 'UNI', 'From'),
    columns: 1,
    vertical: true,

    baseRadioName: undefined,

    /**
     * @cfg showOptionNow
     *
     * Determines whether to show the now option, defaults to true.
     */
    showOptionNow: true,

    /**
     * @cfg showOptionDate
     *
     * Determines whether to show the custom date option, defaults to true.
     */
    showOptionDate: true,

    inputValueNow: 'now',
    inputValueAgo: 'ago',
    inputValueDate: 'date',

    lastTask: undefined,
    selectedValue: undefined,

    initComponent: function () {
        var me = this;

        me.buildItems();
        me.callParent(arguments);

        me.on('afterrender', me.initListeners, me);
    },

    buildItems: function () {
        var me = this;

        me.baseRadioName = me.getId() + 'startperiod';

        me.items = [];

        if (me.showOptionNow) {
            me.items.push({
                boxLabel: Uni.I18n.translate('form.field.startPeriod.optionNow.label', 'UNI', 'Now'),
                itemId: 'option-now',
                name: me.baseRadioName,
                inputValue: me.inputValueNow,
                margin: '0 0 6 0',
                value: true
            });
        }

        if (me.showOptionDate) {
            me.items.push({
                xtype: 'container',
                itemId: 'option-date',
                layout: 'hbox',
                margin: '0 0 6 0',
                name: 'rb',
                items: [
                    {
                        xtype: 'radio',
                        name: me.baseRadioName,
                        inputValue: me.inputValueDate
                    },
                    {
                        xtype: 'datefield',
                        name: 'start-date',
                        allowBlank: false,
                        editable: false,
                        value: new Date(),
                        maxValue: new Date(),
                        width: 128,
                        margin: '0 0 0 6'
                    }
                ]
            });
        }

        me.items.push({
            xtype: 'container',
            itemId: 'option-ago',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'radio',
                    name: me.baseRadioName,
                    inputValue: me.inputValueAgo,
                    value: !me.showOptionNow
                },
                {
                    xtype: 'numberfield',
                    name: 'frequency',
                    hideLabel: true,
                    value: 1,
                    minValue: 1,
                    editable: false,
                    allowBlank: false,
                    width: 64,
                    margin: '0 6 0 6'
                },
                {
                    xtype: 'combobox',
                    name: 'period-interval',
                    displayField: 'name',
                    valueField: 'value',
                    queryMode: 'local',
                    editable: false,
                    hideLabel: true,
                    value: 'months',
                    width: 200,
                    margin: '0 6 0 0',
                    store: new Ext.data.Store({
                        fields: ['name', 'value'],
                        data: (function () {
                            return [
                                {name: Uni.I18n.translate('period.months', 'UNI', 'Month(s)'), value: 'months'},
                                {name: Uni.I18n.translate('period.weeks', 'UNI', 'Week(s)'), value: 'weeks'},
                                {name: Uni.I18n.translate('period.days', 'UNI', 'Day(s)'), value: 'days'},
                                {name: Uni.I18n.translate('period.hours', 'UNI', 'Hour(s)'), value: 'hours'},
                                {name: Uni.I18n.translate('period.minutes', 'UNI', 'Minute(s)'), value: 'minutes'}
                            ];
                        })()
                    }),
                    allowBlank: false,
                    forceSelection: true
                },
                {
                    xtype: 'label',
                    text: Uni.I18n.translate('form.field.startPeriod.optionAgo.label', 'UNI', 'ago'),
                    cls: Ext.baseCSSPrefix + 'form-item-label',
                    style: {
                        fontWeight: 'normal'
                    }
                }
            ]
        });
    },

    initListeners: function () {
        var me = this;

        if (me.showOptionNow) {
            me.selectedValue = 'now';

            me.getOptionNowRadio().on('change', function (scope, newValue, oldValue) {
                if (newValue) {
                    me.selectedValue = 'now';
                    me.fireEvent('periodchange', me.getStartValue());
                }
            }, me);
        } else {
            me.selectedValue = 'ago';
        }

        me.getOptionAgoRadio().on('change', function (scope, newValue, oldValue) {
            if (newValue) {
                me.selectedValue = 'ago';

                if (me.showOptionDate) {
                    me.getOptionDateRadio().suspendEvents();
                    me.getOptionDateRadio().setValue(false);
                    me.getOptionDateRadio().resumeEvents();
                }

                if (me.showOptionNow) {
                    me.getOptionNowRadio().suspendEvents();
                    me.getOptionNowRadio().setValue(false);
                    me.getOptionNowRadio().resumeEvents();
                }

                me.fireEvent('periodchange', me.getStartValue());
            }
        }, me);

        me.getOptionAgoContainer().down('numberfield').on('change', function () {
            if (me.lastTask) {
                me.lastTask.cancel();
            }

            me.lastTask = new Ext.util.DelayedTask(function () {
                me.selectOptionAgo();
            });

            me.lastTask.delay(256);
        }, me);

        me.getOptionAgoContainer().down('combobox').on('change', function () {
            me.selectOptionAgo();
        }, me);

        if (me.showOptionDate) {
            me.getOptionDateRadio().on('change', function (scope, newValue, oldValue) {
                if (newValue) {
                    me.selectedValue = 'date';
                    me.fireEvent('periodchange', me.getStartValue());
                }
            }, me);

            me.getOptionDateContainer().down('datefield').on('change', function () {
                me.selectOptionDate();
            }, me);
        }
    },

    selectOptionNow: function (suspendEvent) {
        this.selectedValue = 'now';

        this.getOptionNowRadio().suspendEvents();
        this.getOptionNowRadio().setValue(true);
        this.getOptionNowRadio().resumeEvents();

        if (!suspendEvent) {
            this.fireEvent('periodchange', this.getStartValue());
        }
    },

    selectOptionAgo: function (suspendEvent) {
        this.selectedValue = 'ago';

        this.getOptionAgoRadio().suspendEvents();
        this.getOptionAgoRadio().setValue(true);
        this.getOptionAgoRadio().resumeEvents();

        if (!suspendEvent) {
            this.fireEvent('periodchange', this.getStartValue());
        }
    },

    selectOptionDate: function (suspendEvent) {
        this.selectedValue = 'date';

        this.getOptionDateRadio().suspendEvents();
        this.getOptionDateRadio().setValue(true);
        this.getOptionDateRadio().resumeEvents();

        if (!suspendEvent) {
            this.fireEvent('periodchange', this.getStartValue());
        }
    },

    getOptionNowRadio: function () {
        return this.down('#option-now');
    },

    getOptionAgoRadio: function () {
        return this.getOptionAgoContainer().down('radio');
    },

    getOptionDateRadio: function () {
        return this.getOptionDateContainer().down('radio');
    },

    getOptionAgoContainer: function () {
        return this.down('#option-ago');
    },

    getOptionDateContainer: function () {
        return this.down('#option-date');
    },

    getStartValue: function () {
        var me = this,
            selectedValue = me.selectedValue,
            amountAgoValue = me.getOptionAgoContainer().down('numberfield').getValue(),
            freqAgoValue = me.getOptionAgoContainer().down('combobox').getValue();

        var result = {
            startNow: selectedValue === 'now'
        };

        if (selectedValue === 'date') {
            var dateValue = me.getOptionDateContainer().down('datefield').getValue();

            var fixedDate = {
                startFixedDay: dateValue.getDate(),
                startFixedMonth: dateValue.getMonth() + 1,
                startFixedYear: dateValue.getFullYear()
            };
            Ext.apply(result, fixedDate);
        } else if (selectedValue === 'ago') {
            var shiftDate = {
                startAmountAgo: amountAgoValue,
                startPeriodAgo: freqAgoValue
            };
            Ext.apply(result, shiftDate);
        }

        return result;
    }
});

/**
 * @class Uni.form.field.OnPeriod
 */
Ext.define('Uni.form.field.OnPeriod', {
    extend: 'Ext.form.RadioGroup',
    xtype: 'uni-form-field-onperiod',

    fieldLabel: Uni.I18n.translate('form.field.onPeriod.label', 'UNI', 'On'),
    columns: 1,
    vertical: true,

    baseRadioName: undefined,
    selectedValue: undefined,

    initComponent: function () {
        var me = this;

        me.buildItems();
        me.callParent(arguments);

        me.on('afterrender', me.initListeners, me);
    },

    buildItems: function () {
        var me = this;

        me.baseRadioName = me.getId() + 'onperiod';
        me.selectedValue = 'currentday';

        me.items = [
            {
                boxLabel: Uni.I18n.translate('form.field.onPeriod.optionCurrent.label', 'UNI', 'Current day of the month'),
                itemId: 'option-current',
                name: me.baseRadioName,
                inputValue: 'currentday',
                margin: '0 0 6 0',
                value: true
            },
            {
                xtype: 'container',
                itemId: 'option-dom',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'radio',
                        name: me.baseRadioName,
                        inputValue: 'dayofmonth',
                        margin: '0 6 0 0'
                    },
                    {
                        xtype: 'label',
                        text: Uni.I18n.translate('form.field.onPeriod.optionDayOfMonth.day', 'UNI', 'Day'),
                        cls: Ext.baseCSSPrefix + 'form-item-label',
                        style: {
                            fontWeight: 'normal'
                        }
                    },
                    {
                        xtype: 'combobox',
                        name: 'period-interval',
                        displayField: 'name',
                        valueField: 'value',
                        queryMode: 'local',
                        editable: false,
                        hideLabel: true,
                        value: 1,
                        width: 64,
                        margin: '0 6 0 6',
                        store: new Ext.data.Store({
                            fields: ['name', 'value'],
                            data: (function () {
                                var data = [];

                                for (var i = 1; i < 29; i++) {
                                    data.push({
                                        name: i,
                                        value: i
                                    });
                                }

                                data.push({
                                    name: 'Last',
                                    value: 31
                                });

                                return data;
                            })()
                        }),
                        allowBlank: false,
                        forceSelection: true
                    },
                    {
                        xtype: 'label',
                        text: Uni.I18n.translate('form.field.onPeriod.optionDayOfMonth.month', 'UNI', 'of the month'),
                        cls: Ext.baseCSSPrefix + 'form-item-label',
                        style: {
                            fontWeight: 'normal'
                        }
                    }
                ]
            },
            {
                xtype: 'container',
                itemId: 'option-dow',
                layout: 'hbox',
                margin: '6 0 0 0',
                items: [
                    {
                        xtype: 'radio',
                        name: me.baseRadioName,
                        inputValue: 'dayofweek'
                    },
                    {
                        xtype: 'combobox',
                        name: 'period-interval',
                        displayField: 'name',
                        valueField: 'value',
                        queryMode: 'local',
                        editable: false,
                        hideLabel: true,
                        value: 1,
                        width: 128,
                        margin: '0 6 0 6',
                        store: new Ext.data.Store({
                            fields: ['name', 'value'],
                            data: (function () {
                                // TODO Create a days of week store.
                                return [
                                    {name: Uni.I18n.translate('general.day.monday', 'UNI', 'Monday'), value: 1},
                                    {name: Uni.I18n.translate('general.day.tuesday', 'UNI', 'Tuesday'), value: 2},
                                    {name: Uni.I18n.translate('general.day.wednesday', 'UNI', 'Wednesday'), value: 3},
                                    {name: Uni.I18n.translate('general.day.thursday', 'UNI', 'Thursday'), value: 4},
                                    {name: Uni.I18n.translate('general.day.friday', 'UNI', 'Friday'), value: 5},
                                    {name: Uni.I18n.translate('general.day.saturday', 'UNI', 'Saturday'), value: 6},
                                    {name: Uni.I18n.translate('general.day.sunday', 'UNI', 'Sunday'), value: 7}
                                ];
                            })()
                        }),
                        allowBlank: false,
                        forceSelection: true
                    }
                ]
            }
        ];
    },

    initListeners: function () {
        var me = this;

        me.getOptionCurrentRadio().on('change', function (scope, newValue, oldValue) {
            if (newValue) {
                me.selectedValue = 'currentday';
                me.fireEvent('periodchange', me.getOnValue());
            }
        }, me);

        me.getOptionDayOfMonthRadio().on('change', function (scope, newValue, oldValue) {
            if (newValue) {
                me.selectedValue = 'dayofmonth';
                me.fireEvent('periodchange', me.getOnValue());
            }
        }, me);

        me.getOptionDayOfMonthContainer().down('combobox').on('change', function () {
            me.selectOptionDayOfMonth();
        }, me);

        me.getOptionDayOfWeekRadio().on('change', function (scope, newValue, oldValue) {
            if (newValue) {
                me.selectedValue = 'dayofweek';
                me.fireEvent('periodchange', me.getOnValue());
            }
        }, me);

        me.getOptionDayOfWeekContainer().down('combobox').on('change', function () {
            me.selectOptionDayOfWeek();
        }, me);
    },

    selectOptionCurrent: function (suspendEvent) {
        this.getOptionCurrentRadio().suspendEvents();
        this.getOptionCurrentRadio().setValue(true);
        this.getOptionCurrentRadio().resumeEvents();

        if (!suspendEvent) {
            this.fireEvent('periodchange', this.getValue());
        }
    },

    selectOptionDayOfMonth: function (suspendEvent) {
        this.selectedValue = 'dayofmonth';

        this.getOptionDayOfMonthRadio().suspendEvents();
        this.getOptionDayOfMonthRadio().setValue(true);
        this.getOptionDayOfMonthRadio().resumeEvents();

        if (!suspendEvent) {
            this.fireEvent('periodchange', this.getValue());
        }
    },

    selectOptionDayOfWeek: function (suspendEvent) {
        this.selectedValue = 'dayofweek';

        this.getOptionDayOfWeekRadio().suspendEvents();
        this.getOptionDayOfWeekRadio().setValue(true);
        this.getOptionDayOfWeekRadio().resumeEvents();

        if (!suspendEvent) {
            this.fireEvent('periodchange', this.getValue());
        }
    },

    getOptionCurrentRadio: function () {
        return this.down('#option-current');
    },

    getOptionDayOfMonthRadio: function () {
        return this.getOptionDayOfMonthContainer().down('radio');
    },

    getOptionDayOfWeekRadio: function () {
        return this.getOptionDayOfWeekContainer().down('radio');
    },

    getOptionDayOfMonthContainer: function () {
        return this.down('#option-dom');
    },

    getOptionDayOfWeekContainer: function () {
        return this.down('#option-dow');
    },

    setOptionCurrentDisabled: function (disabled) {
        var me = this;

        me.getOptionCurrentRadio().setDisabled(disabled);

        me.selectAvailableOption();
    },

    setOptionDayOfMonthDisabled: function (disabled) {
        var me = this,
            radio = me.getOptionDayOfMonthRadio(),
            combo = me.getOptionDayOfMonthContainer().down('combobox');

        radio.setDisabled(disabled);
        combo.setDisabled(disabled);

        if (disabled) {
            me.getOptionDayOfMonthContainer().addCls(Ext.baseCSSPrefix + 'item-disabled');
        } else {
            me.getOptionDayOfMonthContainer().removeCls(Ext.baseCSSPrefix + 'item-disabled');
        }

        me.selectAvailableOption();
    },

    setOptionDayOfWeekDisabled: function (disabled) {
        var me = this,
            radio = me.getOptionDayOfWeekRadio(),
            combo = me.getOptionDayOfWeekContainer().down('combobox');

        radio.setDisabled(disabled);
        combo.setDisabled(disabled);

        me.selectAvailableOption();
    },

    selectAvailableOption: function () {
        var me = this,
            dayRadio = me.getOptionCurrentRadio(),
            monthRadio = me.getOptionDayOfMonthRadio(),
            weekRadio = me.getOptionDayOfWeekRadio();

        if (!monthRadio.getValue() && dayRadio.getValue() && dayRadio.isDisabled()) {
            monthRadio.suspendEvents();
            monthRadio.setValue(true);
            monthRadio.resumeEvents();
        }

        if (!weekRadio.getValue() && monthRadio.getValue() && monthRadio.isDisabled()) {
            weekRadio.suspendEvents();
            weekRadio.setValue(true);
            weekRadio.resumeEvents();
        }

        if (!dayRadio.getValue() && weekRadio.getValue() && weekRadio.isDisabled()) {
            dayRadio.suspendEvents();
            dayRadio.setValue(true);
            dayRadio.resumeEvents();
        }
    },

    getOnValue: function () {
        var me = this,
            selectedValue = me.selectedValue,
            dayOfMonthValue = me.getOptionDayOfMonthContainer().down('combobox').getValue(),
            dayOfWeekValue = me.getOptionDayOfWeekContainer().down('combobox').getValue();

        var result = {
            onCurrentDay: selectedValue === 'currentday'
        };

        if (selectedValue === 'dayofmonth') {
            Ext.apply(result, {
                onDayOfMonth: dayOfMonthValue
            });
        } else if (selectedValue === 'dayofweek') {
            Ext.apply(result, {
                onDayOfWeek: dayOfWeekValue
            });
        }

        return result;
    }
});

/**
 * @class Uni.form.field.AtPeriod
 */
Ext.define('Uni.form.field.AtPeriod', {
    extend: 'Ext.form.FieldContainer',
    xtype: 'uni-form-field-atperiod',

    fieldLabel: Uni.I18n.translate('form.field.atPeriod.label', 'UNI', 'At'),

    layout: {
        type: 'hbox'
    },

    lastHourTask: undefined,
    lastMinuteTask: undefined,

    initComponent: function () {
        var me = this;

        me.buildItems();
        me.callParent(arguments);

        me.on('afterrender', me.initListeners, me);
    },

    buildItems: function () {
        var me = this;

        me.items = [
            {
                xtype: 'numberfield',
                itemId: 'hour-field',
                hideLabel: true,
                valueToRaw: me.formatDisplayOfTime,
                value: 0,
                minValue: 0,
                maxValue: 23,
                editable: false,
                allowBlank: false,
                width: 64,
                margin: '0 6 0 0'
            },
            {
                xtype: 'label',
                text: ':',
                cls: Ext.baseCSSPrefix + 'form-item-label',
                style: {
                    fontWeight: 'normal'
                }
            },
            {
                xtype: 'numberfield',
                itemId: 'minute-field',
                hideLabel: true,
                valueToRaw: me.formatDisplayOfTime,
                value: 0,
                minValue: 0,
                maxValue: 59,
                editable: false,
                allowBlank: false,
                width: 64,
                margin: '0 6 0 6'
            }
        ];
    },

    initListeners: function () {
        var me = this;

        me.getHourField().on('change', function () {
            if (me.lastHourTask) {
                me.lastHourTask.cancel();
            }

            me.lastHourTask = new Ext.util.DelayedTask(function () {
                me.fireEvent('periodchange', me.getValue());
            });

            me.lastHourTask.delay(256);
        }, me);

        me.getMinuteField().on('change', function () {
            if (me.lastMinuteTask) {
                me.lastMinuteTask.cancel();
            }

            me.lastMinuteTask = new Ext.util.DelayedTask(function () {
                me.fireEvent('periodchange', me.getValue());
            });

            me.lastMinuteTask.delay(256);
        }, me);
    },

    getHourField: function () {
        return this.down('#hour-field');
    },

    getMinuteField: function () {
        return this.down('#minute-field');
    },

    getValue: function () {
        var me = this,
            hourValue = me.getHourField().getValue(),
            minuteValue = me.getMinuteField().getValue();

        return {
            atHour: hourValue,
            atMinute: minuteValue
        };
    },

    // TODO Use the date-time xtype for this.
    formatDisplayOfTime: function (value) {
        var result = '00';

        if (value) {
            if (value < 10 && value > 0) {
                result = '0' + value;
            } else if (value >= 10) {
                result = value;
            }
        }
        return result;
    }
});

/**
 * @class Uni.form.RelativePeriod
 */
Ext.define('Uni.form.RelativePeriod', {
    extend: 'Ext.form.FieldContainer',
    xtype: 'uni-form-relativeperiod',

    requires: [
        'Uni.form.field.StartPeriod',
        'Uni.form.field.OnPeriod',
        'Uni.form.field.AtPeriod'
    ],

    /**
     * @cfg startPeriodCfg
     *
     * Custom config for the start period component.
     */
    startPeriodCfg: {},

    /**
     * @cfg noPreviewDateErrorMsg
     *
     * Message shown in the preview when no preview date has been defined.
     */
    noPreviewDateErrorMsg: Uni.I18n.translate('form.relativePeriod.errorMsg', 'UNI', 'It was not possible to calculate the preview date.'),

    previewUrl: '/api/tmr/relativeperiods/preview',

    formatPreviewTextFn: function (dateString) {
        return Uni.I18n.translate(
            'form.relativePeriod.previewText',
            'UNI',
            'The date and time of the relative period is {0}.',
            [dateString]
        );
    },

    initComponent: function () {
        var me = this;

        me.buildItems();
        me.callParent(arguments);
        me.initListeners();

        me.on('afterrender', me.onAfterRender, me);
    },

    onAfterRender: function () {
        var me = this;

        me.updatePeriodFields(me.getValue().startPeriodAgo);
        me.updatePreview();
    },

    buildItems: function () {
        var me = this;

        me.items = [
            Ext.apply(
                {
                    xtype: 'uni-form-field-startperiod',
                    required: true
                },
                me.startPeriodCfg
            ),
            {
                xtype: 'uni-form-field-onperiod'
            },
            {
                xtype: 'uni-form-field-atperiod'
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('form.relativePeriod.preview', 'UNI', 'Preview'),
                items: [
                    {
                        xtype: 'component',
                        itemId: 'preview-label',
                        cls: Ext.baseCSSPrefix + 'form-item-label',
                        style: {
                            fontWeight: 'normal'
                        },
                        html: ''
                    }
                ]
            }
        ];
    },

    initListeners: function () {
        var me = this;

        me.getStartPeriodField().on('periodchange', me.onStartPeriodChange, me);
        me.getOnPeriodField().on('periodchange', me.updatePreview, me);
        me.getAtPeriodField().on('periodchange', me.updatePreview, me);
    },

    onStartPeriodChange: function (value) {
        var me = this;

        me.updatePeriodFields(value.startPeriodAgo);
        me.updatePreview();
    },

    updatePeriodFields: function (frequency) {
        var me = this,
            startField = me.getStartPeriodField(),
            useStartDate = startField.showOptionDate ? startField.getOptionDateRadio().getValue() : false,
            onField = me.getOnPeriodField(),
            atField = me.getAtPeriodField(),
            atHourField = atField.getHourField(),
            atMinuteField = atField.getMinuteField();

        onField.setOptionCurrentDisabled(frequency !== 'months' || useStartDate);
        onField.setOptionDayOfMonthDisabled(frequency !== 'months' || useStartDate);
        onField.setOptionDayOfWeekDisabled(frequency !== 'weeks' || useStartDate);

        atHourField.setDisabled(frequency === 'hours' || frequency === 'minutes');
        atMinuteField.setDisabled(frequency === 'minutes');
    },

    updatePreview: function () {
        var me = this,
            label = me.down('#preview-label'),
            dateString = me.noPreviewDateErrorMsg;

        me.fireEvent('periodchange', me.getValue());
        label.mask();

        Ext.Ajax.request({
            url: me.previewUrl,
            method: 'PUT',
            jsonData: me.formatJsonPreviewRequest(),
            success: function (response, data) {
                var json = Ext.decode(response.responseText, true);
                var dateLong = json.date;
                var zoneOffset = json.zoneOffset;
                if (typeof dateLong !== 'undefined') {
                    var startDate = new Date(dateLong);
                    var startDateUtc = startDate.getTime() + (startDate.getTimezoneOffset() * 60000);
                    var zonedDate = new Date(startDateUtc - (60000 * zoneOffset));
                    dateString = Uni.I18n.formatDate('datetime.longdate', new Date(zonedDate), 'UNI', 'l F j, Y \\a\\t H:i a');
                    dateString = me.formatPreviewTextFn(dateString);
                }
            },
            failure: function (response) {
                // Already caught be the default value of the date string.
            },
            callback: function () {
                label.update(dateString);
                label.unmask();
            }
        });
    },

    formatJsonPreviewRequest: function () {
        var me = this,
            date = new Date(),
            value = me.getValue();

        return {
            date: date.getTime(),
            zoneOffset: date.getTimezoneOffset(),
            relativeDateInfo: value
        };
    },

    getStartPeriodField: function () {
        return this.down('uni-form-field-startperiod');
    },

    getOnPeriodField: function () {
        return this.down('uni-form-field-onperiod');
    },

    getAtPeriodField: function () {
        return this.down('uni-form-field-atperiod');
    },

    getValue: function () {
        var me = this,
            result = {},
            startValue = me.getStartPeriodField().getStartValue(),
            onValue = me.getOnPeriodField().getOnValue(),
            atValue = me.getAtPeriodField().getValue();

        Ext.apply(result, startValue);
        Ext.apply(result, onValue);
        Ext.apply(result, atValue);

        return result;
    }
});

/**
 * @class Uni.form.field.DateTime
 *
 * This class contains the DateTime field.
 *
 *     Ext.create('Uni.form.field.DateTime', {
 *       itemId: 'endOfInterval',
 *       name: 'intervalStart',
 *       fieldLabel: Uni.I18n.translate('deviceloadprofiles.filter.from', 'MDC', 'From'),
 *       labelAlign: 'top',
 *       dateConfig: {
 *         width: 100,
 *         submitValue: true,
 *       }
 *       hoursConfig: {
 *         maxValue: 20
 *       }
 *       minutesConfig: {
 *         minValue: 0
 *       }
 *       separatorConfig: {
 *         html: ':',
 *       }
 *     });
 *
 */
Ext.define('Uni.form.field.DateTime', {
    extend: 'Ext.form.FieldContainer',
    mixins: {
        field: 'Ext.form.field.Field'
    },
    alias: 'widget.date-time',
    layout: 'vbox',
    requires: [
        'Ext.form.field.Date',
        'Ext.form.field.Number',
        'Ext.container.Container'
    ],

    /**
     * @cfg {Object} dateConfig
     * Configuration for dateField allows you override or add any property of this field.
     */
    dateConfig: null,

    /**
     * @cfg {Object} hoursConfig
     * Configuration for hoursField allows you override or add any property of this field.
     */
    hoursConfig: null,

    /**
     * @cfg {Object} separatorConfig
     * Configuration for separatorField allows you override or add any property of this field.
     */
    separatorConfig: null,

    /**
     * @cfg {Object} minutesConfig
     * Configuration for minutesField allows you override or add any property of this field.
     */
    minutesConfig: null,

    initComponent: function () {
        var me = this,
            dateField = {
                xtype: 'datefield',
                itemId: 'date-time-field-date',
                submitValue: false,
                allowBlank: false,
                width: '100%',
                listeners: {
                    change: {
                        fn: me.onItemChange,
                        scope: me
                    }
                }
            },
            hoursField = {
                itemId: 'date-time-field-hours',
                flex: 1,
                maxValue: 23,
                minValue: 0
            },
            minutesField = {
                itemId: 'date-time-field-minutes',
                flex: 1,
                maxValue: 59,
                minValue: 0
            },
            separator = {
                xtype: 'component',
                html: ':',
                margin: '0 5 0 5'
            },
            container = {
                xtype: 'container',
                width: '100%',
                layout: {
                    type: 'hbox',
                    align: 'middle'
                },
                defaults: {
                    xtype: 'numberfield',
                    allowDecimals: false,
                    submitValue: false,
                    value: 0,
                    valueToRaw: me.formatDisplayOfTime,
                    listeners: {
                        change: {
                            fn: me.onItemChange,
                            scope: me
                        },
                        blur: me.numberFieldValidation
                    }
                }
            };

        if (me.layout === 'hbox') {
            delete container.width;
            dateField.width = 130;
            hoursField.width = 80;
            minutesField.width = 80;
        }

        Ext.apply(dateField, me.dateConfig);
        Ext.apply(hoursField, me.hoursConfig);
        Ext.apply(minutesField, me.minutesConfig);
        Ext.apply(separator, me.separatorConfig);

        container.items = [hoursField, separator, minutesField];
        me.items = [dateField, container];

        me.callParent(arguments);
    },

    formatDisplayOfTime: function (value) {
        var result = '00';

        if (value) {
            if (value < 10 && value > 0) {
                result = '0' + value;
            } else if (value >= 10) {
                result = value;
            }
        }
        return result;
    },

    numberFieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        } else if (value > field.maxValue) {
            field.setValue(field.maxValue);
        }
    },

    setValue: function (value) {
        var me = this,
            dateField = me.down('#date-time-field-date'),
            hoursField = me.down('#date-time-field-hours'),
            minutesField = me.down('#date-time-field-minutes');
        if (Ext.isDate(value) || moment(new Date(value)).isValid()) {
            me.eachItem(function (item) {
                item.suspendEvent('change');
            });
            dateField.setValue(moment(value).startOf('day').toDate());
            hoursField.setValue(moment(value).hours());
            minutesField.setValue(moment(value).minutes());
            me.fireEvent('change', me, value);
            me.eachItem(function (item) {
                item.resumeEvent('change');
            });
        } else if (value === undefined || value === null) {
            dateField.reset();
            hoursField.reset();
            minutesField.reset();
        } else {
            console.error('\'' + value + '\' is not a date');
        }
    },

    getValue: function() {
        var me = this,
            date = me.down('#date-time-field-date').getValue(),
            hours = me.down('#date-time-field-hours').getValue(),
            minutes = me.down('#date-time-field-minutes').getValue();

        if (date) {
            date = date.getTime();
            if (hours) {
                date += hours * 3600000;
            }
            if (minutes) {
                date += minutes * 60000;
            }
        }

        if(me.getRawValue) {
            return date;
        }

        date = new Date(date);

        return me.submitFormat ? Ext.Date.format(date, me.submitFormat) : date;
    },

    markInvalid: function(fields){
        this.eachItem(function(field){
            field.markInvalid('');
        });
        this.items.items[0].markInvalid(fields);
    },

    eachItem: function(fn, scope) {
        if(this.items && this.items.each){
            this.items.each(fn, scope || this);
        }
    },

    onItemChange: function () {
        this.fireEvent('change', this, this.getValue());
    }
});


/**
 * @class Uni.form.RelativePeriodPreview
 */
Ext.define('Uni.form.RelativePeriodPreview', {
    extend: 'Ext.container.Container',
    xtype: 'uni-form-relativeperiodpreview',

    requires: [
        'Uni.form.field.DateTime'
    ],

    /**
     * @cfg noPreviewDateErrorMsg
     *
     * Message shown in the preview when no preview date has been defined.
     */
    noPreviewDateErrorMsg: Uni.I18n.translate('form.relativePeriod.errorMsg', 'UNI', 'It was not possible to calculate the preview date.'),

    previewUrl: '/api/tmr/relativeperiods/preview',

    /**
     * @cfg startPeriodValue
     */
    startPeriodValue: undefined,

    startPeriodDate: undefined,

    /**
     * @cfg endPeriodValue
     */
    endPeriodValue: undefined,

    endPeriodDate: undefined,

    initComponent: function () {
        var me = this;

        me.buildItems();
        me.callParent(arguments);
        me.initListeners();

        me.on('afterrender', me.onAfterRender, me);
    },

    onAfterRender: function () {
        var me = this;

        me.updatePreview();
    },

    buildItems: function () {
        var me = this;

        me.items = [
            {
                xtype: 'component',
                itemId: 'preview-label',
                html: me.noPreviewDateErrorMsg,
                cls: Ext.baseCSSPrefix + 'form-item-label',
                style: {
                    fontWeight: 'normal'
                },
                margin: '8 0 8 0'
            },
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'label',
                        text: 'The relative period is defined using',
                        cls: Ext.baseCSSPrefix + 'form-item-label',
                        style: {
                            fontWeight: 'normal'
                        }
                    },
                    {
                        xtype: 'datefield',
                        allowBlank: false,
                        value: new Date(),
                        width: 128,
                        margin: '0 6 0 6'
                    },
                    {
                        xtype: 'label',
                        text: 'at',
                        cls: Ext.baseCSSPrefix + 'form-item-label',
                        style: {
                            fontWeight: 'normal'
                        }
                    },
                    {
                        xtype: 'numberfield',
                        itemId: 'hour-field',
                        hideLabel: true,
                        valueToRaw: me.formatDisplayOfTime,
                        value: 0,
                        minValue: 0,
                        maxValue: 23,
                        allowBlank: false,
                        width: 64,
                        margin: '0 6 0 6'
                    },
                    {
                        xtype: 'label',
                        text: ':',
                        cls: Ext.baseCSSPrefix + 'form-item-label',
                        style: {
                            fontWeight: 'normal'
                        }
                    },
                    {
                        xtype: 'numberfield',
                        itemId: 'minute-field',
                        hideLabel: true,
                        valueToRaw: me.formatDisplayOfTime,
                        value: 0,
                        minValue: 0,
                        maxValue: 59,
                        allowBlank: false,
                        width: 64,
                        margin: '0 6 0 6'
                    },
                    {
                        xtype: 'label',
                        text: 'as reference',
                        cls: Ext.baseCSSPrefix + 'form-item-label',
                        style: {
                            fontWeight: 'normal'
                        }
                    },
                    {
                        xtype: 'button',
                        tooltip: Uni.I18n.translate('relativeperiod.form.referencedete.tooltip', 'TME', 'You can change the reference to define another relative period'),
                        iconCls: 'icon-info-small',
                        ui: 'blank',
                        itemId: 'latestReadingHelp',
                        shadow: false,
                        margin: '6 0 0 6',
                        width: 16
                    }
                ]
            }
        ];
    },

    initListeners: function () {
        var me = this;

        me.getDateField().on('change', me.updatePreview, me);
        me.getHourField().on('change', me.updatePreview, me);
        me.getMinuteField().on('change', me.updatePreview, me);
    },

    updatePreview: function () {
        var me = this,
            label = me.getPreviewLabel(),
            dateString = me.noPreviewDateErrorMsg;

        label.mask();

        if (typeof me.startPeriodValue !== 'undefined' && typeof me.endPeriodValue !== 'undefined') {
            me.startPeriodDate = undefined;
            me.endPeriodDate = undefined;

            Ext.Ajax.request({
                url: me.previewUrl,
                method: 'PUT',
                jsonData: me.formatJsonPreviewRequest(me.startPeriodValue),
                success: function (response, data) {
                    var json = Ext.decode(response.responseText, true);
                    var dateLong = json.date;
                    var zoneOffset = json.zoneOffset;
                    if (typeof dateLong !== 'undefined') {
                        var startDate = new Date(dateLong);
                        var startDateUtc = startDate.getTime() + (startDate.getTimezoneOffset() * 60000);
                        var zonedDate = new Date(startDateUtc - (60000*zoneOffset));
                        me.startPeriodDate = new Date(zonedDate);
                    }

                    me.updatePreviewLabel(me.startPeriodDate, me.endPeriodDate);
                },
                failure: function (response) {
                    me.getPreviewLabel().update(dateString);
                }
            });

            Ext.Ajax.request({
                url: me.previewUrl,
                method: 'PUT',
                jsonData: me.formatJsonPreviewRequest(me.endPeriodValue),
                success: function (response, data) {
                    var json = Ext.decode(response.responseText, true);
                    var dateLong = json.date;
                    var zoneOffset = json.zoneOffset;
                    if (typeof dateLong !== 'undefined') {
                        var startDate = new Date(dateLong);
                        var startDateUtc = startDate.getTime() + (startDate.getTimezoneOffset() * 60000);
                        var zonedDate = new Date(startDateUtc - (60000*zoneOffset));
                        me.endPeriodDate = new Date(zonedDate);
                    }

                    me.updatePreviewLabel(me.startPeriodDate, me.endPeriodDate);
                },
                failure: function (response) {
                    me.getPreviewLabel().update(dateString);
                }
            });
        }
    },

    updatePreviewLabel: function (startDate, endDate) {
        var me = this,
            startDateString = Uni.I18n.formatDate('datetime.longdate', startDate, 'UNI', 'l F j, Y \\a\\t H:i a'),
            endDateString = Uni.I18n.formatDate('datetime.longdate', endDate, 'UNI', 'l F j, Y \\a\\t H:i a'),
            dateString = me.formatPreviewTextFn(startDateString, endDateString);

        if (typeof startDate !== 'undefined' && typeof endDate !== 'undefined') {
            me.getPreviewLabel().update(dateString);
            me.getPreviewLabel().unmask();
        }
    },

    updateStartPeriodValue: function (startPeriodValue) {
        this.startPeriodValue = startPeriodValue;
    },

    updateEndPeriodValue: function (endPeriodValue) {
        this.endPeriodValue = endPeriodValue;
    },

    formatPreviewTextFn: function (startDateString, endDateString) {
        return Uni.I18n.translate(
            'form.relativeperiodpreview.previewText',
            'UNI',
            'From {0} to {1}.',
            [startDateString, endDateString]
        );
    },

    formatJsonPreviewRequest: function (periodValue) {
        var me = this,
            date = me.getValue();

        var result = {
            date: date.getTime(),
            zoneOffset: date.getTimezoneOffset(),
            relativeDateInfo: periodValue
        };
        return result;
    },

    getPreviewLabel: function () {
        return this.down('#preview-label');
    },

    getDateField: function () {
        return this.down('datefield');
    },

    getHourField: function () {
        return this.down('#hour-field');
    },

    getMinuteField: function () {
        return this.down('#minute-field');
    },

    getValue: function () {
        var me = this,
            date = me.getDateField().getValue(),
            hours = me.getHourField().getValue(),
            minutes = me.getMinuteField().getValue();

        date.setHours(hours);
        date.setMinutes(minutes);
        date.setSeconds(0);
        date.setMilliseconds(0);

        return date;
    },

    // TODO Use the date-time xtype for this.
    formatDisplayOfTime: function (value) {
        var result = '00';

        if (value) {
            if (value < 10 && value > 0) {
                result = '0' + value;
            } else if (value >= 10) {
                result = value;
            }
        }
        return result;
    }
});

/**
 * @class Uni.form.field.DisplayFieldWithInfoIcon
 */
Ext.define('Uni.form.field.DisplayFieldWithInfoIcon', {
    extend: 'Ext.form.field.Display',
    xtype: 'displayfield-with-info-icon',
    emptyText: '',

    /**
     * @cfg {String} infoTooltip
     * Info icon tooltip text.
     */
    infoTooltip: null,

    /**
     * @cfg {Function} beforeRenderer
     * Should be used instead of the {@link Ext.form.field.Display.renderer} function.
     */
    beforeRenderer: null,

    requires: [
        'Ext.button.Button'
    ],

    deferredRenderer: function (value, field, tooltip) {
        if (!field.isDestroyed) {
            new Ext.button.Button({
                renderTo: field.getEl().down('.x-form-display-field'),
                tooltip: tooltip,
                iconCls: 'icon-info-small',
                cls: 'uni-btn-transparent',
                style: {
                    display: 'inline-block',
                    "text-decoration": 'none !important'
                }
            });

            field.updateLayout();
        }
    },

    renderer: function (value, field) {
        var me = this;

        if (Ext.isEmpty(value)) {
            return me.emptyText;
        }

        if (Ext.isFunction(me.beforeRenderer)) {
            value = me.beforeRenderer(value, field);
        }

        me.infoTooltip && Ext.defer(this.deferredRenderer, 1, this, [value, field, me.infoTooltip]);
        return '<span style="display: inline-block; float: left; margin-right: 10px;">' + value + '</span>';
    }
});

/**
 * @class Uni.form.field.EditedDisplay
 */
Ext.define('Uni.form.field.EditedDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'edited-displayfield',
    name: 'editedDate',

    deferredRenderer: function (field, icon) {
        field.getEl().down('.x-form-display-field').appendChild(icon);
        field.updateLayout();
    },

    renderer: function (value, field) {
        var icon,
            date,
            tooltipText;

        if (value) {
            date = Ext.isDate(value.date) ? value.date : new Date(value.date);
            icon = document.createElement('span');
            switch (value.flag) {
                case 'ADDED':
                    icon.className = 'icon-edit';
                    tooltipText = Uni.I18n.formatDate('editedDate.format', value, 'MDC', '\\A\\d\\d\\e\\d \\o\\n F d, Y \\a\\t H:i');
                    break;
                case 'EDITED':
                    icon.className = 'icon-edit';
                    tooltipText = Uni.I18n.formatDate('addedDate.format', value, 'MDC', '\\E\\d\\i\\t\\e\\d \\o\\n F d, Y \\a\\t H:i');
                    break;
                case 'REMOVED':
                    icon.className = 'icon-remove';
                    tooltipText = Uni.I18n.formatDate('removedDate.format', value, 'MDC', '\\R\\e\\m\\o\\v\\e\\d \\o\\n F d, Y \\a\\t H:i');
                    break;
            }
            Ext.create('Ext.tip.ToolTip', {
                target: icon,
                html: tooltipText
            });
            Ext.defer(this.deferredRenderer, 1, this, [field, icon]);
        }
        return '';
    }
});

/**
 * @class Uni.form.field.FilterDisplay
 */
Ext.define('Uni.form.field.FilterDisplay', {
    extend: 'Ext.form.FieldContainer',
    requires: [
        'Ext.form.field.Display',
        'Ext.button.Button'
    ],
    xtype: 'filter-display',
    emptyText: '',
    layout: 'hbox',

    initComponent: function () {
        var me = this,
            name = me.name;

        me.items = [
            {
                xtype: 'displayfield',
                name: name,
                renderer: function (value, field) {
                    var filterBtn = field.nextSibling('#filter-display-button'),
                        result = value;

                    if (Ext.isFunction(me.renderer)) {
                        result = me.renderer(value, field);
                    }

                    filterBtn.filterValue = value;
                    filterBtn.setVisible(result ? true : false);

                    return result ? result : me.emptyText;
                }
            },
            {
                xtype: 'button',
                itemId: 'filter-display-button',
                filterBy: me.name,
                cls: 'uni-btn-transparent',
                iconCls: 'icon-filter',
                ui: 'blank',
                shadow: false,
                hidden: true,
                margin: '5 0 0 10',
                width: 16
            }
        ];

        me.callParent(arguments);
    }
});

/**
 * @class Uni.form.field.IntervalFlagsDisplay
 */
Ext.define('Uni.form.field.IntervalFlagsDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'interval-flags-displayfield',
    name: 'intervalFlags',
    fieldLabel: Uni.I18n.translate('intervalFlags.label', 'UNI', 'Interval flags'),
    emptyText: '',

    deferredRenderer: function (field, icon) {
        field.getEl().down('.x-form-display-field').appendChild(icon);
        field.updateLayout();
    },

    renderer: function (value, field) {
        var icon,
            tooltip = '';
        if (!Ext.isArray(value) || !value.length) {
            return this.emptyText;
        }


        icon = document.createElement('span');
        icon.className = 'icon-info-small';
        icon.setAttribute('style', 'width: 16px; height: 16px');
        Ext.Array.each(value, function (value, index) {
            index++;
            tooltip += Uni.I18n.translate('intervalFlags.Flag', 'UNI', 'Flag') + ' ' + index + ': ' + value + '<br>';
        });
        Ext.create('Ext.tip.ToolTip', {
            target: icon,
            html: tooltip
        });
        Ext.defer(this.deferredRenderer, 1, this, [field, icon]);

        return '<span style="display: inline-block; width: 20px; float: left;">' + value.length + '</span>';
    }
});

/**
 * @class Uni.form.field.LastEventDateDisplay
 */
Ext.define('Uni.form.field.LastEventDateDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'last-event-date-displayfield',
    name: 'lastEventDate',
    fieldLabel: Uni.I18n.translate('lastEventDate.label', 'UNI', 'Last event date'),
    emptyText: '',

    requires: [
        'Ext.button.Button'
    ],

    deferredRenderer: function (value, field, tooltip) {
        if (!field.isDestroyed) {
            new Ext.button.Button({
                renderTo: field.getEl().down('.x-form-display-field'),
                tooltip: tooltip,
                iconCls: 'icon-info-small',
                cls: 'uni-btn-transparent',
                style: {
                    display: 'inline-block',
                    "text-decoration": 'none !important'
                }
            });

            field.updateLayout();
        }
    },

    renderer: function (value, field) {
        var result = Uni.I18n.formatDate('lastEventDate.dateFormat', Ext.isDate(value) ? value : new Date(value), 'UNI', 'F d, Y H:i:s'),
            tooltip = Uni.I18n.translate('lastEventDate.tooltip', 'UNI', 'Date and time of last received event');

        if (!value) {
            return this.emptyText;
        }

        Ext.defer(this.deferredRenderer, 1, this, [result, field, tooltip]);
        return '<span style="display: inline-block; float: left; margin-right: 10px;">' + result + '</span>';
    }
});

/**
 * @class Uni.form.field.LastEventTypeDisplay
 */
Ext.define('Uni.form.field.LastEventTypeDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'last-event-type-displayfield',
    name: 'lastEventType',
    fieldLabel: Uni.I18n.translate('lastEventType.label', 'UNI', 'Last event type'),
    emptyText: '',

    requires: [
        'Ext.button.Button'
    ],

    deferredRenderer: function (value, field, tooltip) {
        if (!field.isDestroyed) {
            new Ext.button.Button({
                renderTo: field.getEl().down('.x-form-display-field'),
                tooltip: tooltip,
                iconCls: 'icon-info-small',
                cls: 'uni-btn-transparent',
                style: {
                    display: 'inline-block',
                    "text-decoration": 'none !important'
                }
            });

            field.updateLayout();
        }
    },

    renderer: function (data, field) {
        var result = '',
            tooltip = '<table>';

        if (!data) {
            return this.emptyText;
        }

        Ext.Object.each(data, function(key, value) {
            if (key === 'code') {
                result = value;
            } else {
                tooltip += '<tr>'
                    + '<td style="text-align: right;border: none">'
                    + '<b>' + Uni.I18n.translate('lastEventType.' + key, 'UNI', key) + ':' + '</b>'
                    + '</td>'
                    + '<td>'
                    + '&nbsp;&nbsp;&nbsp;' + value.name + ' ' + '(' + value.id + ')'
                    + '</td>'
                    + '</tr>';
            }
        });

        tooltip += '</table>';

        Ext.defer(this.deferredRenderer, 1, this, [result, field, tooltip]);
        return '<span style="display: inline-block; width: 115px; float: left;">' + result + '</span>';
    }
});

/**
 * @class Uni.form.field.LastReadingDisplay
 */
Ext.define('Uni.form.field.LastReadingDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'last-reading-displayfield',
    name: 'lastReading',
    fieldLabel: Uni.I18n.translate('lastReading.label', 'UNI', 'Last reading'),
    emptyText: '',

    requires: [
        'Ext.button.Button'
    ],

    deferredRenderer: function (value, field, tooltip) {
        if (!field.isDestroyed) {
            new Ext.button.Button({
                renderTo: field.getEl().down('.x-form-display-field'),
                tooltip: tooltip,
                iconCls: 'icon-info-small',
                cls: 'uni-btn-transparent',
                style: {
                    display: 'inline-block',
                    "text-decoration": 'none !important'
                }
            });

            field.updateLayout();
        }
    },

    renderer: function (value, field) {
        var result = Uni.I18n.formatDate('lastReading.dateFormat', Ext.isDate(value) ? value : new Date(value), 'UNI', 'F d, Y H:i:s'),
            tooltip = Uni.I18n.translate('lastReading.tooltip', 'UNI', 'The moment when the data was read out for the last time');

        if (!value) {
            return this.emptyText;
        }

        Ext.defer(this.deferredRenderer, 1, this, [result, field, tooltip]);
        return '<span style="display: inline-block; float: left; margin-right: 10px;">' + result + '</span>';
    }
});

/**
 * @class Uni.form.field.Obis
 */
Ext.define('Uni.form.field.Obis', {
    extend: 'Ext.form.field.Text',
    requires: [
        'Ext.form.VTypes'
    ],
    xtype: 'obis-field',
    name: 'obisCode',
    cls: 'obisCode',
    msgTarget: 'under',
    fieldLabel: Uni.I18n.translate('obis.label', 'UNI', 'OBIS code'),
    emptyText: Uni.I18n.translate('obis.mask', 'UNI', 'x.x.x.x.x.x'),

    afterSubTpl:
        '<div class="x-form-display-field"><i>' +
        Uni.I18n.translate('obis.info', 'UNI', 'Provide the values for the 6 attributes of the Obis code, separated by a "."') +
        '</i></div>'
    ,
    maskRe: /[\d.]+/,
    vtype: 'obisCode',
    required: true,

    initComponent: function () {
        Ext.apply(Ext.form.VTypes, {
            obisCode: function (val, field) {
                var obis = /^(0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5]))$/;
                return obis.test(val);
            },
            obisCodeText: Uni.I18n.translate('obis.error', 'UNI', 'OBIS code is wrong')
        });
        this.callParent(this);
    }
});

/**
 * @class Uni.form.field.ObisDisplay
 */
Ext.define('Uni.form.field.ObisDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'obis-displayfield',
    name: 'obisCode',
    cls: 'obisCode',
    fieldLabel: Uni.I18n.translate('obis.label', 'UNI', 'OBIS code'),
    emptyText: ''
});

/**
 * @class Uni.form.field.Password
 */
Ext.define('Uni.form.field.Password', {
    extend: 'Ext.form.FieldContainer',
    xtype: 'password-field',
    readOnly: false,
    fieldLabel: Uni.I18n.translate('form.password', 'UNI', 'Password'),
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    handler: function (checkbox, checked) {
        var field = this.down('textfield');
        var input = field.getEl().down('input');

        input.dom.type = checked ? 'text' : 'password';
    },

    items: [
        {
            xtype: 'textfield',
            required: true,
            allowBlank: false,
            inputType: 'password',
            name: this.name,
            readOnly: this.readOnly
        },
        {
            xtype: 'checkbox',
            boxLabel: Uni.I18n.translate('comServerComPorts.form.showChar', 'MDC', 'Show characters')
        }
    ],

    initComponent: function() {
        this.items[0].name = this.name;
        this.items[1].handler = this.handler;
        this.items[1].scope = this;

        this.callParent(arguments);
    },

    setValue: function(value) {
        this.down('textfield').setValue(value);
    }
});

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

Ext.define('Uni.form.filter.FilterCombobox', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.uni-filter-combo',
    editable: false,
    multiSelect: true,
    queryMode: 'local',
    triggerAction: 'all',

    loadStore: true,

    initComponent: function () {
        var me = this;
        me.listConfig = {
            getInnerTpl: function () {
                return '<div class="x-combo-list-item"><img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" /> {' + me.displayField + '}</div>';
            }
        };

        me.callParent(arguments);

        if (this.loadStore) {
            me.store.load({
                callback: function () {
                    me.select(me.getValue());
                    me.fireEvent('updateTopFilterPanelTagButtons', me);
                }
            });
        }
    },

    getValue: function () {
        var me = this;
        me.callParent(arguments);
        if (_.isArray(me.value)) {
            me.value = _.compact(me.value)
        }
        return me.value
    }
});


/**
 * @class Uni.grid.column.Action
 */
Ext.define('Uni.grid.column.Action', {
    extend: 'Ext.grid.column.Action',
    alias: 'widget.uni-actioncolumn',

    header: 'Actions',
    width: 100,
    align: 'left',
    iconCls: 'x-uni-action-icon',

    menu: {
        defaultAlign: 'tr-br?',
        plain: true,
        items: []
    },

    constructor: function(config) {
        var me = this,
            cfg = Ext.apply({}, config);

        // reset the items for new menu instance.
        me.menu.items = [];

        // workaround to work with menu instance
        if (_.isString(cfg.items)) {
            var menu = Ext.ClassManager.get(cfg.items);
            Ext.apply(me.menu.items, menu.prototype.items);
        } else {
            Ext.apply(me.menu.items, cfg.items);
        }

        cfg.items = null;
        me.callParent([cfg]);

        this.initMenu();
    },

    /**
     * @private
     */
    initMenu: function () {
        var me = this,
            menuXtype = me.menu.xtype;
        menuXtype == null ? menuXtype = 'menu' : null;
        me.menu = Ext.widget(menuXtype, me.menu);
        me.menu.on('click', function(menu, item, e, eOpts) {
            me.fireEvent('menuclick', menu, item, e, eOpts);
            if (item.action && !Ext.isObject(item.action)) {

                me.fireEvent(item.action, menu.record);
            }
        });
    },

    handler: function(grid, rowIndex, colIndex) {
        var me = this;
        var record = grid.getStore().getAt(rowIndex);
        var cell = grid.getCellByPosition({row: rowIndex, column: colIndex});

        if (me.menu.cell === cell) {
            me.menu.hide();
            me.menu.cell = null;
        } else {
            cell.addCls('active');
            me.menu.record = record;
            me.menu.showBy(cell);
            me.menu.cell = cell;
        }

        // this is for menu toggling, change the code below with accuracy!
        me.menu.on('hide', function() {
            var actions = grid.getEl().query('.' + me.iconCls + ':hover');
            if (!actions.length) {
                me.menu.cell = null;
            }
            cell.removeCls('active');
        });
    }
});

/**
 * @class Uni.grid.column.Default
 */
Ext.define('Uni.grid.column.Default', {
    extend: 'Ext.grid.column.Column',
    xtype: 'uni-default-column',

    header: Uni.I18n.translate('general.default', 'UNI', 'Default'),
    minWidth: 120,
    align: 'left',

    renderer: function (value, metadata) {
        if (value) {
            return '<div class="' + Uni.About.baseCssPrefix + 'default-column-icon'
                + ' default">&nbsp;</div>';
        } else {
            return '';
        }
    }
});

Ext.define('Uni.grid.column.Edited', {
    extend: 'Ext.grid.column.Column',
    xtype: 'edited-column',
    header: Uni.I18n.translate('editedDate.header', 'UNI', 'Edited'),
    minWidth: 100,
    align: 'left',

    requires: [
        'Uni.form.field.EditedDisplay'
    ],

    deferredRenderer: function (value, record, view) {
        try {
            var me = this,
                cmp = view.getCell(record, me).down('.x-grid-cell-inner'),
                field = new Uni.form.field.EditedDisplay({
                    fieldLabel: false
                });
            cmp.setHTML('');
            field.setValue(value);
            field.render(cmp);
        } catch (e) {
        }
    },

    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
        var me = metaData.column;

        Ext.defer(me.deferredRenderer, 1, me, [value, record, view]);
    }
});

/**
 * @class Uni.grid.column.IntervalFlags
 */
Ext.define('Uni.grid.column.IntervalFlags', {
    extend: 'Ext.grid.column.Column',
    xtype: 'interval-flags-column',
    header: Uni.I18n.translate('intervalFlags.label', 'UNI', 'Interval flags'),
    minWidth: 60,
    align: 'left',

    requires: [
        'Uni.form.field.IntervalFlagsDisplay'
    ],

    deferredRenderer: function (value, record, view) {
        var me = this,
            cell;

        try {
            cell = view.getCell(record, me);
        } catch (err) {
            return false;
        }

        var cmp = cell.down('.x-grid-cell-inner');
        var field = new Uni.form.field.IntervalFlagsDisplay({
            fieldLabel: false
        });
        cmp.setHTML('');
        field.setValue(value);
        field.render(cmp);
    },

    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
        var me = metaData.column;
        Ext.defer(me.deferredRenderer, 1, me, [value, record, view]);
    }
});

Ext.define('Uni.grid.column.LastEventType', {
    extend: 'Ext.grid.column.Column',
    xtype: 'last-event-type-column',
    header: Uni.I18n.translate('lastEventType.label', 'UNI', 'Last event type'),
    minWidth: 150,
    align: 'left',

    requires: [
        'Uni.form.field.LastEventTypeDisplay'
    ],

    deferredRenderer: function (value, record, view) {
        var me = this;
        var cmp = view.getCell(record, me).down('.x-grid-cell-inner');
        var field = new Uni.form.field.LastEventTypeDisplay({
            fieldLabel: false
        });
        cmp.setHTML('');
        field.setValue(value);
        field.render(cmp);

        Ext.defer(view.updateLayout, 10, view);
    },

    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
        var me = metaData.column;
        Ext.defer(me.deferredRenderer, 1, me, [value, record, view]);
    }
});

/**
 * @class Uni.grid.column.Obis
 */
Ext.define('Uni.grid.column.Obis', {
    extend: 'Ext.grid.column.Column',
    xtype: 'obis-column',
    header: Uni.I18n.translate('obis.label', 'UNI', 'OBIS code'),
    minWidth: 120,
    align: 'left'
});

Ext.define('Uni.view.window.ReadingTypeDetails', {
    extend: 'Ext.window.Window',
    xtype: 'readingTypeDetails',
    closable: true,
    width: 800,
    height: 550,
    constrain: true,
    autoShow: true,
    modal: true,
    layout: 'fit',
    closeAction: 'destroy',
    floating: true,
    items: {}
});

/**
 * @class Uni.grid.column.ReadingType
 */
Ext.define('Uni.grid.column.ReadingType', {
    extend: 'Ext.grid.column.Column',
    xtype: 'reading-type-column',
    header: Uni.I18n.translate('readingType.label', 'UNI', 'Reading type'),
    minWidth: 280,
    align: 'left',

    requires: [
        'Ext.panel.Tool',
        'Ext.util.Point',
        'Uni.view.window.ReadingTypeDetails',
        'Uni.form.field.ReadingTypeDisplay'
    ],

    deferredRenderer: function (value, record, view) {
        var me = this;
        var cmp = view.getCell(record, me).down('.x-grid-cell-inner');
        var field = new Uni.form.field.ReadingTypeDisplay({
            fieldLabel: false
        });
        cmp.setHTML('');
        field.setValue(value);
        field.render(cmp);

        Ext.defer(view.updateLayout, 10, view);
    },

    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
        var me = metaData.column;
        Ext.defer(me.deferredRenderer, 1, me, [value, record, view]);
    }
});

Ext.define('Uni.grid.column.ValidationFlag', {
    extend: 'Ext.grid.column.Column',
    xtype: 'validation-flag-column',
    header: Uni.I18n.translate('device.registerData.value', 'MDC', 'Value'),

    renderer: function (value, metaData, record) {
        switch (record.get('validationResult')) {
            case 'validationStatus.notValidated':
                return '<span class="validation-column-align"><span class="icon-validation icon-validation-black"></span>';
                break;
            case 'validationStatus.ok':
                return '<span class="validation-column-align"><span class="icon-validation"></span>';
                break;
            case 'validationStatus.suspect':
                return '<span class="validation-column-align"><span class="icon-validation icon-validation-red"></span>';
                break;
            default:
                return '';
                break;
        }
    }
});

Ext.define('Uni.grid.plugin.DragDropWithoutIndication', {
    extend: 'Ext.grid.plugin.DragDrop',
    alias: 'plugin.gridviewdragdropwithoutindication',

    onViewRender : function(view) {
        var me = this,
            scrollEl;

        if (me.enableDrag) {
            if (me.containerScroll) {
                scrollEl = view.getEl();
            }

            me.dragZone = new Ext.view.DragZone({
                view: view,
                ddGroup: me.dragGroup || me.ddGroup,
                dragText: me.dragText,
                containerScroll: me.containerScroll,
                scrollEl: scrollEl
            });
        }

        if (me.enableDrop) {
            me.dropZone = new Ext.grid.ViewDropZone({
                indicatorHtml: '',
                indicatorCls: '',
                view: view,
                ddGroup: me.dropGroup || me.ddGroup
            });
        }
    }

});

/**
 * @class Uni.model.BreadcrumbItem
 */
Ext.define('Uni.model.BreadcrumbItem', {
    extend: 'Ext.data.Model',
    fields: [
        'text',
        'href',
        {name: 'relative', type: 'boolean', defaultValue: true}
    ],

    associations: [
        {
            type: 'hasOne',
            model: 'Uni.model.BreadcrumbItem',
            associationKey: 'child',
            getterName: 'getChild',
            setterName: 'doSetChild'
        }
    ],

    proxy: {
        type: 'memory'
    },

    setChild: function (value, options, scope) {
        this.doSetChild(value, options, scope);
        return value;
    }

});

Ext.define('Uni.model.Script', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'path'
    ]
});

Ext.define('Uni.property.view.DefaultButton', {
    extend: 'Ext.button.Button',
    xtype: 'uni-default-button',

    border: 0,
    icon: '../sky/build/resources/images/form/restore.png',
    height: 28,
    width: 28,
    scale: 'small',
    action: 'delete',
    margin: '0 0 5 5',
    hidden: true
});

/**
 * @class Uni.property.view.property.Base
 * @abstract
 *
 * This is base class for property.
 * Do not use this class directly!
 *
 * Use this class for implementation of custom properties.
 * The childs of this class must implement method:
 * getEditCmp() and getField()
 *
 * @see: Uni.property.view.property.Time for example of custom property implementation
 */
Ext.define('Uni.property.view.property.Base', {
    extend: 'Ext.form.FieldContainer',

    requires: [
        'Uni.property.view.DefaultButton'
    ],

    width: 320,
    resetButtonHidden: false,
    translationKey: 'UNI',

    layout: 'hbox',
    fieldLabel: '',
    required: false,

    items: [
        {
            xtype: 'uni-default-button'
        }
    ],

    isEdit: true,
    isReadOnly: false,
    property: null,
    key: null,

    /**
     * @param {string|null} key
     * @returns {string}
     */
    getName: function (key) {
        key = key ? key : this.key;
        return 'properties.' + key;
    },

    /**
     * @private
     * @param {string} key
     */
    setKey: function (key) {
        this.key = key;

        var label = Uni.I18n.translate(key, this.translationKey, key);
        this.setFieldLabel(label);
    },

    /**
     * Returns Reset button
     * @returns {Uni.property.view.DefaultButton}
     */
    getResetButton: function () {
        return this.down('uni-default-button');
    },

    /**
     * Performs property initialisation
     *
     * @private
     * @param {Uni.property.model.Property} property
     */
    initProperty: function (property) {
        this.property = property;

        if (property) {
            this.key = property.get('key');
            this.itemId = this.key;

            if (this.isEdit) {
                this.required = property.get('required');
            }
        }
    },

    /**
     * Sets the property and update component values
     *
     * @param {Uni.property.model.Property} property
     */
    setProperty: function (property) {
        this.property = property;

        if (property) {
            this.setKey(property.get('key'));
            this.setValue(this.getProperty().get('value'));
            this.updateResetButton();
        }
    },

    /**
     * Updates the reset button state and tooltip
     */
    updateResetButton: function () {
        var resetButtonHidden = this.resetButtonHidden;
        var button = this.getResetButton();

        if (!resetButtonHidden && this.isEdit) {
            button.setTooltip(
                    Uni.I18n.translate('general.restoreDefaultValue', this.translationKey, 'Restore to default value')
                    + ' &quot; ' + this.getProperty().get('default') + '&quot;'
            );

            button.setVisible(!this.getProperty().get('isInheritedOrDefaultValue'));
        }
        this.fireEvent('enableRestoreAll', this);
    },

    /**
     *
     * shows a popup if entered value equals inheritedValue, this lets the user choose between deleting the property or
     * setting the new (same value) on the property
     */
    showPopupEnteredValueEqualsInheritedValue: function (field, property) {
        var me = this;
        Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('general.yes', 'UNI', 'Yes'),
            cancelText: Uni.I18n.translate('general.no', 'UNI', 'No')
        }).show({
            msg: Ext.String.format(Uni.I18n.translate('property.valueSameAsInherited', 'UNI', 'The value of \'{0}\' is the same as the default value.  Do you want to link the value to the default value?'), property.get('key')),
            title: Ext.String.format(Uni.I18n.translate('property.valueSameAs', 'MDC', 'Set \'{0}\' to its default value?'), property.get('key')),
            config: {
                property: me,
                field: field
            },
            fn: me.setPropertyValue
        });
    },

    setPropertyValue: function (btn, text, opt) {
        // var me = this;
        if (btn === 'confirm') {
            var property = opt.config.property;
            //var field = opt.config.field;
            property.restoreDefault();
        }
    },


    /**
     * returns bounded property
     * @returns {Uni.property.model.Property}
     */
    getProperty: function () {
        return this.property;
    },

    /**
     * @abstract
     *
     * You must implement this method on inheritance
     *
     * Example:
     *
     * getEditCmp: function () {
     *   var me = this;
     *   return {
     *       xtype: 'textfield',
     *       name: this.getName(),
     *       itemId: me.key + 'textfield',
     *       width: me.width,
     *       msgTarget: 'under'
     *   }
     * },
     */
    getEditCmp: function () {
        throw 'getDisplayCmp is not implemented';
    },

    /**
     * returns basic display field configuration
     * override this method if you want custom look of display field of custom property.
     *
     * @returns {Object}
     */
    getDisplayCmp: function () {
        return {
            xtype: 'displayfield',
            name: this.getName(),
            itemId: this.key + 'displayfield'
        }
    },

    /**
     * Sets value to the view component
     * Override this method if you have custom logic of value transformation
     * @see Uni.property.view.property.Time for example
     *
     * @param value
     */
    setValue: function (value) {
        this.isEdit
            ? this.getField().setValue(value)
            : this.getDisplayField().setValue(value);
    },

    getValue: function (value) {
        return value;
    },

    /**
     * Return edit field
     * Implement this method on inheritance
     *
     * @returns {Uni.property.view.property.Base}
     */
    getField: function () {
        return null;
    },

    /**
     * Returns display field
     * @returns {*}
     */
    getDisplayField: function () {
        return this.down('displayfield');
    },

    /**
     * performs component initialisation
     */
    initComponent: function () {
        var me = this;
        var cfg = Ext.apply({items: []}, me.config);
        Ext.apply(cfg.items, me.items);

        me.initProperty(me.property);

        var cmp = me.isEdit
                ? me.getEditCmp()
                : me.getDisplayCmp()
            ;

        // apply config object or array of config objects
        if (Ext.isArray(cmp)) {
            var arguments = [0, 0];
            arguments.push.apply(arguments, cmp);
            cfg.items.splice.apply(cfg.items, arguments);
        } else if (Ext.isObject(cmp)) {
            cfg.items.splice(0, 0, cmp);
        }

        Ext.apply(me, cfg);
        me.callParent(arguments);

        // after init
        me.setProperty(me.property);
        me.initListeners();
    },

    initListeners: function () {
        var me = this;
        var field = me.getField();

        if (field) {
            field.on('change', function () {
                me.getProperty().set('isInheritedOrDefaultValue', false);
                me.updateResetButton();
            });
            field.on('blur', function () {
                if (field.getValue() !== '' && !me.getProperty().get('isInheritedOrDefaultValue') && field.getValue() === me.getProperty().get('default')) {
                    me.showPopupEnteredValueEqualsInheritedValue(field, me.getProperty());
                }
            })
        }
        this.on('afterrender', function(){
            me.fireEvent('enableRestoreAll', this);
        });
        this.getResetButton().setHandler(this.restoreDefault, this);
    },

    /**
     * Restores default field value
     */
    restoreDefault: function () {
        var property = this.getProperty();
        var restoreValue = property.get('default');
        this.setValue(restoreValue);
        property.set('isInheritedOrDefaultValue', true);

        this.updateResetButton();
    },

    /**
     * Sets inherited value as default
     */
    useInheritedValue: function () {
        this.getProperty().initInheritedValues();
        this.updateResetButton();
    }
});

/**
 * @class Uni.property.view.property.BaseCombo
 * @abstract
 *
 * This is base class for property which can be a combo (weird, better have separate propery for combo).
 * Do not use this class directly!
 *
 * @see: Uni.property.view.property.Text for example of custom property implementation
 */
Ext.define('Uni.property.view.property.BaseCombo', {
    extend: 'Uni.property.view.property.Base',

    /**
     * @final
     * @returns {Object}
     */
    getEditCmp: function () {
        return this.isCombo() ? this.getComboCmp() : this.getNormalCmp();
    },

    /**
     * Return is component a combobox or not
     *
     * @returns {boolean}
     */
    isCombo: function () {
        return this.getProperty().getSelectionMode() === 'COMBOBOX';
    },

    /**
     * @abstract
     *
     * You must implement this method on inheritance
     */
    getNormalCmp: function() {
        throw 'getNormalCmp is not implemented';
    },

    getComboCmp: function () {
        var me = this;

        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: me.getProperty().getPossibleValues(),
            queryMode: 'local',
            displayField: 'value',
            valueField: 'key',
            value: me.getProperty().get('value'),
            width: me.width,
            forceSelection: me.getProperty().getExhaustive(),
            readOnly: me.isReadOnly
        }
    },

    setValue: function (value) {
        if (this.isEdit) {
            this.isCombo()
                ? this.getComboField().setValue(value)
                : this.callParent(arguments);
        } else {
            this.callParent(arguments);
        }
    },

    getComboField: function () {
        return this.down('combobox');
    },

    initListeners: function () {
        var me = this;
        this.callParent(arguments);
        var field = me.getComboField();

        if (field) {
            field.on('change', function () {
                me.getProperty().set('isInheritedOrDefaultValue', false);
                me.updateResetButton();
            });
        }
    }
});

Ext.define('Uni.property.view.property.Text', {
    extend: 'Uni.property.view.property.BaseCombo',

    getNormalCmp: function () {
        var me = this;
        return {
            xtype: 'textfield',
            name: this.getName(),
            itemId: me.key + 'textfield',
            width: me.width,
            msgTarget: 'under',
            readOnly: me.isReadOnly,
            allowBlank: me.allowBlank
        }
    },

    getField: function () {
        return this.down('textfield');
    }
});

Ext.define('Uni.property.view.property.Combobox', {
    extend: 'Uni.property.view.property.Base',

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: me.getProperty().getPossibleValues(),
            queryMode: 'local',
            displayField: 'value',
            valueField: 'key',
            width: me.width,
            forceSelection: me.getProperty().getExhaustive(),
            readOnly: me.isReadOnly
        }
    },

    getField: function () {
        return this.down('combobox');
    }
});

Ext.define('Uni.property.view.property.Textarea', {
    extend: 'Uni.property.view.property.Base',

    getEditCmp: function () {
        var me = this;
        return {
            xtype: 'textareafield',
            name: this.getName(),
            itemId: me.key + 'textareafield',
            width: me.width,
            grow: true,
            msgTarget: 'under',
            readOnly: me.isReadOnly
        }
    },

    getField: function () {
        return this.down('textareafield');
    }
});

Ext.define('Uni.property.view.property.Password', {
    extend: 'Uni.property.view.property.Base',
    requires: [
        'Uni.form.field.Password'
    ],

    getEditCmp: function () {
        var me = this;
        return {
            xtype: 'password-field',
            name: this.getName(),
            itemId: me.key + 'passwordfield',
            width: me.width,
            msgTarget: 'under',
            readOnly: me.isReadOnly,
            fieldLabel: undefined
        }
    },

    getField: function () {
        return this.down('password-field');
    },

    getPasswordField: function () {
        return this.down('textfield');
    },


    initListeners: function () {
        var me = this;
        this.callParent(arguments);
        var field = me.getPasswordField();

        if (field) {
            field.on('change', function () {
                me.getProperty().set('isInheritedOrDefaultValue', false);
                me.updateResetButton();
            });
        }
    }
});

Ext.define('Uni.property.view.property.Hexstring', {
    extend: 'Uni.property.view.property.Text',

    getNormalCmp: function () {
        var result = this.callParent(arguments);
        result.vtype = 'hexstring';

        return result;
    }
});

Ext.define('Uni.property.view.property.Boolean', {
    extend: 'Uni.property.view.property.Base',

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'checkbox',
            name: this.getName(),
            itemId: me.key + 'checkbox',
            width: me.width,
            cls: 'check',
            msgTarget: 'under',
            readOnly: me.isReadOnly,
            boxLabel: me.boxLabel ? me.boxLabel : ''
        };
    },

    getField: function () {
        return this.down('checkbox');
    },

    setValue: function (value) {
        if (!this.isEdit) {
             value = value ? 'Yes' : 'No';
        }
        this.callParent([value]);
    },

    getDisplayCmp: function () {
        var me = this;

        return {
            xtype: 'displayfield',
            name: this.getName(),
            itemId: me.key + 'displayfield',
            width: me.width,
            msgTarget: 'under'
        }
    },


    getDisplayField: function () {
        return this.down('displayfield');
    }
});

Ext.define('Uni.property.view.property.Number', {
    extend: 'Uni.property.view.property.BaseCombo',

    getNormalCmp: function () {
        var me = this;
        var minValue = null;
        var maxValue = null;
        var allowDecimals = true;
        var rule = me.getProperty().getValidationRule();

        if (rule != null) {
            minValue = rule.get('minimumValue');
            maxValue = rule.get('maximumValue');
            allowDecimals = rule.get('allowDecimals');
        }

        return {
            xtype: 'numberfield',
            name: this.getName(),
            itemId: me.key + 'numberfield',
            width: me.width,
            hideTrigger: true,
            keyNavEnabled: false,
            mouseWheelEnabled: false,
            minValue: minValue,
            maxValue: maxValue,
            allowDecimals: allowDecimals,
            msgTarget: 'under',
            readOnly: me.isReadOnly
        };
    },

    getComboCmp: function () {
        var result = this.callParent(arguments);
        result.fieldStyle = 'text-align:right;';

        return result;
    },

    getField: function () {
        return this.down('numberfield');
    }
});

Ext.define('Uni.property.view.property.NullableBoolean', {
    extend: 'Uni.property.view.property.Base',

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'radiogroup',
            itemId: me.key + 'radiogroup',
            name: this.getName(),
            allowBlank: false,
            vertical: true,
            columns: 1,
            readOnly: me.isReadOnly,
            items: [
                {
                    boxLabel: Uni.I18n.translate('true', me.translationKey, 'True'),
                    name: 'rb',
                    itemId: 'rb_1_' + me.key,
                    inputValue: true
                },
                {
                    boxLabel: Uni.I18n.translate('false', me.translationKey, 'False'),
                    name: 'rb',
                    itemId: 'rb_2_' + me.key,
                    inputValue: false
                },
                {
                    boxLabel: Uni.I18n.translate('none', me.translationKey, 'None'),
                    name: 'rb',
                    itemId: 'rb_3_' + me.key,
                    inputValue: null
                }
            ]
        };
    },

    getField: function () {
        return this.down('radiogroup');
    },

    setValue: function (value) {
        var result = {rb: null};
        if (Ext.isBoolean(value)) {
            result.rb = value;
        }

        if (!this.isEdit) {
            if (value === true) {
                result = Uni.I18n.translate('yes', this.translationKey, 'Yes');
            } else if (value === false) {
                result = Uni.I18n.translate('no', this.translationKey, 'No');
            } else {
                result = Uni.I18n.translate('na', this.translationKey, 'N/A');
            }
        }

        this.callParent([result]);
    }
});

Ext.define('Uni.property.view.property.Date', {
    extend: 'Uni.property.view.property.Base',

    format: 'd/m/Y',
    formats: [
        'd.m.Y',
        'd m Y'
    ],

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'datefield',
            name: this.getName(),
            itemId: me.key + 'datefield',
            format: me.format,
            altFormats: me.formats.join('|'),
            width: me.width,
            required: me.required,
            readOnly: me.isReadOnly
        };
    },

    getField: function () {
        return this.down('datefield');
    },

    setValue: function (value) {
        if (value !== null && value !== '') {
            value = new Date(value);

            if (!this.isEdit) {
                value = value.toLocaleDateString();
            }
        }
        this.callParent([value]);
    },

    getValue: function (value) {
        if (value !== null && value !== '') {
            var newDate = new Date(value.getFullYear(), value.getMonth(), value.getDate(),
                0, 0, 0, 0);
            return newDate.getTime();
        } else
        {
            return value;
        }
    }
});

Ext.define('Uni.property.view.property.DateTime', {
    extend: 'Uni.property.view.property.Date',

    timeFormat: 'H:i:s',

    getEditCmp: function () {
        var me = this,
            result = [];

        result[0] = this.callParent(arguments);
        result[1] = {
            xtype: 'timefield',
            name: me.getName() + '.time',
            margin: '0 0 0 16',
            itemId: me.key + 'timefield',
            format: me.timeFormat,
            width: me.width,
            required: me.required,
            readOnly: me.isReadOnly
        };

        return result;
    },

    getTimeField: function () {
        return this.down('timefield');
    },

    getDateField: function () {
        return this.down('datefield');
    },

    setValue: function (value) {
        var dateValue = null,
            timeValue = null;

        if (value !== null && value !== '') {
            var date = new Date(value);
            dateValue = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0, 0);
            timeValue = new Date(1970, 0, 1, date.getHours(), date.getMinutes(), date.getSeconds(), 0);
        }

        if (!this.isEdit) {
            this.callParent([date.toLocaleString()]);
        } else {
            this.callParent([dateValue]);
            this.getTimeField().setValue(timeValue);
        }
    },

    getValue: function (value) {
        var timeValue = this.getTimeField().getValue(),
            dateValue = this.getDateField().getValue();

        if (timeValue !== null && timeValue !== '' && dateValue !== null && dateValue !== '') {
            var newDate = new Date(dateValue.getFullYear(), dateValue.getMonth(), dateValue.getDate(),
                timeValue.getHours(), timeValue.getMinutes(), timeValue.getSeconds(), 0);
            return newDate.getTime();
        }

        return null;
    }
});

/**
 * @class Uni.property.model.field.TimeUnit
 */
Ext.define('Uni.property.model.field.TimeUnit', {
    extend: 'Ext.data.Model',
    fields: ['timeUnit']
});

Ext.define('Uni.property.store.TimeUnits', {
    extend: 'Ext.data.Store',
    requires: [
        'Uni.property.model.field.TimeUnit'
    ],
    model: 'Uni.property.model.field.TimeUnit',
  //  autoLoad: true,

    proxy: {
        type: 'rest',
        url: '../../api/mdc/field/timeUnit',
        reader: {
            type: 'json',
            root: 'timeUnits'
        }
    }
});


Ext.define('Uni.property.view.property.Period', {
    extend: 'Uni.property.view.property.BaseCombo',
    requires: [
        'Uni.property.store.TimeUnits'
    ],

    getNormalCmp: function () {
        var me = this;

        return [
            {
                xtype: 'numberfield',
                itemId: me.key + 'numberfield',
                name: this.getName() + '.numberfield',
                width: me.width,
                required: me.required,
                readOnly: me.isReadOnly
            },
            {
                xtype: 'combobox',
                margin: '0 0 0 16',
                itemId: me.key + 'combobox',
                name: this.getName() + '.combobox',
                store: 'Uni.property.store.TimeUnits',
              //  queryMode: 'local',
                displayField: 'timeUnit',
                valueField: 'timeUnit',
                width: me.width,
                forceSelection: false,
                required: me.required,
                readOnly: me.isReadOnly
            }
        ];
    },

    getComboCmp: function () {
        var store = Ext.create('Ext.data.Store', {
            fields: [
                {name: 'key', type: 'string'},
                {name: 'value', type: 'string'}
            ]
        });

        //clear store
        store.loadData([], false);
        this.getProperty().getPossibleValues().each(function (item) {
            var timeDurationValue = item.get('count') + " " + item.get('timeUnit');
            store.add({key: timeDurationValue, value: timeDurationValue});
        });

        var result = this.callParent(arguments);
        result.store = store;

        return result;
    },

    getField: function () {
        return this.down('numberfield');
    },

    setValue: function (value) {
        var unit = null,
            count = null,
            timeDuration = null;

        if (value != null) {
            unit = value.timeUnit;
            count = value.count;
            timeDuration = count + ' ' + unit;
        }

        if (this.isEdit) {
            if (this.isCombo()) {
                this.getComboField().setValue(timeDuration);
            } else {
                this.getField().setValue(count);
                this.getComboField().setValue(unit);
            }
        } else {
            this.callParent([timeDuration]);
        }
    },

    getValue: function (value) {
        if (!this.isCombo()) {
            var result = {};
            result.count = value.numberfield;
            result.timeUnit = value.combobox;

            return result;
        } else {
            return value;
        }
    }
});

Ext.define('Uni.property.view.property.Time', {
    extend: 'Uni.property.view.property.Base',

    timeFormat: 'H:i:s',

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'timefield',
            name: this.getName(),
            itemId: me.key + 'timefield',
            format: me.timeFormat,
            width: me.width,
            required: me.required,
            readOnly: me.isReadOnly
        };
    },

    getField: function () {
        return this.down('timefield');
    },

    setValue: function (value) {
        if (value !== null && value !== '') {
            value = new Date(value * 1000);

            if (!this.isEdit) {
                value = value.toLocaleTimeString();
            }
        }

        this.callParent([value]);
    },

    getValue: function (value) {
        if (value != null && value != '') {
            var newDate = new Date(1970, 0, 1, value.getHours(), value.getMinutes(), value.getSeconds(), 0);
            return newDate.getTime() / 1000;
        } else {
            return value;
        }
    }
});

Ext.define('Uni.property.view.property.CodeTable', {
    extend: 'Uni.property.view.property.Base',

    getEditCmp: function () {
        var me = this;

        return [
            {
                xtype: 'textfield',
                name: this.getName(),
                itemId: me.key + 'codetable',
                width: me.width,
                readOnly: true
            },
            {
                xtype: 'button',
                text: '...',
                scale: 'small',
                action: 'showCodeTable',
                disabled: me.isReadOnly
            }
        ];
    },

    getField: function () {
        return this.down('textfield');
    }
});

Ext.define('Uni.property.view.property.Reference', {
    extend: 'Uni.property.view.property.BaseCombo'
});

/**
 * A control that allows selection of multiple items in a list.
 */
Ext.define('Ext.ux.form.MultiSelect', {

    extend: 'Ext.form.FieldContainer',

    mixins: {
        bindable: 'Ext.util.Bindable',
        field: 'Ext.form.field.Field'
    },

    alternateClassName: 'Ext.ux.Multiselect',
    alias: ['widget.multiselectfield', 'widget.multiselect'],

    requires: ['Ext.panel.Panel', 'Ext.view.BoundList', 'Ext.layout.container.Fit'],

    uses: ['Ext.view.DragZone', 'Ext.view.DropZone'],

    layout: 'anchor',

    /**
     * @cfg {String} [dragGroup=""] The ddgroup name for the MultiSelect DragZone.
     */

    /**
     * @cfg {String} [dropGroup=""] The ddgroup name for the MultiSelect DropZone.
     */

    /**
     * @cfg {String} [title=""] A title for the underlying panel.
     */

    /**
     * @cfg {Boolean} [ddReorder=false] Whether the items in the MultiSelect list are drag/drop reorderable.
     */
    ddReorder: false,

    /**
     * @cfg {Object/Array} tbar An optional toolbar to be inserted at the top of the control's selection list.
     * This can be a {@link Ext.toolbar.Toolbar} object, a toolbar config, or an array of buttons/button configs
     * to be added to the toolbar. See {@link Ext.panel.Panel#tbar}.
     */

    /**
     * @cfg {String} [appendOnly=false] `true` if the list should only allow append drops when drag/drop is enabled.
     * This is useful for lists which are sorted.
     */
    appendOnly: false,

    /**
     * @cfg {String} [displayField="text"] Name of the desired display field in the dataset.
     */
    displayField: 'text',

    /**
     * @cfg {String} [valueField="text"] Name of the desired value field in the dataset.
     */

    /**
     * @cfg {Boolean} [allowBlank=true] `false` to require at least one item in the list to be selected, `true` to allow no
     * selection.
     */
    allowBlank: true,

    /**
     * @cfg {Number} [minSelections=0] Minimum number of selections allowed.
     */
    minSelections: 0,

    /**
     * @cfg {Number} [maxSelections=Number.MAX_VALUE] Maximum number of selections allowed.
     */
    maxSelections: Number.MAX_VALUE,

    /**
     * @cfg {String} [blankText="This field is required"] Default text displayed when the control contains no items.
     */
    blankText: 'This is a required field',

    /**
     * @cfg {String} [minSelectionsText="Minimum {0}item(s) required"]
     * Validation message displayed when {@link #minSelections} is not met.
     * The {0} token will be replaced by the value of {@link #minSelections}.
     */
    minSelectionsText: 'Minimum {0} item(s) required',

    /**
     * @cfg {String} [maxSelectionsText="Maximum {0}item(s) allowed"]
     * Validation message displayed when {@link #maxSelections} is not met
     * The {0} token will be replaced by the value of {@link #maxSelections}.
     */
    maxSelectionsText: 'Maximum {0} item(s) required',

    /**
     * @cfg {String} [delimiter=","] The string used to delimit the selected values when {@link #getSubmitValue submitting}
     * the field as part of a form. If you wish to have the selected values submitted as separate
     * parameters rather than a single delimited parameter, set this to `null`.
     */
    delimiter: ',',

    /**
     * @cfg String [dragText="{0} Item{1}"] The text to show while dragging items.
     * {0} will be replaced by the number of items. {1} will be replaced by the plural
     * form if there is more than 1 item.
     */
    dragText: '{0} Item{1}',

    /**
     * @cfg {Ext.data.Store/Array} store The data source to which this MultiSelect is bound (defaults to `undefined`).
     * Acceptable values for this property are:
     * <div class="mdetail-params"><ul>
     * <li><b>any {@link Ext.data.Store Store} subclass</b></li>
     * <li><b>an Array</b> : Arrays will be converted to a {@link Ext.data.ArrayStore} internally.
     * <div class="mdetail-params"><ul>
     * <li><b>1-dimensional array</b> : (e.g., <tt>['Foo','Bar']</tt>)<div class="sub-desc">
     * A 1-dimensional array will automatically be expanded (each array item will be the combo
     * {@link #valueField value} and {@link #displayField text})</div></li>
     * <li><b>2-dimensional array</b> : (e.g., <tt>[['f','Foo'],['b','Bar']]</tt>)<div class="sub-desc">
     * For a multi-dimensional array, the value in index 0 of each item will be assumed to be the combo
     * {@link #valueField value}, while the value at index 1 is assumed to be the combo {@link #displayField text}.
     * </div></li></ul></div></li></ul></div>
     */

    ignoreSelectChange: 0,

    /**
     * @cfg {Object} listConfig
     * An optional set of configuration properties that will be passed to the {@link Ext.view.BoundList}'s constructor.
     * Any configuration that is valid for BoundList can be included.
     */

    initComponent: function(){
        var me = this;

        me.bindStore(me.store, true);
        if (me.store.autoCreated) {
            me.valueField = me.displayField = 'field1';
            if (!me.store.expanded) {
                me.displayField = 'field2';
            }
        }

        if (!Ext.isDefined(me.valueField)) {
            me.valueField = me.displayField;
        }
        me.items = me.setupItems();


        me.callParent();
        me.initField();
        me.addEvents('drop');
    },

    setupItems: function() {
        var me = this;

        me.boundList = Ext.create('Ext.view.BoundList', Ext.apply({
            anchor: 'none 100%',
            deferInitialRefresh: false,
            border: 1,
            multiSelect: true,
            store: me.store,
            displayField: me.displayField,
            disabled: me.disabled
        }, me.listConfig));
        me.boundList.getSelectionModel().on('selectionchange', me.onSelectChange, me);

        // Only need to wrap the BoundList in a Panel if we have a title.
        if (!me.title) {
            return me.boundList;
        }

        // Wrap to add a title
        me.boundList.border = false;
        return {
            border: true,
            anchor: 'none 100%',
            layout: 'anchor',
            title: me.title,
            tbar: me.tbar,
            items: me.boundList
        };
    },

    onSelectChange: function(selModel, selections){
        if (!this.ignoreSelectChange) {
            this.setValue(selections);
        }
    },

    getSelected: function(){
        return this.boundList.getSelectionModel().getSelection();
    },

    // compare array values
    isEqual: function(v1, v2) {
        var fromArray = Ext.Array.from,
            i = 0,
            len;

        v1 = fromArray(v1);
        v2 = fromArray(v2);
        len = v1.length;

        if (len !== v2.length) {
            return false;
        }

        for(; i < len; i++) {
            if (v2[i] !== v1[i]) {
                return false;
            }
        }

        return true;
    },

    afterRender: function(){
        var me = this,
            records;

        me.callParent();
        if (me.selectOnRender) {
            records = me.getRecordsForValue(me.value);
            if (records.length) {
                ++me.ignoreSelectChange;
                me.boundList.getSelectionModel().select(records);
                --me.ignoreSelectChange;
            }
            delete me.toSelect;
        }

        if (me.ddReorder && !me.dragGroup && !me.dropGroup){
            me.dragGroup = me.dropGroup = 'MultiselectDD-' + Ext.id();
        }

        if (me.draggable || me.dragGroup){
            me.dragZone = Ext.create('Ext.view.DragZone', {
                view: me.boundList,
                ddGroup: me.dragGroup,
                dragText: me.dragText
            });
        }
        if (me.droppable || me.dropGroup){
            me.dropZone = Ext.create('Ext.view.DropZone', {
                view: me.boundList,
                ddGroup: me.dropGroup,
                handleNodeDrop: function(data, dropRecord, position) {
                    var view = this.view,
                        store = view.getStore(),
                        records = data.records,
                        index;

                    // remove the Models from the source Store
                    data.view.store.remove(records);

                    index = store.indexOf(dropRecord);
                    if (position === 'after') {
                        index++;
                    }
                    store.insert(index, records);
                    view.getSelectionModel().select(records);
                    me.fireEvent('drop', me, records);
                }
            });
        }
    },

    isValid : function() {
        var me = this,
            disabled = me.disabled,
            validate = me.forceValidation || !disabled;


        return validate ? me.validateValue(me.value) : disabled;
    },

    validateValue: function(value) {
        var me = this,
            errors = me.getErrors(value),
            isValid = Ext.isEmpty(errors);

        if (!me.preventMark) {
            if (isValid) {
                me.clearInvalid();
                me.down('boundlist').removeCls('x-form-invalid-field');
            } else {
                me.markInvalid(errors);
                me.down('boundlist').addCls('x-form-invalid-field');
            }
            me.fireEvent('fieldvaliditychange', me, isValid);
        }

        return isValid;
    },

    markInvalid : function(errors) {
        // Save the message and fire the 'invalid' event
        var me = this,
            oldMsg = me.getActiveError();
        me.setActiveErrors(Ext.Array.from(errors));
        if (oldMsg !== me.getActiveError()) {
            me.updateLayout();
        }
    },

    /**
     * Clear any invalid styles/messages for this field.
     *
     * __Note:__ this method does not cause the Field's {@link #validate} or {@link #isValid} methods to return `true`
     * if the value does not _pass_ validation. So simply clearing a field's errors will not necessarily allow
     * submission of forms submitted with the {@link Ext.form.action.Submit#clientValidation} option set.
     */
    clearInvalid : function() {
        // Clear the message and fire the 'valid' event
        var me = this,
            hadError = me.hasActiveError();
        me.unsetActiveError();
        if (hadError) {
            me.updateLayout();
        }
    },

    getSubmitData: function() {
        var me = this,
            data = null,
            val;
        if (!me.disabled && me.submitValue && !me.isFileUpload()) {
            val = me.getSubmitValue();
            if (val !== null) {
                data = {};
                data[me.getName()] = val;
            }
        }
        return data;
    },

    /**
     * Returns the value that would be included in a standard form submit for this field.
     *
     * @return {String} The value to be submitted, or `null`.
     */
    getSubmitValue: function() {
        var me = this,
            delimiter = me.delimiter,
            val = me.getValue();

        return Ext.isString(delimiter) ? val.join(delimiter) : val;
    },

    getValue: function(){
        return this.value || [];
    },

    getRecordsForValue: function(value){
        var me = this,
            records = [],
            all = me.store.getRange(),
            valueField = me.valueField,
            i = 0,
            allLen = all.length,
            rec,
            j,
            valueLen;

        for (valueLen = value.length; i < valueLen; ++i) {
            for (j = 0; j < allLen; ++j) {
                rec = all[j];
                if (rec.get(valueField) == value[i]) {
                    records.push(rec);
                }
            }
        }

        return records;
    },

    setupValue: function(value){
        var delimiter = this.delimiter,
            valueField = this.valueField,
            i = 0,
            out,
            len,
            item;

        if (Ext.isDefined(value)) {
            if (delimiter && Ext.isString(value)) {
                value = value.split(delimiter);
            } else if (!Ext.isArray(value)) {
                value = [value];
            }

            for (len = value.length; i < len; ++i) {
                item = value[i];
                if (item && item.isModel) {
                    value[i] = item.get(valueField);
                }
            }
            out = Ext.Array.unique(value);
        } else {
            out = [];
        }
        return out;
    },

    setValue: function(value){
        var me = this,
            selModel = me.boundList.getSelectionModel(),
            store = me.store;

        // Store not loaded yet - we cannot set the value
        if (!store.getCount()) {
            store.on({
                load: Ext.Function.bind(me.setValue, me, [value]),
                single: true
            });
            return;
        }

        value = me.setupValue(value);
        me.mixins.field.setValue.call(me, value);

        if (me.rendered) {
            ++me.ignoreSelectChange;
            selModel.deselectAll();
            selModel.select(me.getRecordsForValue(value));
            --me.ignoreSelectChange;
        } else {
            me.selectOnRender = true;
        }
    },

    clearValue: function(){
        this.setValue([]);
    },

    onEnable: function(){
        var list = this.boundList;
        this.callParent();
        if (list) {
            list.enable();
        }
    },

    onDisable: function(){
        var list = this.boundList;
        this.callParent();
        if (list) {
            list.disable();
        }
    },

    getErrors : function(value) {
        var me = this,
            format = Ext.String.format,
            errors = [],
            numSelected;

        value = Ext.Array.from(value || me.getValue());
        numSelected = value.length;

        if (!me.allowBlank && numSelected < 1) {
            errors.push(me.blankText);
        }
        if (numSelected < me.minSelections) {
            errors.push(format(me.minSelectionsText, me.minSelections));
        }
        if (numSelected > me.maxSelections) {
            errors.push(format(me.maxSelectionsText, me.maxSelections));
        }
        return errors;
    },

    onDestroy: function(){
        var me = this;

        me.bindStore(null);
        Ext.destroy(me.dragZone, me.dropZone);
        me.callParent();
    },

    onBindStore: function(store){
        var boundList = this.boundList;

        if (boundList) {
            boundList.bindStore(store);
        }
    }

});

Ext.define('Uni.property.view.property.Multiselect', {
    extend: 'Uni.property.view.property.Base',
    requires: [
        'Ext.ux.form.MultiSelect'
    ],

    getEditCmp: function () {
        var me = this;

        return [
            {
                items: [
                    {
                        xtype: 'multiselect',
                        itemId: me.key + 'multiselect',
                        name: me.getName(),
                        allowBlank: !me.getProperty().get('required'),
                        store: me.getProperty().getPredefinedPropertyValues().possibleValues(),
                        displayField: 'name',
                        valueField: 'id',
                        width: me.width,
                        height: 194,
                    readOnly: me.isReadOnly,
                        msgTarget: 'multiselect-invalid-id-' + me.id,
                        validateOnChange: false,
                            listeners: {
                            change: function (field, newValue) {
                                var count = newValue.length;

                                field.nextSibling('#multiselectSelectedItemsInfo').update(Ext.String.format(Uni.I18n.translatePlural('multiselect.selected', count, 'UNI', '{0} items selected'), count));
                            },
                            fieldvaliditychange: function (field, isValid) {
                                field.nextSibling('#multiselectError').setVisible(!isValid);
                            }
                        }
                    },
                    {
                        xtype: 'component',
                        itemId: 'multiselectSelectedItemsInfo',
                        html: Ext.String.format(Uni.I18n.translatePlural('multiselect.selected', 0, 'UNI', '{0} items selected'), 0)
                    },
                    {
                        xtype: 'component',
                        itemId: 'multiselectError',
                        cls: 'x-form-invalid-under',
                        hidden: true,
                        height: 36,
                        html: '<div id="multiselect-invalid-id-' + me.id + '"></div>'
                    }
                ]
            }
        ];
    },

    getDisplayCmp: function () {
        var me = this,
            store = me.getProperty().getPredefinedPropertyValues().possibleValues();

        return {
            xtype: 'displayfield',
            name: me.getName(),
            itemId: me.key + 'displayfield',
            renderer: function (data) {
                var result = '';

                Ext.isArray(data) && Ext.Array.each(data, function (item) {
                    var flag = store.getById(item);

                    flag && (result += flag.get('name') + '<br>');
                });

                return result;
            }
        }
    },

    getField: function () {
        return this.down('multiselect');
    },

    setValue: function (value) {
        var field = this.getField();

        if (this.isEdit) {
            Ext.isArray(value) ? field.setValue(value) : field.reset();
        } else {
            this.getDisplayField().setValue(Ext.isArray(value) ? value : []);
        }
    }
});

/**
 * @class Uni.property.controller.Registry
 * @singleton
 *
 * Properties registry.
 * Use it to add custom properties to the property map.
 *
 * Example:
 *
 * // add properties by one
 * var registry = Uni.property.controller.Registry;
 * registry.addProperty('CUSTOM_PROPERTY', 'App.view.property.Custom');
 *
 * // or provide a config
 * registry.addProperties({
 *      'CUSTOM_1': 'App.view.property.Custom1',
 *      'CUSTOM_2': 'App.view.property.Custom2'
 * })
 *
 */
Ext.define('Uni.property.controller.Registry', {
    extend: 'Ext.app.Controller',
    singleton: true,
    requires: [
        'Uni.property.view.property.Text',
        'Uni.property.view.property.Combobox',
        'Uni.property.view.property.Textarea',
        'Uni.property.view.property.Password',
        'Uni.property.view.property.Hexstring',
        'Uni.property.view.property.Boolean',
        'Uni.property.view.property.Number',
        'Uni.property.view.property.NullableBoolean',
        'Uni.property.view.property.Date',
        'Uni.property.view.property.DateTime',
        'Uni.property.view.property.Period',
        'Uni.property.view.property.Time',
        'Uni.property.view.property.CodeTable',
        'Uni.property.view.property.Reference',
        'Uni.property.view.property.Multiselect'
    ],

    /**
     * Default properties registered
     */
    propertiesMap: {
        TEXT: 'Uni.property.view.property.Text',
        COMBOBOX: 'Uni.property.view.property.Combobox',
        TEXTAREA: 'Uni.property.view.property.Textarea',
        PASSWORD: 'Uni.property.view.property.Password',
        HEXSTRING: 'Uni.property.view.property.Hexstring',
        BOOLEAN: 'Uni.property.view.property.Boolean',
        NUMBER: 'Uni.property.view.property.Number',
        NULLABLE_BOOLEAN: 'Uni.property.view.property.NullableBoolean',
        DATE: 'Uni.property.view.property.Date',
        CLOCK: 'Uni.property.view.property.DateTime',
        TIMEDURATION: 'Uni.property.view.property.Period',
        TIMEOFDAY: 'Uni.property.view.property.Time',
        CODETABLE: 'Uni.property.view.property.CodeTable',
        REFERENCE: 'Uni.property.view.property.Reference',
        EAN13: 'Uni.property.view.property.Text',
        EAN18: 'Uni.property.view.property.Text',
        ENCRYPTED_STRING: 'Uni.property.view.property.Text',
        UNKNOWN: 'Uni.property.view.property.Text',
        LISTVALUE: 'Uni.property.view.property.Multiselect'
    },

    // store must be registered on some ctrl (not in the responsibility of this class: move later?)
    stores: [
        'Uni.property.store.TimeUnits'
    ],

    /**
     * Register new property
     *
     * @param {string} key
     * @param {string} model
     */
    addProperty: function (key, model) {
        if (!Ext.isString(key)) {
            throw '!Ext.isString(key)'
        }

        if (!this.getProperty(key)) {
            this.propertiesMap[key] = model;
        }
    },

    /**
     * Register properties config
     *
     * @param {Object} properties
     */
    addProperties: function (properties) {
        Ext.apply(this.propertiesMap, properties)
    },

    /**
     * Retrieve property widget
     * @see Uni.property.view.property.Base
     *
     * @param {string} key
     * @returns {string|null}
     */
    getProperty: function (key) {
        return this.propertiesMap[key] || null;
    }
});

Ext.define('Uni.property.form.PropertyHydrator', {
    extract: function(record) {
        return record.getData(true);
    },
    falseAndZeroChecker: function(value) {
        if (null != value) {
            if (value.toString() == "false") {
                return false;
            }
            if (value.toString() == "0") {
                return 0;
            }
        }
        return value || null
    },
    hydrate: function(data, record) {
        var values = data;
        var me = this;
        if (typeof record === 'undefined' || !record.properties()) {
            return false;
        }
        record.properties().each(function (property) {
            if (property.get('isInheritedOrDefaultValue') === true) {
                if (property.get('required') === true && property.get('hasDefaultValue')) {
                    var value = me.falseAndZeroChecker(values[property.get('key')]);
                    propertyValue = Ext.create('Uni.property.model.PropertyValue');
                    property.setPropertyValue(propertyValue);
                    propertyValue.set('value', value);
                } else {
                    property.setPropertyValue(null);
                }
            } else {
                var value = me.falseAndZeroChecker(values[property.get('key')]);
                if (!property.raw['propertyValueInfo']) {
                    propertyValue = Ext.create('Uni.property.model.PropertyValue');
                    property.setPropertyValue(propertyValue);
                }
                var propertyValue = property.getPropertyValue();
                propertyValue.set('value', value);
            }

        });
    }
});

/**
 * @class Uni.property.form.Property
 *
 * Properties form. used for display properties.
 * Usage example:
 *
 * // assume you have alredy specify property in view like {xtype: 'property-form'}
 *
 * var form = cmp.down('property-form');
 *
 * // record must have properties() association specified
 * form.loadRecord(record);
 *
 * // You can redraw form with new properties set:
 * form.initProperties(record.properties());
 *
 * // or update current form values
 * form.loadRecord(record);
 * // or
 * form.setProperties(record.properties());
 */
Ext.define('Uni.property.form.Property', {
    extend: 'Ext.form.Panel',
    alias: 'widget.property-form',
    hydrator: 'Uni.property.form.PropertyHydrator',
    border: 0,
    requires: [
        'Uni.property.controller.Registry',
        'Uni.property.form.PropertyHydrator'
    ],
    defaults: {
        labelWidth: 250,
        resetButtonHidden: false
    },
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    initialised: false,
    isEdit: true,
    isReadOnly: false,
    inheritedValues: false,

    /**
     * Loads record to the form.
     * If form is not initialised performs initProperties()
     *
     * @param record
     */
    loadRecord: function (record) {
        this.initProperties(record.properties());
        this.callParent(arguments);
    },

    loadRecordAsNotRequired: function(record){
        var properties = record.properties();
        _.each(properties.data.items,function(item){
            item.set('required',false)
        });
        this.loadRecord(record);
    },

    /**
     * Initialises form, creates form field based on properties specification in property registry:
     * @see Uni.property.controller.Registry
     *
     * @param {MixedCollection} properties
     */
    initProperties: function(properties) {
        var me = this;
        var registry = Uni.property.controller.Registry;

        me.removeAll();
        properties.each(function (property) {
            if (!(property instanceof Uni.property.model.Property)) {
                throw '!(entry instanceof Uni.property.model.Property)';
            }

            me.inheritedValues
                ? property.initInheritedValues()
                : property.initValues();

            properties.commitChanges();

            var type = property.getType();
            var fieldType = registry.getProperty(type);
            if (fieldType) {
                var field = Ext.create(fieldType, Ext.apply(me.defaults, {
                    property: property,
                    isEdit: me.isEdit,
                    isReadOnly: me.isReadOnly
                }));

                me.add(field);
            }
        });

        this.initialised = true;
    },

    useInheritedValues: function() {
        this.items.each(function(item){
            item.useInheritedValue();
        });
        this.inheritedValues = true;
    },

    getFieldValues: function(dirtyOnly) {
        var data = this.getValues(false, dirtyOnly, false, true);
        return this.unFlattenObj(data);
    },

    updateRecord: function() {
        var me = this;
        var raw = me.getFieldValues();
        var values = {};
        _.each(raw.properties || [], function(rawValue, key){
            var field = me.getPropertyField(key);
            values[key] = field.getValue(rawValue);
        });

        this.getForm().hydrator.hydrate(values, me.getRecord());
    },

    unFlattenObj: function(object) {
        return _(object).inject(function(result, value, keys) {
            var current = result,
                partitions = keys.split('.'),
                limit = partitions.length - 1;

            _(partitions).each(function(key, index) {
                current = current[key] = (index == limit ? value : (current[key] || {}));
            });

            return result;
        }, {});
    },

    /**
     * Updates the form with the new properties data
     *
     * @param {MixedCollection} properties
     */
    setProperties: function(properties) {
        var me = this;

        properties.each(function (property) {
            if (!(property instanceof Uni.property.model.Property)) {
                throw '!(entry instanceof Uni.property.model.Property)';
            }

            var field = me.getPropertyField(property.get('key'));
            if (field) {
                field.setProperty(property);
            }
        });
    },

    restoreAll: function() {
        this.items.each(function(item){
            item.restoreDefault();
        })
    },

    /**
     * Returns property field by property model
     * @param {string} key
     * @returns {Uni.property.view.property.Base}
     */
    getPropertyField: function(key) {
        return this.getComponent(key);
    }
});

Ext.define('Uni.property.model.PossibleValue', {
    extend: 'Ext.data.Model',
    fields: ['id', 'name']
});

Ext.define('Uni.property.model.PredefinedPropertyValue', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.PossibleValue'
    ],

    fields: [
        {name: 'exhaustive', type: 'boolean'},
        {name: 'selectionMode', type: 'string'},
        {name: 'possibleValues', type: 'auto'}
    ],
    associations: [
        {name: 'possibleValues', type: 'hasMany', model: 'Uni.property.model.PossibleValue', associationKey: 'possibleValues'}
    ]
});

Ext.define('Uni.property.model.PropertyValue', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'value'},
        {name: 'defaultValue'},
        {name: 'inheritedValue'}
    ]
});

Ext.define('Uni.property.model.PropertyValidationRule', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'allowDecimals'},
        {name: 'minimumValue'},
        {name: 'maximumValue'}
    ]
});

Ext.define('Uni.property.model.PropertyType', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'simplePropertyType'}
    ],
    requires: [
        'Uni.property.model.PredefinedPropertyValue',
        'Uni.property.model.PropertyValidationRule'
    ],
    associations: [
        {
            name: 'predefinedPropertyValuesInfo',
            type: 'hasOne',
            model: 'Uni.property.model.PredefinedPropertyValue',
            associationKey: 'predefinedPropertyValuesInfo',
            getterName: 'getPredefinedPropertyValue',
            setterName: 'setPredefinedPropertyValue',
            foreignKey: 'predefinedPropertyValuesInfo'
        },
        {
            name: 'propertyValidationRule',
            type: 'hasOne',
            model: 'Uni.property.model.PropertyValidationRule',
            associationKey: 'propertyValidationRule',
            getterName: 'getPropertyValidationRule',
            setterName: 'setPropertyValidationRule',
            foreignKey: 'propertyValidationRule'
        }
    ]
});

/**
 * @class Uni.property.model.Property
 */
Ext.define('Uni.property.model.Property', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'key', type: 'string'},
        {name: 'required', type: 'boolean'},
        {name: 'value', persist: false},
        {name: 'default', persist: false},
        {name: 'hasDefault', persist: false},
        {name: 'isInheritedOrDefaultValue', type: 'boolean', defaultValue: true, persist: false}
    ],
    requires: [
        'Uni.property.model.PropertyValue',
        'Uni.property.model.PropertyType'
    ],
    associations: [
        {
            name: 'propertyValueInfo',
            type: 'hasOne',
            model: 'Uni.property.model.PropertyValue',
            associationKey: 'propertyValueInfo',
            getterName: 'getPropertyValue',
            setterName: 'setPropertyValue',
            foreignKey: 'propertyValueInfo'
        },
        {
            name: 'propertyTypeInfo',
            type: 'hasOne',
            model: 'Uni.property.model.PropertyType',
            associationKey: 'propertyTypeInfo',
            getterName: 'getPropertyType',
            setterName: 'setPropertyType',
            foreignKey: 'propertyTypeInfo'
        }
    ],

    /**
     * Sets property values and defaults based on property associated objects
     */
    initValues: function () {
        var me = this;
        var value = null;
        var restoreValue = '';
        var isInheritedValue = true;
        var hasDefaultValue = false;

        // was on try-catch
        if (me.raw['propertyValueInfo']) {
            var propertyValue = me.getPropertyValue() || null;

            if (null !== propertyValue) {
                value = propertyValue.get('value');
                isInheritedValue = false;
                if (value === propertyValue.get('defaultValue')) {
                    isInheritedValue = true;
                }

                if (propertyValue.get('inheritedValue') !== '') {
                    restoreValue = propertyValue.get('inheritedValue');
                } else {
                    restoreValue = propertyValue.get('defaultValue');
                    if (typeof propertyValue.get('defaultValue') !== 'undefined' && typeof propertyValue.get('defaultValue') !== '') {
                        hasDefaultValue = true;
                    }
                }

                if (value === '') {
                    value = restoreValue;
                    isInheritedValue = true;
                }
            }
        }

        me.set('isInheritedOrDefaultValue', isInheritedValue);
        me.set('value', value);
        me.set('default', restoreValue);
        me.set('hasDefaultValue', hasDefaultValue);
    },

    initInheritedValues: function() {
        var me = this;
        var value = null;
        var hasDefaultValue = false;
        var isDefaultValue = false;

        // was on try-catch
        if (me.raw['propertyValueInfo']) {
            var propertyValue = me.getPropertyValue() || null;
            if (null !== propertyValue) {
                value = propertyValue.get('value');
                if (value === propertyValue.get('defaultValue')){
                      isDefaultValue = true;
                }

                if (!value) {
                    value = propertyValue.get('defaultValue');
                    hasDefaultValue = true;
                }
                propertyValue.set('inheritedValue', value);
                propertyValue.set('value', '');
            }
        }
        if (isDefaultValue || (typeof me.raw['propertyValueInfo'] === 'undefined')) {
            me.set('isInheritedOrDefaultValue', true);
        } else {
            me.set('isInheritedOrDefaultValue', false);
        }
        me.set('value', value);
        me.set('default', value);
        me.set('hasDefaultValue', hasDefaultValue);
    },

    getType: function () {
        return this.getPropertyType().get('simplePropertyType');
    },

    getValidationRule: function () {
        var propertyType = this.getPropertyType();

        if (propertyType.raw['propertyValidationRule']) {
            return propertyType.getPropertyValidationRule();
        } else {
            return null;
        }
    },

    getPredefinedPropertyValues: function () {
        var propertyType = this.getPropertyType();

        if (propertyType.raw['predefinedPropertyValuesInfo']) {
            return propertyType.getPredefinedPropertyValue();
        } else {
            return null;
        }
    },

    getPossibleValues: function () {
        var values = this.getPredefinedPropertyValues();
        return values
            ? values.get('possibleValues')
            : null
            ;
    },

    getSelectionMode: function () {
        var values = this.getPredefinedPropertyValues();
        return values
            ? values.get('selectionMode')
            : null
            ;
    },

    getExhaustive: function () {
        var values = this.getPredefinedPropertyValues();
        return values
            ? values.get('exhaustive')
            : null
            ;
    }
});

/**
 * @class Uni.util.Common
 *
 * This class contains the commonly used functions.
 */
Ext.define('Uni.util.Common', {
    singleton: true,

    /**
     * Performs a callback function after all required stores will be loaded. Example usage:
     *
     *     var me =this;
     *
     *     Uni.util.Common.loadNecessaryStores([
     *          'Mdc.store.Domains',
     *          'Mdc.store.Subdomains',
     *          'Mdc.store.EventsOrActions'
     *     ], function () {
     *          me.getFilterForm().loadRecord(router.filter);
     *          me.setFilterView();
     *     }, false);
     *
     * @param {String/Array} stores The stores which must be loaded.
     * @param {Function} callback The callback function.
     * @param {Number} [timeout=30000 ms] Time after which the callback will be performed regardless stores loading.
     * Pass `false` to wait until the stores will be loaded.
     */
    loadNecessaryStores: function (stores, callback, timeout) {
        var me = this,
            counter,
            timeoutId,
            check;

        if (Ext.isString(stores)) {
            stores = [stores];
        }

        counter = stores.length;

        if (timeout !== false) {
            timeoutId = setTimeout(function () {
                counter = 0;
                callback();
            }, timeout || 30000);
        }

        check = function () {
            counter--;
            if (counter === 0) {
                clearTimeout(timeoutId);
                callback();
            }
        };

        Ext.Array.each(stores, function (storeClass) {
            try{
                var store = Ext.getStore(storeClass),
                    isLoading = store.isLoading();

                if (!isLoading && store.getCount()) {
                    check();
                } else if (isLoading) {
                    store.on('load', check, me, {single: true});
                } else {
                    store.load(function () {
                        check();
                    });
                }
            } catch(e) {
                check();
                console.error('\'' + storeClass + '\' not found');
            }
        });
    }
});

Ext.define('Uni.util.FormErrorMessage', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uni-form-error-message',
    ui: 'form-error-framed',
    text: null,
    defaultText: 'There are errors on this page that require your attention.',
    layout: {
        type: 'hbox',
        align: 'middle'
    },
    errorIcon: null,
    defaultErrorIcon: 'x-uni-form-error-msg-icon',
    margin: '7 0 32 0',
    beforeRender: function () {
        var me = this;
        if (!me.text) {
            me.text = me.defaultText;
        }
        if (!me.errorIcon) {
            me.errorIcon = me.defaultErrorIcon
        }
        me.renew();
        me.callParent(arguments)
    },

    renew: function () {
        var me = this;
        me.removeAll(true);
        me.add([
            {
                xtype: 'box',
                height: 22,
                width: 26,
                cls: me.errorIcon
            },
            {
                ui: 'form-error',
                name: 'errormsgpanel',
                html: me.text
            }
        ]);
    },

    setText: function (text) {
        var me = this;
        me.text = text;
        me.renew();
    }

});

Ext.define('Uni.util.FormInfoMessage', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uni-form-info-message',
    cls: Uni.About.baseCssPrefix + 'panel-no-items-found',
    ui: 'small',
    framed: true,
    text: null,
    layout: {
        type: 'hbox',
        align: 'middle'
    },
    margin: '7 0 32 0',
    beforeRender: function () {
        var me = this;
        me.renew();
        me.callParent(arguments)
    },

    renew: function () {
        var me = this;
        me.removeAll(true);
        me.add([
            {
                ui: 'form-error',
                name: 'errormsgpanel',
                html: me.text
            }
        ]);
    },

    setText: function (text) {
        var me = this;
        me.text = text;
        me.renew();
    }

});

/**
 * @class Uni.util.Hydrator
 * todo: rename on
 *
 * This is the hydrator which allows you to work with the associations of the Ext.data.model
 */
Ext.define('Uni.util.Hydrator', {

    /**
     * Extracts data from the provided object
     * @param object Ext.data.Model
     * @returns {Object}
     */
    extract: function (object) {
        var me = this,
            data = object.getData();

        object.associations.each(function (association) {
            switch (association.type) {
                case 'hasOne':
                    data[association.name] = me.extractHasOne(object.get(association.name));
                    break;
                case 'hasMany':
                    data[association.name] = me.extractHasMany(object[association.name]());
                    break;
            }
        });

        return data;
    },

    /**
     * Extracts data from the association object to the Integer
     *
     * @param object The associated record
     *
     * @returns {Number}
     */
    extractHasOne: function (object) {
        return object ? object.getId() : null;
    },

    /**
     * Extracts data from the store to the array
     *
     * @param store The associated store
     *
     * @returns {Number[]}
     */
    extractHasMany: function (store) {
        var result = [];

        store.each(function (record) {
            result.push(record.getId());
        });

        return result;
    },

    // todo: replace on normal promises
    Promise: function(){
        return {
            callback: null,
            callbacks: [],
            when: function(callbacks) {
                this.callbacks = callbacks;
                return this;
            },
            then: function (callback) {
                this.callbacks.length ? this.callback = callback : callback();
                return this;
            },
            resolve: function(fn) {
                var i = _.indexOf(this.callbacks, fn);
                this.callbacks.splice(i, 1);

                if (!this.callbacks.length) {
                    this.callback.call();
                }
                return this;
            }
        }
    },

    /**
     * Hydrates data to the provided object
     *
     * @param data
     * @param object
     */
    hydrate: function (data, object) {
        var me = this,
            fieldData = _.pick(data, object.fields.keys),
            associationData = _.pick(data, object.associations.keys);

        // set object fields
        _.each(fieldData, function (item, key) {
            object.set(key, item);
        });

        var promise = new this.Promise();
        var callbacks = [];

        // set object associations
        _.each(associationData, function (item, key) {
            var association = object.associations.get(key);
            var callback = function() {
                promise.resolve(callback);
            };
            callbacks.push(callback);
            switch (association.type) {
                case 'hasOne':
                    object.set(association.foreignKey, item);
                    var getter = association.createGetter();
                    getter.call(object, callback);
                    break;
                case 'hasMany':
                    me.hydrateHasMany(item, object[key]()).then(callback);
                    break;
            }
        });

        // promise replace here
        return promise.when(callbacks);
    },

    /**
     * Hydrates array data to the associated model store
     *
     * @param data
     * @param store selected association
     */
    hydrateHasMany: function (data, store) {
        store.removeAll(); //todo: replace on allowClear property

        if (!data) {
            return this;
        }

        if (!_.isArray(data)) {
            data = [data];
        }

        var promise = new this.Promise();
        var callbacks = [];

        _.map(data, function (id) {
            var callback = function(record) {
                if (record) {
                    store.add(record);
                }
                promise.resolve(callback);
            };

            callbacks.push(callback);
            store.model.load(id, {
                callback: callback
            });
        });

        // promise replace here
        return promise.when(callbacks);
    }
});

/**
 * @class Uni.util.QueryString
 * @deprecated Use Uni.controller.history.Router instead
 */
Ext.define('Uni.util.QueryString', {
    singleton: true,

    buildQueryString: function (config) {
        var me = this,
            queryString = me.getQueryString(),
            queryObject = Ext.Object.fromQueryString(queryString);

        Ext.apply(queryObject, config || {});

        queryObject = me.cleanQueryObject(queryObject);
        return Ext.Object.toQueryString(queryObject);
    },

    /**
     * Cleans up a query object by removing undefined parameters.
     *
     * @param queryObject
     * @returns {Object} Cleaned up query object
     */
    cleanQueryObject: function (queryObject) {
        var queryObjectCopy = Ext.clone(queryObject || {});

        for (var key in queryObject) {
            if (queryObject.hasOwnProperty(key) && !Ext.isDefined(queryObject[key])) {
                delete queryObjectCopy[key];
            }
        }

        return queryObjectCopy;
    },

    buildHrefWithQueryString: function (config) {
        var me = this,
            url = location.href.split('?')[0],
            queryString = me.buildQueryString(config);
        return url + '?' + queryString;
    },

    getQueryString: function () {
        var token = Ext.util.History.getToken() || document.location.href.split('?')[1],
            queryStringIndex = token.indexOf('?');
        return queryStringIndex < 0 ? '' : token.substring(queryStringIndex + 1);
    },

    getQueryStringValues: function () {
        return Ext.Object.fromQueryString(this.getQueryString());
    }
});

Ext.define('Uni.util.When', {
    success: null,
    failure: null,
    callback: null,
    toExecute: [],
    context: [],
    args: [],
    results: [],
    simple: [],
    count: null,
    failed: false,


    constructor: function () {
        var me = this;
        this.init();
        me.callParent(arguments);
    },

    init: function(){
        this.success = null;
        this.failure = null;
        this.callback = null;
        this.toExecute = [];
        this.context = [];
        this.args = [];
        this.results = [];
        this.simple = [];
        this.count = null;
        this.failed = false;
    },

    when: function (functionsToExecute) {
        this.init();
        for (var i in functionsToExecute) {
            this.toExecute.push(functionsToExecute[i].action);
            this.context.push(functionsToExecute[i].context);
            this.args.push(functionsToExecute[i].args);
            this.simple.push(functionsToExecute[i].simple);
            this.count++;
        }
        return this;
    },
    then: function (callBackObject) {
        this.success = callBackObject.success;
        this.failure = callBackObject.failure;
        this.callback = callBackObject.callback;

        var me = this;

        for (var i in this.toExecute) {

            var args = [];
            var makeSuccessCallBack = function (i) {
                return function () {
                    me.count--;
                    me.results[i] = arguments;
                    if (me.count === 0) {
                        if (me.failed === false) {
                            me.resolveSuccess(me.success);
                        } else {
                            me.resolveFailure(me.failure);
                        }
                    }
                };
            };


            var makeFailureCallBack = function (i) {
                return function () {
                    me.count--;
                    me.results[i] = arguments;
                    me.failed = true;
                    if (me.count === 0) {
                        me.resolveFailure(me.failure);
                    }
                };
            };

            var makeGeneralCallBack = function (i) {
                return function () {
                    me.count--;
                    me.results[i] = arguments;
                    if (me.count === 0) {
                        if (me.failed === false) {
                            me.resolveSuccess(me.callback);
                        } else {
                            me.resolveFailure(me.callback);
                        }
                    }
                };
            };

            if (typeof this.args[i] != 'undefined') {
                args = this.args[i];
            }


            if (typeof this.callback === 'undefined') {
                if (this.simple[i] === false) {
                    args.push({success: makeSuccessCallBack(i), failure: makeFailureCallBack(i)});
                } else {
                    args.push({callback: makeSuccessCallBack(i)});
                }
            } else {
                if (this.simple[i] === false) {
                    args.push({success: makeGeneralCallBack(i), failure: makeGeneralCallBack(i)});
                } else {
                    args.push({callback: makeGeneralCallBack(i)});
                }
            }
            this.toExecute[i].apply(this.context[i], args);
        }
    },

    resolveSuccess: function (successCallBack) {
        successCallBack(this.results);
    },

    resolveFailure: function (failureCallBack) {
        failureCallBack();

    }
});

/**
 * @class Uni.view.navigation.AppCenter
 */
Ext.define('Uni.view.navigation.AppCenter', {
    extend: 'Ext.button.Button',
    xtype: 'uni-nav-appcenter',

    text: '',
    iconCls: 'icon-appcenter',
    cls: Uni.About.baseCssPrefix + 'nav-appcenter',

    menu: {
        xtype: 'menu',
        plain: true,
        showSeparator: false,
        forceLayout: true,
        cls: Uni.About.baseCssPrefix + 'nav-appcenter-menu',
        items: [
            {
                xtype: 'dataview',
                cls: Uni.About.baseCssPrefix + 'nav-appcenter-dataview',
                tpl: [
                    '<div class="handlebar"></div>',
                    '<tpl for=".">',
                    '<a href="{url}"',
                    '<tpl if="isExternal"> target="_blank"</tpl>',
                    '>',
                    '<div class="app-item',
                    '<tpl if="isActive"> x-pressed</tpl>',
                    '">',
                    '<div class="icon icon-{icon}">&nbsp;</div>',
                    '<span class="name">{name}</span>',
                    '</div>',
                    '</a>',
                    '</tpl>'
                ],
                itemSelector: 'div.app-item',
                store: 'apps'
            }
        ]
    },

    initComponent: function () {
        this.callParent(arguments);
    }
});

/**
 * @class Uni.view.navigation.Logo
 */
Ext.define('Uni.view.navigation.Logo', {
    extend: 'Ext.button.Button',
    xtype: 'uni-nav-logo',
    ui: 'navigationlogo',

    text: 'Connexo',

    action: 'home',
    scale: 'medium',
    href: '#',
    hrefTarget: '_self',

    initComponent: function () {
        this.callParent(arguments);
    },

    setLogoTitle: function (title) {
        if (this.rendered) {
            this.setText(title);
        } else {
            this.text = title;
        }
    },

    setLogoGlyph: function (glyph) {
        if (this.rendered) {
            this.setGlyph(glyph);
        } else {
            this.glyph = glyph;
        }
    }
});

/**
 * @class Uni.view.search.Basic
 *
 * Temporary simple search button while features/stories are being developed.
 * Also see: http://jira.eict.vpdc/browse/JP-651
 */
Ext.define('Uni.view.search.Basic', {
    extend: 'Ext.button.Button',
    alias: 'widget.searchBasic',
    itemId: 'searchButton',
    cls: 'search-button',
    glyph: 'xe021@icomoon',
    scale: 'small'
});

/**
 * @class Uni.view.navigation.Help
 */
Ext.define('Uni.view.navigation.Help', {
    extend: 'Ext.button.Button',
    alias: 'widget.navigationHelp',
    action: 'help',
    glyph: 'xe009@icomoon',
    scale: 'small',
    cls: 'nav-help',

    initComponent: function () {
        this.callParent(arguments);
    }
});

/**
 * @class Uni.view.user.Menu
 */
Ext.define('Uni.view.user.Menu', {
    extend: 'Ext.button.Button',
    xtype: 'userMenu',
    scale: 'small',
    cls: 'user-menu',
    iconCls: 'icon-user',

    menu: [
        /*{
         text: 'Profile'
         },
         {
         text: 'Settings',
         glyph: 'xe010@icomoon'
         },
         {
         xtype: 'menuseparator'
         },*/
        {
            text: 'Logout',
            action: 'logout',
            href: '/apps/login/index.html?logout=true'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

/**
 * @class Uni.view.navigation.Header
 */
Ext.define('Uni.view.navigation.Header', {
    extend: 'Ext.container.Container',
    alias: 'widget.navigationHeader',

    ui: 'navigationheader',

    requires: [
        'Uni.view.navigation.AppCenter',
        'Uni.view.navigation.Logo',
        'Uni.view.search.Basic',
        'Uni.view.search.Quick',
        'Uni.view.notifications.Anchor',
        'Uni.view.navigation.Help',
        'Uni.view.user.Menu'
    ],

    layout: {
        type: 'hbox',
        align: 'stretch'
    },

    height: 48,

    /**
     * Most items here have been disabled until their respective stories are fully developed.
     * Also see: http://jira.eict.vpdc/browse/JP-651
     */
    items: [
        {
            xtype: 'uni-nav-appcenter'
        },
        {
            xtype: 'uni-nav-logo'
        },
        {
            xtype: 'component',
            flex: 1
        },
//        {
//            xtype: 'searchBasic'
//        }
//        {
//            xtype: 'searchQuick',
//            flex: 1
//        }
//        {
//            xtype: 'notificationsAnchor'
//        },
//        {
//            xtype: 'navigationHelp'
//        },
        {
            xtype: 'button',
            itemId: 'globalSearch',
            text: Uni.I18n.translate('navigation.header.search', 'UNI', 'Search'),
            cls: 'search-button',
            iconCls: 'icon-search',
            scale: 'small',
            action: 'search',
            href: '#/search',
            hidden: true
        },
        {
            xtype: 'userMenu'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

/**
 * @class Uni.view.navigation.Footer
 */
Ext.define('Uni.view.navigation.Footer', {
    extend: 'Ext.container.Container',
    alias: 'widget.navigationFooter',
    cls: 'nav-footer',
    height: 30,

    layout: {
        type: 'hbox',
        align: 'middle'
    },

    items: [
        {
            xtype: 'component',
            cls: 'powered-by',
            html: 'Powered by <a href="http://www.energyict.com/en/smart-grid" target="_blank">' +
                'Elster EnergyICT Jupiter 1.0.0' +
                '</a>, <a href="http://www.energyict.com/en/smart-grid" target="_blank">' +
                'Smart data management</a>'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

/**
 * @class Uni.view.navigation.Menu
 */
Ext.define('Uni.view.navigation.Menu', {
    extend: 'Ext.container.Container',
    alias: 'widget.navigationMenu',
    ui: 'navigationmenu',

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    defaults: {
        xtype: 'button',
        ui: 'menuitem',
        hrefTarget: '_self',
        toggleGroup: 'menu-items',
        action: 'menu-main',
        enableToggle: true,
        allowDepress: false,
        cls: 'menu-item',
        tooltipType: 'title',
        scale: 'large'
    },

    removeAllMenuItems: function () {
        this.removeAll();
    },

    addMenuItem: function (model) {
        var me = this,
            item = me.createMenuItemFromModel(model);

        // TODO Sort the buttons on their model's index value, instead of relying on insert.
        if (model.data.index === '' || model.data.index === null || model.data.index === undefined) {
            this.add(item);
        } else {
            this.insert(model.data.index, item);
        }
    },

    createMenuItemFromModel: function (model) {
        var iconCls = model.data.glyph ? 'uni-icon-' + model.data.glyph : 'uni-icon-none',
            href = model.data.portal ? '#/' + model.data.portal : model.data.href;

        return {
            tooltip: model.data.text,
            text: model.data.text,
            href: href,
            data: model,
            iconCls: iconCls,
            hidden: model.data.hidden
        };
    },

    selectMenuItem: function (model) {
        var me = this,
            itemId = model.id;

        this.items.items.forEach(function (item) {
            if (itemId === item.data.id) {
                me.deselectAllMenuItems();
                item.toggle(true, false);
            }
        });
    },

    deselectAllMenuItems: function () {
        this.items.items.forEach(function (item) {
            item.toggle(false, false);
        });
    }
});

/**
 * @class Uni.view.breadcrumb.Link
 */
Ext.define('Uni.view.breadcrumb.Link', {
    extend: 'Ext.Component',
    alias: 'widget.breadcrumbLink',
    ui: 'link',

    text: '',
    href: '',

    beforeRender: function () {
        var me = this;

        me.callParent();

        // Apply the renderData to the template args
        Ext.applyIf(me.renderData, {
            text: me.text || '&#160;',
            href: me.href
        });
    },

    renderTpl: [
        '<tpl if="href">',
        '<a href="{href}">',
        '</tpl>',
        '{text}',
        '<tpl if="href"></a></tpl>'
    ]
});

/**
 * @class Uni.view.breadcrumb.Separator
 */
Ext.define('Uni.view.breadcrumb.Separator', {
    extend: 'Ext.Component',
    alias: 'widget.breadcrumbSeparator',
    ui: 'linkseparator',
    html: '&nbsp;'
});

/**
 * @class Uni.view.breadcrumb.Trail
 */
Ext.define('Uni.view.breadcrumb.Trail', {
    extend: 'Ext.container.Container',
    alias: 'widget.breadcrumbTrail',
    ui: 'breadcrumbtrail',

    requires: [
        'Uni.view.breadcrumb.Link',
        'Uni.view.breadcrumb.Separator',
        'Uni.controller.history.Settings'
    ],

    layout: {
        type: 'hbox',
        align: 'middle'
    },

    setBreadcrumbItem: function (item) {
        var me = this;

        if (me.rendered) {
            Ext.suspendLayouts();
        }

        me.removeAll();
        me.addBreadcrumbItem(item);

        if (me.rendered) {
            Ext.resumeLayouts(true);
        }
    },

    addBreadcrumbItem: function (item, baseHref) {
        // TODO Append '#/' when necessary.
        baseHref = baseHref || '';

        if (item.data.relative && baseHref.length > 0) {
            baseHref += Uni.controller.history.Settings.tokenDelimiter;
        }

        var child,
            href = item.data.href,
            link = Ext.widget('breadcrumbLink', {
                text: item.data.text
            });

        try {
            child = item.getChild();
        } catch (ex) {
            // Ignore.
        }

        if (child !== undefined && child.rendered) {
            link.setHref(baseHref + href);
        } else if (child !== undefined && !child.rendered) {
            link.href = baseHref + href;
        }

        this.addBreadcrumbComponent(link);

        // Recursively add the children.
        if (child !== undefined && child !== null) {
            if (item.data.relative) {
                baseHref += href;
            }

            this.addBreadcrumbItem(child, baseHref);
        }
    },

    addBreadcrumbComponent: function (component) {
        var itemCount = this.items.getCount();

        if (itemCount % 2 === 1) {
            this.add(Ext.widget('breadcrumbSeparator'));
        }

        this.add(component);
    }
});

/**
 * @class Uni.view.Viewport
 */
Ext.define('Uni.view.Viewport', {
    extend: 'Ext.container.Viewport',

    requires: [
        'Ext.layout.container.Border',
        'Uni.view.navigation.Header',
        'Uni.view.navigation.Footer',
        'Uni.view.navigation.Menu',
        'Uni.view.container.ContentContainer',
        'Uni.view.breadcrumb.Trail'
    ],

    layout: 'border',
    items: [
        {
            xtype: 'navigationHeader',
            region: 'north',
            weight: 30
        },
        {
            xtype: 'container',
            ui: 'navigationwrapper',
            region: 'west',
            layout: 'absolute',
            width: 48,
            items: [
                {
                    xtype: 'navigationMenu'
                }
            ],
            weight: 10
        },
        {
            xtype: 'container',
            region: 'center',
            itemId: 'contentPanel',
            layout: 'fit'
        },
        {
            region: 'north',
            xtype: 'container',
            itemId: 'northContainer',
            cls: 'north',
            layout: 'hbox',
            ui: 'breadcrumbtrailcontainer',
            height: 48,
            items: [
                {
                    xtype: 'breadcrumbTrail',
                    itemId: 'breadcrumbTrail'
                }
            ],
            weight: 20
        }
    ]
});

Ext.define('Uni.view.button.TagButton', {
    extend: 'Ext.button.Split',
    alias: 'widget.tag-button',
    split: true,
    menu: {},
    ui: 'tag',
    arrowCls: null,
    afterRender: function () {
        var me = this,
            baseSpan = me.getEl().first(),
            textSpan = baseSpan.first().first(),
            closeIcon = baseSpan.createChild({
                tag: 'span',
                cls: 'x-btn-tag-right'
            }),
            closeIconEl = baseSpan.getById(closeIcon.id);
        textSpan.addCls(me.iconCls ? 'x-btn-tag-text' : 'x-btn-tag-text-noicon');
        closeIconEl.on('click', function(){
            me.fireEvent('closeclick', me);
            me.destroy();
        });
        this.callParent(arguments)
    }
});

Ext.define('Uni.view.button.SortItemButton', {
    extend: 'Uni.view.button.TagButton',
    alias: 'widget.sort-item-btn',
    name: 'sortitembtn',
    iconCls: 'x-btn-sort-item-asc',
    sortOrder: 'asc'
});

Ext.define('Uni.view.button.StepButton', {
    extend: 'Ext.button.Button',
    alias: 'widget.step-button',
    ui: 'step-active'
});

/**
 * @class Uni.view.container.EmptyGridContainer
 *
 * The {@link Uni.view.container.EmptyGridContainer} shows a custom component when the grid
 * it is displaying does not have any data. By default it shows the grid, after the store is
 * done loading with no items then the custom {@link #emptyComponent} will be shown.
 *
 * The {@link #grid} needs to be of the type {@link Ext.grid.Panel} and have a valid store
 * attached as property. While the {@link #emptyComponent} can be any type of component.
 *
 * # How to use
 *
 *     @example
 *     {
 *         xtype: 'emptygridcontainer',
 *         grid: {
 *             xtype: 'Ext.grid.Panel',
 *             store: 'myStore',
 *             // Other properties.
 *         },
 *         emptyComponent: {
 *             xtype: 'component',
 *             html: '<h4>There are no items</h4>'
 *         }
 *     }
 *
 */
Ext.define('Uni.view.container.EmptyGridContainer', {
    extend: 'Ext.container.Container',
    xtype: 'emptygridcontainer',

    layout: 'card',
    activeItem: 1,

    /**
     * @cfg {Object/Ext.grid.Panel}
     *
     * Grid to show in the panel
     */
    grid: null,

    /**
     * @cfg {Object/Ext.Component}
     *
     * Component to show when the grid store is empty after loading.
     */
    emptyComponent: null,

    mixins: {
        bindable: 'Ext.util.Bindable'
    },

    items: [
        {
            xtype: 'container',
            itemId: 'emptyContainer'
        },
        {
            xtype: 'container',
            itemId: 'gridContainer'
        }
    ],

    initComponent: function () {
        var me = this,
            grid = me.grid,
            emptyCmp = me.emptyComponent;

        if (!(grid instanceof Ext.Component)) {
            grid = Ext.clone(grid);
        }

        me.items[1].items = grid;

        if (!(emptyCmp instanceof Ext.Component)) {
            emptyCmp = Ext.clone(emptyCmp);
        }

        me.items[0].items = emptyCmp;

        this.callParent(arguments);

        me.grid = me.getGridCt().items.items[0];
        me.bindStore(me.grid.store || 'ext-empty-store', true);

        this.on('beforedestroy', this.onBeforeDestroy, this);
    },

    getStoreListeners: function () {
        return {
            beforeload: this.onBeforeLoad,
            load: this.onLoad
        };
    },

    onBeforeDestroy: function () {
        this.bindStore('ext-empty-store');
    },

    onBeforeLoad: function () {
        var me = this;

        me.getLayout().setActiveItem(me.getGridCt());
    },

    onLoad: function () {
        var me = this,
            count = me.grid.store.getCount(),
            isEmpty = count === 0;

        me.getLayout().setActiveItem(isEmpty ? me.getEmptyCt() : me.getGridCt());
    },

    getGridCt: function () {
        return this.down('#gridContainer');
    },

    getEmptyCt: function () {
        return this.down('#emptyContainer');
    }
});

/**
 * @class Uni.view.container.PreviewContainer
 *
 * The {@link Uni.view.container.EmptyGridContainer} shows a custom component when the grid
 * it is displaying does not have any data. By default it shows the grid, after the store is
 * done loading with no items then the custom {@link #emptyComponent} will be shown.
 *
 * The {@link #grid} needs to be of the type {@link Ext.grid.Panel} and have a valid store
 * attached as property. While the {@link #emptyComponent} and {@link #previewComponent}
 * can be any type of component.
 *
 * # How to use
 *
 *     @example
 *     {
 *         xtype: 'preview-container',
 *         grid: {
 *             xtype: 'Ext.grid.Panel',
 *             store: 'myStore',
 *             // Other properties.
 *         },
 *         emptyComponent: {
 *             xtype: 'component',
 *             html: 'There are no items'
 *         },
 *         previewComponent: {
 *             xtype: 'component',
 *             html: 'Some preview.'
 *         }
 *     }
 *
 */
Ext.define('Uni.view.container.PreviewContainer', {
    extend: 'Ext.container.Container',
    xtype: 'preview-container',
    itemId: 'preview-container',

    layout: 'card',
    activeItem: 1,

    /**
     * @cfg {Object/Ext.grid.Panel}
     *
     * Grid to show in the panel
     */
    grid: null,

    /**
     * @cfg {Object/Ext.Component}
     *
     * Component to show when the grid store is empty after loading.
     */
    emptyComponent: null,

    /**
     * @cfg {Object/Ext.Component}
     *
     * Component to show the preview in of the selected row. This is hidden when there are no rows.
     */
    previewComponent: null,

    /**
     * @cfg {Boolean}
     *
     * Select a row by default or not, by default true.
     */
    selectByDefault: true,

    mixins: {
        bindable: 'Ext.util.Bindable'
    },

    items: [
        {
            xtype: 'container'
        },
        {
            xtype: 'container',
            itemId: 'wrapper-container',
            items: [
            ]
        }
    ],

    initComponent: function () {
        var me = this,
            grid = me.grid,
            emptyCmp = me.emptyComponent,
            previewCmp = me.previewComponent;

        // Empty component.

        if (!(emptyCmp instanceof Ext.Component)) {
            emptyCmp = Ext.clone(emptyCmp);
        }
        me.items[0] = emptyCmp;

        // Grid and preview component.

        me.items[1].items = [];

        if (!(grid instanceof Ext.Component)) {
            grid = Ext.clone(grid);
        }

        // TODO Hardcoded height until [JP-2852] is implemented.
        grid.maxHeight = 450;

        me.items[1].items.push(grid);

        if (!(previewCmp instanceof Ext.Component)) {
            previewCmp = Ext.clone(previewCmp);
        }

        me.items[1].items.push(previewCmp);

        // Continue.

        me.callParent(arguments);

        me.grid = me.getWrapperCt().items.items[0];
        me.previewComponent = me.getWrapperCt().items.items[1];

        me.bindStore(me.grid.store || 'ext-empty-store', true);
        me.initChildPagingBottom();
        me.initGridListeners();

        me.on('beforedestroy', me.onBeforeDestroy, me);
    },

    doChildPagingOperation: function (xtype, operation) {
        var me = this,
            pagingComponent;

        if (Ext.isDefined(me.previewComponent)) {
            pagingComponent = me.previewComponent.down(xtype);
        } else {
            return;
        }

        if (pagingComponent !== null
            && Ext.isDefined(pagingComponent)
            && pagingComponent.getXType() === xtype) {
            pagingComponent.updatePagingParams = false;
            operation(pagingComponent);
        }
    },

    resetChildPagingTop: function () {
        var me = this,
            pagingTopXType = 'pagingtoolbartop';

        me.doChildPagingOperation(pagingTopXType, function (pagingComponent) {
            pagingComponent.resetPaging();
        });
    },

    initChildPagingBottom: function () {
        var me = this,
            pagingBottomXType = 'pagingtoolbarbottom';

        me.doChildPagingOperation(pagingBottomXType, function (pagingComponent) {
            pagingComponent.updatePagingParams = false;
        });
    },

    resetChildPagingBottom: function () {
        var me = this,
            pagingBottomXType = 'pagingtoolbarbottom';

        me.doChildPagingOperation(pagingBottomXType, function (pagingComponent) {
            pagingComponent.resetPaging();
        });
    },

    initGridListeners: function () {
        var me = this;

        me.grid.on('selectionchange', me.onGridSelectionChange, me);
    },

    onGridSelectionChange: function () {
        var me = this,
            selection = me.grid.view.getSelectionModel().getSelection();

        if (me.rendered) {
            Ext.suspendLayouts();
        }

        if (me.previewComponent) {
            me.previewComponent.setVisible(selection.length === 1);
        }

        me.resetChildPagingTop();
        me.resetChildPagingBottom();
        me.grid.getView().focusRow(selection[0]);
        if (me.rendered) {
            Ext.resumeLayouts(true);
        }
    },

    getStoreListeners: function () {
        return {
            beforeload: this.onBeforeLoad,
            bulkremove: this.onLoad,
            remove: this.onLoad,
            clear: this.onLoad,
            load: this.onLoad
        };
    },

    onBeforeDestroy: function () {
        this.bindStore('ext-empty-store');
    },

    onBeforeLoad: function () {
        var me = this,
            activeIndex = me.items.indexOf(me.getLayout().getActiveItem());

        try {
            me.grid.getView().getSelectionModel().deselectAll(true);
        } catch (ex) {
            // Ignore exceptions for when the selection model is not ready yet.
        }

        if (activeIndex !== 1) {
            me.getLayout().setActiveItem(1);
        }
    },

    onLoad: function () {
        var me = this,
            count = me.grid.store.getCount(),
            isEmpty = count === 0,
            activeIndex = me.items.indexOf(me.getLayout().getActiveItem());

        if (isEmpty && activeIndex !== 0) {
            me.getLayout().setActiveItem(0);
        } else if (!isEmpty && activeIndex !== 1) {
            me.getLayout().setActiveItem(1);
        }

        if (me.selectByDefault && !isEmpty) {
            me.grid.getView().getSelectionModel().preventFocus = true;
            me.grid.getView().getSelectionModel().select(0);
        }
    },

    getWrapperCt: function () {
        return this.down('#wrapper-container');
    }
});

/**
 * @class Uni.view.form.CheckboxGroup
 * This is the checkboxgroup extension, which allows to auto-load checkboxes from bounded store/
 *
 * Example:
 *   {
 *      xtype: 'checkboxstore',
 *      fieldLabel: 'Select users',
 *      store: 'App.store.Users',
 *      columns: 1,
 *      vertical: true,
 *      name: 'users'
 *   }
 */
Ext.define('Uni.view.form.CheckboxGroup', {
    extend: 'Ext.form.CheckboxGroup',
    alias: 'widget.checkboxstore',

    mixins: {
        bindable: 'Ext.util.Bindable'
    },

    /**
     * This field will be used as boxLabel on checkbox
     */
    displayField: 'name',

    /**
     * This field will define the value of checkbox
     */
    valueField: 'id',

    initComponent: function () {
        var me = this;
        me.bindStore(me.store || 'ext-empty-store', true);
        this.callParent(arguments);
    },

    /**
     * @private
     * Refreshes the content of the checkbox group
     */
    refresh: function () {
        var me = this;
        me.removeAll();
        me.store.each(function (record) {
            me.add({
                xtype: 'checkbox',
                boxLabel: record.get(me.displayField),
                inputValue: record.get(me.valueField),
                name: me.name,
                getModelData: function () {
                    return null;
                }
            });
        });
    },

    getModelData: function () {
        var me = this,
            groups = [],
            object = {};

        Ext.Array.each(me.query('checkbox'), function (checkbox) {
            if (checkbox.getValue()) {
                me.store.each(function (group) {
                    if (group.get(me.valueField) === checkbox.inputValue) {
                        groups.push(group.getId());
                    }
                });
            }
        });

        object[me.name] = groups;

        return object;
    },

    setValue: function (data) {
        var values = {};
        values[this.name] = data;
        this.callParent([values]);
    },

    getStoreListeners: function () {
        return {
            load: this.refresh
        };
    }
});

/**
 * @class Uni.view.grid.SelectionGrid
 */
Ext.define('Uni.view.grid.SelectionGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'selection-grid',

    requires: [
        'Ext.grid.plugin.BufferedRenderer'
    ],

    bottomToolbarHeight: 27,

    selType: 'checkboxmodel',
    selModel: {
        mode: 'MULTI',
        showHeaderCheckbox: false
    },

    overflowY: 'auto',
    maxHeight: 450,

    extraTopToolbarComponent: undefined,

    plugins: [
        {
            ptype: 'bufferedrenderer',
            trailingBufferZone: 5,
            leadingBufferZone: 5,
            scrollToLoadBuffer: 10,
            onViewResize: function (view, width, height, oldWidth, oldHeight) {
                if (!oldHeight || height !== oldHeight) {
                    var me = this,
                        newViewSize,
                        scrollRange;
                    if (view.all.getCount()) {
                        delete me.rowHeight;
                    }
                    scrollRange = me.getScrollHeight();
                    newViewSize = 18;
                    me.viewSize = me.setViewSize(newViewSize);
                    me.stretchView(view, scrollRange);
                }
            }
        }
    ],

    /**
     * @cfg counterTextFn
     *
     * The translation function to use to translate the selected count on top of the
     * text above the grid.
     *
     * @param {Number} count Count to base the translation on.
     * @returns {String} Translation value based on the count.
     */
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'grid.BulkSelection.counterText',
            count,
            'UNI',
            '{0} items selected'
        );
    },

    /**
     * @cfg uncheckText
     *
     * Text used for the uncheck all button.
     */
    uncheckText: Uni.I18n.translate('general.uncheckAll', 'UNI', 'Uncheck all'),

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                xtype: 'toolbar',
                dock: 'top',
                itemId: 'topToolbarContainer',
                layout: {
                    type: 'hbox',
                    align: 'middle'
                },
                items: [
                    {
                        xtype: 'text',
                        itemId: 'selectionCounter',
                        text: me.counterTextFn(0),
                        margin: '0 8 0 0'
                    },
                    {
                        xtype: 'button',
                        itemId: 'uncheckAllButton',
                        text: me.uncheckText,
                        action: 'uncheckAll',
                        margin: '0 0 0 8',
                        disabled: true
                    }
                ]
            }
        ];

        me.callParent(arguments);

        me.getUncheckAllButton().on('click', me.onClickUncheckAllButton, me);
        me.on('selectionchange', me.onSelectionChange, me);

        me.addComponentInToolbar();
    },

    onClickUncheckAllButton: function (button) {
        var me = this;

        me.view.getSelectionModel().deselectAll();
        button.setDisabled(true);
    },

    onSelectionChange: function () {
        var me = this,
            selection = me.view.getSelectionModel().getSelection();

        me.getSelectionCounter().setText(me.counterTextFn(selection.length));
        me.getUncheckAllButton().setDisabled(selection.length === 0);
        me.doLayout();
    },

    getSelectionCounter: function () {
        return this.down('#selectionCounter');
    },

    getUncheckAllButton: function () {
        return this.down('#uncheckAllButton');
    },

    getTopToolbarContainer: function () {
        return this.down('#topToolbarContainer');
    },

    addComponentInToolbar: function () {
        var me = this;
        me.getTopToolbarContainer().add(
            me.extraTopToolbarComponent
        )
    }
});

/**
 * @class Uni.view.grid.BulkSelection
 *
 * The bulk selection component is used for when all or selected items for a specific
 * screen need to be added. A checkbox is used to select the models in the infinite
 * scrolling grid below the radio options. You can either select all items or only
 * a few specific ones from the grid and then press the add button.
 *
 * The 'allitemsadd' or 'selecteditemsadd' event is fired respectively for the all
 * and selected items adding.
 *
 * Example:
 *     {
 *         xtype: 'Uni.view.grid.BulkSelection',
 *
 *         store: 'Mdc.store.ValidationRuleSetsForDeviceConfig',
 *
 *         counterTextFn: function (count) {
 *             return Uni.I18n.translatePlural(
 *                 'validation.noValidationRuleSetSelected',
 *                 count,
 *                 'MDC',
 *                 '{0} validation rule sets selected'
 *             );
 *         },
 *
 *         allLabel: Uni.I18n.translate('ruleset.allRuleSets', 'MDC', 'All validation rule sets'),
 *         allDescription: Uni.I18n.translate(
 *             'ruleset.selectAllRuleSets',
 *             'MDC',
 *             'Select all validation rule sets related to device configuration'
 *         ),
 *
 *         selectedLabel: Uni.I18n.translate('ruleset.selectedRuleSets', 'MDC', 'Selected validation rule sets'),
 *         selectedDescription: Uni.I18n.translate(
 *             'ruleset.selectRuleSets',
 *             'MDC',
 *             'Select validation rule sets in table'
 *         ),
 *
 *         columns: [
 *             {
 *                 header: Uni.I18n.translate('validation.ruleSetName', 'MDC', 'Validation rule set'),
 *                 dataIndex: 'name',
 *                 renderer: function (value, metaData, record) {
 *                     metaData.tdAttr = 'data-qtip="' + record.get('description') + '"';
 *                     return '<a href="#/administration/validation/rulesets/' + record.getId() + '">' + value + '</a>';
 *                 },
 *                 flex: 1
 *             },
 *             {
 *                 header: Uni.I18n.translate('validation.activeRules', 'CFG', 'Active rules'),
 *                 dataIndex: 'numberOfRules',
 *                 flex: 1,
 *                 renderer: function (value, b, record) {
 *                     var numberOfActiveRules = record.get('numberOfRules') - record.get('numberOfInactiveRules');
 *                     return numberOfActiveRules;
 *                 }
 *             },
 *             {
 *                 header: Uni.I18n.translate('validation.inactiveRules', 'CFG', 'Inactive rules'),
 *                 dataIndex: 'numberOfInactiveRules',
 *                 flex: 1
 *             },
 *             {
 *                 xtype: 'uni-actioncolumn',
 *                 items: 'Mdc.view.setup.validation.AddRuleSetActionMenu'
 *             }
 *         ],
 *
 *         // Other code...
 *     }
 */
Ext.define('Uni.view.grid.BulkSelection', {
    extend: 'Uni.view.grid.SelectionGrid',
    xtype: 'bulk-selection-grid',

    maxHeight: 600,

    /**
     * @cfg allLabel
     *
     * Text to show for the all items label.
     */
    allLabel: Uni.I18n.translate('grid.BulkSelection.allLabel', 'UNI', 'All items'),

    /**
     * @cfg allDescription
     *
     * Description to show under the all items label.
     */
    allDescription: Uni.I18n.translate(
        'grid.BulkSelection.allDescription',
        'UNI',
        'Select all items'
    ),

    /**
     * @cfg selectedLabel
     *
     * Text to show for the selected items label.
     */
    selectedLabel: Uni.I18n.translate('grid.BulkSelection.selectedLabel', 'UNI', 'Selected items'),

    /**
     * @cfg selectedDescription
     *
     * Description to show under the selected items label.
     */
    selectedDescription: Uni.I18n.translate(
        'grid.BulkSelection.selectedDescription',
        'UNI',
        'Select items in table'
    ),

    /**
     * @cfg addText
     *
     * Text used for the add button.
     */
    addText: Uni.I18n.translate('general.add', 'UNI', 'Add'),

    /**
     * @cfg cancelText
     *
     * Text used for the cancel button.
     */
    cancelText: Uni.I18n.translate('general.cancel', 'UNI', 'Cancel'),

    /**
     * @cfg cancelHref
     *
     * The URL to be used for the cancel button.
     */
    cancelHref: window.location.href,

    /**
     * @cfg allChosenByDefault
     *
     * The property that determines what radio button will be selected when using the
     * bulk selection component. By default the all items option is chosen. Set this
     * to 'false' to select the selected items option.
     */
    allChosenByDefault: true,

    /**
     * @cfg allInputValue
     */
    allInputValue: 'allItems',

    /**
     * @cfg selectedInputValue
     */
    selectedInputValue: 'selectedItems',

    /**
     * @cfg radioGroupName
     */
    radioGroupName: 'selectedGroupType-' + new Date().getTime() * Math.random(),

    /**
     * @cfg bottomToolbarHidden
     */
    bottomToolbarHidden: false,

    gridHeight: 0,
    gridHeaderHeight: 0,

    initComponent: function () {
        var me = this;

        me.addEvents(
            /**
             * @event allitemsadd
             *
             * Fires after pressing the add button while having the all items option chosen.
             */
            'allitemsadd',
            /**
             * @event selecteditemsadd
             *
             * Fires after pressing the add button while having the selected items option chosen.
             *
             * @param {Ext.data.Model[]} The selected items.
             */
            'selecteditemsadd'
        );

        me.callParent(arguments);

        me.addDocked({
            xtype: 'radiogroup',
            dock: 'top',
            itemId: 'itemradiogroup',
            columns: 1,
            vertical: true,
            submitValue: false,
            defaults: {
                padding: '0 0 16 0'
            },
            items: [
                {
                    name: me.radioGroupName,
                    boxLabel: '<b>' + me.allLabel + '</b>',
                    afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">' + me.allDescription + '</span>',
                    inputValue: me.allInputValue,
                    checked: me.allChosenByDefault
                },
                {
                    name: me.radioGroupName,
                    boxLabel: '<b>' + me.selectedLabel + '</b>',
                    afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">' + me.selectedDescription + '</span>',
                    inputValue: me.selectedInputValue,
                    checked: !me.allChosenByDefault
                }
            ]
        }, 0);

        me.addDocked({
            xtype: 'toolbar',
            dock: 'bottom',
            itemId: 'bottomToolbar',
            layout: 'hbox',
            items: [
                {
                    xtype: 'button',
                    itemId: 'addButton',
                    text: me.addText,
                    action: 'add',
                    ui: 'action'
                },
                {
                    xtype: 'button',
                    itemId: 'cancelButton',
                    text: me.cancelText,
                    action: 'cancel',
                    ui: 'link',
                    href: me.cancelHref
                }
            ]
        });

        me.getSelectionGroupType().on('change', me.onChangeSelectionGroupType, me);
        me.getAddButton().on('click', me.onClickAddButton, me);
        me.on('selectionchange', me.onBulkSelectionChange, me);

        if (me.bottomToolbarHidden) {
            me.hideBottomToolbar();
        }

        me.store.on('afterrender', me.onChangeSelectionGroupType, me, {
            single: true
        });

        me.store.on('load', me.onSelectDefaultGroupType, me, {
            single: true
        });
    },

    onSelectDefaultGroupType: function () {
        var me = this,
            value = {};

        if (me.rendered) {
            value[me.radioGroupName] = me.allChosenByDefault ? me.allInputValue : me.selectedInputValue;
            me.getSelectionGroupType().setValue(value);
            me.getSelectionGroupType().fireEvent('change');
        }

        me.onChangeSelectionGroupType();
    },

    onChangeSelectionGroupType: function (radiogroup, value) {
        var me = this;
        if (me.view) {
            var selection = me.view.getSelectionModel().getSelection();

            me.getAddButton().setDisabled(!me.isAllSelected() && selection.length === 0);
            me.setGridVisible(!me.isAllSelected());
        }
    },

    setGridVisible: function (visible) {
        var me = this,
            gridHeight = me.gridHeight,
            gridHeaderHeight = me.gridHeaderHeight,
            currentGridHeight,
            currentGridHeaderHeight,
            noBorderCls = 'force-no-border';

        me.getTopToolbarContainer().setVisible(visible);

        if (me.rendered) {
            currentGridHeight = me.getView().height;
            currentGridHeaderHeight = me.headerCt.height;

            if (!visible) {
                gridHeight = 0;
                gridHeaderHeight = 0;

                me.addCls(noBorderCls);
            } else {
                me.removeCls(noBorderCls);
            }

            if (currentGridHeight !== 0 && currentGridHeaderHeight !== 0) {
                me.gridHeight = currentGridHeight;
                me.gridHeaderHeight = currentGridHeaderHeight;
            }

            if (typeof gridHeight === 'undefined') {
                var row = me.getView().getNode(0),
                    rowElement = Ext.get(row);

                if (rowElement !== null) {
                    var count = me.store.getCount() > 10 ? 10 : me.store.getCount();
                    gridHeight = count * rowElement.getHeight();
                }
            }

            me.getView().height = gridHeight;
            me.headerCt.height = gridHeaderHeight;
            me.doLayout();
        }
    },

    onClickAddButton: function () {
        var me = this,
            selection = me.view.getSelectionModel().getSelection();

        if (me.isAllSelected()) {
            me.fireEvent('allitemsadd');
        } else if (selection.length > 0) {
            me.fireEvent('selecteditemsadd', selection);
        }
    },

    onBulkSelectionChange: function () {
        var me = this,
            selection = me.view.getSelectionModel().getSelection();

        me.getAddButton().setDisabled(!me.isAllSelected() && selection.length === 0);
    },

    isAllSelected: function () {
        var me = this,
            groupType = me.getSelectionGroupType().getValue();

        return groupType[me.radioGroupName] === me.allInputValue;
    },

    getSelectionGroupType: function () {
        return this.down('radiogroup');
    },

    getAddButton: function () {
        return this.down('#addButton');
    },

    getCancelButton: function () {
        return this.down('#cancelButton');
    },

    getBottomToolbar: function () {
        return this.down('#bottomToolbar');
    },

    hideBottomToolbar: function () {
        this.getBottomToolbar().setVisible(false);
    }
});

/**
 * @class Uni.view.grid.ConnectedGrid
 *
 * This connected grid component is used when we have list of some items,
 * and wanted to choose only several of them.
 * Two grid panels are created, and we can move items from one grid to another using buttons, or drag'n'drop.
 *
 * Example:
 *
 * {
 *   xtype: 'fieldcontainer',
 *       fieldLabel: Uni.I18n.translate('comtask.messages', 'MDC', 'Messages'),
 *   labelWidth: 200,
 *   items:[
 *     {
 *       xtype: 'displayfield',
 *       value: Uni.I18n.translate('comtask.messages.text', 'MDC', 'Send pending messages of these message categories every time this communication task executes')
 *     },
 *     {
 *       xtype: 'connected-grid',
 *       allItemsTitle: Uni.I18n.translate('comtask.message.cathegories', 'MDC', 'Message categories'),
 *       allItemsStoreName: 'Mdc.store.MessageCategories',
 *       selectedItemsTitle: Uni.I18n.translate('comtask.selected.message.cathegories', 'MDC', 'Selected message categories'),
 *       selectedItemsStoreName: 'Mdc.store.SelectedMessageCategories',
 *       displayedColumn: 'name',
 *       disableIndication: true,
 *       enableSorting: true
 *     }
 *   ]
 * },
 *
 */


Ext.define('Uni.view.grid.ConnectedGrid', {
    extend: 'Ext.container.Container',
    xtype: 'connected-grid',

    requires: [
        'Uni.grid.plugin.DragDropWithoutIndication'
    ],

    layout: {
        type: 'hbox'
    },

    allItemsTitle: null,

    allItemsStoreName: null,

    selectedItemsTitle: null,

    selectedItemsStoreName: null,

    displayedColumn: null,

    disableIndication: false,

    enableSorting: false,


    initComponent: function () {
        var me = this,
            allItems = me.id + 'allItemsGrid',
            selectedItems = me.id + 'selectedItemsGrid',
            dragDropPlugin = 'gridviewdragdrop';

        if (me.disableIndication) {
            dragDropPlugin = 'gridviewdragdropwithoutindication'
        }


        if (Ext.isEmpty(me.displayedColumn)) {
            me.displayedColumn = 'name';
        }

        me.items = [
            {
                xtype: 'gridpanel',
                itemId: 'allItemsGrid',
                store: me.allItemsStoreName,
                title: me.allItemsTitle,
                hideHeaders: true,
                selModel: {
                    mode: "MULTI"
                },
                columns: [
                    {
                        dataIndex: me.displayedColumn,
                        flex: 1
                    }
                ],
                viewConfig: {
                    plugins: {
                        ptype: dragDropPlugin,
                        dragGroup: allItems,
                        dropGroup: selectedItems
                    },
                    listeners: {
                        drop: function (node, data, dropRec, dropPosition) {
                            me.enableSorting && me.getAllItemsStore().sort(me.displayedColumn, 'ASC');
                        }
                    }
                },
                height: 400,
                width: 200
            },
            {
                xtype: 'container',
                margin: '0 10',
                layout: {
                    type: 'vbox',
                    align: 'center',
                    pack: 'center'
                },
                defaults: {
                    margin: '5'
                },
                items: [
                    {
                        xtype: 'container',
                        height: 100
                    },
                    {
                        xtype: 'button',
                        itemId: 'selectAllItems',
                        width: 50,
                        text: '>>',
                        handler: function () {
                            me.selectAllItems();
                        }
                    },
                    {
                        xtype: 'button',
                        itemId: 'selectItems',
                        width: 50,
                        text: '>',
                        handler: function () {
                            me.selectItems();
                        }
                    },
                    {
                        xtype: 'button',
                        itemId: 'deselectItems',
                        width: 50,
                        text: '<',
                        handler: function () {
                            me.deselectItems();

                        }
                    },
                    {
                        xtype: 'button',
                        itemId: 'deselectAllItems',
                        width: 50,
                        text: '<<',
                        handler: function () {
                            me.deselectAllItems();
                        }
                    }
                ]
            },
            {
                xtype: 'gridpanel',
                itemId: 'selectedItemsGrid',
                store: me.selectedItemsStoreName,
                title: me.selectedItemsTitle,
                hideHeaders: true,
                selModel: {
                    mode: "MULTI"
                },
                columns: [
                    {
                        dataIndex: me.displayedColumn,
                        flex: 1
                    }
                ],
                viewConfig: {
                    plugins: {
                        ptype: dragDropPlugin,
                        dragGroup: selectedItems,
                        dropGroup: allItems
                    },
                    listeners: {
                        drop: function (node, data, dropRec, dropPosition) {
                            me.enableSorting && me.getSelectedItemsStore().sort(me.displayedColumn, 'ASC');
                        }
                    }
                },
                height: 400,
                width: 200
            }
        ];

        me.callParent(arguments);
    },

    getAllItemsGrid: function () {
        return this.down('#allItemsGrid');
    },

    getSelectedItemsGrid: function () {
        return this.down('#selectedItemsGrid');
    },

    getAllItemsStore: function () {
        var allItemsGrid = this.getAllItemsGrid();

        if (allItemsGrid) {
            return allItemsGrid.getStore();
        } else {
            return null;
        }
    },

    getSelectedItemsStore: function () {
        var selectedItemsGrid = this.getSelectedItemsGrid();

        if (selectedItemsGrid) {
            return selectedItemsGrid.getStore();
        } else {
            return null;
        }
    },

    selectAllItems: function () {
        var allItemsStore = this.getAllItemsStore(),
            selectedItemsStore = this.getSelectedItemsStore();

        if (allItemsStore && selectedItemsStore) {
            allItemsStore.each(function (record) {
                selectedItemsStore.add(record);
            });
            allItemsStore.removeAll();
        }

        this.enableSorting && selectedItemsStore.sort(this.displayedColumn, 'ASC');
    },

    selectItems: function () {
        var allItemsGrid = this.getAllItemsGrid(),
            selectedRecords = allItemsGrid.getSelectionModel().getSelection(),
            allItemsStore = this.getAllItemsStore(),
            selectedItemsStore = this.getSelectedItemsStore();

        if (allItemsStore && selectedItemsStore) {
            Ext.Array.each(selectedRecords, function (record) {
                allItemsStore.remove(record);
                selectedItemsStore.add(record);
            });
        }

        this.enableSorting && selectedItemsStore.sort(this.displayedColumn, 'ASC');
    },

    deselectItems: function () {
        var selectedItemsGrid = this.getSelectedItemsGrid(),
            selectedRecords = selectedItemsGrid.getSelectionModel().getSelection(),
            allItemsStore = this.getAllItemsStore(),
            selectedItemsStore = this.getSelectedItemsStore();

        if (allItemsStore && selectedItemsStore) {
            Ext.Array.each(selectedRecords, function (record) {
                allItemsStore.add(record);
                selectedItemsStore.remove(record);
            });
        }

        this.enableSorting && allItemsStore.sort(this.displayedColumn, 'ASC');
    },

    deselectAllItems: function () {
        var allItemsStore = this.getAllItemsStore(),
            selectedItemsStore = this.getSelectedItemsStore();

        if (allItemsStore && selectedItemsStore) {
            selectedItemsStore.each(function (record) {
                allItemsStore.add(record);
            });
            selectedItemsStore.removeAll();
        }

        this.enableSorting && allItemsStore.sort(this.displayedColumn, 'ASC');
    }
});

Ext.define('Uni.view.menu.NavigationItem', {
    extend: 'Ext.menu.Item',
    alias: 'widget.navigation-item',
    arrowCls: null,
    renderTpl: [
        '<tpl if="plain">',
        '{text}',
        '<tpl else>',
                '<a id="{id}-itemEl"',
                ' class="' + Ext.baseCSSPrefix + 'menu-item-link{childElCls}"',
                ' href="{href}"',
                '<tpl if="hrefTarget"> target="{hrefTarget}"</tpl>',
                ' hidefocus="true"',
                ' unselectable="on"',
                '<tpl if="tabIndex">',
                    ' tabIndex="{tabIndex}"',
                '</tpl>',
                '>',
                '<div role="img" id="{id}-iconEl" class="' + Ext.baseCSSPrefix + 'menu-item-icon {iconCls}',
                    '{childElCls} {glyphCls}" style="<tpl if="icon">background-image:url({icon});</tpl>',
                    '<tpl if="glyph && glyphFontFamily">font-family:{glyphFontFamily};</tpl>">',
                    '<tpl if="glyph">&#{glyph};</tpl>',
                '</div>',
                '<span class="navigation-item-number">{index}</span>',
                '<span id="{id}-textEl" class="' + Ext.baseCSSPrefix + 'menu-item-text" unselectable="on">{text}</span>',
                '</a>',
        '</tpl>'
    ]
});

Ext.define('Uni.view.menu.NavigationMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.navigation-menu',
    cls: 'x-navigation-menu',

    requires: [
        'Uni.view.menu.NavigationItem'
    ],

    defaults: {
        xtype: 'navigation-item'
    },

    floating: false,
    hidden: false,
    activeStep: 1,
    jumpBack: true,
    jumpForward: false,

    listeners: {
        add: function (menu, item, index) {
            item.renderData.index = item.index = ++index;
            this.updateItemCls(index)
        },
        click: function (menu, item) {
            item.index < menu.activeStep ?
                (menu.jumpBack ? menu.moveTo(item.index) : null) :
                (menu.jumpForward ? menu.moveTo(item.index) : null)
        }
    },

    updateItemCls: function (index) {
        var me = this,
            item = me.items.getAt(index - 1);
        item.removeCls(['step-completed', 'step-active', 'step-non-completed']);
        index < me.activeStep ? item.addCls('step-completed') :
            (index > me.activeStep ? item.addCls('step-non-completed') :
                item.addCls('step-active'));
    },

    moveTo: function (step) {
        var me = this;
        me.moveToStep(step);
        me.fireEvent('movetostep', me.activeStep)
    },

    moveToStep: function (step) {
        var me = this,
            stepCount = me.items.getCount();
        if (1 < step < stepCount) {
            me.activeStep = step;
            me.items.each(function (item) {
                var index = item.index;
                me.updateItemCls(index);
            });
        }
    },

    getActiveStep: function () {
        return this.activeStep;
    },

    moveNextStep: function () {
        this.moveToStep(this.activeStep + 1);
    },

    movePrevStep: function () {
        this.moveToStep(this.activeStep - 1);
    }
});

/**
 * @class Uni.view.navigation.SubMenu
 *
 * Common submenu that supports adding buttons and toggling.
 *
 * Styling will also be applied automatically to anything that is in the component.
 *
 * How and where content is shown after clicking a button in the submenu is free to choose.
 * Switching between panels can easily be done using a card layout.
 * Toggling is done automatically: when the url changes, the button with the current href is selected.
 *
 *
 * # Example usage
 *
 *     @example
 *       side: [
 *          {
 *            xtype: 'navigationSubMenu',
 *            itemId: 'myMenu'
 *          }
 *        ],
 */
Ext.define('Uni.view.navigation.SubMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.navigationSubMenu',
    floating: false,
    ui: 'side-menu',
    plain: true,
    width: 256,

    defaults: {
        xtype: 'menuitem',
        hrefTarget: '_self'
    },

    selectedCls: 'current',

    initComponent: function () {
        var me = this;
        Ext.util.History.addListener('change', function (token) {
            me.checkNavigation(token);
        });
        this.callParent(this);
    },

    toggleMenuItem: function (index) {
        var cls = this.selectedCls;
        var item = this.items.getAt(index);
        if (item.hasCls(cls)) {
            item.removeCls(cls);
        } else {
            item.addCls(cls);
        }
    },

    cleanSelection: function () {
        var cls = this.selectedCls;
        this.items.each(function (item) {
            item.removeCls(cls);
        });
    },

    checkNavigation: function (token) {
        var me = this;
        me.items.each(function (item, index) {
            if ((item.href != null) && (Ext.String.endsWith(item.href, token))) {
                me.cleanSelection();
                me.toggleMenuItem(index);
            }
        });
    }
});

/**
 * @class Uni.view.notifications.NoItemsFoundPanel
 *
 * The no items found panel is primarily meant to be shown as a configuration for
 * {@link Uni.view.container.PreviewContainer#emptyComponent empty components} in a
 * {@link Uni.view.container.PreviewContainer}. It can also be used independently for whichever
 * use-case needs it.
 *
 *     @example
 *     xtype: 'preview-container',
 *     grid: {
 *         xtype: 'my-favorite-grid',
 *     },
 *     emptyComponent: {
 *         xtype: 'no-items-found-panel',
 *         title: 'No favorite items found',
 *         reasons: [
 *             'No favorite items have been defined yet.',
 *             'No favorite items comply to the filter.'
 *         ],
 *         stepItems: [
 *             {
 *                 text: 'Add item',
 *                 action: 'addItem'
 *             }
 *         ]
 *     },
 *     previewComponent: {
 *         xtype: 'my-favorite-preview'
 *     }
 */
Ext.define('Uni.view.notifications.NoItemsFoundPanel', {
    extend: 'Ext.container.Container',
    xtype: 'no-items-found-panel',

    /**
     * @cfg {String}
     *
     * Title to be shown on the panel.
     */
    title: Uni.I18n.translate('notifications.NoItemsFoundPanel.title', 'UNI', 'No items found'),

    /**
     * @cfg {String}
     *
     * Text shown above the reasons.
     */
    reasonsText: Uni.I18n.translate('notifications.NoItemsFoundPanel.reasonsText', 'UNI', 'This could be because:'),

    /**
     * @cfg {String[]/String}
     *
     * An array of reasons formatted as string. A single string value is also
     * supported. If no reasons are given, the reasons section of the panel is
     * not shown to the user.
     *
     *     @example
     *     reasons = [
     *         'No items have been defined yet.',
     *         'No items comply to the filter.'
     *     ]
     */
    reasons: [],

    /**
     * @cfg {String}
     *
     * Text shown above the step components.
     */
    stepsText: Uni.I18n.translate('notifications.NoItemsFoundPanel.stepsText', 'UNI', 'Possible steps:'),

    /**
     * @cfg {Object[]/Object}
     *
     * Configuration objects for the items that need to be added for possible
     * steps to take if there are no items found. By default an item configuration
     * is assumed to be a button, but any component configuration is possible.
     *
     * If no steps can or should be taken, the steps section is not shown.
     *
     *     @example
     *     stepItems = [
     *         {
     *             text: 'Add item',
     *             action: 'addItem'
     *         },
     *         {
     *             text: 'Import item',
     *             action: 'importItem'
     *         }
     *     ]
     */
    stepItems: [],

    layout: {
        type: 'vbox'
    },

    items: [
        {
            xtype: 'panel',
            itemId: 'wrapper',
            cls: Uni.About.baseCssPrefix + 'panel-no-items-found',
            ui: 'medium',
            framed: true,
            layout: {
                type: 'vbox',
                align: 'stretch'
            }
        }
    ],

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
        var wrapper = me.down('#wrapper');

        wrapper.setTitle(me.title);

        if (Ext.isArray(me.reasons) || Ext.isString(me.reasons)) {
            var formattedReasons = me.formatReasons(me.reasons);

            wrapper.add({
                xtype: 'component',
                html: formattedReasons
            });
        }

        if (!Ext.isEmpty(me.stepItems) && Ext.isArray(me.stepItems) || Ext.isObject(me.stepItems)) {
            wrapper.add({
                xtype: 'component',
                html: '<span class="steps-text">' + me.stepsText + '</span>'
            });

            wrapper.add(me.createSteps(me.stepItems));
        }
    },

    formatReasons: function (reasons) {
        var me = this,
            result = '<span class="reasons-text">' + me.reasonsText + '</span>',
            formattedReasons = '';

        if (Ext.isArray(reasons)) {
            Ext.Array.each(reasons, function (reason) {
                formattedReasons += me.formatReason(reason);
            });
        } else if (Ext.isString(reasons)) {
            formattedReasons += me.formatReason(reasons);
        }

        return result + '<ul>' + formattedReasons + '</ul>';
    },

    formatReason: function (reason) {
        return '<li>' + reason + '</li>';
    },

    createSteps: function (stepItems) {
        var container = Ext.create('Ext.container.Container', {
            cls: 'steps',
            layout: {
                type: 'hbox'
            },
            defaults: {
                xtype: 'button',
                hrefTarget: '_self',
                margin: '0 8px 0 0'
            }
        });

        if (Ext.isArray(stepItems)) {
            Ext.Array.each(stepItems, function (stepItem) {
                container.add(Ext.clone(stepItem));
            });
        } else if (Ext.isString(stepItems)) {
            container.add(Ext.clone(stepItems));
        }

        return container;
    }
});

Ext.define('Uni.view.panel.StepPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.step-panel',
    text: 'Some step text',

    indexText: '12',
    index: null,

    isLastItem: null,
    isFirstItem: null,
    isMiddleItem: null,
    isOneItem: null,

    isActiveStep: null,
    isCompletedStep: null,
    isNonCompletedStep: null,

    state: 'noncompleted',

    layout: {
        type: 'vbox',
        align: 'left'
    },

    states: {
        active: ['step-active', 'step-label-active'],
        completed: ['step-completed', 'step-label-completed'],
        noncompleted: ['step-non-completed', 'step-label-non-completed']
    },

    items: [],

    handler: function () {
    },

    getStepDots: function () {
        return {
            layout: {
                type: 'vbox',
                align: 'left'
            },
            cls: 'x-panel-step-dots',
            items: [
                {
                    xtype: 'box',
                    name: 'bottomdots',
                    cls: 'x-image-step-dots'
                }
            ]
        }
    },


    getStepLabel: function () {
        var me = this;
        return {
            name: 'step-label-side',
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            items: [
                {
                    xtype: 'button',
                    name: 'steppanellabel',
                    text: me.text,
                    cls: 'x-label-step',
                    ui: 'step-label-active',
                    handler: me.handler
                }
            ]
        }
    },

    getStepPanelLayout: function () {
        var me = this;
        return {
            name: 'basepanel',
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            items: [
                {
                    name: 'steppanelbutton',
                    xtype: 'step-button',
                    ui: 'step-active',
                    text: me.indexText,
                    handler: me.handler
                },
                me.getStepLabel()
            ]
        }
    },

    doStepLayout: function () {
        var me = this,
            items = null;
        me.isFirstItem && (items = [me.getStepPanelLayout(), me.getStepDots()]);
        me.isLastItem && (items = [me.getStepDots(), me.getStepPanelLayout()]);
        me.isMiddleItem && (items = [me.getStepDots(), me.getStepPanelLayout(), me.getStepDots()]);
        me.isOneItem && (items = [me.getStepPanelLayout()]);
        me.items = items
    },

    afterRender: function (panel) {
        panel.stepButton = this.down('panel[name=basepanel]');
        panel.stepLabel = this.down();
        console.log( this.stepButton, this.stepLabel);
     //   this.setState(this.state);
    },

    setState: function (state) {
        !state && (this.state = state);
        console.log(this, this.stepButton, this.stepLabel);
        this.stepButton.setUI(this.states[this.state][0]);
        this.stepLabel.setUI(this.states[this.state][1]);
    },

    getState: function(){
        return this.state;
    },

    initComponent: function () {
        var me = this;
        me.doStepLayout();
        me.callParent(arguments)
    }
});

/**
 * @class Uni.view.toolbar.PagingBottom
 */
Ext.define('Uni.view.toolbar.PagingBottom', {
    extend: 'Ext.toolbar.Paging',
    xtype: 'pagingtoolbarbottom',
    ui: 'pagingtoolbarbottom',

    defaultButtonUI: 'default',

    requires: [
        'Uni.util.QueryString',
        'Uni.util.History'
    ],

    /**
     * @cfg {Object} Query parameters
     *
     * Query parameters to use when loading the store, e.g. for filtering or sorting.
     */
    params: {},

    /**
     * @cfg {Number} Default page size
     *
     * The default page size to use when initializing the paging component.
     */
    defaultPageSize: 10,

    totalCount: 0,
    totalPages: 0,
    isFullTotalCount: false,
    isSecondPagination: false,

    /**
     * @cfg {String} Limit parameter
     *
     * The limit parameter is used in the URL to define the amount of items that are visible per page.
     */
    pageSizeParam: 'limit',

    /**
     * @cfg {String} Start parameter
     *
     * The start parameter is used in the URL to define the start of the current paging options.
     */
    pageStartParam: 'start',

    /**
     * @cfg {Boolean} Defer load
     *
     * Whether to load the store when the paging gets initialized or not.
     */
    deferLoading: false,

    /**
     * @cfg {Boolean}
     *
     * Whether to update the paging parameters in the URL or not, default 'true'.
     */
    updatePagingParams: true,

    itemsPerPageMsg: Uni.I18n.translate('general.itemsPerPage', 'UNI', 'Items per page'),

    firstText: Uni.I18n.translate('general.firstPage', 'UNI', 'First page'),
    prevText: Uni.I18n.translate('general.previousPage', 'UNI', 'Previous page'),
    nextText: Uni.I18n.translate('general.nextPage', 'UNI', 'Next page'),
    lastText: Uni.I18n.translate('general.lastPage', 'UNI', 'Last page'),

    pageSizeStore: Ext.create('Ext.data.Store', {
        fields: ['value'],
        data: [
            {value: '10'},
            {value: '20'},
            {value: '50'},
            {value: '100'}
        ]
    }),

    pageNavItemTpl: new Ext.XTemplate('<a href="{1}">{0}</a>'),
    currentPageNavItemTpl: new Ext.XTemplate('<span>{0}</span>'),

    initComponent: function () {
        this.callParent(arguments);

        this.initPageSizeAndStartFromQueryString();

        var pagingCombo = this.child('#pagingCombo');
        pagingCombo.setRawValue('' + this.store.pageSize);
    },

    initPageSizeAndStartFromQueryString: function () {
        var queryStrings = Uni.util.QueryString.getQueryStringValues(),
            pageSize = queryStrings[this.pageSizeParam],
            pageStart = queryStrings[this.pageStartParam];

        if (this.isSecondPagination) {
            pageStart = (this.store.currentPage - 1) * this.store.pageSize;
            pageSize = this.store.pageSize;
        } else {
            pageStart = parseInt(pageStart, this.defaultPageSize) || 0;
            pageSize = parseInt(pageSize, this.defaultPageSize) || this.store.pageSize;
        }
        this.initPageSizeAndStart(pageSize, pageStart);
    },

    initPageSizeAndStart: function (pageSize, pageStart) {
        var me = this,
            pageNum = Math.max(Math.ceil((pageStart + 1) / pageSize), 1);

        if (this.store.currentPage !== pageNum) {
            this.store.currentPage = pageNum;
        }

        pageSize = this.adjustPageSize(pageSize);
        if (this.store.pageSize !== pageSize) {
            this.store.pageSize = pageSize;
        }

        this.initExtraParams();

        if (!me.deferLoading) {
            this.store.load({
                params: me.params,
                callback: function (records) {
                    if (records !== null && records.length === 0 && pageNum > 1) {
                        me.initPageSizeAndStart(pageSize, pageStart - pageSize);
                    }
                }
            });
        }
    },

    adjustPageSize: function (pageSize) {
        var match = pageSize,
            minDiff;

        this.pageSizeStore.each(function (record) {
            var value = parseInt(record.data.value, 10),
                diff = Math.abs(pageSize - value);

            if (diff < minDiff || typeof minDiff === 'undefined') {
                minDiff = diff;
                match = value;
            }
        });

        return match;
    },

    onPageSizeChange: function (combobox, value) {
        var me = this,
            pageSize = parseInt(value, 10);

        me.resetPageSize(pageSize);
        me.updateQueryString();
    },

    resetPageSize: function (pageSize) {
        var me = this,
            newPage = Math.max(Math.ceil((me.getPageStartValue() + 1) / pageSize), 1);

        me.store.currentPage = newPage;
        me.store.pageSize = pageSize;
        me.totalPages = 0;

        this.initExtraParams();

        me.store.load({
            params: me.params,
            callback: function (records) {
                if (records !== null && records.length === 0 && newPage > 1) {
                    me.initPageSizeAndStart(pageSize, pageStart - pageSize);
                }
            }
        });
    },

    updateQueryString: function (start) {
        var me = this;

        me.updateHrefIfNecessary(me.buildQueryString(start));
    },

    resetQueryString: function () {
        var me = this;

        var obj = {};
        obj[me.pageSizeParam] = undefined;
        obj[me.pageStartParam] = undefined;

        me.updateHrefIfNecessary(Uni.util.QueryString.buildHrefWithQueryString(obj));
    },

    updateHrefIfNecessary: function (href) {
        if (this.updatePagingParams && location.href !== href) {
            Uni.util.History.suspendEventsForNextCall();
            location.href = href;
        }
    },

    resetPaging: function () {
        var me = this,
            item = me.child('#pageNavItem');

        me.totalCount = 0;
        me.totalPages = 0;
        me.isFullTotalCount = false;
        me.store.currentPage = 1;

        me.initPageNavItems(item, 1, me.totalPages);
        me.resetQueryString();
    },

    buildQueryString: function (start) {
        var me = this;

        if (typeof start === 'undefined') {
            start = me.getPageStartValue();
        }

        var obj = {};
        obj[me.pageSizeParam] = me.store.pageSize;
        obj[me.pageStartParam] = start;

        return Uni.util.QueryString.buildHrefWithQueryString(obj);
    },

    getPageStartValue: function (pageOffset) {
        var me = this,
            pageData = me.getPageData(),
            start = Math.max(pageData.fromRecord - 1, 0);

        pageOffset = pageOffset || 0;
        return start + me.store.pageSize * pageOffset;
    },

    getPagingItems: function () {
        var me = this;

        return [
            {
                xtype: 'tbtext',
                text: me.itemsPerPageMsg
            },
            {
                xtype: 'combobox',
                itemId: 'pagingCombo',
                store: me.pageSizeStore,
                width: 64,
                queryMode: 'local',
                displayField: 'value',
                valueField: 'value',
                enableKeyEvents: true,
                keyNavEnabled: false,
                submitValue: false,
                isFormField: false,
                allowBlank: false,
                forceSelection: true,
                editable: false,
                scope: me,
                listeners: {
                    change: me.onPageSizeChange,
                    scope: me
                }
            },
            {
                xtype: 'component',
                html: '&nbsp;',
                flex: 1
            },
            {
                itemId: 'first',
                ui: 'gridnav',
                tooltip: me.firstText,
                overflowText: me.firstText,
                iconCls: Ext.baseCSSPrefix + 'tbar-page-first',
                disabled: true,
                handler: me.moveFirst,
                scope: me
            },
            {
                itemId: 'prev',
                ui: 'gridnav',
                tooltip: me.prevText,
                overflowText: me.prevText,
                iconCls: Ext.baseCSSPrefix + 'tbar-page-prev',
                disabled: true,
                handler: me.movePrevious,
                scope: me
            },
            {
                xtype: 'container',
                itemId: 'pageNavItem',
                cls: 'pagenav',
                layout: 'hbox'
            },
            {
                itemId: 'next',
                ui: 'gridnav',
                tooltip: me.nextText,
                overflowText: me.nextText,
                iconCls: Ext.baseCSSPrefix + 'tbar-page-next',
                disabled: true,
                handler: me.moveNext,
                scope: me
            },
            {
                itemId: 'last',
                ui: 'gridnav',
                tooltip: me.lastText,
                overflowText: me.lastText,
                iconCls: Ext.baseCSSPrefix + 'tbar-page-last',
                disabled: true,
                handler: me.moveLast,
                scope: me
            }
        ];
    },

    onLoad: function () {
        var me = this,
            pageData,
            currPage,
            pageCount,
            count,
            isEmpty,
            item;

        count = me.store.getCount();
        isEmpty = count === 0;
        if (!isEmpty) {
            pageData = me.getPageData();
            currPage = pageData.currentPage;
            pageCount = pageData.pageCount;
            if (me.isSecondPagination) {
                me.totalCount = me.store.getTotalCount();
            } else {
                me.totalCount = me.totalCount < me.store.getTotalCount() ? me.store.getTotalCount() : me.totalCount;
            }
            me.totalPages = Math.ceil(me.totalCount / me.store.pageSize);
        } else {
            currPage = 0;
            pageCount = 0;
        }

        Ext.suspendLayouts();
        item = me.child('#pageNavItem');
        me.initPageNavItems(item, currPage, me.totalPages);

        me.setChildDisabled('#first', currPage === 1 || isEmpty);
        me.setChildDisabled('#prev', currPage === 1 || isEmpty);
        me.setChildDisabled('#next', currPage === pageCount || isEmpty);

        if (me.isFullTotalCount || (typeof pageData !== 'undefined' && me.store.pageSize * pageData.currentPage >= me.totalCount)) {
            me.setChildDisabled('#last', typeof pageData === 'undefined' || me.totalPages === pageData.currentPage);
            me.isFullTotalCount = true;
        }

        me.updateInfo();
        Ext.resumeLayouts(true);

        me.fireEvent('change', me, pageData);
    },

    moveLast: function () {
        var me = this,
            pageCount = me.getPageData().pageCount,
            last = pageCount < me.totalPages ? me.totalPages : pageCount;

        if (me.fireEvent('beforechange', me, last) !== false) {
            me.initExtraParams();

            me.store.loadPage(last, {
                params: me.params
            });

            me.updateQueryString();
            return true;
        }
        return false;
    },

    moveFirst: function () {
        var me = this;

        if (this.fireEvent('beforechange', this, 1) !== false) {
            me.initExtraParams();

            me.store.loadPage(1, {
                params: me.params
            });

            me.updateQueryString();
            return true;
        }
        return false;
    },

    movePrevious: function () {
        var me = this,
            store = me.store,
            prev = store.currentPage - 1;

        if (prev > 0) {
            if (me.fireEvent('beforechange', me, prev) !== false) {
                me.initExtraParams();

                store.loadPage(store.currentPage - 1, {
                    params: me.params
                });

                me.updateQueryString();
                return true;
            }
        }
        return false;
    },

    moveNext: function () {
        var me = this,
            store = me.store,
            total = me.getPageData().pageCount,
            next = store.currentPage + 1;

        if (next <= total) {
            if (me.fireEvent('beforechange', me, next) !== false) {
                me.initExtraParams();

                store.loadPage(store.currentPage + 1, {
                    params: me.params
                });

                me.updateQueryString();
                return true;
            }
        }
        return false;
    },

    initPageNavItems: function (container, currPage, pageCount) {
        var me = this,
            pagesShowingCount = 10,
            startPage = Math.max(1, currPage - 5),
            endPage = Math.min(startPage + pagesShowingCount - 1, pageCount),
            pageOffset,
            start;

        if (endPage - startPage < pagesShowingCount - 1) {
            startPage = Math.max(1, endPage - pagesShowingCount + 1);
        }

        startPage = startPage < 1 ? 1 : startPage;
        endPage = endPage > pageCount ? pageCount : endPage;

        if (container.rendered) {
            Ext.suspendLayouts();
        }

        container.removeAll();
        for (var i = startPage; i <= endPage; i++) {
            pageOffset = i - currPage;
            start = me.getPageStartValue(pageOffset);
            container.add(me.createPageNavItem(i, start, pageOffset === 0));
        }
        if (container.rendered) {
            Ext.resumeLayouts(true);
        }
    },

    createPageNavItem: function (page, start, isCurrent) {
        var me = this,
            result = me.formatSinglePageNavItem(page, start, isCurrent),
            navItem = Ext.create('Ext.Component', {
                baseCls: Ext.baseCSSPrefix + 'toolbar-text',
                cls: isCurrent ? 'active' : '',
                html: result
            });

        if (!isCurrent) {
            navItem.on('afterrender', function () {
                me.addNavItemClickHandler(me, page, navItem);
            });
        }

        return navItem;
    },

    addNavItemClickHandler: function (me, page, navItem) {
        navItem.getEl().on('click', function () {
            Ext.History.suspendEvents();
            me.initExtraParams();
            me.store.loadPage(page, {
                params: me.params,
                callback: function () {
                    var task = Ext.create('Ext.util.DelayedTask', function () {
                        Ext.History.resumeEvents();
                    });
                    task.delay(250);
                }
            });
        });
    },

    formatSinglePageNavItem: function (page, start, isCurrent) {
        var me = this,
            template = isCurrent ? me.currentPageNavItemTpl : me.pageNavItemTpl,
            href = me.buildQueryString(start);

        return template.apply([page, href]);
    },

    initExtraParams: function () {
        var me = this;
        if (Ext.isArray(me.params)) {
            me.params.forEach(function (entry) {
                var key = Object.keys(entry)[0];
                var value = entry[key];
                me.store.getProxy().setExtraParam(key, value);
            });
        }
    }

});

/**
 * @class Uni.view.toolbar.PagingTop
 *
 *  this.dockedItems = [
 {
     xtype: 'pagingtoolbartop',
     store: this.store,
     dock: 'top',
     items: [
         {
             xtype: 'component',
             flex: 1
         },
         {
             text: 'Create device type',
             itemId: 'createDeviceType',
             xtype: 'button',
             action: 'createDeviceType'
         },
         {
             text: 'Bulk action',
             itemId: 'deviceTypesBulkAction',
             xtype: 'button'
         }
     ]
 },
 {
 xtype: 'pagingtoolbarbottom',
 store: this.store,
 dock: 'bottom'
}];
 */
Ext.define('Uni.view.toolbar.PagingTop', {
    extend: 'Ext.toolbar.Paging',
    xtype: 'pagingtoolbartop',
    ui: 'pagingtoolbartop',

    displayInfo: false,
    usesExactCount: false,

    /**
     * @cfg {String}
     *
     *
     */
    displayMsg: Uni.I18n.translate('general.displayMsgItems', 'UNI', '{0} - {1} of {2} items'),

    /**
     * @cfg {String}
     */
    displayMoreMsg: Uni.I18n.translate('general.displayMsgMoreItems', 'UNI', '{0} - {1} of more than {2} items'),

    /**
     * @cfg {String}
     */
    emptyMsg: Uni.I18n.translate('general.noItemsToDisplay', 'UNI', 'There are no items to display'),

    isFullTotalCount: false,
    totalCount: -1,

    defaultButtonUI: 'default',

    initComponent: function () {
        this.callParent(arguments);
    },

    getPagingItems: function () {
        return [
            {
                xtype: 'tbtext',
                itemId: 'displayItem'
            },
            '->'
        ];
    },

    updateInfo: function () {
        var me = this,
            displayItem = me.child('#displayItem'),
            store = me.store,
            pageData = me.getPageData(),
            totalCount,
            msg;

        if (displayItem) {
            if (me.usesExactCount) {
                me.totalCount = store.getTotalCount();
            } else {
                me.totalCount = me.totalCount < store.getTotalCount() ? store.getTotalCount() : me.totalCount;
            }
            if (store.getCount() === 0) {
                me.totalCount = -1;
                msg = me.emptyMsg;
            } else {
                totalCount = me.totalCount - 1;
                msg = me.displayMoreMsg;
                if (me.isFullTotalCount || store.pageSize * pageData.currentPage >= me.totalCount || me.usesExactCount) {
                    me.isFullTotalCount = true;
                    totalCount = me.totalCount;
                    msg = me.displayMsg;
                }
                msg = Ext.String.format(
                    msg,
                    pageData.fromRecord,
                    pageData.toRecord,
                    totalCount
                );
            }
            displayItem.setText(msg);
        }
    },

    resetPaging: function () {
        var me = this;

        me.onLoad();
        me.totalCount = -1;
        me.isFullTotalCount = false;
    },

    onLoad: function () {
        Ext.suspendLayouts();
        this.updateInfo();
        Ext.resumeLayouts(true);

        this.fireEvent('change', this, this.getPageData());
    }
});

/**
 * @class Uni.view.window.Confirmation
 */
Ext.define('Uni.view.window.Confirmation', {
    extend: 'Ext.window.MessageBox',
    xtype: 'confirmation-window',
    cls: Uni.About.baseCssPrefix + 'confirmation-window',

    /**
     * @cfg {String}
     *
     * Text for the confirmation button. By default 'Remove'.
     */
    confirmText: Uni.I18n.translate('general.remove', 'UNI', 'Remove'),

    /**
     * @cfg {String}
     *
     * Text for the cancellation button. By default 'Cancel'.
     */
    cancelText: Uni.I18n.translate('general.cancel', 'UNI', 'Cancel'),

    /**
     * @cfg {Function}
     *
     * Callback to call if the user chooses to confirm. By default it hides the window.
     */
    confirmation: function () {
        var btn = this.header.child('[type=close]');
        // Give a temporary itemId so it can act like a confirm button.
        btn.itemId = 'confirm';
        this.btnCallback(btn);
        delete btn.itemId;
        this.hide();
    },

    /**
     * @cfg {Function}
     *
     * Callback to call if the user chooses to cancel. By default it closes the window.
     */
    cancellation: function () {
        this.close();
    },

    initComponent: function () {
        var me = this;

        me.buttons = [
            {
                xtype: 'button',
                action: 'confirm',
                name: 'confirm',
                scope: me,
                text: me.confirmText,
                ui: 'remove',
                handler: me.confirmation
            },
            {
                xtype: 'button',
                action: 'cancel',
                name: 'cancel',
                scope: me,
                text: me.cancelText,
                ui: 'link',
                handler: me.cancellation
            }
        ];

        me.callParent(arguments);
    },

    show: function (config) {
        Ext.apply(config, {
            icon: Ext.MessageBox.ERROR
        });

        this.callParent(arguments);
    }
});

/**
 * @class Uni.view.window.Wizard
 */
Ext.define('Uni.view.window.Wizard', {
    extend: 'Ext.window.Window',
    constrain: true,

    requires: [
        'Ext.layout.container.Card',
        'Uni.view.navigation.SubMenu'
    ],

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    minWidth: 400,
    minHeight: 200,

    /**
     * @cfg {Object/Ext.Component}
     *
     * Configuration of the wizard steps. AN array of components is expected.
     */
    steps: null,

    /**
     * @cfg {String}
     *
     * Configuration of the wizard title.
     */
    title: '',

    /**
     * @cfg {String/Ext.Component}
     *
     * Description text or component that goes below the wizard title.
     */
    description: {
        xtype: 'component',
        html: ''
    },

    items: [
        {
            // Title and description
            xtype: 'container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    itemId: 'wizardTitle',
                    html: ''
                },
                {
                    xtype: 'container',
                    itemId: 'wizardDescription',
                    html: ''
                }
            ]
        },
        {
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'container',
                    layout: {
                        type: 'vbox',
                        align: 'stretchmax'
                    },
                    items: [
                        {
                            xtype: 'component',
                            html: '<h3>' + Uni.I18n.translate('window.wizard.menu.title', 'UNI', 'Steps') + '</h3>'
                        },
                        {
                            xtype: 'navigationSubMenu',
                            itemId: 'stepsMenu'
                        }
                    ]
                },
                {
                    xtype: 'container',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    items: [
                        {
                            xtype: 'component',
                            itemId: 'stepsTitle',
                            html: '&nbsp;'
                        },
                        {
                            xtype: 'container',
                            itemId: 'stepsContainer',
                            layout: 'card',
                            flex: 1,
                            items: []
                        }
                    ]
                }

            ]
        }
    ],

    bbar: [
        {
            xtype: 'component',
            flex: 1
        },
        {
            text: Uni.I18n.translate('window.wizard.tools.previous', 'UNI', '&laquo; Previous'),
            action: 'prev',
            scope: this,
            handler: this.prevStep,
            disabled: true
        },
        {
            text: Uni.I18n.translate('window.wizard.tools.next', 'UNI', 'Next &raquo;'),
            action: 'next',
            scope: this,
            handler: this.nextStep,
            disabled: true
        },
        {
            text: Uni.I18n.translate('window.wizard.tools.finish', 'UNI', 'Finish'),
            action: 'finish'
        },
        {
            text: Uni.I18n.translate('window.wizard.tools.cancel', 'UNI', 'Cancel'),
            action: 'cancel'
        }
    ],

    initComponent: function () {
        var steps = this.steps;

        if (steps) {
            if (!(steps instanceof Ext.Component)) {
                // Never modify a passed config object, that could break the expectations of the using code.
                steps = Ext.clone(steps);
            }

            // Needs to be mapped to the non-rendered config object.
            this.items[1].items[1].items[1].items = steps;
        }

        this.callParent(arguments);
        this.addCls(Uni.About.baseCssPrefix + 'window-wizard');

        this.setTitle(this.title);
        this.setDescription(this.description);

        if (steps) {
            this.initStepsMenu(steps);
        }

        this.initNavigation();
    },

    initStepsMenu: function (steps) {
        var me = this,
            stepsMenu = this.getStepsMenuCmp();

        for (var i = 0; i < steps.length; i++) {
            var step = steps[i];

            var stepButton = stepsMenu.add({
                text: step.title,
                pressed: i === 0,
                stepIndex: i
            });

            stepButton.on('click', function () {
                me.goToStep(this.stepIndex);
            });
        }

        me.checkNavigationState();
    },

    initNavigation: function () {
        var me = this,
            prevButton = this.down('button[action=prev]'),
            nextButton = this.down('button[action=next]'),
            cancelButton = this.down('button[action=cancel]');

        prevButton.on('click', me.prevStep, me);
        nextButton.on('click', me.nextStep, me);
        cancelButton.on('click', me.close, me);
    },

    goToStep: function (step) {
        var stepsContainer = this.getStepsContainerCmp();
        stepsContainer.getLayout().setActiveItem(step);
        this.checkNavigationState();
    },

    prevStep: function () {
        var layout = this.getStepsContainerCmp().getLayout(),
            prevCmp = layout.getPrev();

        if (prevCmp) {
            layout.setActiveItem(prevCmp);
        }

        this.checkNavigationState();
    },

    nextStep: function () {
        var layout = this.getStepsContainerCmp().getLayout(),
            nextCmp = layout.getNext();

        if (nextCmp) {
            layout.setActiveItem(nextCmp);
        }

        this.checkNavigationState();
    },

    initStepsTitle: function () {
        var stepsContainer = this.getStepsContainerCmp(),
            stepCmp = stepsContainer.getLayout().getActiveItem(),
            stepsTitle = this.getStepsTitleCmp();

        if (typeof stepCmp !== 'undefined' && stepCmp.hasOwnProperty('title')) {
            stepsTitle.update('<h3>' + stepCmp.title + '</h3>');
        }
    },

    checkNavigationState: function () {
        var menu = this.getStepsMenuCmp(),
            layout = this.getStepsContainerCmp().getLayout(),
            activeItem = layout.getActiveItem(),
            prevCmp = layout.getPrev(),
            prevButton = this.down('button[action=prev]'),
            nextCmp = layout.getNext(),
            nextButton = this.down('button[action=next]');

        for (var i = 0; i < this.getStepsContainerCmp().items.length; i++) {
            var item = this.getStepsContainerCmp().items.items[i];
            if (item.getId() === activeItem.getId()) {
                menu.toggleMenuItem(i);
                break;
            }
        }

        this.initStepsTitle();
        prevButton.setDisabled(!prevCmp);
        nextButton.setDisabled(!nextCmp);
    },

    /**
     * @inheritdoc Ext.panel.Panel#setTitle
     */
    setTitle: function (title) {
        this.callParent(arguments);
        this.getTitleCmp().update('<h2>' + title + '</h2>');
    },

    /**
     * TODO
     *
     * @param {String/Ext.Component} htmlOrCmp
     */
    setDescription: function (htmlOrCmp) {
        this.getDescriptionCmp().removeAll();

        if (!(htmlOrCmp instanceof Ext.Component)) {
            // Never modify a passed config object, that could break the expectations of the using code.
            htmlOrCmp = Ext.clone(htmlOrCmp);
        }

        this.getDescriptionCmp().add(htmlOrCmp);
    },

    getTitleCmp: function () {
        return this.down('#wizardTitle');
    },

    getDescriptionCmp: function () {
        return this.down('#wizardDescription');
    },

    getStepsMenuCmp: function () {
        return this.down('#stepsMenu');
    },

    getStepsTitleCmp: function () {
        return this.down('#stepsTitle');
    },

    getStepsContainerCmp: function () {
        return this.down('#stepsContainer');
    }

});

/**
 * @class Uni.view.window.ReadingTypeWizard
 */
Ext.define('Uni.view.window.ReadingTypeWizard', {
    extend: 'Uni.view.window.Wizard',

    requires: [
        'Ext.form.RadioGroup'
    ],

    width: 800,
    height: 600,

    title: Uni.I18n.translate('window.readingtypewizard.title', 'UNI', 'Select a reading type'),

    description: {
        xtype: 'container',
        layout: 'vbox',
        items: [
            {
                xtype: 'component',
                html: Uni.I18n.translate('window.readingtypewizard.description', 'UNI',
                    'Use the steps below to define a value for the different attributes of a reading type'
                )
            },
            {
                xtype: 'component',
                html: 'TODO'
            }
        ]
    },

    initComponent: function () {
        // Sadly, the Ext.clone function is not enough to create a duplicate of a store.
        var intervalMinuteStore = Ext.create('Ext.data.Store', {
                fields: ['text', 'value']
            }),
            intervalHourStore = Ext.create('Ext.data.Store', {
                fields: ['text', 'value']
            }),
            fixedBlockStore = Ext.create('Ext.data.Store', {
                fields: ['text', 'value']
            }),
            rollingBlockStore = Ext.create('Ext.data.Store', {
                fields: ['text', 'value']
            });

        this.initMeasuringPeriodForm(
            intervalMinuteStore,
            intervalHourStore,
            fixedBlockStore,
            rollingBlockStore
        );

        var commodityElecStore = Ext.create('Ext.data.Store', {
                fields: ['text', 'value']
            }),
            commodityFluidStore = Ext.create('Ext.data.Store', {
                fields: ['text', 'value']
            }),
            commodityGasStore = Ext.create('Ext.data.Store', {
                fields: ['text', 'value']
            }),
            commodityMatterStore = Ext.create('Ext.data.Store', {
                fields: ['text', 'value']
            }),
            commodityOtherStore = Ext.create('Ext.data.Store', {
                fields: ['text', 'value']
            });

        this.initCommodityForm(
            commodityElecStore,
            commodityFluidStore,
            commodityGasStore,
            commodityMatterStore,
            commodityOtherStore
        );

        this.steps = [
            {
                title: Uni.I18n.translate('window.readingtypewizard.introduction', 'UNI', 'Introduction'),
                xtype: 'container',
                layout: {
                    type: 'vbox',
                    align: 'stretchmax'
                },
                items: [
                    {
                        xtype: 'component',
                        html: Uni.I18n.translate('window.readingtypewizard.introduction.content', 'UNI',
                            '<p>A reading type provides a detailed description of a reading value. It is described in ' +
                                'terms of 18 key attributes.</p>' +
                                '<p>Every attribute that has a value of zero is not applicable to the description.</p>' +
                                '<p>Step through this wizard to define a value for each attribute or compound attribute ' +
                                'of the reading type. You can skip steps or jump to a specific step by using the ' +
                                'navigation on the left.</p>'
                        )
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.macroperiod', 'UNI', 'Macro period'),
                xtype: 'container',
                layout: {
                    type: 'anchor'
                },
                items: [
                    {
                        xtype: 'component',
                        html: '<p>' + Uni.I18n.translate('window.readingtypewizard.macroperiod.description', 'UNI',
                            'Reflects how the data is viewed or captured over a long period of time.') + '</p>'
                    },
                    {
                        xtype: 'radiogroup',
                        itemId: 'macroPeriod',
                        columns: 1,
                        anchor: '100%',
                        items: [
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.notapplicable',
                                'UNI', 'Not applicable (0)'), name: 'macroPeriod', inputValue: 0, checked: true},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.macroperiod.daily',
                                'UNI', 'Daily (11)'), name: 'macroPeriod', inputValue: 11},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.macroperiod.weekly',
                                'UNI', 'Weekly (24)'), name: 'macroPeriod', inputValue: 24},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.macroperiod.monthly',
                                'UNI', 'Monthly (13)'), name: 'macroPeriod', inputValue: 13},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.macroperiod.seasonal',
                                'UNI', 'Seasonal (22)'), name: 'macroPeriod', inputValue: 22},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.macroperiod.billingperiod',
                                'UNI', 'Billing period (8)'), name: 'macroPeriod', inputValue: 8},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.macroperiod.specifiedperiod',
                                'UNI', 'Specified period (32)'), name: 'macroPeriod', inputValue: 32}
                        ]
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.dataaggregation', 'UNI', 'Data aggregation'),
                xtype: 'container',
                layout: {
                    type: 'anchor'
                },
                items: [
                    {
                        xtype: 'component',
                        html: '<p>' + Uni.I18n.translate('window.readingtypewizard.dataaggregation.description',
                            'UNI', 'DMay be used to define a mathematical operation carried out over the ' +
                                'time period (#1).') + '</p>'
                    },
                    {
                        xtype: 'radiogroup',
                        itemId: 'dataAggregation',
                        columns: 1,
                        anchor: '100%',
                        items: [
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.notapplicable',
                                'UNI', 'Not applicable (0)'), name: 'dataAggregation', inputValue: 0, checked: true},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.normal',
                                'UNI', 'Normal (12)'), name: 'dataAggregation', inputValue: 12},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.nominal',
                                'UNI', 'Nominal (11)'), name: 'dataAggregation', inputValue: 11},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.average',
                                'UNI', 'Average (2)'), name: 'dataAggregation', inputValue: 2},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.sum',
                                'UNI', 'Sum (26)'), name: 'dataAggregation', inputValue: 26},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.excess',
                                'UNI', 'Excess (4)'), name: 'dataAggregation', inputValue: 4},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.lowthreshold',
                                'UNI', 'Low threshold (7)'), name: 'dataAggregation', inputValue: 7},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.highthreshold',
                                'UNI', 'High threshold (5)'), name: 'dataAggregation', inputValue: 5},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.low',
                                'UNI', 'Low (28)'), name: 'dataAggregation', inputValue: 28},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.minimum',
                                'UNI', 'Minimum (28)'), name: 'dataAggregation', inputValue: 9},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.secondminimum',
                                'UNI', 'Second minimum (17)'), name: 'dataAggregation', inputValue: 17},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.maximum',
                                'UNI', 'Maximum (16)'), name: 'dataAggregation', inputValue: 8},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.secondmaximum',
                                'UNI', 'Second maximum (16)'), name: 'dataAggregation', inputValue: 16},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.thirdmaximum',
                                'UNI', 'Third maximum (23)'), name: 'dataAggregation', inputValue: 23},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.fourthmaximum',
                                'UNI', 'Fourth maximum (24)'), name: 'dataAggregation', inputValue: 24},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.fifthmaximum',
                                'UNI', 'Fifth maximum (25)'), name: 'dataAggregation', inputValue: 25},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.high',
                                'UNI', 'High (27)'), name: 'dataAggregation', inputValue: 27}
                        ]
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.measuringperiod', 'UNI', 'Measuring period'),
                xtype: 'container',
                layout: {
                    type: 'anchor'
                },
                items: [
                    {
                        xtype: 'component',
                        html: '<p>' + Uni.I18n.translate('window.readingtypewizard.measuringperiod.description', 'UNI',
                            'Describes the way the value was originally measured. This doesn\'t represent the ' +
                                'frequency at which it is reported or presented.') + '</p>'
                    },
                    {
                        xtype: 'radiogroup',
                        itemId: 'measuringPeriod',
                        columns: 1,
                        anchor: '100%',
                        items: [
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.notapplicable',
                                'UNI', 'Not applicable (0)'), name: 'measuringPeriod', inputValue: 0, checked: true},

                            {
                                xtype: 'component',
                                html: '<p>' + Uni.I18n.translate('window.readingtypewizard.measuringperiod.interval',
                                    'UNI', 'Interval') + '</p>'
                            },
                            {
                                xtype: 'container',
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'radiofield',
                                        name: 'measuringPeriod',
                                        inputValue: 'intervalMinute'
                                    },
                                    {
                                        xtype: 'combobox',
                                        itemId: 'intervalMinute',
                                        displayField: 'text',
                                        valueField: 'value',
                                        queryMode: 'local',
                                        store: intervalMinuteStore
                                    }
                                ]
                            },
                            {
                                xtype: 'container',
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'radiofield',
                                        name: 'measuringPeriod',
                                        inputValue: 'intervalHour'
                                    },
                                    {
                                        xtype: 'combobox',
                                        itemId: 'intervalHour',
                                        displayField: 'text',
                                        valueField: 'value',
                                        queryMode: 'local',
                                        store: intervalHourStore
                                    }
                                ]
                            },
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaggregation.specifiedinterval',
                                'UNI', 'Specified interval (100)'), name: 'measuringPeriod', inputValue: 100},

                            {
                                xtype: 'component',
                                html: '<p>' + Uni.I18n.translate('window.readingtypewizard.measuringperiod.fixedblock',
                                    'UNI', 'Fixed block') + '</p>'
                            },
                            {
                                xtype: 'container',
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'radiofield',
                                        name: 'measuringPeriod',
                                        inputValue: 'fixedBlock'
                                    },
                                    {
                                        xtype: 'combobox',
                                        itemId: 'fixedBlock',
                                        displayField: 'text',
                                        valueField: 'value',
                                        queryMode: 'local',
                                        store: fixedBlockStore
                                    }
                                ]
                            },
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.measuringperiod.specifiedfixedblock',
                                'UNI', 'Specified fixed block (101)'), name: 'measuringPeriod', inputValue: 101},

                            {
                                xtype: 'component',
                                html: '<p>' + Uni.I18n.translate('window.readingtypewizard.measuringperiod.fixedblock',
                                    'UNI', 'Fixed block') + '</p>'
                            },
                            {
                                xtype: 'container',
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'radiofield',
                                        name: 'measuringPeriod',
                                        inputValue: 'rollingBlock'
                                    },
                                    {
                                        xtype: 'combobox',
                                        itemId: 'rollingBlock',
                                        displayField: 'text',
                                        valueField: 'value',
                                        queryMode: 'local',
                                        store: rollingBlockStore
                                    }
                                ]
                            },
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.measuringperiod.specifiedrollingblock',
                                'UNI', 'Specified rolling block (102)'), name: 'measuringPeriod', inputValue: 102}
                        ]
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.dataaccumulation', 'UNI', 'Data accumulation'),
                xtype: 'container',
                layout: {
                    type: 'anchor'
                },
                items: [
                    {
                        xtype: 'component',
                        html: '<p>' + Uni.I18n.translate('window.readingtypewizard.dataaccumulation.description', 'UNI',
                            'Indicates how the value is represented to accumulate over time.') + '</p>'
                    },
                    {
                        xtype: 'radiogroup',
                        itemId: 'dataAccumulation',
                        columns: 1,
                        anchor: '100%',
                        items: [
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.notapplicable',
                                'UNI', 'Not applicable (0)'), name: 'dataAccumulation', inputValue: 0, checked: true},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.bulkquantity',
                                'UNI', 'Bulk quantity (1)'), name: 'dataAccumulation', inputValue: 1},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.deltadata',
                                'UNI', 'Delta data (4)'), name: 'dataAccumulation', inputValue: 4},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.cumulative',
                                'UNI', 'Cumulative (3)'), name: 'dataAccumulation', inputValue: 3},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.continiouscumulative',
                                'UNI', 'Continious cumulative (2)'), name: 'dataAccumulation', inputValue: 2},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.indicating',
                                'UNI', 'Indicating (6)'), name: 'dataAccumulation', inputValue: 6},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.summation',
                                'UNI', 'Summation (9)'), name: 'dataAccumulation', inputValue: 9},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.timedelay',
                                'UNI', 'Time delay (10)'), name: 'dataAccumulation', inputValue: 10},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.instantaneous',
                                'UNI', 'Instantaneous (12)'), name: 'dataAccumulation', inputValue: 12},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.latchingquantity',
                                'UNI', 'Latching quantity (13)'), name: 'dataAccumulation', inputValue: 13},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.dataaccumulation.boundedquantity',
                                'UNI', 'Bounded quantity (14)'), name: 'dataAccumulation', inputValue: 14}
                        ]
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.flowdirection', 'UNI', 'Flow direction'),
                xtype: 'container',
                layout: {
                    type: 'anchor'
                },
                items: [
                    {
                        xtype: 'component',
                        html: '<p>' + Uni.I18n.translate('window.readingtypewizard.flowdirection.description', 'UNI',
                            'Indicates how the value is represented to accumulate over time.') + '</p>'
                    },
                    {
                        xtype: 'radiogroup',
                        itemId: 'flowDirection',
                        columns: 1,
                        anchor: '100%',
                        items: [
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.notapplicable',
                                'UNI', 'Not applicable (0)'), name: 'flowDirection', inputValue: 0, checked: true},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.forward',
                                'UNI', 'Forward (1)'), name: 'flowDirection', inputValue: 1},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.reverse',
                                'UNI', 'Reverse (19)'), name: 'flowDirection', inputValue: 19},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.lagging',
                                'UNI', 'Lagging (2)'), name: 'flowDirection', inputValue: 2},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.leading',
                                'UNI', 'Leading (3)'), name: 'flowDirection', inputValue: 3},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.net',
                                'UNI', 'Net (4)'), name: 'flowDirection', inputValue: 4},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.total',
                                'UNI', 'Total (20)'), name: 'flowDirection', inputValue: 20},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.totalbyphase',
                                'UNI', 'Total by phase (21)'), name: 'flowDirection', inputValue: 21},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrant1',
                                'UNI', 'Quadrant 1 (15)'), name: 'flowDirection', inputValue: 15},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrants1and2',
                                'UNI', 'Quadrants 1 and 2 (5)'), name: 'flowDirection', inputValue: 5},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrants1and3',
                                'UNI', 'Quadrants 1 and 3 (7)'), name: 'flowDirection', inputValue: 7},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrants1and4',
                                'UNI', 'Quadrants 1 and 4 (8)'), name: 'flowDirection', inputValue: 8},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrants1minus4',
                                'UNI', 'Quadrants 1 minus 4 (9)'), name: 'flowDirection', inputValue: 9},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrant2',
                                'UNI', 'Quadrant 2 (16)'), name: 'flowDirection', inputValue: 16},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrants2and3',
                                'UNI', 'Quadrant 2 and 3 (10)'), name: 'flowDirection', inputValue: 10},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrants2and4',
                                'UNI', 'Quadrant 2 and 4 (11)'), name: 'flowDirection', inputValue: 11},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrants2minus3',
                                'UNI', 'Quadrant 2 minus 3 (12)'), name: 'flowDirection', inputValue: 12},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrant3',
                                'UNI', 'Quadrant 3 (17)'), name: 'flowDirection', inputValue: 17},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrants3and4',
                                'UNI', 'Quadrants 3 and 4 (13)'), name: 'flowDirection', inputValue: 13},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrants3minus2',
                                'UNI', 'Quadrants 3 minus 2 (14)'), name: 'flowDirection', inputValue: 14},
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.flowdirection.quadrant4',
                                'UNI', 'Bounded quantity (18)'), name: 'flowDirection', inputValue: 18}
                        ]
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.commodity', 'UNI', 'Commodity'),
                xtype: 'container',
                layout: {
                    type: 'anchor'
                },
                items: [
                    {
                        xtype: 'component',
                        html: '<p>' + Uni.I18n.translate('window.readingtypewizard.commodity.description',
                            'UNI', 'Some description.') + '</p>'
                    },
                    {
                        xtype: 'radiogroup',
                        itemId: 'commodity',
                        columns: 1,
                        anchor: '100%',
                        items: [
                            {boxLabel: Uni.I18n.translate('window.readingtypewizard.notapplicable',
                                'UNI', 'Not applicable (0)'), name: 'commodity', inputValue: 0, checked: true}
                        ]
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.measurementkind', 'UNI', 'Measurement kind'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Measurement kind</h3>'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.interharmonics', 'UNI', 'Interharmonics'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Interharmonics</h3>'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.argument', 'UNI', 'Argument'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Argument</h3>'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.timeofuse', 'UNI', 'Time of use'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Time of use</h3>'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.criticalpeakperiod', 'UNI', 'Critical peak period'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Critical peak period</h3>'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.consumptiontier', 'UNI', 'Consumption tier'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Consumption tier</h3>'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.phase', 'UNI', 'Phase'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Phase</h3>'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.multiplier', 'UNI', 'Multiplier'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Multiplier</h3>'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.unitofmeasure', 'UNI', 'Unit of measure'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Unit of measure</h3>'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('window.readingtypewizard.currency', 'UNI', 'Currency'),
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'component',
                        html: '<h3>Currency</h3>'
                    }
                ]
            }
        ];

        this.callParent(arguments);
    },

    initMeasuringPeriodForm: function (intervalMinuteStore, intervalHourStore, fixedBlockStore, rollingBlockStore) {
        this.populateSimpleTypeStore(intervalMinuteStore, 'window.readingtypewizard.minute', '{0} minutes', [
            [1, 3], // value, enumeration
            [2, 10],
            [3, 14],
            [5, 6],
            [10, 1],
            [12, 78],
            [15, 2],
            [20, 31],
            [30, 5],
            [60, 7]
        ]);

        this.populateSimpleTypeStore(intervalHourStore, 'window.readingtypewizard.hour', '{0} hours', [
            [2, 79], // value, enumeration
            [3, 83],
            [4, 80],
            [6, 81],
            [12, 82],
            [24, 4]
        ]);

        this.populateSimpleTypeStore(fixedBlockStore, 'window.readingtypewizard.minutefixed', '{0} minutes fixed block', [
            [1, 56], // value, enumeration
            [5, 55],
            [10, 54],
            [15, 53],
            [20, 52],
            [30, 51],
            [60, 50]
        ]);

        this.populateRollingBlockStore(rollingBlockStore, 'window.readingtypewizard.minuterolling', '{0} minutes rolling block with {1} minute subintervals', [
            [
                [60, 30], // values
                57 // enumeration
            ],
            [
                [60, 20],
                58
            ],
            [
                [60, 15],
                59
            ],
            [
                [60, 12],
                60
            ],
            [
                [60, 10],
                61
            ],
            [
                [60, 6],
                62
            ],
            [
                [60, 5],
                63
            ],
            [
                [60, 4],
                64
            ],
            [
                [30, 15],
                65
            ],
            [
                [30, 10],
                66
            ],
            [
                [30, 6],
                67
            ],
            [
                [30, 5],
                68
            ],
            [
                [30, 3],
                69
            ],
            [
                [30, 2],
                70
            ],
            [
                [15, 5],
                71
            ],
            [
                [15, 3],
                72
            ],
            [
                [15, 1],
                73
            ]
        ]);
    },

    initCommodityForm: function (commodityElecStore, commodityFluidStore, commodityGasStore, commodityMatterStore, commodityOtherStore) {
        var baseKey = 'window.readingtypewizard.commodity.';

        this.populateKeyValueStore(commodityElecStore, [
            [baseKey + 'electricityprimarymetered', 'fallback', 1]
        ]);

        this.populateKeyValueStore(commodityFluidStore, [
            ['key', 'fallback', 1]
        ]);

        this.populateKeyValueStore(commodityGasStore, [
            ['key', 'fallback', 1]
        ]);

        this.populateKeyValueStore(commodityMatterStore, [
            ['key', 'fallback', 1]
        ]);

        this.populateKeyValueStore(commodityOtherStore, [
            ['key', 'fallback', 1]
        ]);
    },

    populateSimpleTypeStore: function (store, key, fallback, data) {
        for (var i = 0; i < data.length; i++) {
            var obj = data[i],
                value = obj[0],
                enumeration = obj[1];

            var addition = {
                text: Uni.I18n.translatePlural(key, value, 'UNI', fallback) + ' (' + enumeration + ')',
                value: enumeration
            };

            store.add(addition);
        }
    },

    populateRollingBlockStore: function (store, key, fallback, data) {
        for (var i = 0; i < data.length; i++) {
            var obj = data[i],
                values = obj[0],
                enumeration = obj[1];

            var addition = {
                text: Uni.I18n.translate(key, 'UNI', fallback, values) + ' (' + enumeration + ')',
                value: enumeration
            };

            store.add(addition);
        }
    },

    populateKeyValueStore: function (store, data) {
        for (var i = 0; i < data.length; i++) {
            var obj = data[i],
                key = obj[0],
                fallback = obj[1],
                enumeration = obj[2];

            var addition = {
                text: Uni.I18n.translate(key, 'UNI', fallback) + ' (' + enumeration + ')',
                value: enumeration
            };

            store.add(addition);
        }
    }

});

/**
 * Copyright (C) 2013 Eoko
 *
 * This file is part of Opence.
 *
 * Opence is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Opence is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Opence. If not, see <http://www.gnu.org/licenses/gpl.txt>.
 *
 * @copyright Copyright (C) 2013 Eoko
 * @licence http://www.gnu.org/licenses/gpl.txt GPLv3
 * @author Éric Ortega <eric@eoko.fr>
 */

/**
 * Key navigation for {@link Ext.ux.Rixo.form.field.GridPicker}.
 *
 * @since 2013-06-20 16:07
 */
Ext.define('Ext.ux.Rixo.form.field.GridPickerKeyNav', {
	extend: 'Ext.util.KeyNav'

	,constructor: function(config) {
		this.pickerField = config.pickerField;
		this.grid = config.grid;
		this.callParent([config.target, Ext.apply({}, config, this.defaultHandlers)]);
	}

	,defaultHandlers: {
		up: function() {
			this.goUp(1);
		}

		,down: function() {
			this.goDown(1);
		}

		,pageUp: function() {
			this.goUp(10);
		}

		,pageDown: function() {
			this.goDown(10);
		}

		,home: function() {
			this.highlightAt(0);
		}

		,end: function() {
			var count = this.getGrid().getStore().getCount();
			if (count > 0) {
				this.highlightAt(count - 1);
			}
		}

		,tab: function(e) {
			var pickerField = this.getPickerField();
			if (pickerField.selectOnTab) {
				this.selectHighlighted(e);
				pickerField.triggerBlur();
			}
			// Tab key event is allowed to propagate to field
			return true;
		}

		,enter: function(e) {
			this.selectHighlighted(e);
		}
	}

	,goUp: function(n) {
		var grid = this.getGrid(),
			store = grid.getStore(),
			sm = grid.getSelectionModel(),
			lastSelected = sm.lastSelected,
			count = store.getCount(),
			nextIndex = count - n;

		if (count > 0) {
			if (lastSelected) {
				nextIndex = store.indexOf(lastSelected) - n;
				if (nextIndex < 0) {
					nextIndex = count - 1;
				}
			}

			this.highlightAt(nextIndex);
		}
	}

	,goDown: function(n) {
		var grid = this.getGrid(),
			store = grid.getStore(),
			sm = grid.getSelectionModel(),
			lastSelected = sm.lastSelected,
			count = store.getCount(),
			nextIndex = 0;

		if (count > 0) {
			if (lastSelected) {
				nextIndex = store.indexOf(lastSelected) + n;
				if (nextIndex >= count) {
					nextIndex = 0;
				}
			}

			this.highlightAt(nextIndex);
		}
	}

	,getPickerField: function() {
		return this.pickerField;
	}

	,getGrid: function() {
		return this.grid;
	}

	,highlightAt: function(index) {
		this.getPickerField().highlightAt(index);
	}

	,selectHighlighted: function(e) {
		var selection = this.getGrid().getSelectionModel().getSelection(),
			selected = selection && selection[0],
			pickerField = this.pickerField;
		if (selected) {
			pickerField.setValue(selected.get(pickerField.valueField))
		}
	}
});

/**
 * @since 2013-06-19 21:55
 * @author Éric Ortega <eric@planysphere.fr>
 */
Ext.define('Ext.ux.Rixo.form.field.GridPicker', {
	extend: 'Ext.form.field.ComboBox'

	,alias: 'widget.gridpicker'

	,requires: [
		'Ext.grid.Panel'
		,'Ext.ux.Rixo.form.field.GridPickerKeyNav'
	]

	,defaultGridConfig: {
		xclass: 'Ext.grid.Panel'

		,floating: true
		,focusOnToFront: false
		,resizable: true

		,hideHeaders: true
		,stripeRows: false

//		,viewConfig: {
//			stripeRows: false
//		}
		,rowLines: false

		,initComponent: function() {
			Ext.grid.Panel.prototype.initComponent.apply(this, arguments);

			var store = this.getStore();

			this.query('pagingtoolbar').forEach(function(pagingToolbar) {
				pagingToolbar.bindStore(store);
			});
		}
	}

	/**
	 * Configuration object for the picker grid. It will be merged with {@link #defaultGridConfig}
	 * before creating the grid with {@link #createGrid}.
	 *
	 * @cfg {Object}
	 */
	,gridConfig: null

//	/**
//	 * @cfg {Boolean}
//	 */
//	,multiSelect: false

	/**
	 * Overriden: delegates to {@link #createGrid}.
	 *
	 * @protected
	 */
	,createPicker: function() {
		// We must assign it for Combo's onAdded method to work
		return this.picker = this.createGrid();
	}

	/**
	 * Creates the picker's grid.
	 *
	 * @protected
	 */
	,createGrid: function() {
		var grid = Ext.create(this.getGridConfig());
		this.bindGrid(grid);
		return grid;
	}

	/**
	 * @return {Ext.grid.Panel}
	 */
	,getGrid: function() {
		return this.getPicker();
	}

	/**
	 * Gets the configuration for the picked's grid.
	 *
	 * The returned object will be modified, so it must be an instance dedicated to
	 * this object.
	 *
	 * @return {Object}
	 * @protected
	 */
	,getGridConfig: function() {
		var config = {};
		
		Ext.apply(config, this.gridConfig, this.defaultGridConfig);

		Ext.applyIf(config, {
			store: this.store

			,columns: [{
				dataIndex: this.displayField || this.valueField
				,flex: 1
			}]
		});

		// Avoid "Layout run failed" error
		// See: http://stackoverflow.com/a/21740832/1387519
		if (!config.width) {
			config.width = this.inputEl.getWidth();
		}

		return config;
	}

	/**
	 * Binds the specified grid to this picker.
	 *
	 * @param {Ext.grid.Panel}
	 * @private
	 */
	,bindGrid: function(grid) {

		this.grid = grid;

		grid.ownerCt = this;
		grid.registerWithOwnerCt();

		this.mon(grid, {
			scope: this

			,itemclick: this.onItemClick
			,refresh: this.onListRefresh

			,beforeselect: this.onBeforeSelect
			,beforedeselect: this.onBeforeDeselect
			,selectionchange: this.onListSelectionChange

			// fix the fucking buffered view!!!
			,afterlayout: function(grid) {
				if (grid.getStore().getCount()) {
					if (!grid.fixingTheFuckingLayout) {
						var el = grid.getView().el;
						grid.fixingTheFuckingLayout = true
						el.setHeight('100%');
						el.setStyle('overflow-x', 'hidden');
						grid.fixingTheFuckingLayout = false;
					}
				}
			}

		});

		// Prevent deselectAll, that is called liberally in combo box code, to actually deselect
		// the current value
		var me = this,
			sm = grid.getSelectionModel(),
			uber = sm.deselectAll;
		sm.deselectAll = function() {
			if (!me.ignoreSelection) {
				uber.apply(this, arguments);
			}
		};
	}

	/**
	 * Highlight (i.e. select) the specified record.
	 *
	 * @param {Ext.data.Record}
	 * @private
	 */
	,highlightRecord: function(record) {
		var grid = this.getGrid(),
			sm = grid.getSelectionModel(),
			view = grid.getView(),
			node = view.getNode(record),
			plugins = grid.plugins,
			bufferedPlugin = plugins && plugins.filter(function(p) {
				return p instanceof Ext.grid.plugin.BufferedRenderer
			})[0];

		sm.select(record, false, true);

		if (node) {
			Ext.fly(node).scrollIntoView(view.el, false);
		} else if (bufferedPlugin) {
			bufferedPlugin.scrollTo(grid.store.indexOf(record));
		}
	}

	/**
	 * Highlight the record at the specified index.
	 *
	 * @param {Integer} index
	 * @private
	 */
	,highlightAt: function(index) {
		var grid = this.getGrid(),
			sm = grid.getSelectionModel(),
			view = grid.getView(),
			node = view.getNode(index),
			plugins = grid.plugins,
			bufferedPlugin = plugins && plugins.filter(function(p) {
				return p instanceof Ext.grid.plugin.BufferedRenderer
			})[0];

		sm.select(index, false, true);

		if (node) {
			Ext.fly(node).scrollIntoView(view.el, false);
		} else if (bufferedPlugin) {
			bufferedPlugin.scrollTo(index);
		}
	}

	// private
	,onExpand: function() {
		var me = this,
			keyNav = me.listKeyNav,
			selectOnTab = me.selectOnTab;

		// Handle BoundList navigation from the input field. Insert a tab listener specially to enable selectOnTab.
		if (keyNav) {
			keyNav.enable();
		} else {
			keyNav = me.listKeyNav = Ext.create('Ext.ux.Rixo.form.field.GridPickerKeyNav', {
				target: this.inputEl
				,forceKeyDown: true
				,pickerField: this
				,grid: this.getGrid()
			});
		}

		// While list is expanded, stop tab monitoring from Ext.form.field.Trigger so it doesn't short-circuit selectOnTab
		if (selectOnTab) {
			me.ignoreMonitorTab = true;
		}

		Ext.defer(keyNav.enable, 1, keyNav); //wait a bit so it doesn't react to the down arrow opening the picker

		this.focusWithoutSelection(10);
	}

	// private
	,focusWithoutSelection: function(delay) {
		function focus() {
			var me = this,
				previous = me.selectOnFocus;
			me.selectOnFocus = false;
			me.inputEl.focus();
			me.selectOnFocus = previous;
		}

		return function(delay) {
			if (Ext.isNumber(delay)) {
//				Ext.defer(focus, delay, me.inputEl);
				Ext.defer(focus, delay, this);
			} else {
				focus.call(this);
			}
		};
	}()

	// private
	,doAutoSelect: function() {
		var me = this,
			picker = me.picker,
			lastSelected, itemNode;
		if (picker && me.autoSelect && me.store.getCount() > 0) {
			// Highlight the last selected item and scroll it into view
			lastSelected = picker.getSelectionModel().lastSelected;
			if (lastSelected) {
				picker.getSelectionModel().select(lastSelected, false, true);
			}
		}
	}

	// private
	,onTypeAhead: function() {
		var me = this,
			displayField = me.displayField,
			record = me.store.findRecord(displayField, me.getRawValue()),
			grid = me.getPicker(),
			newValue, len, selStart;

		if (record) {
			newValue = record.get(displayField);
			len = newValue.length;
			selStart = me.getRawValue().length;

			//grid.highlightItem(grid.getNode(record));
			this.highlightRecord(record);

			if (selStart !== 0 && selStart !== len) {
				me.setRawValue(newValue);
				me.selectText(selStart, newValue.length);
			}
		}
	}
}, function() {

	// Specific to Ext 4.2.0
    /**
     * Jupiter specific: We use a higher version, but the last registered package version is used to determine
     * what Ext.getVersion() returns. So we get our package versions instead. Commenting the below out cause of that.
     */
//	if (Ext.getVersion().isLessThan('4.2.1')) {
//		Ext.require('Ext.ux.Rixo.form.field.GridPicker-4-2-0');
//	}

	// Polyfill for forEach
	// source: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/forEach
	if (!Array.prototype.forEach) {
		Array.prototype.forEach = function(fun /*, thisArg */) {
			"use strict";

			if (this === void 0 || this === null)
				throw new TypeError();

			var t = Object(this);
			var len = t.length >>> 0;
			if (typeof fun !== "function")
				throw new TypeError();

			var thisArg = arguments.length >= 2 ? arguments[1] : void 0;
			for (var i = 0; i < len; i++) {
				if (i in t)
					fun.call(thisArg, t[i], i, t);
			}
		};
	}
});

/**
 * GridPicker implementation part that is specific to Ext 4.2.0.
 *
 * @author Éric Ortega <eric@planysphere.fr>
 * @since 2014-02-12 21:30
 */
Ext.define('Ext.ux.Rixo.form.field.GridPicker-4-2-0', {
	override: 'Ext.ux.Rixo.form.field.GridPicker'

	/**
	 * Gets the option to load the store with the specified query.
	 *
	 * @param {String} queryString
	 * @return {String}
	 * @protected
	 */
	,getLoadOptions: function(queryString) {
		var filter = this.queryFilter;
		if (filter) {
			filter.disabled = false;
			filter.setValue(this.enableRegEx ? new RegExp(queryString) : queryString);
			return {
				filters: [filter]
			};
		}
	}

	/**
	 * @inheritdoc
	 * Overridden in order to implement {@link #getLoadOptions}.
	 */
	,loadPage: function(pageNum){
		this.store.loadPage(pageNum, this.getLoadOptions());
	}

	/**
	 * @inheritdoc
	 * Overridden in order to implement {@link #getLoadOptions}.
	 */
	,doQuery: function(queryString, forceAll, rawQuery) {
		queryString = queryString || '';

		// store in object and pass by reference in 'beforequery'
		// so that client code can modify values.
		var me = this,
			qe = {
				query: queryString,
				forceAll: forceAll,
				combo: me,
				cancel: false
			},
			store = me.store,
			isLocalMode = me.queryMode === 'local';

		if (me.fireEvent('beforequery', qe) === false || qe.cancel) {
			return false;
		}

		// get back out possibly modified values
		queryString = qe.query;
		forceAll = qe.forceAll;

		// query permitted to run
		if (forceAll || (queryString.length >= me.minChars)) {
			// expand before starting query so LoadMask can position itself correctly
			me.expand();

			// make sure they aren't querying the same thing
			if (!me.queryCaching || me.lastQuery !== queryString) {
				me.lastQuery = queryString;

				if (isLocalMode) {
					if (me.queryFilter) {
						// Querying by a typed string...
						if (queryString || !forceAll) {
	
							// Ensure queryFilter is enabled and set the new value
							me.queryFilter.disabled = false;
							me.queryFilter.setValue(me.enableRegEx ? new RegExp(queryString) : queryString);
						}
	
						// Disable query value filter if no query string or forceAll passed
						else {
							me.queryFilter.disabled = true;
						}
	
						// Filter the Store according to the updated filter
						store.filter();
					}
				} else {
					// Set flag for onLoad handling to know how the Store was loaded
					me.rawQuery = rawQuery;

					// In queryMode: 'remote', we assume Store filters are added by the developer as remote filters,
					// and these are automatically passed as params with every load call, so we do *not* call clearFilter.
					if (me.pageSize) {
						// if we're paging, we've changed the query so start at page 1.
						me.loadPage(1);
					} else {
						store.load(this.getLoadOptions(queryString));
					}
				}
			}

			// Clear current selection if it does not match the current value in the field
			if (me.getRawValue() !== me.getDisplayValue()) {
				me.ignoreSelection++;
				me.picker.getSelectionModel().deselectAll();
				me.ignoreSelection--;
			}

			if (isLocalMode) {
				me.doAutoSelect();
			}
			if (me.typeAhead) {
				me.doTypeAhead();
			}
		}
		return true;
	}
});

