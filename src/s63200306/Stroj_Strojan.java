package s63200306;

//za igralno polje 1 in 2 v > 90% primerov odpre > 90% polj

import skupno.*;
import java.util.*;

import java.time.LocalTime;

public class Stroj_Strojan implements Stroj {

    private int visina;
    private int sirina;

    private int stNakljucnoIzbranih;

    private Random generator;
    /** stevec potez */
    private int poteza;

    /** polja, za katera vemo da so varna in jih bomo prednostno odprli */
    private static ArrayList<Polje> poljeJeVarnoOdpreti;
    private static ArrayList<Polje> poljeJeVarnoOdpreticopy;

    /** polja, za katera vemo, da vsebujejo mino, ter jih ne želimo izbrati */
    private static ArrayList<Polje> naPoljuJeMina;
    /** polja, ki imajo ob nekem polju vsa enako moznost vsebovanja mina */
    private static ArrayList<ArrayList<Polje>> poljaMetKovanca;

    @Override
    public void zacetek(int visina, int sirina, int stMin) {
        // System.out.printf("<%s> zacetek(%d, %d, %d)%n%n", LocalTime.now(), visina,
        // sirina, stMin);
        this.visina = visina;
        this.sirina = sirina;
        this.poteza = 1;
        this.generator = new Random(1234567);
        naPoljuJeMina = new ArrayList<>();
        poljeJeVarnoOdpreti = new ArrayList<>();
        poljeJeVarnoOdpreticopy = new ArrayList<>();
        poljaMetKovanca = new ArrayList<>();

        stNakljucnoIzbranih = 0;
    }

    @Override
    public Polje izberi(int[][] stanje, long preostaliCas) {
        // System.out.printf("<%s> [%d] izberi(stanje, %d)%n", LocalTime.now(),
        // this.poteza, preostaliCas);

        Polje izbranoPolje = null;
        if (poteza == 1) {
            izbranoPolje = new Polje(this.visina / 2, this.sirina / 2);
        } else {
            ArrayList<Polje> prostaPolja = new ArrayList<Polje>();
            ArrayList<Polje> odprtaPolja = new ArrayList<Polje>();
            for (int i = 0; i < stanje.length; i++) {
                for (int j = 0; j < stanje[i].length; j++) {
                    if (stanje[i][j] < 0) // če polje še ni odprto ima vrednost -1
                        prostaPolja.add(new Polje(i, j));
                    if (stanje[i][j] > 0) {
                        odprtaPolja.add(new Polje(i, j));
                    } // polj z vrednostjo 0 ne stejemo zraven
                }
            }
            ArrayList<Polje> zaprtaPoljaObOdprtih = new ArrayList<Polje>();
            for (Polje p : prostaPolja) {
                if (steviloOdprtihSosedov(p, stanje) > 0) {
                    zaprtaPoljaObOdprtih.add(p);
                }
            }
            // zaprta polja ob odprtih, ki niso vsebovana v seznamu naPoljuJeMina
            ArrayList<Polje> zaprtaNeoznacenaPolja = new ArrayList<Polje>();
            for (Polje p : zaprtaPoljaObOdprtih) {
                if (naPoljuJeMina.contains(p) == false) {
                    zaprtaNeoznacenaPolja.add(p);
                }
            }

            // tukaj pišeš, katero polje želiš odpreti
            for (Polje p : odprtaPolja) {
                naPoljuJeMina = oznacevalecMin(p, stanje, prostaPolja, zaprtaPoljaObOdprtih, odprtaPolja);
            }
            ArrayList<Polje> preverba = new ArrayList<>();
            preverba.addAll(poljeJeVarnoOdpreti);
            for (Polje p : preverba) {
                if (naPoljuJeMina.contains(p))
                    poljeJeVarnoOdpreti.remove(p);
            }
            //System.out.println();
            //izpisiStanje(stanje);
            poljaMetKovanca = odstraniDvojnike2D(poljaMetKovanca);
            poljeJeVarnoOdpreti = odstraniDvojnike(poljeJeVarnoOdpreti);
            naPoljuJeMina = odstraniDvojnike(naPoljuJeMina);
            izbranoPolje = izbiraPolja(zaprtaPoljaObOdprtih, zaprtaNeoznacenaPolja, stanje, prostaPolja, odprtaPolja);
            poljeJeVarnoOdpreticopy = odstraniDvojnike(poljeJeVarnoOdpreticopy);
        }
        
        this.poteza++;
        return izbranoPolje;
    }

    @Override
    public void konecIgre(boolean[][] mine, int razlog, int stOdprith) {
        System.out.printf("<%s> konecIgre(mine, %s, %d)%n", LocalTime.now(), Konstante.OPIS[razlog], stOdprith);

    }

