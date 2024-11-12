package gestionzoo.gestionemployes;

public interface IGestion<T> {
    void ajouterEmploye(T t);
    boolean rechercherEmploye(String nom);
    boolean rechercherEmploye(T t);
    boolean supprimerEmploye(T t);
    void displayEmploye();
    void trierEmployeParId();
    void trierEmployeParNomDepartementEtGrade();
}
