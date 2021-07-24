
package s12345678;

import java.util.*;
import java.time.LocalTime;
import skupno.*;

/**
 * Stroj, ki poteze izbira naklju"cno (razen v prvi potezi, ko izbere polje (w /
 * 2, h / 2)).
 *
 * Izpisi, ki jih stroj ustvarja, so namenjeni zgolj la"zjemu spremljanju poteka
 * komunikacije med ogrodjem in strojem. V svojem stroju jih lahko mirno
 * izbri"sete ...
 */
public class Stroj_Nakljucko implements Stroj {

    /** vi"sina plo"s"ce */
    private int visina;

    /** "sirina plo"s"ce */
    private int sirina;

    /** naklju"cni generator, ki ga bom uporabljal za izbiro potez */
    private Random generator;

    /** "stevec potez (zaporedna "stevilka naslednje poteze) */
    private int poteza;

    /**
     * Na za"cetku partije med drugim inicializiram naklju"cni generator.
     */
    @Override
    public void zacetek(int visina, int sirina, int stMin) {
        System.out.printf("<%s> zacetek(%d, %d, %d)%n%n", LocalTime.now(), visina, sirina, stMin);
        this.visina = visina;
        this.sirina = sirina;
        this.generator = new Random(12345678);
        this.poteza = 1;
    }

    /**
     * V prvi potezi vrnem polje (h/2, w/2), v ostalih pa naklju"cno izbrano prosto
     * polje.
     */
    @Override
    public Polje izberi(int[][] stanje, long preostaliCas) {
        System.out.printf("<%s> [%d] izberi(stanje, %d)%n", LocalTime.now(), this.poteza, preostaliCas);
        this.izpisiStanje(stanje);

        Polje izbranoPolje = null;
        if (this.poteza == 1) {
            izbranoPolje = new Polje(this.visina / 2, this.sirina / 2);
        } else {
            List<Polje> prostaPolja = new ArrayList<>();
            for (int i = 0; i < stanje.length; i++) { // vrstice
                for (int j = 0; j < stanje[i].length; j++) { // stolpci
                    if (stanje[i][j] < 0) {
                        prostaPolja.add(new Polje(i, j));
                    }
                }
            }
            izbranoPolje = prostaPolja.get(this.generator.nextInt(prostaPolja.size()));
        }
        this.poteza++;

        System.out.println("Moja izbira: " + izbranoPolje);
        System.out.println();
        return izbranoPolje;
    }

    /**
     * Ta metoda bi lahko imela tudi prazno telo (ne smemo pa je izpustiti!).
     */
    @Override
    public void konecIgre(boolean[][] mine, int razlog, int stOdprtih) {
        System.out.printf("<%s> konecIgre(mine, %s, %d)%n", LocalTime.now(), Konstante.OPIS[razlog], stOdprtih);
        izpisiMine(mine);
    }

    private void izpisiStanje(int[][] stanje) {
        for (int i = 0; i < this.visina; i++) {
            for (int j = 0; j < this.sirina; j++) {
                System.out.print((stanje[i][j] < 0) ? ("?") : (stanje[i][j]));
            }
            System.out.println();
        }
    }

    private void izpisiMine(boolean[][] mine) {
        for (int i = 0; i < this.visina; i++) {
            for (int j = 0; j < this.sirina; j++) {
                System.out.print(mine[i][j] ? "+" : "-");
            }
            System.out.println();
        }
    }
}
