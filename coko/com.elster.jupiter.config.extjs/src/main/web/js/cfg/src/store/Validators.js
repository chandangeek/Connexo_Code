Ext.define('Cfg.store.Validators', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.Validator',
    storeId: 'validators',
    autoLoad: false,

    //data : {
    //    validators: [
    //        {
    //            implementation: "com.elster.jupiter.validators.impl.MissingValuesValidator",
    //            displayName: "Check missing values",
    //            properties: []
    //        },
    //        {
    //            implementation: "com.elster.jupiter.validators.impl.IntervalStateValidator",
    //            displayName: "Reading quality",
    //            properties: [
    //                {
    //                    key: "readingQualities",
    //                    name: "Reading quality",
    //                    propertyTypeInfo: {
    //                        simplePropertyType: "LISTREADINGQUALITY",
    //                        predefinedPropertyValuesInfo: {
    //                            possibleValues: [
    //                                {
    //                                    cimCode: '0.0.0',
    //                                    systemName: 'Not Applicable',
    //                                    categoryName: 'Valid',
    //                                    indexName: 'Data Valid'
    //                                },
    //                                {
    //                                    cimCode: '1.1.0',
    //                                    systemName: 'End device',
    //                                    categoryName: 'Diagnostics',
    //                                    indexName: 'Diagnostics Flag'
    //                                },
    //                                {
    //                                    cimCode: '2.2.0',
    //                                    systemName: 'Metering system network',
    //                                    categoryName: 'Power quality',
    //                                    indexName: 'Power Quality Flag'
    //                                },
    //                                {
    //                                    cimCode: '3.3.0',
    //                                    systemName: 'Meter Data Management System',
    //                                    categoryName: 'Tamper',
    //                                    indexName: 'Revenue Protection'
    //                                },
    //                                {
    //                                    cimCode: '4.4.0',
    //                                    systemName: 'Other System',
    //                                    categoryName: 'Data collection',
    //                                    indexName: 'Alarm Flag'
    //                                },
    //                                {
    //                                    cimCode: '5.6.0',
    //                                    systemName: 'Externally specified',
    //                                    categoryName: 'Validation',
    //                                    indexName: 'Failed validation - Generic'
    //                                },
    //                                {
    //                                    cimCode: '*.0.*',
    //                                    systemName: 'All systems',
    //                                    categoryName: 'Valid',
    //                                    indexName: 'All indexes'
    //                                },
    //                                {
    //                                    cimCode: '1.7.0',
    //                                    systemName: 'End device',
    //                                    categoryName: 'Edited',
    //                                    indexName: 'Manually Edited - Generic'
    //                                },
    //                                {
    //                                    cimCode: '*.1.*',
    //                                    systemName: 'All systems',
    //                                    categoryName: 'Diagnostics',
    //                                    indexName: 'All indexes'
    //                                },
    //                                {
    //                                    cimCode: '4.8.0',
    //                                    systemName: 'Other System',
    //                                    categoryName: 'Estimated',
    //                                    indexName: 'Estimated - Generic'
    //                                },
    //                                {
    //                                    cimCode: '4.10.0',
    //                                    systemName: 'Other System',
    //                                    categoryName: 'Questionable',
    //                                    indexName: 'Indeterminate'
    //                                },
    //                                {
    //                                    cimCode: '4.11.0',
    //                                    systemName: 'Other System',
    //                                    categoryName: 'Derived',
    //                                    indexName: 'Derived - Deterministic'
    //                                },
    //                                {
    //                                    cimCode: '4.12.0',
    //                                    systemName: 'Other System',
    //                                    categoryName: 'Projected',
    //                                    indexName: 'Projected - Generic'
    //                                },
    //                                {
    //                                    cimCode: '0.0.1',
    //                                    systemName: 'Not Applicable',
    //                                    categoryName: 'Valid',
    //                                    indexName: 'Validated'
    //                                },
    //                                {
    //                                    cimCode: '1.1.1',
    //                                    systemName: 'End device',
    //                                    categoryName: 'Diagnostics',
    //                                    indexName: 'Battery Low'
    //                                },
    //                                {
    //                                    cimCode: '2.2.1',
    //                                    systemName: 'Metering system network',
    //                                    categoryName: 'Power quality',
    //                                    indexName: 'Excessive OutageCount'
    //                                },
    //                                {
    //                                    cimCode: '3.3.1',
    //                                    systemName: 'Meter Data Management System',
    //                                    categoryName: 'Tamper',
    //                                    indexName: 'Cover Opened'
    //                                },
    //                                {
    //                                    cimCode: '4.4.1',
    //                                    systemName: 'Other System',
    //                                    categoryName: 'Data collection',
    //                                    indexName: 'Overflow Condition Detected'
    //                                },
    //                                {
    //                                    cimCode: '5.6.1',
    //                                    systemName: 'Externally specified',
    //                                    categoryName: 'Validation',
    //                                    indexName: 'Failed validation - Zero Usages'
    //                                },
    //                                {
    //                                    cimCode: '1.7.1',
    //                                    systemName: 'End device',
    //                                    categoryName: 'Edited',
    //                                    indexName: 'Manually Added'
    //                                },
    //                                {
    //                                    cimCode: '4.10.1',
    //                                    systemName: 'Other System',
    //                                    categoryName: 'Questionable',
    //                                    indexName: 'Manually Accepted'
    //                                },
    //                                {
    //                                    cimCode: '4.11.1',
    //                                    systemName: 'Other System',
    //                                    categoryName: 'Derived',
    //                                    indexName: 'Derived - Inferred'
    //                                }
    //                            ],
    //                            selectionMode: "LIST",
    //                            exhaustive: false,
    //                            editable: false
    //                        }
    //                    },
    //                    required: true
    //                }
    //            ]
    //        },
    //        {
    //            implementation: "com.elster.jupiter.validators.impl.RegisterIncreaseValidator",
    //            displayName: "Register increase",
    //            properties: [
    //                {
    //                    key: "failEqualData",
    //                    name: "Fail equal data",
    //                    propertyTypeInfo: {
    //                        "simplePropertyType": "BOOLEAN"
    //                    },
    //                    required: true
    //                }
    //            ]
    //        }
    //    ]
    //},
    //
    //proxy: {
    //    type: 'memory',
    //    reader: {
    //        type: 'json',
    //        root: 'validators'
    //    }
    //}

    proxy: {
        type: 'rest',
        url: '/api/val/validation/validators',
        reader: {
            type: 'json',
            root: 'validators'
        }
    }
});
