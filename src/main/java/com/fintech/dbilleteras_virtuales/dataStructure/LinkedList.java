package com.fintech.dbilleteras_virtuales.dataStructure;

public class LinkedList<T> {

    public ListNode<T> firstListNode;
    public ListNode<T> lastListNode;
    public int size;

    public LinkedList() {
        this.firstListNode = null;
        this.lastListNode = null;
        this.size = 0;
    }

    public void add(T value) {
        ListNode<T> newNode = new ListNode<>(value, null);

        if (firstListNode == null) {
            firstListNode = newNode;
            lastListNode = newNode;
        } else {
            lastListNode.setNextNode(newNode);
            lastListNode = newNode;
        }
        size++;
    }

    public void remove(T value) {
        if (firstListNode == null) {
            return;
        }

        if (firstListNode.getNodeValue().equals(value)) {
            firstListNode = firstListNode.getNextNode();
            if (firstListNode == null) {
                lastListNode = null;
            }
            size--;
            return;
        }

        ListNode<T> current = firstListNode;
        while (current.getNextNode() != null) {
            if (current.getNextNode().getNodeValue().equals(value)) {
                current.setNextNode(current.getNextNode().getNextNode());
                if (current.getNextNode() == null) {
                    lastListNode = current;
                }
                size--;
                return;
            }
            current = current.getNextNode();
        }
    }

    public T searchByIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of range: " + index);
        }

        ListNode<T> current = firstListNode;
        for (int i = 0; i < index; i++) {
            current = current.getNextNode();
        }
        return current.getNodeValue();
    }

    public void print() {
        if (firstListNode == null) {
            System.out.println("The list is empty");
            return;
        }

        ListNode<T> current = firstListNode;
        System.out.print("List: ");
        while (current != null) {
            System.out.print(current.getNodeValue());
            if (current.getNextNode() != null) {
                System.out.print(" -> ");
            }
            current = current.getNextNode();
        }
        System.out.println();
    }

    public boolean isEmpty() {
        return firstListNode == null;
    }

    public int getSize() {
        return size;
    }

    public T searchByValue(T value) {
        ListNode<T> current = firstListNode;

        while (current != null) {
            if (current.getNodeValue().equals(value)) {
                return current.getNodeValue();
            }
            current = current.getNextNode();
        }

        return null;
    }

    public T searchById(String id) {
        ListNode<T> current = firstListNode;

        while (current != null) {

            if (current.getNodeValue().toString().contains(id)) {
                return current.getNodeValue();
            }
            current = current.getNextNode();
        }

        return null;
    }

    public ListNode<T> firstNode() {
        return firstListNode;
    }

}
