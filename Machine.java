package enigma;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Zack Ferry
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = new ArrayList<Rotor>();
        _usedRotors = new ArrayList<Rotor>();
        Iterator<Rotor> s = allRotors.iterator();
        while (s.hasNext()) {
            _allRotors.add(s.next());
        }
        _plugboard = new Permutation("", _alphabet);
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        return _usedRotors.get(k);
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        _usedRotors.clear();
        for (int i = 0; i < rotors.length; i++) {
            for (int r = 0; r < _allRotors.size(); r++) {
                if (rotors[i].trim().equals(_allRotors.get(r).name())) {
                    _usedRotors.add(_allRotors.get(r));
                }
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        for (int i = 1; i <= setting.length(); i++) {
            char c  = setting.charAt(i - 1);
            getRotor(i).set(c);
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugboard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    private void advanceRotors() {
        boolean[] check = new boolean[_usedRotors.size()];
        for (int i = 0; i < _usedRotors.size(); i++) {
            if (getRotor(i).atNotch()) {
                check[i] = true;
                check[i - 1] = true;
            }
        }
        check[_usedRotors.size() - 1] = true;
        for (int a = 0; a < _usedRotors.size(); a++) {
            if (check[a]) {
                getRotor(a).advance();
            }
        }
    }

    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        for (int i = _usedRotors.size() - 1;  i > 0; i--) {
            c = getRotor(i).convertForward(c);
        }
        c = getRotor(0).convertForward(c);
        for (int i = 1; i < _usedRotors.size(); i++) {
            c = getRotor(i).convertBackward(c);
        }
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String s = "";
        for (int i = 0; i < msg.length(); i++) {
            int c = _alphabet.toInt(msg.charAt(i));
            if (c == -1) {
                System.out.print("1" + msg.charAt(i) + "1");
                throw new EnigmaException("Not in alphabet");
            }
            c = convert(c);
            s += _alphabet.toChar(c);
        }
        return s;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;
    /** Number of rotors. */
    private final int _numRotors;
    /** Number of pawls. */
    private final int _pawls;
    /** Full list of rotors. */
    private ArrayList<Rotor> _allRotors;
    /** Used rotors. */
    private ArrayList<Rotor> _usedRotors;
    /** Plugboard needed. */
    private Permutation _plugboard;
}
