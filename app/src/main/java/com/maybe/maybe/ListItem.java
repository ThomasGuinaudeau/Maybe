package com.maybe.maybe;

public class ListItem {
    private long id;
    private String name;
    private int quantity;

    public ListItem(long id, String name, int quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }
}
