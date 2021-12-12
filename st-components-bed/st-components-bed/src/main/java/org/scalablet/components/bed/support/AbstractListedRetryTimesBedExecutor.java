package org.scalablet.components.bed.support;

import org.scalablet.components.bed.BedExecutor;
import org.scalablet.components.bed.BedExecutorCmd;

import javax.annotation.Nonnull;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

/**
 * 根据重试时间列表构建
 *
 * @author abomb4 2021-11-11 09:55:19
 */
public abstract class AbstractListedRetryTimesBedExecutor<T extends BedExecutorCmd> implements BedExecutor<T> {

    /** Setter lock */
    private final Object lock = new Object();
    /** 重试时间列表，秒为单位 */
    @SuppressWarnings("VolatileArrayField")
    private volatile int[] retryTimeList;


    /**
     * 用重试时间列表构建
     *
     * @param retryTimeList 重试时间列表（秒），第一个参数会作为首次执行延迟，如两个元素就执行重试一次，共执行两次
     */
    protected AbstractListedRetryTimesBedExecutor(Collection<Integer> retryTimeList) {
        this.setRetryTimeList(retryTimeList);
    }

    /**
     * 设置重试时间列表
     *
     * @param retryTimeList 不能为空，不能为 null，不能包含小于 0 的值
     */
    public final void setRetryTimeList(Collection<Integer> retryTimeList) {
        if (retryTimeList == null || retryTimeList.isEmpty()) {
            throw new IllegalArgumentException("retryTimeList cannot be null or empty");
        }
        int i = 0;
        for (Integer integer : retryTimeList) {
            if (integer < 0) {
                throw new IllegalArgumentException(
                        "retryTimesLimit[" + i + "] = " + integer + " lesser than 0 is " + "illegal");
            }
            i += 1;
        }
        synchronized (this.lock) {
            final int[] arr = new int[retryTimeList.size()];
            int j = 0;
            for (Integer integer : retryTimeList) {
                arr[j] = integer;
                j += 1;
            }
            this.retryTimeList = arr;
        }
    }

    /**
     * 获取重试时间列表
     *
     * @return 重试时间列表，不可变
     */
    protected List<Integer> getRetryTimeList() {
        return new ListView();
    }

    @Override
    public RetryControl shouldRetry(T cmd, int executedTimes) {
        final int[] list = this.retryTimeList;
        return executedTimes > list.length
                ? RetryControl.noRetry()
                : RetryControl.retry(list[executedTimes]);
    }

    /**
     * @serial include
     */
    private class ListView extends AbstractList<Integer>
            implements RandomAccess, java.io.Serializable {
        @java.io.Serial
        private static final long serialVersionUID = -2764017481108945198L;

        @Override
        public int size() {
            return AbstractListedRetryTimesBedExecutor.this.retryTimeList.length;
        }

        @Nonnull
        @Override
        public Object[] toArray() {
            return IntStream.of(AbstractListedRetryTimesBedExecutor.this.retryTimeList).boxed().toArray();
        }

        @Nonnull
        @Override
        @SuppressWarnings("unchecked")
        public <V> V[] toArray(V[] a) {
            int size = this.size();
            if (a.length < size) {
                return IntStream.of(AbstractListedRetryTimesBedExecutor.this.retryTimeList).boxed()
                        .map(v -> (V) v)
                        .toArray(value -> a);
            }
            System.arraycopy(AbstractListedRetryTimesBedExecutor.this.retryTimeList, 0, a, 0, size);
            if (a.length > size) {
                // noinspection AssignmentToNull
                a[size] = null;
            }
            return a;
        }

        @Override
        public Integer get(int index) {
            return AbstractListedRetryTimesBedExecutor.this.retryTimeList[index];
        }

        @Override
        public Integer set(int index, Integer element) {
            throw new UnsupportedOperationException("immutable");
        }

        @Override
        public int indexOf(Object o) {
            int[] a = AbstractListedRetryTimesBedExecutor.this.retryTimeList;
            final int length = a.length;
            for (int i = 0; i < length; i++) {
                if (o.equals(a[i])) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public boolean contains(Object o) {
            // noinspection ListIndexOfReplaceableByContains
            return this.indexOf(o) >= 0;
        }

        @Override
        public Spliterator<Integer> spliterator() {
            return Spliterators.spliterator(AbstractListedRetryTimesBedExecutor.this.retryTimeList, Spliterator.ORDERED);
        }

        @Override
        public void forEach(Consumer<? super Integer> action) {
            Objects.requireNonNull(action);
            for (Integer e : AbstractListedRetryTimesBedExecutor.this.retryTimeList) {
                action.accept(e);
            }
        }

        @Override
        public void replaceAll(UnaryOperator<Integer> operator) {
            throw new UnsupportedOperationException("immutable");
        }

        @Override
        public void sort(Comparator<? super Integer> c) {
            throw new UnsupportedOperationException("immutable");
        }

        @Nonnull
        @Override
        public Iterator<Integer> iterator() {
            return new ArrayItr();
        }

        private class ArrayItr implements Iterator<Integer> {
            private int cursor;

            @Override
            public boolean hasNext() {
                return this.cursor < AbstractListedRetryTimesBedExecutor.this.retryTimeList.length;
            }

            @Override
            public Integer next() {
                int i = this.cursor;
                if (i >= AbstractListedRetryTimesBedExecutor.this.retryTimeList.length) {
                    throw new NoSuchElementException();
                }
                this.cursor = i + 1;
                return AbstractListedRetryTimesBedExecutor.this.retryTimeList[i];
            }

            @Override
            public String toString() {
                return "ArrayItr{" +
                        "cursor=" + this.cursor +
                        ", a=" + Arrays.toString(AbstractListedRetryTimesBedExecutor.this.retryTimeList) +
                        '}';
            }
        }
    }
}
