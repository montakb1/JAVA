package gestionzoo.entities;

public class Zoo {
    private Animal[] animals;
    private String name;
    private String city;
    private final int NBR_CAGES;
    private static final int MAX_ANIMALS = 25;
    private int numberOfAnimals;
    private Aquatic[] aquaticAnimals;
    private int numberOfAquaticAnimals;
    private static final int MAX_AQUATIC = 10;

    public Zoo(String name, String city) {
        setName(name); // Utilisation du setter pour la validation
        this.city = city;
        this.NBR_CAGES = MAX_ANIMALS;
        this.animals = new Animal[MAX_ANIMALS];
        this.numberOfAnimals = 0;
        this.aquaticAnimals = new Aquatic[MAX_AQUATIC];
        this.numberOfAquaticAnimals = 0;
    }

    // Instruction 17: Modification de addAnimal pour inclure isZooFull
    public boolean addAnimal(Animal animal) {
        if (animal == null) return false;
        if (isZooFull()) {
            System.out.println("Le zoo est plein !");
            return false;
        }
        if (searchAnimal(animal) != -1) {
            System.out.println("Cet animal existe déjà !");
            return false;
        }

        animals[numberOfAnimals] = animal;
        numberOfAnimals++;
        return true;
    }

    public boolean isZooFull() {
        return numberOfAnimals >= NBR_CAGES;
    }

    public int searchAnimal(Animal animal) {
        if (animal == null) return -1;
        for (int i = 0; i < numberOfAnimals; i++) {
            if (animals[i].equals(animal)) {
                return i;
            }
        }
        return -1;
    }

    public boolean removeAnimal(Animal animal) {
        int index = searchAnimal(animal);
        if (index == -1) return false;

        for (int i = index; i < numberOfAnimals - 1; i++) {
            animals[i] = animals[i + 1];
        }
        animals[numberOfAnimals - 1] = null;
        numberOfAnimals--;
        return true;
    }

    public static Zoo comparerZoo(Zoo z1, Zoo z2) {
        if (z1 == null) return z2;
        if (z2 == null) return z1;
        return (z1.numberOfAnimals >= z2.numberOfAnimals) ? z1 : z2;
    }

    // Getters et Setters avec validation
    public String getName() { return name; }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du zoo ne peut pas être vide");
        }
        this.name = name;
    }

    public void displayZoo() {
        System.out.println("\nInformations du Zoo:");
        System.out.println("Nom: " + name);
        System.out.println("Ville: " + city);
        System.out.println("Nombre de cages: " + NBR_CAGES);
        System.out.println("Nombre d'animaux actuel: " + numberOfAnimals);
    }

    public void displayAnimals() {
        System.out.println("\nAnimaux dans " + name + ":");
        for (int i = 0; i < numberOfAnimals; i++) {
            System.out.println((i + 1) + ". " + animals[i]);
        }
    }
    // Instruction 26
    public void addAquaticAnimal(Aquatic aquatic) {
        if (aquatic == null) return;
        if (numberOfAquaticAnimals >= MAX_AQUATIC) {
            System.out.println("Le tableau des animaux aquatiques est plein !");
            return;
        }
        aquaticAnimals[numberOfAquaticAnimals++] = aquatic;
    }

    // Instruction 27
    public void swimAll() {
        for (int i = 0; i < numberOfAquaticAnimals; i++) {
            aquaticAnimals[i].swim();
        }
    }

    // Instruction 29
    public float maxPenguinSwimmingDepth() {
        float maxDepth = 0;
        for (int i = 0; i < numberOfAquaticAnimals; i++) {
            if (aquaticAnimals[i] instanceof Penguin) {
                Penguin penguin = (Penguin) aquaticAnimals[i];
                if (penguin.getSwimmingDepth() > maxDepth) {
                    maxDepth = penguin.getSwimmingDepth();
                }
            }
        }
        return maxDepth;
    }

    // Instruction 30
    public void displayNumberOfAquaticsByType() {
        int dolphins = 0;
        int penguins = 0;

        for (int i = 0; i < numberOfAquaticAnimals; i++) {
            if (aquaticAnimals[i] instanceof Dolphin) {
                dolphins++;
            } else if (aquaticAnimals[i] instanceof Penguin) {
                penguins++;
            }
        }

        System.out.println("Nombre de dauphins : " + dolphins);
        System.out.println("Nombre de pingouins : " + penguins);
    }
    @Override
    public String toString() {
        return "Zoo{" +
                "name='" + name + '\'' +
                ", city='" + city + '\'' +
                ", nbrCages=" + NBR_CAGES +
                ", nombreAnimaux=" + numberOfAnimals +
                '}';
    }
}