    // -----------------------------------------------------------------------------------------------------------------
    // pomozne funkcije
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Poišče naključno polje in preveri če je na njem mina, če je, poišče novo
     * naključno polje
     */
    private Polje izbiraPolja(ArrayList<Polje> zaprtaPoljaObOdprtih, ArrayList<Polje> zaprtaNeoznacenaPolja,
            int[][] stanje, ArrayList<Polje> prostaPolja, ArrayList<Polje> odprtaPolja) {
        Polje izbranoPolje;

        // ce nimamo varnih polj vzamemo polje v neposredni blizini odprtega polja,
        // katerega vsota odprtih sosedov je najmanjsa
        if (poljeJeVarnoOdpreti.size() > 0) {
            izbranoPolje = poljeJeVarnoOdpreti.get(0);
            poljeJeVarnoOdpreticopy.addAll(poljeJeVarnoOdpreti);
            if (vrednostPolja(izbranoPolje, stanje) != -1 || naPoljuJeMina.contains(izbranoPolje)) {
                poljeJeVarnoOdpreti.remove(izbranoPolje);
                return izbiraPolja(zaprtaPoljaObOdprtih, zaprtaNeoznacenaPolja, stanje, prostaPolja, odprtaPolja);
            }
            if (poljeJeVarnoOdpreti.contains(izbranoPolje))
                poljeJeVarnoOdpreti.remove(izbranoPolje);
        } else if (naPoljuJeMina.size() == 0) {
            izbranoPolje = prostaPolja.get(0);
            stNakljucnoIzbranih++;
            if (naPoljuJeMina.contains(izbranoPolje) || vrednostPolja(izbranoPolje, stanje) >= 0)
                return izbiraPolja(zaprtaPoljaObOdprtih, zaprtaNeoznacenaPolja, stanje, prostaPolja, odprtaPolja);
        } else {
            izbranoPolje = prostaPolja.get(stNakljucnoIzbranih % prostaPolja.size());
            stNakljucnoIzbranih++;
            if (naPoljuJeMina.contains(izbranoPolje) || vrednostPolja(izbranoPolje, stanje) >= 0)
                return izbiraPolja(zaprtaPoljaObOdprtih, zaprtaNeoznacenaPolja, stanje, prostaPolja, odprtaPolja);
        }
        return izbranoPolje;
    }

    /**
     * na danem odprtem polju pove, kateri zaprti sosedi imajo mine in jih doda k
     * seznamu polj z minami (v metodi izberi to funkcijo klicemo na vsako odprto
     * polje, ki ima vrednost razlicno od 0)
     */
    private ArrayList<Polje> oznacevalecMin(Polje polje, int[][] stanje, ArrayList<Polje> prostaPolja,
            ArrayList<Polje> zaprtaPoljaObOdprtih, ArrayList<Polje> odprtaPolja) {
        ArrayList<Polje> zaprtiSosedi = sosednjaZaprtaPolja(polje, stanje);
        ArrayList<Polje> odprtiSosedi = sosednjaOdprtaPolja(polje, stanje);

        int vrednostPolja = vrednostPolja(polje, stanje);

        if (vrednostPolja(polje, stanje) == zaprtiSosedi.size()) {
            for (Polje sosed : zaprtiSosedi) {
                if (prostaPolja.contains(sosed) && naPoljuJeMina.contains(sosed) == false)
                    naPoljuJeMina.add(sosed);
            }
        }
        if (vrednostPolja - SteviloSosedovZMinami(polje, stanje) == zaprtiSosedi.size()
                - SteviloSosedovZMinami(polje, stanje)) {
            for (Polje sosed : zaprtiSosedi) {
                if (prostaPolja.contains(sosed) && naPoljuJeMina.contains(sosed) == false)
                    naPoljuJeMina.add(sosed);
            }
        }

        if (SteviloSosedovZMinami(polje, stanje) == vrednostPolja) {
            ArrayList<Polje> zaprtiSosediBrezMin = sosednjaZaprtaPolja(polje, stanje);
            for (Polje sosed : zaprtiSosediBrezMin) {
                if (naPoljuJeMina.contains(sosed) == false && prostaPolja.contains(sosed)
                        && poljeJeVarnoOdpreti.contains(sosed) == false && zaprtaPoljaObOdprtih.contains(sosed)) {
                    poljeJeVarnoOdpreti.add(sosed);
                }
            }
        }
    
        odstraniDvojnike(naPoljuJeMina);
        return naPoljuJeMina;
    } 

    /** Ce je polje na igralem polju vrne true */
    private boolean jeVeljavno(Polje polje) {
        int vrstica = polje.vr();
        int stolpec = polje.st();
        if (vrstica < 0 || vrstica > this.visina - 1 || stolpec < 0 || stolpec > this.sirina - 1)
            return false;
        return true;
    }

