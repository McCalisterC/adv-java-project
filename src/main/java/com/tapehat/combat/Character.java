package com.tapehat.combat;

public class Character {

    private int hp;
    private String name;

    public Character(String name, int initialHp) {
        this.name = name;
        this.hp = initialHp;
    }

    // Getters and Setters for hp & name
    public int getHp() {
        return hp;
    }
    public void setHp(int newHp) { hp = newHp; }

    public void setName(String name) { this.name = name; }
    public String getName(){
        return name;
    }

    public void attack(Character target) {
        target.takeDamage(5); // Placeholder damage
    }

    public void heal() {
        this.hp += 10; // Placeholder heal
    }

    public void takeDamage(int damage) {
        this.hp -= damage;
    }
}