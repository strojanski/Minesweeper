
package skupno;

/**
 * Ta razred vsebuje konstante, ki podajajo kode ob zaklju"cku partije.
 */
public class Konstante {
    /** igralec je odprl vsa prazna polja */
    public static final int ZMAGA = 0;

    /** igralec je odprl polje z mino */
    public static final int MINA = 1;

    /** igralec je izbral neveljavno polje */
    public static final int NEVELJAVNO_POLJE = 2;

    /** igralec je izbral "ze odprto polje */
    public static final int ZE_ODPRTO_POLJE = 3;

    /** igralec je prekora"cil razpolo"zljivi "cas */
    public static final int PREKORACITEV_CASA = 4;

    /** OPIS[i] = besedilni opis kode i */
    public static final String[] OPIS = {
        "ZMAGA", "MINA", "NEVELJAVNO_POLJE", "ZE_ODPRTO_POLJE", "PREKORACITEV_CASA"
    };
}
