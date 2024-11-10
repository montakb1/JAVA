package prosit1;

import java.util.Scanner;

public class ZooManagement {
    public static void main(String[] args) {
        // Création d'un scanner pour la saisie de l'utilisateur
        Scanner scanner = new Scanner(System.in);

        // Saisie du nom du zoo
        System.out.print("Entrez le nom du zoo : ");
        String zooName = scanner.nextLine();

        // Saisie du nombre de cages, avec validation
        int nbrCages = 0;
        boolean isValid = false;
        while (!isValid) {
            System.out.print("Entrez le nombre de cages : ");
            if (scanner.hasNextInt()) {
                nbrCages = scanner.nextInt();
                if (nbrCages > 0) {
                    isValid = true;
                } else {
                    System.out.println("Veuillez entrer un nombre positif.");
                }
            } else {
                System.out.println("Veuillez entrer un entier.");
                scanner.next(); // Pour ignorer la saisie non valide
            }
        }

        // Fermeture du scanner
        scanner.close();

        // Affichage du message
        System.out.println(zooName + " comporte " + nbrCages + " cages");
    }
}
