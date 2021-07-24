
package ogrodje;

import skupno.*;
import java.util.*;

/**
 * Objekt tega razreda hrani trenutno stanje pri iskanju min.
 */
public class Stanje {

    /** objekt, ki hrani razporeditev min */
    private Razpored razpored;

    /* ~[i][j] == status polja (i, j):
          -1: polje je "se zaprto
          0--8: polje je odprto in vsebuje to "stevilko
    */
    private int[][] status;

    /** "stevilo "ze odprtih polj */
    private int stOdprtihPolj;

    public Stanje(Razpored razpored) {
        this.razpored = razpored;
        int visina = this.razpored.vrniVisino();
        int sirina = this.razpored.vrniSirino();
        this.status = new int[visina][sirina];
        for (int i = 0;  i < visina;  i++) {
            Arrays.fill(this.status[i], -1);
        }
        this.stOdprtihPolj = 0;
    }

    /** Vrne status podanega polja (-1: zaprto; 0--8: odprto in vsebuje to
     * "stevilo). */
    public int vrniStatus(Polje polje) {
        return this.status[polje.vr()][polje.st()];
    }

    /** Vrne kopijo matrike, v kateri element [i][j] podaja status polja
     * (i, j). */
    public int[][] kopijaStatusa() {
        int[][] kopija = new int[this.status.length][];
        for (int i = 0;  i < this.status.length;  i++) {
            kopija[i] = Arrays.copyOf(this.status[i], this.status[i].length);
        }
        return kopija;
    }

    /**
     * Nastavi status podanega polja. 
     * @param status -1 ali 0--8
     */
    private void nastaviStatus(Polje polje, int status) {
        this.status[polje.vr()][polje.st()] = status;
    }

    /** Vrne true natanko v primeru, "ce podano polje vsebuje mino. */
    public boolean vsebujeMino(Polje polje) {
        return this.razpored.vsebujeMino(polje);
    }

    /** Vrne "stevilo odprtih polj. */
    public int vrniSteviloOdprtih() {
        return this.stOdprtihPolj;
    }

    /**
     * Odpre podano polje.  "Ce se v polju prika"ze ni"cla (tj. nobeden od
     * sosedov ne vsebuje mine), potem rekurzivno odpre vsa sosednja polja.
     * @return true, "ce je polje prazno; false, "ce vsebuje mino
     */
    public boolean odpri(Polje polje) {
        if (this.vsebujeMino(polje)) {
            return false;
        }
        int stMinVOkolici = razpored.steviloSosednjihMin(polje);
        this.nastaviStatus(polje, stMinVOkolici);
        this.stOdprtihPolj++;

        if (stMinVOkolici == 0) {
            for (Polje sosed: razpored.sosedje(polje)) {
                if (this.vrniStatus(sosed) < 0) {
                    this.odpri(sosed);
                }
            }
        }
        return true;
    }

    /**
     * Vrne true natanko v primeru, "ce so odprta vsa polja razen tistih, ki
     * vsebujejo mine.
     */
    public boolean vseOdprto() {
        return (this.stOdprtihPolj == this.razpored.steviloProstihPolj());
    }

    /** Vrne izpis statusne matrike (vrednosti -1 nadomesti z znakom ?). */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int[] vrstica: this.status) {
            for (int element: vrstica) {
                sb.append(element < 0 ? "?" : Integer.toString(element));
            }
            sb.append(String.format("%n"));
        }
        return sb.toString();
    }

    /** 
     * Vrne izpis v primeru odkritja polja z mino.  Mine se prika"zejo z
     * znakom M.
     */
    public String izpisPoPorazu() {
        StringBuilder sb = new StringBuilder();
        int visina = this.razpored.vrniVisino();
        int sirina = this.razpored.vrniSirino();

        for (int i = 0;  i < visina;  i++) {
            for (int j = 0;  j < sirina;  j++) {
                int element = this.status[i][j];
                sb.append(element < 0 ? 
                        (this.razpored.vsebujeMino(new Polje(i, j)) ? "M" : "-") :
                        Integer.toString(element));
            }
            sb.append(String.format("%n"));
        }
        return sb.toString();
    }
}
