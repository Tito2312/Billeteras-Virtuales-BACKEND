package com.fintech.dbilleteras_virtuales.dataStructure;

public class ListNode<T> {

    public T nodeValue;

    public ListNode<T> nextNode;

    public ListNode(T nodeValue, ListNode<T> nextNode) {
        this.nodeValue = nodeValue;
        this.nextNode = nextNode;
    }

    public T getNodeValue() {
        return nodeValue;
    }

    public void setNodeValue(T nodeValue) {
        this.nodeValue = nodeValue;
    }

    public ListNode<T> getNextNode() {
        return nextNode;
    }

    public void setNextNode(ListNode<T> nextNode) {
        this.nextNode = nextNode;
    }

}
