package com.maybe.maybe;

public class CycleStateResource {
    private final String[] states;
    private final int[] resources;
    private int pointer;

    public CycleStateResource(String[] states, int[] resources) {
        this.states = states;
        this.resources = resources;
        pointer = 0;
    }

    public void goNext() {
        if (pointer == states.length - 1)
            pointer = 0;
        else
            pointer++;
    }

    public void goToState(String state) {
        for (int i = 0; i < states.length; i++)
            if (states[i].equals(state))
                pointer = i;
    }

    public String getState() {
        return states[pointer];
    }

    public Integer getResource() {
        return resources[pointer];
    }
}
