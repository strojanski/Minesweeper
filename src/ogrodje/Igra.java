
package ogrodje;

import skupno.*;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

/**
 * Vstopna to"cka ogrodja.  Objekt tega razreda predstavlja posamezno igro
 * (partijo).
 */
public class Igra {

    /** privzeta "casovna omejitev (v sekundah) */
    private static final int PRIVZETA_CASOVNA_OMEJITEV = 5;

    /** Izjema tega tipa se spro"zi ob neveljavnih parametrih. */
    private static class ParametriException extends RuntimeException {
        public ParametriException(String sporocilo) {
            super(sporocilo);
        }
    }

    public static void main(String[] args) {
        // dekodiramo parametre in odigramo igro ...
        Igra igra = null;
        try {
            igra = Igra.izParametrov(args);
        } catch (ParametriException ex) {
            System.err.println(ex.getMessage());
            System.err.println();
            izpisiNavodila();
            return;
        } catch (Razpored.IzjemaPriBranju ex) {
            System.err.println("Napaka pri branju razporeda:");
            System.err.println(ex.getMessage());
            return;
        }
        igra.odigraj();
    }

    /** objekt, ki hrani razporeditev min po plo"s"ci */
    private Razpored razpored;

    /** stroj, ki bo igral (null, "ce bomo igrali s "clovekom) */
    private Stroj stroj;

    /** "casovna omejitev v milisekundah */
    private long zacetniCas;

    private Igra(Razpored razpored, Stroj stroj, long zacetniCas) {
        this.razpored = razpored;
        this.stroj = stroj;
        this.zacetniCas = zacetniCas;
    }

    /** Dekodira parametre ter ustvari in vrne objekt, ki predstavlja igro. */
    private static Igra izParametrov(String[] args) {
        if (args.length == 0) {
            throw new ParametriException("Nezadostno število parametrov");
        }

        Razpored razpored = null;
        Stroj stroj = null;
        long seme = 0;                   // seme naklju"cnega generatorja
        boolean fiksenRazpored = false;  // prisotnost parametra -r
        boolean b1234 = false;           // prisotnost parametra 1, 2, 3 ali 4
        boolean bHWM = false;            // prisotnost parametra hxw_m
        int casovnaOmejitev = PRIVZETA_CASOVNA_OMEJITEV;

        int iArg = 0;
        while (iArg < args.length) {
            switch (args[iArg]) {
                case "1":
                case "2":
                case "3":
                case "4":
                    if (fiksenRazpored || b1234 || bHWM) {
                        throw new ParametriException("Parametri 1, 2, 3, 4, hxw_m in -r se med seboj izključujejo");
                    }
                    b1234 = true;
                    razpored = new Razpored(Integer.parseInt(args[iArg]) - 1);
                    break;

                case "-r":
                    if (b1234 || bHWM) {
                        throw new ParametriException("Parametri 1, 2, 3, 4, hxw_m in -r se med seboj izključujejo");
                    }
                    try (Scanner sc = new Scanner(new File(args[++iArg]))) {
                        fiksenRazpored = true;
                        razpored = Razpored.preberi(sc);
                    } catch (FileNotFoundException ex) {
                        throw new ParametriException(String.format("Ne najdem datoteke %s", args[iArg]));
                    } catch (Razpored.IzjemaPriBranju ex) {
                        throw ex;
                    }
                    break;

                case "-s":
                    try {
                        seme = Long.parseLong(args[++iArg]);
                        if (seme <= 0) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException ex) {
                        throw new ParametriException("Seme (vrednost parametra -s) mora biti pozitivno število tipa long");
                    }
                    break;

                case "-t":
                    try {
                        casovnaOmejitev = Integer.parseInt(args[++iArg]);
                        if (casovnaOmejitev <= 1) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException ex) {
                        throw new ParametriException("Časovna omejitev (vrednost parametra -t) mora biti pozitivno število tipa int");
                    }
                    break;

                default:
                    if (Character.isDigit(args[iArg].charAt(0))) {
                        // parameter hxw_m
                        if (fiksenRazpored || b1234) {
                            throw new ParametriException("Parametri 1, 2, 3, 4, hxw_m in -r se med seboj izključujejo");
                        }
                        try {
                            bHWM = true;
                            String[] hwm = args[iArg].split("_");
                            String[] hw = hwm[0].split("x");
                            int visina = Integer.parseInt(hw[0]);
                            int sirina = Integer.parseInt(hw[1]);
                            int stMin = Integer.parseInt(hwm[1]);
                            if (!Razpored.preveriMere(visina, sirina, stMin)) {
                                throw new NumberFormatException();
                            }
                            razpored = new Razpored(visina, sirina, stMin);
                        } catch (NumberFormatException ex) {
                            throw new ParametriException(Razpored.OMEJITVE_IZPIS);
                        }
                    } else {
                        String imeStroja = args[iArg];
                        try {
                            stroj = (Stroj) Class.forName(imeStroja).getDeclaredConstructor().newInstance();
                        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
                            throw new ParametriException(String.format("Ne morem izdelati objekta razreda %s", imeStroja));
                        }
                    }
                    break;
            }
            iArg++;
        }

        if (!b1234 && !bHWM && !fiksenRazpored) {
            throw new ParametriException("Manjka parameter za nastavitev razporeda (1 | 2 | 3 | 4 | <h>x<w>_<m> | -r <datoteka>)");
        }

        if (fiksenRazpored && seme > 0) {
            throw new ParametriException("Stikali -r in -s se med seboj izključujeta");
        }

        if (!fiksenRazpored) {
            // naklju"cno razporedi mine po plo"s"ci
            razpored.postaviNakljucno(seme == 0 ? new Random() : new Random(seme));
        }

        return new Igra(razpored, stroj, 1000L * casovnaOmejitev);
    }

