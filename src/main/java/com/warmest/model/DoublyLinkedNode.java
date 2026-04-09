package com.warmest.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoublyLinkedNode {
    private final String key;
    private int value;
    private DoublyLinkedNode prev;
    private DoublyLinkedNode next;

    public DoublyLinkedNode(String key, int value) {
        this.key = key;
        this.value = value;
    }
}