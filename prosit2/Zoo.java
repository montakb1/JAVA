package prosit2;

public class Zoo {
    private Animal[] animals;
    private String name;
    private String city;
    private int nbrCages;
    private static final int MAX_ANIMALS = 25;
    private int numberOfAnimals;

    // Constructeur paramétré
    public Zoo(String name, String city, int nbrCages) {
        this.name = name;
        this.city = city;
        this.nbrCages = nbrCages;
        this.animals = new Animal[MAX_ANIMALS];
        this.numberOfAnimals = 0;
    }

    // Méthode pour ajouter un animal
    public boolean addAnimal(Animal animal) {
        if (numberOfAnimals < MAX_ANIMALS) {
            animals[numberOfAnimals] = animal;
            numberOfAnimals++;
            return true;
        }
        return false;
    }

    // Méthode pour chercher un animal
    public int searchAnimal(String name) {
        for (int i = 0; i < numberOfAnimals; i++) {
            if (animals[i].getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    // Méthode pour supprimer un animal
    public boolean removeAnimal(String name) {
        int index = searchAnimal(name);
        if (index != -1) {
            for (int i = index; i < numberOfAnimals - 1; i++) {
                animals[i] = animals[i + 1];
            }
            animals[numberOfAnimals - 1] = null;
            numberOfAnimals--;
            return true;
        }
        return false;
    }

    // Méthode pour afficher tous les animaux
    public void displayAnimals() {
        System.out.println("\nListe des animaux dans " + name + ":");
        for (int i = 0; i < numberOfAnimals; i++) {
            System.out.println(i + 1 + ". " + animals[i]);
        }
    }

    public void displayZoo() {
        System.out.println("\nInformations du Zoo:");
        System.out.println("Nom: " + name);
        System.out.println("Ville: " + city);
        System.out.println("Nombre de cages: " + nbrCages);
        System.out.println("Nombre d'animaux: " + numberOfAnimals);
    }

    @Override
    public String toString() {
        return "Zoo{" +
                "name='" + name + '\'' +
                ", city='" + city + '\'' +
                ", nbrCages=" + nbrCages +
                ", nombreAnimaux=" + numberOfAnimals +
                '}';
    }
}
