
package ogrodje;

import skupno.*;
import java.util.*;

/**
 * Objekt tega razreda hrani razporeditev min po igralni povr"sini.
 */
public class Razpored {

    // lastnosti plo"s"c pri parametrih 1, 2, 3 in 4
    private static int[] VISINA = {9, 16, 16, 24};
    private static int[] SIRINA = {9, 16, 30, 30};
    private static int[] ST_MIN = {10, 40, 99, 180};

    // omejitve glede velikosti plo"s"ce in "stevila min
    private static int MIN_HW = 4;
    private static int MAX_HW = 100;
    private static int MIN_M = 1;
    private static int MIN_LUFT = 10;   // minimum vrednosti h * w - m

    /** Izjema tega tipa se spro"zi v primeru napake pri branju datoteke z
     * razporedom. */
    public static class IzjemaPriBranju extends RuntimeException {
        public IzjemaPriBranju(String sporocilo) {
            super(sporocilo);
        }
    }

    public static String OMEJITVE_IZPIS =
        String.format("Omejitve:%n" +
                "   %d <= širina <= %d%n" +
                "   %d <= višina <= %d%n" +
                "   %d <= število_min <= širina * višina - %d",
                MIN_HW, MAX_HW, MIN_HW, MAX_HW,
                MIN_M, MIN_LUFT);

    /** vi"sina plo"s"ce */
    private int visina;

    /** "sirina plo"s"ce */
    private int sirina;

    /** "stevilo min na plo"s"ci */
    private int stMin;

    /** [i][j] == true natanko tedaj, ko polje (i, j) vsebuje mino */
    private boolean[][] mine;

    /** Objekt inicializra s podatki o te"zavnosti (1: 9x9_10; 2: 16x16_40; 3:
     * 16x30_99; 4: 24x30_180). */
    public Razpored(int tezavnost) {
        this(VISINA[tezavnost], SIRINA[tezavnost], ST_MIN[tezavnost]);
    }

    /** Inicializira razporeditev za podano vi"sino in "sirino plo"s"ce ter 
     * 16x16_40; 3: 16x30_99; 4: 24x30_180). */
    public Razpored(int visina, int sirina, int stMin) {
        this(visina, sirina, stMin, new boolean[visina][sirina]);
    }

    private Razpored(int visina, int sirina, int stMin, boolean[][] mine) {
        this.visina = visina;
        this.sirina = sirina;
        this.stMin = stMin;
        this.mine = mine;
    }

    public int vrniVisino() {
        return this.visina;
    }

    public int vrniSirino() {
        return this.sirina;
    }

    public int vrniSteviloMin() {
        return this.stMin;
    }

    /** Vrne kopijo matrike razporeditve min. */
    public boolean[][] kopijaMatrikeMin() {
        boolean[][] m = new boolean[this.visina][this.sirina];
        for (int i = 0;  i < this.visina;  i++) {
            for (int j = 0;  j < this.sirina;  j++) {
                m[i][j] = this.mine[i][j];
            }
        }
        return m;
    }

    /** Vrne "stevilo polj, ki ne vsebujejo mine. */
    public int steviloProstihPolj() {
        return this.visina * this.sirina - this.stMin;
    }

    /** Vrne true natanko tedaj, ko podano polje vsebuje mino.  */
    public boolean vsebujeMino(Polje polje) {
        return this.mine[polje.vr()][polje.st()];
    }

    /** Vrne true natanko tedaj, ko je podano polje veljavno (znotraj plo"s"ce). */
    public boolean poljeVeljavno(Polje polje) {
        int vr = polje.vr();
        int st = polje.st();
        return (vr >= 0 && st >= 0 && vr < this.visina && st < this.sirina);
    }

    /** Vrne skupno "stevilo min na vseh osmih sosedih podanega polja. */
    public int steviloSosednjihMin(Polje polje) {
        int n = 0;
        int vr = polje.vr();
        int st = polje.st();
        for (int i = -1;  i <= 1;  i++) {
            for (int j = -1;  j <= 1;  j++) {
                Polje sosed = new Polje(vr + i, st + j);
                if (this.poljeVeljavno(sosed) && this.vsebujeMino(sosed)) {
                    n++;
                }
            }
        }
        return n;
    }

