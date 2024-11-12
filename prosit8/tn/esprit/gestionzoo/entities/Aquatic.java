package gestionzoo.entities;

import gestionzoo.exceptions.InvalidAgeException;
import gestionzoo.interfaces.Carnivore;
import gestionzoo.interfaces.Food;

public abstract class Aquatic extends Animal implements Carnivore<Food> {
    private String habitat;

    public Aquatic() throws InvalidAgeException {
        super("Inconnu", "Inconnu", 0, true);
    }

    public Aquatic(String family, String name, int age, boolean isMammal, String habitat) throws InvalidAgeException {
        super(family, name, age, isMammal);
        setHabitat(habitat);
    }

    public String getHabitat() {
        return habitat;
    }

    public void setHabitat(String habitat) {
        if (habitat == null || habitat.trim().isEmpty()) {
            throw new IllegalArgumentException("L'habitat ne peut pas être vide");
        }
        this.habitat = habitat;
    }

    // Instruction 28: Méthode swim rendue abstraite
    public abstract void swim();

    // Instruction 31: Redéfinition de equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aquatic aquatic = (Aquatic) o;
        return getName().equals(aquatic.getName()) &&
                getAge() == aquatic.getAge() &&
                habitat.equals(aquatic.habitat);
    }

    @Override
    public String toString() {
        return super.toString() + ", Aquatic{" +
                "habitat='" + habitat + '\'' +
                '}';
    }
    @Override
    public void eatMeat(Food meat) {
        if (meat == Food.MEAT || meat == Food.BOTH) {
            System.out.println("L'animal aquatique mange de la viande.");
        } else {
            System.out.println("L'animal aquatique ne peut pas manger de plantes.");
        }
    }
}