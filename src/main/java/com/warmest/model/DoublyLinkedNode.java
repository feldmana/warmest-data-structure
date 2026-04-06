package com.warmest.model;

import lombok.Data;

@Data
    public   class DoublyLinkedNode {
        String key;
        int value;
        DoublyLinkedNode prev;
        DoublyLinkedNode next;

        public DoublyLinkedNode(String key, int value) {
            this.key = key;
            this.value = value;
        }
    }