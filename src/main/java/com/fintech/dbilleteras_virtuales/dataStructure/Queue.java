package com.fintech.dbilleteras_virtuales.dataStructure;

public class Queue<T> {

    private LinkedList<T> list;

    public Queue() {
        list = new LinkedList<>();
    }

    public void enqueue(T value) {
        list.add(value);
    }

    public T dequeue() {

        if (isEmpty()) {
            return null;
        }

        T value = list.firstListNode.getNodeValue();
        list.remove(value);

        return value;
    }

    public boolean isEmpty() {
        return list.size == 0;
    }

    public T peek() {
        if (isEmpty()) {
            return null;
        }

        return list.firstListNode.getNodeValue();
    }
}
