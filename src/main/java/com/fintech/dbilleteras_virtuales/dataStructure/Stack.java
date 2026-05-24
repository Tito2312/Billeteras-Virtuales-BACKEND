package com.fintech.dbilleteras_virtuales.dataStructure;

public class Stack<T> {
    private StackNode<T> top;
    private int size;

    public Stack() {
        this.top = null;
        this.size = 0;
    }

    public void push(T data) {
        StackNode<T> newNode = new StackNode<>(data);
        newNode.setNext(top);
        top = newNode;
        size++;
    }

    public T pop() {
        if (isEmpty()) {
            throw new RuntimeException("The stack is empty");
        }

        T data = top.getData();
        top = top.getNext();
        size--;
        return data;
    }

    public T top() {
        if (isEmpty()) {
            throw new RuntimeException("The stack is empty");
        }
        return top.getData();
    }

    public boolean isEmpty() {
        return top == null;
    }

    public int getSize() {
        return size;
    }

    public void print() {
        if (isEmpty()) {
            System.out.println("The stack is empty");
            return;
        }

        System.out.println("Stack (top -> base):");
        StackNode<T> current = top;
        while (current != null) {
            System.out.println(current.getData());
            current = current.getNext();
        }
    }

    public void clear() {
        top = null;
        size = 0;
    }
}
