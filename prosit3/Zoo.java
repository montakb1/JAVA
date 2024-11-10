package prosit3;

public class Zoo {
    private Animal[] animals;
    private String name;
    private String city;
    private final int NBR_CAGES;  // Constante selon l'instruction 14
    private static final int MAX_ANIMALS = 25;
    private int numberOfAnimals;

    public Zoo(String name, String city) {
        this.name = name;
        this.city = city;
        this.NBR_CAGES = MAX_ANIMALS;  // Nombre de cages égal au maximum d'animaux
        this.animals = new Animal[MAX_ANIMALS];
        this.numberOfAnimals = 0;
    }

    // Instruction 10: Méthode pour ajouter un animal
    public boolean addAnimal(Animal animal) {
        if (animal == null) return false;
        if (isZooFull()) return false;
        if (searchAnimal(animal) != -1) return false;  // Animal déjà présent

        animals[numberOfAnimals] = animal;
        numberOfAnimals++;
        return true;
    }

    // Instruction 11: Méthode pour afficher les animaux
    public void displayAnimals() {
        System.out.println("\nAnimaux dans " + name + ":");
        for (int i = 0; i < numberOfAnimals; i++) {
            System.out.println((i + 1) + ". " + animals[i]);
        }
    }

    // Instruction 11: Méthode de recherche d'animal
    public int searchAnimal(Animal animal) {
        if (animal == null) return -1;
        for (int i = 0; i < numberOfAnimals; i++) {
            if (animals[i].equals(animal)) {
                return i;
            }
        }
        return -1;
    }

    // Instruction 13: Méthode pour supprimer un animal
    public boolean removeAnimal(Animal animal) {
        int index = searchAnimal(animal);
        if (index == -1) return false;

        // Décalage des animaux
        for (int i = index; i < numberOfAnimals - 1; i++) {
            animals[i] = animals[i + 1];
        }
        animals[numberOfAnimals - 1] = null;
        numberOfAnimals--;
        return true;
    }

    // Instruction 15: Vérification si le zoo est plein
    public boolean isZooFull() {
        return numberOfAnimals >= NBR_CAGES;
    }

    // Instruction 16: Comparaison de deux zoos
    public static Zoo comparerZoo(Zoo z1, Zoo z2) {
        if (z1 == null) return z2;
        if (z2 == null) return z1;
        return (z1.numberOfAnimals >= z2.numberOfAnimals) ? z1 : z2;
    }

    public void displayZoo() {
        System.out.println("\nInformations du Zoo:");
        System.out.println("Nom: " + name);
        System.out.println("Ville: " + city);
        System.out.println("Nombre de cages: " + NBR_CAGES);
        System.out.println("Nombre d'animaux actuel: " + numberOfAnimals);
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
