package gestionzoo.entities;

import gestionzoo.exceptions.InvalidAgeException;
import gestionzoo.interfaces.Food;
import gestionzoo.interfaces.Omnivore;

public class Terrestrial extends Animal implements Omnivore<Food> {
    private int nbrLegs;

    public Terrestrial() throws InvalidAgeException {
        super("Inconnu", "Inconnu", 0, true);
    }

    public Terrestrial(String family, String name, int age, boolean isMammal, int nbrLegs) throws InvalidAgeException {
        super(family, name, age, isMammal);
        setNbrLegs(nbrLegs);
    }

    public int getNbrLegs() {
        return nbrLegs;
    }

    public void setNbrLegs(int nbrLegs) {
        if (nbrLegs < 0) {
            throw new IllegalArgumentException("Le nombre de pattes ne peut pas être négatif");
        }
        this.nbrLegs = nbrLegs;
    }

    @Override
    public String toString() {
        return super.toString() + ", Terrestrial{" +
                "nbrLegs=" + nbrLegs +
                '}';
    }
    @Override
    public void eatMeat(Food meat) {
        if (meat == Food.MEAT || meat == Food.BOTH) {
            System.out.println("L'animal terrestre mange de la viande.");
        } else {
            System.out.println("L'animal terrestre ne peut pas manger de plantes.");
        }
    }
    @Override
    public void eatPlant(Food plant) {
        if (plant == Food.PLANT || plant == Food.BOTH) {
            System.out.println("L'animal terrestre mange des plantes.");
        } else {
            System.out.println("L'animal terrestre ne peut pas manger de viande.");
        }
    }

    @Override
    public void eatPlantAndMeat(Food food) {
        if (food == Food.BOTH) {
            System.out.println("L'animal terrestre mange des plantes et de la viande.");
        } else if (food == Food.MEAT) {
            eatMeat(food);
        } else {
            eatPlant(food);
        }
    }
}
