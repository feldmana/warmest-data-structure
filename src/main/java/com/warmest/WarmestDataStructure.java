package com.warmest;

import com.warmest.model.DoublyLinkedNode;
import com.warmest.structure.WarmestDataStructureInterface;

import java.util.HashMap;

public class WarmestDataStructure implements WarmestDataStructureInterface {

    private final HashMap<String, DoublyLinkedNode> map = new HashMap<>();

    // Dummy nodes; tail.prev is always the warmest key
    private final DoublyLinkedNode head = new DoublyLinkedNode(null, 0);
    private DoublyLinkedNode tail = head; // tail == head means empty list

    private void unlink(DoublyLinkedNode node) {
        DoublyLinkedNode prev = node.getPrev();
        DoublyLinkedNode next = node.getNext();

        prev.setNext(next);
        if (next != null) next.setPrev(prev);

        if (node == tail) {
            tail = prev;
        }
    }

    private void insertAtTail(DoublyLinkedNode node) {
        node.setPrev(tail);
        node.setNext(null);
        tail.setNext(node);
        tail = node;
    }

    @Override
    public Integer put(String key, int value) {
        Integer existingValue = null;
        DoublyLinkedNode node = map.get(key);
        if (node != null) {
            existingValue = node.getValue();
            unlink(node);
        }
        DoublyLinkedNode newNode = new DoublyLinkedNode(key, value);
        insertAtTail(newNode);
        map.put(key, newNode);
        return existingValue;
    }

    @Override
    public Integer remove(String key) {
        DoublyLinkedNode node = map.remove(key);
        if (node != null) {
            unlink(node);
            return node.getValue();
        }
        return null;
    }

    @Override
    public Integer get(String key) {
        DoublyLinkedNode node = map.get(key);
        if (node != null) {
            unlink(node);
            insertAtTail(node);
            return node.getValue();
        }
        return null;
    }


    @Override
    public String getWarmest() {
        if (tail == head) {
            return null;
        }

        return tail.getKey();
    }
}