    /** Ce je polje odprto ali pa izven igralne plosce vrne false */
    private boolean poljeJeVeljavno(Polje polje, ArrayList<Polje> odprtaPolja) {
        int vrstica = polje.vr();
        int stolpec = polje.st();
        if (odprtaPolja.contains(polje) || vrstica < 0 || vrstica > this.visina - 1 || stolpec < 0
                || stolpec > this.sirina - 1)
            return false;
        return true;
    }

    private ArrayList<Polje> deljenaPolja(Polje p1, Polje p2, ArrayList<Polje> zaprtaPoljaObOdprtih, int[][] stanje) {
        // presek sosedov p1 in p2
        ArrayList<Polje> skupna = new ArrayList<>();
        ArrayList<Polje> sosediP1 = sosednjaZaprtaPolja(p1, stanje);
        ArrayList<Polje> sosediP2 = sosednjaZaprtaPolja(p2, stanje);

        for (Polje sosedP1 : sosediP1) {
            if (sosediP2.contains(sosedP1))
                skupna.add(sosedP1);
        }
        for (Polje sosedP2 : sosediP2) {
            if (sosediP1.contains(sosedP2))
                skupna.add(sosedP2);
        }
        return skupna;
    }

    // preveriti moras ce ta obstajajo ko klices te metode
    private Polje zgornje(Polje polje) {
        return new Polje(polje.vr() - 1, polje.st());
    }

    private Polje spodnje(Polje polje) {
        return new Polje(polje.vr() + 1, polje.st());
    }

    private Polje levo(Polje polje) {
        return new Polje(polje.vr(), polje.st() - 1);
    }

    private Polje desno(Polje polje) {
        return new Polje(polje.vr(), polje.st() + 1);
    }

    private ArrayList<Polje> odstraniDvojnike(ArrayList<Polje> polja1) {
        Set<Polje> poljaZMinami = new LinkedHashSet<>();
        poljaZMinami.addAll(polja1);
        polja1.clear();
        polja1.addAll(poljaZMinami);
        return polja1;
    }

    private ArrayList<ArrayList<Polje>> odstraniDvojnike2D(ArrayList<ArrayList<Polje>> polja1) {
        ArrayList<ArrayList<Polje>> k = new ArrayList<>();
        for (ArrayList<Polje> pari : polja1) {
            if (k.contains(pari) == false)
                k.add(pari);
        }
        return k;
    }

    private boolean poljeJeObRobu(Polje polje, int[][] stanje) {
        if (SosednjaPolja(polje, stanje).size() == 8)
            return false;
        return true;
    }

    public int vrednostPolja(Polje polje, int[][] stanje) {
        return stanje[polje.vr()][polje.st()];
    }

    /** Iz seznama naPoljuJeMina presteje stevilo min v okolici nekega polja */
    private int SteviloSosedovZMinami(Polje polje, int[][] stanje) {
        int stSosedovZMino = 0;
        ArrayList<Polje> sosedi = SosednjaPolja(polje, stanje);
        for (Polje p : sosedi) {
            if (naPoljuJeMina.contains(p))
                stSosedovZMino++;
        }
        return stSosedovZMino;
    }

    /**
     * Vrne vsoto vrednosti odprtih polj v blizini danega polja.
     */
    public int vsotaSosedov(Polje polje, int[][] stanje) {
        int vsota = 0;
        ArrayList<Polje> sosedi = sosednjaOdprtaPolja(polje, stanje);
        for (Polje sosed : sosedi) {
            vsota += vrednostPolja(sosed, stanje);
        }
        return vsota;
    }

    private int steviloOdprtihSosedov(Polje polje, int[][] stanje) {
        return sosednjaOdprtaPolja(polje, stanje).size();
    }

    private int steviloZaprtihSosedov(Polje polje, int[][] stanje) {
        return sosednjaZaprtaPolja(polje, stanje).size();
    }

    /**
     * Vrne seznam odprtih polj v okolici danega polja.
     * 
     * @param polje
     * @param stanje
     * @return odprta sosednja polja
     */
    public ArrayList<Polje> sosednjaOdprtaPolja(Polje polje, int[][] stanje) {
        ArrayList<Polje> sosednja = SosednjaPolja(polje, stanje);
        ArrayList<Polje> odprtaSosednja = new ArrayList<>();

        for (Polje p : sosednja) {
            if (vrednostPolja(p, stanje) != -1)
                odprtaSosednja.add(p);
        }
        return odprtaSosednja;
    }

    /**
     * Vrne seznam zaprtih polj v okolici danega polja.
     * 
     * @param polje
     * @param stanje
     * @return
     */
    public ArrayList<Polje> sosednjaZaprtaPolja(Polje polje, int[][] stanje) {
        ArrayList<Polje> sosednja = SosednjaPolja(polje, stanje);
        ArrayList<Polje> zaprtaSosednja = new ArrayList<>();

        for (Polje p : sosednja) {
            if (vrednostPolja(p, stanje) == -1)
                zaprtaSosednja.add(p);
        }
        return zaprtaSosednja;
    }

