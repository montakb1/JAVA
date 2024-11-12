package gestionzoo.entities;

import gestionzoo.exceptions.InvalidAgeException;

public class Dolphin extends Aquatic {
    private float swimmingSpeed;

    public Dolphin() throws InvalidAgeException {
        super();
    }

    public Dolphin(String family, String name, int age, boolean isMammal, String habitat, float swimmingSpeed) throws InvalidAgeException {
        super(family, name, age, isMammal, habitat);
        setSwimmingSpeed(swimmingSpeed);
    }

    public float getSwimmingSpeed() {
        return swimmingSpeed;
    }

    public void setSwimmingSpeed(float swimmingSpeed) {
        if (swimmingSpeed < 0) {
            throw new IllegalArgumentException("La vitesse de nage ne peut pas être négative");
        }
        this.swimmingSpeed = swimmingSpeed;
    }

    @Override
    public void swim() {
        System.out.println("This dolphin is swimming.");
    }

    @Override
    public String toString() {
        return super.toString() + ", Dolphin{" +
                "swimmingSpeed=" + swimmingSpeed +
                '}';
    }
}
