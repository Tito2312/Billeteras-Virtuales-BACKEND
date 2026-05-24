package com.fintech.dbilleteras_virtuales.dataStructure;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HashTable<K, V> {

    private static final int DEFAULT_CAPACITY = 16;
    private LinkedList<Entry<K, V>>[] buckets;
    private int size;

    @SuppressWarnings("unchecked")
    public HashTable() {
        buckets = new LinkedList[DEFAULT_CAPACITY];
        for (int i = 0; i < DEFAULT_CAPACITY; i++) {
            buckets[i] = new LinkedList<>();
        }
        size = 0;
    }

    private int getBucketIndex(K key) {
        return Math.abs(key.hashCode() % buckets.length);
    }

    public void put(K key, V value) {
        int index = getBucketIndex(key);
        for (Entry<K, V> entry : buckets[index]) {
            if (entry.key.equals(key)) {
                entry.value = value;
                return;
            }
        }
        buckets[index].add(new Entry<>(key, value));
        size++;
    }

    public V get(K key) {
        int index = getBucketIndex(key);
        for (Entry<K, V> entry : buckets[index]) {
            if (entry.key.equals(key)) {
                return entry.value;
            }
        }
        return null;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public void remove(K key) {
        int index = getBucketIndex(key);
        buckets[index].removeIf(entry -> entry.key.equals(key));
        size--;
    }

    public List<V> values() {
        List<V> result = new ArrayList<>();
        for (LinkedList<Entry<K, V>> bucket : buckets) {
            for (Entry<K, V> entry : bucket) {
                result.add(entry.value);
            }
        }
        return result;
    }

    public int size() { return size; }

    public boolean isEmpty() { return size == 0; }

    private static class Entry<K, V> {
        K key;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