    /**
     * V arraylist shrani sosednja polja, od levo zgornjega do desno spodnjega.
     * 
     * @param polje
     * @param stanje
     * @return seznam polju sosednjih polj
     */
    private ArrayList<Polje> SosednjaPolja(Polje polje, int[][] stanje) {
        ArrayList<Polje> sosednjaPolja = new ArrayList<Polje>();
        int vrstica = polje.vr();
        int stolpec = polje.st();

        // v seznamu so razvrščeni od zgoraj levega do spodaj desnega (NW -> N -> NE ->
        // W -> E -> SW -> S -> SE)
        while (vrstica >= 0 && stolpec >= 0 && vrstica < visina && stolpec < sirina) {
            if (vrstica > 0 && stolpec > 0)
                sosednjaPolja.add(new Polje(vrstica - 1, stolpec - 1));
            if (vrstica > 0)
                sosednjaPolja.add(new Polje(vrstica - 1, stolpec));
            if (vrstica > 0 && stolpec < sirina - 1)
                sosednjaPolja.add(new Polje(vrstica - 1, stolpec + 1));
            if (stolpec > 0)
                sosednjaPolja.add(new Polje(vrstica, stolpec - 1));
            if (stolpec < sirina - 1)
                sosednjaPolja.add(new Polje(vrstica, stolpec + 1));
            if (vrstica < visina - 1 && stolpec > 0)
                sosednjaPolja.add(new Polje(vrstica + 1, stolpec - 1));
            if (vrstica < visina - 1)
                sosednjaPolja.add(new Polje(vrstica + 1, stolpec));
            if (vrstica < visina - 1 && stolpec < sirina - 1)
                sosednjaPolja.add(new Polje(vrstica + 1, stolpec + 1));
            break;
        }

        for (Polje p : sosednjaPolja) {
            if (p.vr() > stanje.length || p.st() > stanje[0].length || p.st() < 0 || p.vr() < 0)
                sosednjaPolja.remove(p);
        }
        return sosednjaPolja;
    }

    private void izpisiStanje(int[][] stanje) {
        System.out.println(0 + " " + "012345678901234567890123456789");
        for (int i = 0; i < this.visina; i++) {
            System.out.print(i % 10 + " ");
            for (int j = 0; j < this.sirina; j++) {
                System.out.print((stanje[i][j] < 0) ? ("?") : (stanje[i][j]));
            }
            System.out.println();
        }
    }

    private void izpisiMine(boolean[][] mine) {
        for (int i = 0; i < this.visina; i++) {
            System.out.print(i % 10 + " ");
            for (int j = 0; j < this.sirina; j++) {
                System.out.print((mine[i][j]) ? ("+") : ("-"));
            }
            System.out.println();
        }
    }

    private void izpisiMine1(boolean[][] mine) {
        for (int i = 0; i < this.visina; i++) {
            System.out.print(i % 10 + " ");
            for (int j = 0; j < this.sirina; j++) {
                Polje p = new Polje(i, j);
                if (mine[i][j] && naPoljuJeMina.contains(p))
                    System.out.print("P");
                else if (!mine[i][j] && naPoljuJeMina.contains(p))
                    System.out.print("F");
                else if (mine[i][j])
                    System.out.print("+");
                else {
                    System.out.print("-");
                }
            }
            System.out.println();
        }
    }

    private String preverjevalecMin(boolean[][] mine) {
        ArrayList<Polje> laznoOdkriteMine = new ArrayList<>();
        for (int i = 0; i < this.visina; i++) {
            for (int j = 0; j < this.sirina; j++) {
                Polje p = new Polje(i, j);

                if (mine[i][j] == false && naPoljuJeMina.contains(p))
                    laznoOdkriteMine.add(p);
            }
        }
        if (laznoOdkriteMine.size() == 0)
            return ("Ni lazno odkritih min.");
        else
            return Arrays.toString(laznoOdkriteMine.toArray());
    }

    private String preverjevalecVarnihPolj(boolean[][] mine) {
        ArrayList<Polje> laznoVarnaPolja = new ArrayList<>();
        for (int i = 0; i < this.visina; i++) {
            for (int j = 0; j < this.sirina; j++) {
                Polje p = new Polje(i, j);

                if (mine[i][j] == true && poljeJeVarnoOdpreticopy.contains(p))
                    laznoVarnaPolja.add(p);
            }
        }
        if (laznoVarnaPolja.size() == 0)
            return ("Ni lazno odkritih varnih polj.");
        else
            return Arrays.toString(laznoVarnaPolja.toArray());
    }
}