package com.fintech.dbilleteras_virtuales.dataStructure;

public class PriorityQueue<T extends Comparable<T>> {
    private PriorityQueueNode<T> head;
    private int size;

    private static class PriorityQueueNode<T> {
        private T data;
        private PriorityQueueNode<T> next;

        public PriorityQueueNode(T data) {
            this.data = data;
            this.next = null;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public PriorityQueueNode<T> getNext() {
            return next;
        }

        public void setNext(PriorityQueueNode<T> next) {
            this.next = next;
        }
    }

    public PriorityQueue() {
        this.head = null;
        this.size = 0;
    }

    public void enqueue(T data) {
        PriorityQueueNode<T> newNode = new PriorityQueueNode<>(data);

        if (head == null || data.compareTo(head.getData()) < 0) {
            newNode.setNext(head);
            head = newNode;
        } else {
            PriorityQueueNode<T> current = head;
            while (current.getNext() != null &&
                    data.compareTo(current.getNext().getData()) >= 0) {
                current = current.getNext();
            }
            newNode.setNext(current.getNext());
            current.setNext(newNode);
        }
        size++;
    }

    public T dequeue() {
        if (isEmpty()) {
            throw new RuntimeException("The priority queue is empty");
        }

        T data = head.getData();
        head = head.getNext();
        size--;
        return data;
    }

    public T front() {
        if (isEmpty()) {
            throw new RuntimeException("The priority queue is empty");
        }
        return head.getData();
    }

    public boolean isEmpty() {
        return head == null;
    }

    public int getSize() {
        return size;
    }

    public void print() {
        if (isEmpty()) {
            System.out.println("The priority queue is empty");
            return;
        }

        System.out.println("Priority Queue (highest priority -> lowest priority):");
        PriorityQueueNode<T> current = head;
        while (current != null) {
            System.out.println(current.getData());
            current = current.getNext();
        }
    }

    public void clear() {
        head = null;
        size = 0;
    }
}
