package prosit2;

import java.util.Scanner;

public class ZooManagement {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Initialisation du zoo
        Zoo myZoo = initializeZoo();

        boolean running = true;
        while (running) {
            displayMenu();
            int choice = getIntInput("Choisissez une option : ");

            switch (choice) {
                case 1:
                    addNewAnimal(myZoo);
                    break;
                case 2:
                    removeExistingAnimal(myZoo);
                    break;
                case 3:
                    searchForAnimal(myZoo);
                    break;
                case 4:
                    myZoo.displayAnimals();
                    break;
                case 5:
                    myZoo.displayZoo();
                    break;
                case 6:
                    running = false;
                    break;
                default:
                    System.out.println("Option invalide. Veuillez réessayer.");
            }
        }

        scanner.close();
        System.out.println("Programme terminé. Au revoir!");
    }

    private static Zoo initializeZoo() {
        System.out.println("Initialisation du Zoo");
        System.out.print("Entrez le nom du zoo : ");
        String name = scanner.nextLine();

        System.out.print("Entrez la ville du zoo : ");
        String city = scanner.nextLine();

        int nbrCages = getIntInput("Entrez le nombre de cages : ");

        return new Zoo(name, city, nbrCages);
    }

    private static void displayMenu() {
        System.out.println("\n=== Menu de Gestion du Zoo ===");
        System.out.println("1. Ajouter un animal");
        System.out.println("2. Supprimer un animal");
        System.out.println("3. Chercher un animal");
        System.out.println("4. Afficher tous les animaux");
        System.out.println("5. Afficher les informations du zoo");
        System.out.println("6. Quitter");
    }

    private static void addNewAnimal(Zoo zoo) {
        System.out.println("\nAjout d'un nouvel animal");

        System.out.print("Entrez la famille de l'animal : ");
        String family = scanner.nextLine();

        System.out.print("Entrez le nom de l'animal : ");
        String name = scanner.nextLine();

        int age = getIntInput("Entrez l'âge de l'animal : ");

        System.out.print("L'animal est-il un mammifère ? (oui/non) : ");
        boolean isMammal = scanner.nextLine().toLowerCase().startsWith("o");

        Animal newAnimal = new Animal(family, name, age, isMammal);
        if (zoo.addAnimal(newAnimal)) {
            System.out.println("Animal ajouté avec succès!");
        } else {
            System.out.println("Impossible d'ajouter l'animal. Le zoo est plein.");
        }
    }

    private static void removeExistingAnimal(Zoo zoo) {
        System.out.print("\nEntrez le nom de l'animal à supprimer : ");
        String name = scanner.nextLine();

        if (zoo.removeAnimal(name)) {
            System.out.println("Animal supprimé avec succès!");
        } else {
            System.out.println("Animal non trouvé.");
        }
    }

    private static void searchForAnimal(Zoo zoo) {
        System.out.print("\nEntrez le nom de l'animal à chercher : ");
        String name = scanner.nextLine();

        int index = zoo.searchAnimal(name);
        if (index != -1) {
            System.out.println("Animal trouvé à l'index : " + index);
        } else {
            System.out.println("Animal non trouvé.");
        }
    }

    private static int getIntInput(String message) {
        int value = 0;
        boolean validInput = false;

        do {
            try {
                System.out.print(message);
                value = Integer.parseInt(scanner.nextLine());
                if (value > 0) {
                    validInput = true;
                } else {
                    System.out.println("Veuillez entrer un nombre positif.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un nombre valide.");
            }
        } while (!validInput);

        return value;
    }
}
