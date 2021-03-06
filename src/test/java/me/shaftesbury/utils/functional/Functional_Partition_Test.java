package me.shaftesbury.utils.functional;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.fail;
public class Functional_Partition_Test {
    public Functional_Partition_Test() {
    }

    @Test
    void partitionTest1() {
        final Collection<Integer> m = Functional.init(FunctionalTest.triplingGenerator, 5);
        final Pair<List<Integer>, List<Integer>> r = Functional.partition(Functional.isOdd, m);

        assertThat(r.getLeft()).containsExactly(3, 9, 15);
        assertThat(r.getRight()).containsExactly(6, 12);
    }

    @Test
    void curriedPartitionTest1() {
        final Collection<Integer> m = Functional.init(FunctionalTest.triplingGenerator, 5);
        final Pair<List<Integer>, List<Integer>> r = Functional.partition(Functional.isOdd).apply(m);

        assertThat(r.getLeft()).containsExactly(3, 9, 15);
        assertThat(r.getRight()).containsExactly(6, 12);
    }

    @Test
    void iterablePartitionTest1() {
        final Iterable<Integer> m = Functional.seq.init(FunctionalTest.triplingGenerator, 5);
        final Pair<List<Integer>, List<Integer>> r = Functional.partition(Functional.isOdd, m);

        assertThat(r.getLeft()).containsExactly(3, 9, 15);
        assertThat(r.getRight()).containsExactly(6, 12);
    }

    @Test
    void partitionTest2() {
        final Collection<Integer> l = Functional.init(FunctionalTest.doublingGenerator, 5);
        final Pair<List<Integer>, List<Integer>> r = Functional.partition(Functional.isEven, l);
        assertThat(r.getLeft()).containsExactlyElementsOf(l);
        assertThat(r.getRight()).containsExactly();
    }

    @Test
    void partitionTest3() {
        final Collection<Integer> l = Functional.init(FunctionalTest.doublingGenerator, 5);
        final Pair<List<Integer>, List<Integer>> r = Functional.partition(Functional.isEven, l);
        assertThat(r.getLeft()).isEqualTo(Functional.filter(Functional.isEven, l));
    }

    @Test
    void partitionRangesOfIntAndStore() {
        final int noElems = 13;
        final int noPartitions = 5;
        final List<Functional.Range<Integer>> partitions = Functional.partition(noElems, noPartitions);

        // Store the ranges in a map to exercise the hashCode()
        final Map<Functional.Range<Integer>, Pair<Integer, Integer>> map =
                Functional.toDictionary(
                        Functional.identity(),
                        range -> Pair.of(range.from(), range.to()), partitions);

        final List<Functional.Range<Integer>> extractedRanges = Functional.map(Map.Entry::getKey, map.entrySet());

        assertThat(extractedRanges.containsAll(partitions)).isTrue();
        assertThat(partitions.containsAll(extractedRanges)).isTrue();
    }

    @Test
    void seqPartitionRangesOfIntAndStore() {
        final int noElems = 13;
        final int noPartitions = 5;
        final List<Functional.Range<Integer>> partitions = Functional.toList(Functional.seq.partition(noElems, noPartitions));

        // Store the ranges in a map to exercise the hashCode()
        final Map<Integer, Functional.Range<Integer>> map = Functional.toDictionary(Functional.Range::from, Functional.identity(), partitions);

        final List<Functional.Range<Integer>> extractedRanges = Functional.map(Map.Entry::getValue, map.entrySet());

        assertThat(extractedRanges.containsAll(partitions)).isTrue();
        assertThat(partitions.containsAll(extractedRanges)).isTrue();
    }

