package com.elster.jupiter.util.streams;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;
import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DecoratedStreamTest {

    private static class Customer {
        private final String name;
        private final String address;

        private Customer(String name, String address) {
            this.name = name;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        @Override
        public String toString() {
            return "Customer{" +
                    "name='" + name + '\'' +
                    ", address='" + address + '\'' +
                    '}';
        }
    }

    @Test
    public void testDistinctByProperty() {
        List<Customer> customers = Arrays.asList(
                new Customer("E", "F"),
                new Customer("A", "B"),
                new Customer("C", "D"),
                new Customer("A", "B"),
                new Customer("G", "H"),
                new Customer("K", "L"),
                new Customer("G", "H"),
                new Customer("A", "B"),
                new Customer("K", "L"),
                new Customer("I", "J"),
                new Customer("G", "H"),
                new Customer("K", "L"),
                new Customer("M", "N"),
                new Customer("K", "L"),
                new Customer("O", "P")
        );

        List<Customer> collect = decorate(customers.stream())
                .distinct(Customer::getName)
                .collect(toList());

        assertThat(collect).hasSize(8);
    }


}