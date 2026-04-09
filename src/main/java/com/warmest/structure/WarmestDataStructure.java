package com.warmest.structure;

import com.warmest.model.DoublyLinkedNode;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class WarmestDataStructure implements WarmestDataStructureInterface {

    private final HashMap<String, DoublyLinkedNode> map = new HashMap<>();

    private final DoublyLinkedNode head = new DoublyLinkedNode(null, 0);
    private DoublyLinkedNode tail = head;

    @Override
    public synchronized Integer put(String key, int value) {
        Integer existingValue = null;
        DoublyLinkedNode node = map.get(key);
        if (node != null) {
            existingValue = node.getValue();
            node.setValue(value);
            unlink(node);
            insertAtTail(node);
        } else {
            DoublyLinkedNode newNode = new DoublyLinkedNode(key, value);
            insertAtTail(newNode);
            map.put(key, newNode);
        }
        return existingValue;
    }

    @Override
    public synchronized Integer remove(String key) {
        DoublyLinkedNode node = map.remove(key);
        if (node != null) {
            unlink(node);
            return node.getValue();
        }
        return null;
    }

    @Override
    public synchronized Integer get(String key) {
        DoublyLinkedNode node = map.get(key);
        if (node != null) {
            unlink(node);
            insertAtTail(node);
            return node.getValue();
        }
        return null;
    }

    @Override
    public synchronized String getWarmest() {
        if (tail == head) {
            return null;
        }

        return tail.getKey();
    }

    private void unlink(DoublyLinkedNode node) {
        DoublyLinkedNode prev = node.getPrev();
        DoublyLinkedNode next = node.getNext();

        prev.setNext(next);
        if (next != null) {
            next.setPrev(prev);
        }

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
}