    /** Vrne seznam veljavnih sosedov podanega polja. */
    public List<Polje> sosedje(Polje polje) {
        List<Polje> rezultat = new ArrayList<>();
        int vr = polje.vr();
        int st = polje.st();
        for (int i = -1;  i <= 1;  i++) {
            for (int j = -1;  j <= 1;  j++) {
                Polje sosed = new Polje(vr + i, st + j);
                if (this.poljeVeljavno(sosed)) {
                    rezultat.add(sosed);
                }
            }
        }
        return rezultat;
    }

    /**
     * S pomo"cjo podanega naklju"cnega generatorja razporedi mine po
     * povr"sini.  Polje (h / 2, w / 2) in njegovo neposredno sose"s"cino
     * pusti prazno.
     */
    public void postaviNakljucno(Random generator) {
        int r = this.visina / 2;
        int c = this.sirina / 2;
        List<Polje> polja = new ArrayList<>();
        for (int i = 0;  i < this.visina;  i++) {
            for (int j = 0;  j < this.sirina;  j++) {
                if (Math.abs(i - r) > 1 || Math.abs(j - c) > 1) {
                    polja.add(new Polje(i, j));
                }
            }
        }
        Collections.shuffle(polja, generator);
        for (Polje polje: polja.subList(0, this.stMin)) {
            this.mine[polje.vr()][polje.st()] = true;
        }
    }

    /** Razpored prebere s pomo"cjo podanega bralnika. */
    public static Razpored preberi(Scanner bralnik) {
        int visina = 0;
        int sirina = 0;
        int stMin = 0;
        List<Polje> poljaZMinami = new ArrayList<>();

        // beri vrstice, sestavljene iz znakov + (mina) in - (prosto polje)
        while (bralnik.hasNextLine()) {
            // izbri"si presledke in tabulatorje
            String vrstica = bralnik.nextLine().replaceAll(" ", "").replaceAll("\t", "");
            int novaSirina = vrstica.length();
            if (novaSirina == 0) {   // presko"ci prazne vrstice
                continue;
            }
            if (sirina == 0) {
                sirina = novaSirina;
            } else if (novaSirina != sirina) {
                throw new IzjemaPriBranju(String.format("vrstica %d ni enako dolga kot vrstica 0", visina));
            }
            for (int i = 0;  i < sirina;  i++) {
                char znak = vrstica.charAt(i);
                if (znak != '+' && znak != '-') {
                    throw new IzjemaPriBranju(String.format("polje (%d, %d): neveljaven znak (%c)", visina, i, znak));
                }
                if (znak == '+') {
                    stMin++;
                    poljaZMinami.add(new Polje(visina, i));
                }
            }
            visina++;
        }

        // preveri ustreznost vi"sine, "sirine in "stevila min
        if (!preveriMere(visina, sirina, stMin)) {
            throw new IzjemaPriBranju(OMEJITVE_IZPIS);
        }

        // izdelaj in napolni tabelo <mine>
        boolean[][] mine = new boolean[visina + 2][sirina + 2];
        for (Polje p: poljaZMinami) {
            mine[p.vr()][p.st()] = true;
        }
        return new Razpored(visina, sirina, stMin, mine);
    }

    /**
     * Vrne true natanko tedaj, ko vi"sina, "sirina in "stevilo min ustrezajo
     * kriterijem.
     */
    public static boolean preveriMere(int visina, int sirina, int stMin) {
        return (sirina >= MIN_HW && sirina <= MAX_HW &&
                visina >= MIN_HW && visina <= MAX_HW &&
                stMin >= MIN_M && stMin <= sirina * visina - MIN_LUFT);
    }

    /** Vrne izpis razporeda v obliki matrike, sestavljene iz znakov + in -. */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int vr = 0;  vr < this.visina;  vr++) {
            for (int st = 0;  st < this.sirina;  st++) {
                sb.append(this.mine[vr][st] ? "+" : "-");
            }
            sb.append(String.format("%n"));
        }
        return sb.toString();
    }
}