    @Test
    void cantRemoveFromSeqPartitionRangesOfIntAndStore() {
        final int noElems = 13;
        final int noPartitions = 5;
        final Iterable<Functional.Range<Integer>> partitions = Functional.seq.partition(noElems, noPartitions);
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> partitions.iterator().remove());
    }

    @Test
    void cantRestartIteratorFromSeqPartitionRangesOfIntAndStore() {
        final int noElems = 13;
        final int noPartitions = 5;
        final Iterable<Functional.Range<Integer>> partitions = Functional.seq.partition(noElems, noPartitions);
        try {
            partitions.iterator();
        } catch (final UnsupportedOperationException e) {
            fail("Shouldn't reach this point");
        }
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(partitions::iterator);
    }

    @Test
    void partitionRangesOfInt() {
        final int noElems = 13;
        final int noPartitions = 5;
        final List<Functional.Range<Integer>> partitions = Functional.partition(noElems, noPartitions);

        final List<Integer> expectedStart = Arrays.asList(0, 3, 6, 9, 11);
        final List<Integer> expectedEnd = Arrays.asList(3, 6, 9, 11, 13);
        final List<Pair<Integer, Integer>> expected = Functional.zip(expectedStart, expectedEnd);

        final List<Pair<Integer, Integer>> output = Functional.map(range -> Pair.of(range.from(), range.to()), partitions);

        assertThat(output).containsExactlyElementsOf(expected);
    }

    @Test
    void exactlyEvenPartitionRangesOfInt() {
        final int noElems = 10;
        final int noPartitions = 5;
        final List<Functional.Range<Integer>> partitions = Functional.partition(noElems, noPartitions);

        final List<Integer> expectedStart = Arrays.asList(0, 2, 4, 6, 8);
        final List<Integer> expectedEnd = Arrays.asList(2, 4, 6, 8, 10);
        final List<Pair<Integer, Integer>> expected = Functional.zip(expectedStart, expectedEnd);

        final List<Pair<Integer, Integer>> output = Functional.map(range -> Pair.of(range.from(), range.to()), partitions);

        assertThat(output).containsExactlyElementsOf(expected);
    }

    @Test
    void partitionFewerRangesOfIntThanPartitionsRequested() {
        final int noElems = 7;
        final int noPartitions = 10;
        final List<Functional.Range<Integer>> partitions = Functional.partition(noElems, noPartitions);

        final List<Integer> expectedStart = Arrays.asList(0, 1, 2, 3, 4, 5, 6);
        final List<Integer> expectedEnd = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        final List<Functional.Range<Integer>> expected =
                Functional.concat(
                        Functional.map(
                                pair -> new Functional.Range<Integer>(pair.getLeft(), pair.getRight()), Functional.zip(expectedStart, expectedEnd)),
                        Functional.init(Functional.constant(new Functional.Range<Integer>(7, 7)), 3));

        assertThat(partitions).containsExactlyElementsOf(expected);
    }

    @Test
    void partitionRangesOfString() {
        final int noElems = 13;
        final int noPartitions = 5;
        final List<Functional.Range<String>> partitions =
                Functional.partition(
                        i -> Integer.toString(i - 1),
                        noElems, noPartitions);

        final List<String> expectedStart = Arrays.asList("0", "3", "6", "9", "11");
        final List<String> expectedEnd = Arrays.asList("3", "6", "9", "11", "13");
        final List<Pair<String, String>> expected = Functional.zip(expectedStart, expectedEnd);

        final List<Pair<String, String>> output = Functional.map(range -> Pair.of(range.from(), range.to()), partitions);

        assertThat(output).containsExactlyElementsOf(expected);
    }

    @Test
    void partitionWithEmptySource() {
        assertThatIllegalArgumentException().isThrownBy(() -> Functional.partition(0, 1));
    }

    @Test
    void partitionWithZeroOutputRanges() {
        assertThatIllegalArgumentException().isThrownBy(() -> Functional.partition(1, 0));
    }

    @Test
    void seqPartitionRangesOfString() {
        final int noElems = 13;
        final int noPartitions = 5;
        final List<Functional.Range<String>> partitions = Functional.toList(
                Functional.seq.partition(
                        i -> Integer.toString(i - 1),
                        noElems, noPartitions));

        final List<String> expectedStart = Arrays.asList("0", "3", "6", "9", "11");
        final List<String> expectedEnd = Arrays.asList("3", "6", "9", "11", "13");
        final List<Pair<String, String>> expected = Functional.zip(expectedStart, expectedEnd);

        final List<Pair<String, String>> output = Functional.map(range -> Pair.of(range.from(), range.to()), partitions);

        assertThat(output).containsExactlyElementsOf(expected);
    }

    @Test
    void seqPartitionRangesOfString2() {
        final int noElems = 13;
        final int noPartitions = 5;
        final Iterable<Functional.Range<String>> output =
                Functional.seq.partition(
                        i -> Integer.toString(i - 1),
                        noElems, noPartitions);

        final List<String> expectedStart = Arrays.asList("0", "3", "6", "9", "11");
        final List<String> expectedEnd = Arrays.asList("3", "6", "9", "11", "13");
        final List<Pair<String, String>> expected_ = Functional.zip(expectedStart, expectedEnd);

        final List<Functional.Range<String>> expected = Functional.map(pair -> new Functional.Range<String>(pair.getLeft(), pair.getRight()), expected_);
        final Iterator<Functional.Range<String>> iterator = output.iterator();

        for (int i = 0; i < 20; ++i)
            assertThat(iterator.hasNext()).isTrue();

        for (final Functional.Range<String> element : expected) {
            final Functional.Range<String> next = iterator.next();
            assertThat(next).isEqualTo(element);
        }

        assertThat(iterator.hasNext()).isFalse();
        try {
            iterator.next();
        } catch (final NoSuchElementException e) {
            return;
        }

        fail("Should not reach this point");
    }
}