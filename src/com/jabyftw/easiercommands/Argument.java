package com.jabyftw.easiercommands;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by Rafael on 20/02/2015.
 */
public class Argument {

    private final HashMap<ArgumentType, Object> arguments = new HashMap<>();
    private final String initialString;

    public Argument(String argument) {
        this.initialString = argument;
        addArgumentType(ArgumentType.STRING, argument);
    }

    public String getInitialString() {
        return initialString;
    }

    public void addArgumentType(ArgumentType argumentType, Object object) {
        this.arguments.put(argumentType, object);
    }

    public Object getArgument(Class<?> expectedClass) {
        for(ArgumentType argumentType : getArgumentTypes()) {
            if(argumentType.getClazz().isAssignableFrom(expectedClass))
                return arguments.get(argumentType);
        }

        return null;
    }

    public Set<ArgumentType> getArgumentTypes() {
        return arguments.keySet();
    }

    @Override
    public String toString() {
        return getClass().getName() + " -> " + getArgumentTypes().toString();
    }
}

