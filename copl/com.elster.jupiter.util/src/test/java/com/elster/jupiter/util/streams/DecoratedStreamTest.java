package com.elster.jupiter.util.streams;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;
import static com.elster.jupiter.util.streams.Predicates.not;
import static java.util.stream.Collectors.toList;
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

    private static class SubclassedCustomer extends Customer {

        private SubclassedCustomer(String name, String address) {
            super(name, address);
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

    @Test
    public void testPartitionPer() {
        List<String> strings = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
        List<String> collect = decorate(strings.stream())
                .partitionPer(3)
                .map(list -> list.stream().collect(java.util.stream.Collectors.joining()))
                .collect(toList());
        assertThat(collect).isEqualTo(Arrays.asList("ABC", "DEF", "GHI", "JKL", "MNO", "PQR", "STU", "VWX", "YZ"));
    }

    @Test
    public void testTakeWhile() {
        List<String> strings = Arrays.asList("A", "C", "E", "G", "H", "I", "K");
        String characters = decorate(strings.stream())
                .filter(not(String::isEmpty))
                .takeWhile(s -> ('A' - s.charAt(0)) % 2 == 0)
                .collect(Collectors.joining());

        assertThat(characters).isEqualTo("ACEG");
    }

    @Test
    public void testTakeWhileAllMatch() {
        List<String> strings = Arrays.asList("A", "C", "E", "G", "I", "K");
        String characters = decorate(strings.stream())
                .filter(not(String::isEmpty))
                .takeWhile(s -> ('A' - s.charAt(0)) % 2 == 0)
                .collect(Collectors.joining());

        assertThat(characters).isEqualTo("ACEGIK");
    }

    @Test
    public void testTakeWhileIfFirstIsAlreadyNonMatch() {
        List<String> strings = Arrays.asList("B", "C", "E", "G", "H", "I", "K");
        String characters = decorate(strings.stream())
                .filter(not(String::isEmpty))
                .takeWhile(s -> ('A' - s.charAt(0)) % 2 == 0)
                .collect(Collectors.joining());

        assertThat(characters).isEqualTo("");
    }

    @Test
    public void testFilterSubType() {
        List<String> strings = Arrays.asList("B", "C", "E", "G", "H", "I", "K");
        List<SubclassedCustomer> collect = decorate(strings.stream())
                .map(letter -> (letter.charAt(0) - 'A') % 2 == 1 ? new SubclassedCustomer(letter, "address") : new Customer(letter, "address"))
                .filterSubType(SubclassedCustomer.class)
                .collect(Collectors.toList());

        assertThat(collect).hasSize(2);

        assertThat(collect.get(0).getName()).isEqualTo("B");
        assertThat(collect.get(1).getName()).isEqualTo("H");
    }

}