package com.fintech.dbilleteras_virtuales.dataStructure;

public class Queue<T> {
    private QueueNode<T> front;
    private QueueNode<T> rear;
    private int size;

    public Queue() {
        this.front = null;
        this.rear = null;
        this.size = 0;
    }

    public void enqueue(T data) {
        QueueNode<T> newNode = new QueueNode<>(data);

        if (isEmpty()) {
            front = newNode;
            rear = newNode;
        } else {
            rear.setNext(newNode);
            rear = newNode;
        }
        size++;
    }

    public T dequeue() {
        if (isEmpty()) {
            throw new RuntimeException("The queue is empty");
        }

        T data = front.getData();
        front = front.getNext();

        if (front == null) {
            rear = null;
        }

        size--;
        return data;
    }

    public T front() {
        if (isEmpty()) {
            throw new RuntimeException("The queue is empty");
        }
        return front.getData();
    }

    public boolean isEmpty() {
        return front == null;
    }

    public int getSize() {
        return size;
    }

    public void print() {
        if (isEmpty()) {
            System.out.println("The queue is empty");
            return;
        }

        System.out.println("Queue (front -> rear):");
        QueueNode<T> current = front;
        while (current != null) {
            System.out.println(current.getData());
            current = current.getNext();
        }
    }

    public void clear() {
        front = null;
        rear = null;
        size = 0;
    }
}
