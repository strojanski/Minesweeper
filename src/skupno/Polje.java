
package skupno;

/**
 * Objekt tega razreda predstavlja polje igralne povr"sine.
 */
public class Polje {

    /** indeks vrstice (element intervala [0, h - 1] */
    private int vrstica;

    /** indeks stolpca (element intervala [0, w - 1] */
    private int stolpec;

    /**
     * Inicializira objekt, ki predstavlja polje s podanim indeksom vrstice
     * in stolpca.
     */
    public Polje(int vrstica, int stolpec) {
        this.vrstica = vrstica;
        this.stolpec = stolpec;
    }

    /** Vrne indeks vrstice polja this. */
    public int vr() {
        return this.vrstica;
    }

    /** Vrne indeks stolpca polja this. */
    public int st() {
        return this.stolpec;
    }

    /** Vrne predstavitev polja v obliki niza (vrstica, stolpec). */
    @Override
    public String toString() {
        return String.format("(%d, %d)", this.vrstica, this.stolpec);
    }

    /** Vrne (v praksi povratno enoli"cno) zgo"s"cevalno kodo polja this. */
    @Override
    public int hashCode() {
        return 113 * this.vrstica + 127 * this.stolpec;
    }

    /**
     * Vrne true, "ce kazalca `this' in `drugi' ka"zeta na objekta tipa Polje
     * z enakima indeksoma vrstice in stolpca.
     */
    @Override
    public boolean equals(Object drugo) {
        if (this == drugo) {
            return true;
        }
        if (!(drugo instanceof Polje)) {
            return false;
        }
        Polje p = (Polje) drugo;
        return (this.vrstica == p.vrstica && this.stolpec == p.stolpec);
    }
}
