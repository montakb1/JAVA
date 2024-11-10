package gestionzoo.entities;

public class Terrestrial extends Animal {
    private int nbrLegs;

    public Terrestrial() {
        super("Inconnu", "Inconnu", 0, true);
    }

    public Terrestrial(String family, String name, int age, boolean isMammal, int nbrLegs) {
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
}
