package net.adarw;

import java.util.ArrayList;

public class Template {
    public static class Component{
        public enum ComponentType{
            BOOLEAN,
            STRING,
        }
        public ComponentType type;
        public String name;

        public Component(ComponentType type, String name){
            this.type = type;
            this.name = name;
        }

    }

    public ArrayList<Component> components = new ArrayList<>();
}
