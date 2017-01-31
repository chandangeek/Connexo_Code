/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.time.StopWatch;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.sql.DataSource;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.LongSummaryStatistics;

@Component(name = "com.elster.jupiter.orm.console", service = ConsoleCommands.class,
        property = {"osgi.command.scope=orm", "osgi.command.function=ddl",
                "osgi.command.function=executeQuery",
                "osgi.command.function=listDataModels", "osgi.command.function=pingDb", "osgi.command.function=clearCaches"}, immediate = true)
public class ConsoleCommands {

    private volatile BundleContext context;
    private volatile DataSource dataSource;
    private volatile OrmService ormService;

    @Activate
    public void activate(BundleContext context) {
        this.context = context;
    }

    @Deactivate
    public void deactivate() {

    }

    @Reference
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    public void listDataModels() {
        this.ormService.getDataModels()
                .stream()
                .map(dataModel -> dataModel.getName() + ": " + dataModel.getDescription())
                .sorted()
                .forEach(System.out::println);
    }

    public void ddl(String... componentName) {
        for (String component : componentName) {
            printDDL(component);
        }
    }

    private void printDDL(String componentName) {
        try {
            String filter = "(name=" + componentName + ")";
            Collection<ServiceReference<InstallService>> references = context.getServiceReferences(InstallService.class, filter);
            if (references.isEmpty()) {
                System.out.println("Nothing to do for " + componentName);
            }
            for (ServiceReference<InstallService> reference : references) {
                print(context.getService(reference));
                context.ungetService(reference);
            }
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }

    }

    private void print(InstallService service) {
        try {
            Field[] declaredFields = service.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                if (DataModel.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    DataModel dataModel = (DataModel) field.get(service);
                    print(dataModel);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void print(DataModel dataModel) {
        for (Table<?> table : dataModel.getTables()) {
            for (String s : table.getDdl()) {
                System.out.println(s);
            }
        }
    }

    public void executeQuery(String query) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                ResultSet resultSet = statement.executeQuery();
                new ResultSetPrinter(System.out).print(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void pingDb(int times) {
        Format format = new DecimalFormat("##0.00");
        double NANOSECONDS_IN_MS = 1_000_000.0d;
        List<Long> stats = new ArrayList<>(times);
        try (Connection connection = dataSource.getConnection()) {
            String jdbcUrl = connection.getMetaData().getURL();
            try (PreparedStatement statement = connection.prepareStatement("select 1 from DUAL")) {
                for (int i = 0; i < times; i++) {
                    StopWatch watch = new StopWatch();
                    statement.execute();
                    watch.stop();
                    stats.add(watch.getElapsed());
                    System.out.println("  Reply from " + jdbcUrl + ": time=" + format.format(watch.getElapsed() / NANOSECONDS_IN_MS) + "ms");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        LongSummaryStatistics longSummaryStatistics = stats.stream().mapToLong(l -> l).summaryStatistics();
        System.out.println("Approximate round trip times in milli-seconds (on " + longSummaryStatistics.getCount() + " times)");
        System.out.println("   Minimum = " + format.format(longSummaryStatistics.getMin() / NANOSECONDS_IN_MS) + "ms, " +
                "Maximum = " + format.format(longSummaryStatistics.getMax() / NANOSECONDS_IN_MS) + "ms, " +
                "Average = " + format.format(longSummaryStatistics.getAverage() / NANOSECONDS_IN_MS) + "ms");
    }

    public void clearCaches() {
        ormService.getDataModels()
                .stream()
                .map(DataModel::getTables)
                .flatMap(List::stream)
                .map(TableImpl.class::cast)
                .forEach(TableImpl::renewCache);
    }

    class ResultSetPrinter {

        private final PrintStream out;

        public ResultSetPrinter() {
            super();
            out = System.out;
        }

        public ResultSetPrinter(PrintStream out) {
            super();
            this.out = out;
        }

        public void print(ResultSet resultSet) {
            try {
                this.print(resultSet, resultSet.getMetaData());
            } catch (SQLException e) {
                e.printStackTrace(System.err);
            }
        }

        private void print(ResultSet resultSet, ResultSetMetaData metaData) throws SQLException {
            this.printHeader(metaData);
            this.printRows(resultSet, metaData);
        }

        private void printHeader(ResultSetMetaData metaData) throws SQLException {
            int numberOfColumns = metaData.getColumnCount();
            for (int i = 0; i < numberOfColumns; i++) {
                out.print(metaData.getColumnLabel(i + 1));
                if (i < numberOfColumns - 1) {
                    out.print("\t");
                }
            }
            out.println();
        }

        private void printRows(ResultSet resultSet, ResultSetMetaData metaData) throws SQLException {
            int numberOfRows = 0;
            while (resultSet.next()) {
                this.printRow(resultSet, metaData);
                numberOfRows++;
                out.println();
            }
            out.println("numberOfRows = " + numberOfRows);
        }

        private void printRow(ResultSet resultSet, ResultSetMetaData metaData) throws SQLException {
            int numberOfColumns = metaData.getColumnCount();
            for (int i = 0; i < numberOfColumns; i++) {
                out.print(resultSet.getObject(i + 1));
                if (i < numberOfColumns - 1) {
                    out.print("\t");
                }
            }
        }

    }
}
