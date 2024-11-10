package gestionzoo.main;


import gestionzoo.entities.*;

import java.util.Scanner;

public class ZooManagement {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        Zoo myZoo = initializeZoo();
        Zoo secondZoo = null;

        boolean running = true;
        while (running) {
            try {
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
                        if (secondZoo == null) {
                            secondZoo = initializeZoo();
                        }
                        compareZoos(myZoo, secondZoo);
                        break;
                    case 7:
                        System.out.println(myZoo.isZooFull() ? "Le zoo est plein!" : "Le zoo n'est pas plein.");
                        break;
                    case 8:
                        testAquaticAnimals();
                        break;
                    case 9:
                        running = false;
                        break;
                    default:
                        System.out.println("Option invalide. Veuillez réessayer.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Erreur : " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Une erreur inattendue s'est produite : " + e.getMessage());
            }
        }

        scanner.close();
        System.out.println("Programme terminé. Au revoir!");
    }

    private static Zoo initializeZoo() {
        while (true) {
            try {
                System.out.println("\nInitialisation du Zoo");
                System.out.print("Entrez le nom du zoo : ");
                String name = scanner.nextLine();

                System.out.print("Entrez la ville du zoo : ");
                String city = scanner.nextLine();

                return new Zoo(name, city);
            } catch (IllegalArgumentException e) {
                System.out.println("Erreur : " + e.getMessage());
            }
        }
    }

    private static void displayMenu() {
        System.out.println("\n=== Menu de Gestion du Zoo ===");
        System.out.println("1. Ajouter un animal");
        System.out.println("2. Supprimer un animal");
        System.out.println("3. Chercher un animal");
        System.out.println("4. Afficher tous les animaux");
        System.out.println("5. Afficher les informations du zoo");
        System.out.println("6. Comparer avec un autre zoo");
        System.out.println("7. Vérifier si le zoo est plein");
        System.out.println("8. Tester les animaux aquatiques");
        System.out.println("9. Quitter");
    }

    private static void addNewAnimal(Zoo zoo) {
        try {
            System.out.println("\nAjout d'un nouvel animal");
            System.out.println("1. Animal Terrestre");
            System.out.println("2. Dauphin");
            System.out.println("3. Pingouin");
            int choice = getIntInput("Choisissez le type d'animal : ");

            System.out.print("Entrez la famille de l'animal : ");
            String family = scanner.nextLine();

            System.out.print("Entrez le nom de l'animal : ");
            String name = scanner.nextLine();

            int age = getIntInput("Entrez l'âge de l'animal : ");

            System.out.print("L'animal est-il un mammifère ? (oui/non) : ");
            boolean isMammal = scanner.nextLine().toLowerCase().startsWith("o");

            Animal newAnimal;

            switch (choice) {
                case 1:
                    int nbrLegs = getIntInput("Entrez le nombre de pattes : ");
                    newAnimal = new Terrestrial(family, name, age, isMammal, nbrLegs);
                    break;

                case 2:
                    System.out.print("Entrez l'habitat : ");
                    String dolphinHabitat = scanner.nextLine();
                    float swimmingSpeed = getFloatInput("Entrez la vitesse de nage : ");
                    newAnimal = new Dolphin(family, name, age, isMammal, dolphinHabitat, swimmingSpeed);
                    break;

                case 3:
                    System.out.print("Entrez l'habitat : ");
                    String penguinHabitat = scanner.nextLine();
                    float swimmingDepth = getFloatInput("Entrez la profondeur de nage : ");
                    newAnimal = new Penguin(family, name, age, isMammal, penguinHabitat, swimmingDepth);
                    break;

                default:
                    System.out.println("Type d'animal invalide");
                    return;
            }

            if (zoo.addAnimal(newAnimal)) {
                System.out.println("Animal ajouté avec succès!");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur lors de l'ajout de l'animal : " + e.getMessage());
        }
    }

    private static void removeExistingAnimal(Zoo zoo) {
        System.out.print("\nEntrez le nom de l'animal à supprimer : ");
        String name = scanner.nextLine();
        try {
            Animal animalToRemove = new Animal("Inconnu", name, 0, false);
            if (zoo.removeAnimal(animalToRemove)) {
                System.out.println("Animal supprimé avec succès!");
            } else {
                System.out.println("Animal non trouvé.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private static void searchForAnimal(Zoo zoo) {
        System.out.print("\nEntrez le nom de l'animal à chercher : ");
        String name = scanner.nextLine();
        try {
            Animal animalToFind = new Animal("Inconnu", name, 0, false);
            int index = zoo.searchAnimal(animalToFind);
            if (index != -1) {
                System.out.println("Animal trouvé à l'index : " + index);
            } else {
                System.out.println("Animal non trouvé.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private static void compareZoos(Zoo zoo1, Zoo zoo2) {
        Zoo biggerZoo = Zoo.comparerZoo(zoo1, zoo2);
        System.out.println("\nLe zoo avec le plus d'animaux est : " + biggerZoo);
    }

    private static void testAquaticAnimals() {
        try {
            System.out.println("\n=== Test des animaux aquatiques ===");

            // Création des instances
            Aquatic aquatic = new Aquatic("Poissons", "Nemo", 2, false, "Océan");
            Dolphin dolphin = new Dolphin("Cétacés", "Flipper", 10, true, "Océan Pacifique", 40.0f);
            Penguin penguin = new Penguin("Sphéniscidés", "Rico", 3, true, "Antarctique", 100.0f);

            // Test de la méthode swim()
            System.out.println("\nTest de la méthode swim() :");
            System.out.print("Aquatic : ");
            aquatic.swim();
            System.out.print("Dolphin : ");
            dolphin.swim();
            System.out.print("Penguin : ");
            penguin.swim();

            // Affichage des informations
            System.out.println("\nInformations des animaux aquatiques :");
            System.out.println("Aquatic : " + aquatic);
            System.out.println("Dolphin : " + dolphin);
            System.out.println("Penguin : " + penguin);

        } catch (IllegalArgumentException e) {
            System.out.println("Erreur lors du test des animaux aquatiques : " + e.getMessage());
        }
    }

    private static int getIntInput(String message) {
        int value = 0;
        boolean validInput = false;

        do {
            try {
                System.out.print(message);
                value = Integer.parseInt(scanner.nextLine());
                if (value >= 0) {
                    validInput = true;
                } else {
                    System.out.println("Veuillez entrer un nombre positif ou nul.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un nombre valide.");
            }
        } while (!validInput);

        return value;
    }

    private static float getFloatInput(String message) {
        float value = 0;
        boolean validInput = false;

        do {
            try {
                System.out.print(message);
                value = Float.parseFloat(scanner.nextLine());
                if (value >= 0) {
                    validInput = true;
                } else {
                    System.out.println("Veuillez entrer un nombre positif ou nul.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un nombre valide.");
            }
        } while (!validInput);

        return value;
    }
}