    /** Izpi"se navodila za zagon programa. */
    private static void izpisiNavodila() {
        System.err.println("java ogrodje.Igra");
        System.err.println("    (((1 | 2 | 3 | 4 | <h>x<w>_<m>) [-s <seme>]) | -r <datoteka>)");
        System.err.println("    [<Stroj>]");
        System.err.println("    [-t <čas_v_sekundah>]");
    }

    /** Odigra partijo s "clovekom ali strojem. */
    private void odigraj() {
        if (this.stroj == null) {
            this.odigrajSClovekom();
        } else {
            this.odigrajSStrojem();
        }
    }

    /** Odigra partijo s "clovekom. */
    private void odigrajSClovekom() {
        Stanje stanje = new Stanje(this.razpored);
        System.out.println(stanje);
        Scanner sc = new Scanner(System.in);
        int visina = this.razpored.vrniVisino();
        int sirina = this.razpored.vrniSirino();

        // dokler je "se kak"sno prazno polje odprto ...
        while (!stanje.vseOdprto()) {
            int vrstica = -1;
            int stolpec = -1;
            Polje polje = null;

            // dokler uporabnik ne vnese koordinat veljavnega in zaprtega
            // polja ...
            while (vrstica < 0 || stolpec < 0) {
                do {
                    System.out.printf("Vnesite indeks vrstice (0-%d): ", visina - 1);
                    try {
                        vrstica = sc.nextInt();
                    } catch (NumberFormatException ex) {
                        vrstica = -1;
                    }
                } while (vrstica < 0 || vrstica >= visina);

                do {
                    System.out.printf("Vnesite indeks stolpca (0-%d): ", sirina - 1);
                    try {
                        stolpec = sc.nextInt();
                    } catch (NumberFormatException ex) {
                        stolpec = -1;
                    }
                } while (stolpec < 0 || stolpec >= sirina);

                polje = new Polje(vrstica, stolpec);

                if (stanje.vrniStatus(polje) >= 0) {
                    System.out.printf("Polje %s je že odprto.%n", polje);
                    vrstica = -1;
                }
            }
            System.out.println();

            if (stanje.odpri(polje)) {
                // polje je prazno
                System.out.println(stanje);
            } else {
                System.out.println("Žal ste naleteli na mino ...");
                System.out.println();
                System.out.println(stanje.izpisPoPorazu());
                return;
            }
        }
        System.out.println("+-------------------+");
        System.out.println("| Č E S T I T K E ! |");
        System.out.println("+-------------------+");
    }

    /** Odigra partijo s strojem. */
    private void odigrajSStrojem() {
        Stanje stanje = new Stanje(this.razpored);
        int visina = this.razpored.vrniVisino();
        int sirina = this.razpored.vrniSirino();
        int stMin = this.razpored.vrniSteviloMin();
        boolean[][] mine = this.razpored.kopijaMatrikeMin();

        this.stroj.zacetek(visina, sirina, stMin);

        // trenutno razpolo"zljivi "cas (v milisekundah)
        long preostaliCas = this.zacetniCas;

        // dokler je "se kak"sno prazno polje odprto ...
        while (!stanje.vseOdprto()) {
            int vrstica = -1;
            int stolpec = -1;

            // naro"ci stroju, naj izbere potezo
            long t = System.currentTimeMillis();
            Polje polje = this.stroj.izberi(stanje.kopijaStatusa(), preostaliCas);
            preostaliCas -= System.currentTimeMillis() - t;

            if (preostaliCas < 0) {
                this.stroj.konecIgre(mine, Konstante.PREKORACITEV_CASA, stanje.vrniSteviloOdprtih());
                return;
            }
            if (!this.razpored.poljeVeljavno(polje)) {
                this.stroj.konecIgre(mine, Konstante.NEVELJAVNO_POLJE, stanje.vrniSteviloOdprtih());
                return;
            }
            if (stanje.vrniStatus(polje) >= 0) {
                this.stroj.konecIgre(mine, Konstante.ZE_ODPRTO_POLJE, stanje.vrniSteviloOdprtih());
                return;
            }
            if (!stanje.odpri(polje)) {
                this.stroj.konecIgre(mine, Konstante.MINA, stanje.vrniSteviloOdprtih());
                return;
            }
        }

        // stroj je odprl vsa prazna polja
        this.stroj.konecIgre(mine, Konstante.ZMAGA, stanje.vrniSteviloOdprtih());
    }
}
