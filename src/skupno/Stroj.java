
package skupno;

/**
 * Objekt tega vmesnika predstavlja strojnega igralca.
 */
public interface Stroj {

    /**
     * Ta metoda se pokli"ce ob za"cetku igre.
     * @param sirina "sirina igralne povr"sine
     * @param visina vi"sina igralne povr"sine
     * @param stMin "stevilo min na igralni povr"sini
     */
    public abstract void zacetek(int sirina, int visina, int stMin);

    /**
     * Ta metoda se pokli"ce, ko mora stroj `this' odpreti polje.
     * @param stanje [i][j] = stanje polja (i, j):
     *    -1 (polje je zaprto);
     *    0..8 (polje vsebuje to "stevilko)
     * @param preostaliCas preostali razpolo"zljivi "cas (v milisekundah)
     * @return izbrano polje
     */
    public abstract Polje izberi(int[][] stanje, long preostaliCas);

    /**
     * Ta metoda se pokli"ce ob koncu igre.
     * @param mine razporeditev min
     *    ([i][j] == true natanko tedaj, ko polje (i, j) vsebuje mino)
     * @param razlog koda zaklju"cka igre (ena od konstant v razredu Konstante)
     * @param stOdprtih "stevilo uspe"sno odprtih polj
     */
    public abstract void konecIgre(boolean[][] mine, int razlog, int stOdprtih);
}
