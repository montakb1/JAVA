package gestionzoo.entities;

import gestionzoo.exceptions.InvalidAgeException;

public class Penguin extends Aquatic {
    private float swimmingDepth;

    public Penguin() throws InvalidAgeException {
        super();
    }

    public Penguin(String family, String name, int age, boolean isMammal, String habitat, float swimmingDepth) throws InvalidAgeException {
        super(family, name, age, isMammal, habitat);
        setSwimmingDepth(swimmingDepth);
    }

    public float getSwimmingDepth() {
        return swimmingDepth;
    }

    public void setSwimmingDepth(float swimmingDepth) {
        if (swimmingDepth < 0) {
            throw new IllegalArgumentException("La profondeur de nage ne peut pas être négative");
        }
        this.swimmingDepth = swimmingDepth;
    }

    // Ajout de l'implémentation de la méthode swim()
    @Override
    public void swim() {
        System.out.println("Ce pingouin nage à une profondeur de " + swimmingDepth + " mètres");
    }

    @Override
    public String toString() {
        return super.toString() + ", Penguin{" +
                "swimmingDepth=" + swimmingDepth +
                '}';
    }
}
