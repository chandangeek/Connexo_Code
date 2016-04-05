Ext.define('Cal.store.TimeOfUseCalendars', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.timeofuse.Calendar',
    autoLoad: false,
    /*proxy: {
     type: 'rest',
     url: '/api/scs/servicecalls',
     timeout: 120000,
     reader: {
     type: 'json',
     root: 'serviceCalls'
     }
     }*/

    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
            root: 'calenders'
        }
    },

    data: {
        calenders: [{
            name: "Residential TOU Example",
            category: "TOU",
            mRID: "optional",
            id: 1,
            description: "From example provided by Robert Ritchy",
            timeZone: "EDT",
            startYear: 2010,
            events: [
                {
                    id: 1,
                    name: "On peak",
                    code: 3
                },
                {
                    id: 2,
                    name: "Off peak",
                    code: 5
                },
                {
                    id: 3,
                    name: "Demand response",
                    code: 97
                }
            ],
            dayTypes: [
                {
                    id: 1,
                    name: "Summer weekday",
                    ranges: [
                        {
                            from: {
                                hour: 0,
                                minute: 0,
                                second: 0
                            },
                            event: 2
                        },
                        {
                            from: {
                                hour: 13,
                                minute: 0,
                                second: 0
                            },
                            event: 1
                        },
                        {
                            from: {
                                hour: 20,
                                minute: 0,
                                second: 0
                            },
                            event: 2
                        }
                    ]
                },
                {
                    id: 2,
                    name: "Weekend",
                    ranges: [
                        {
                            from: {
                                hour: 0,
                                minute: 0,
                                second: 0
                            },
                            event: 2
                        }
                    ]
                },
                {
                    id: 3,
                    name: "Holiday",
                    ranges: [
                        {
                            from: {
                                hour: 0,
                                minute: 0,
                                second: 0
                            },
                            event: 2
                        }
                    ]
                },
                {
                    id: 4,
                    name: "Winter day",
                    ranges: [
                        {
                            from: {
                                hour: 0,
                                minute: 0,
                                second: 0
                            },
                            event: 2
                        },
                        {
                            from: {
                                hour: 5,
                                minute: 0,
                                second: 0
                            },
                            event: 1
                        },
                        {
                            from: {
                                hour: 9,
                                minute: 0,
                                second: 0
                            },
                            event: 1
                        },
                        {
                            from: {
                                hour: 17,
                                minute: 0,
                                second: 0
                            },
                            event: 2
                        },
                        {
                            from: {
                                hour: 21,
                                minute: 0,
                                second: 0
                            },
                            event: 2
                        }
                    ]
                },
                {
                    id: 5,
                    name: "Demand response",
                    ranges: [
                        {
                            from: {
                                hour: 0,
                                minute: 0,
                                second: 0
                            },
                            event: 3
                        }
                    ]
                }
            ],
            weekTemplate: [
                {
                    name: "Wednesday",
                    type: 1
                },
                {
                    name: "Thursday",
                    type: 3
                },
                {
                    name: "Friday",
                    type: 1
                },
                {
                    name: "Saturday",
                    type: 2
                },
                {
                    name: "Sunday",
                    type: 4
                },
                {
                    name: "Monday",
                    type: 1
                },
                {
                    name: "Tuesday",
                    type: 5
                }
            ],
            periods: [
                {
                    name: "Summer"
                },
                {
                    name: "Winter"
                }
            ]

        }]
    }
});
