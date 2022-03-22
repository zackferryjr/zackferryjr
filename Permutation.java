package enigma;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Zack Ferry
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = cycles;
        for (int i = 0; i < _alphabet.size(); i++) {
            if (!cycles.contains(_alphabet.toChar(i) + "")) {
                addCycle(_alphabet.toChar(i) + "");
            }
        }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        if (_cycles == "") {
            _cycles += "(" + cycle + ")";
        } else {
            _cycles += " (" + cycle + ")";
        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char c = _alphabet.toChar(wrap(p));
        if (_cycles.charAt(_cycles.indexOf(c) + 1) == ')') {
            int i = _cycles.indexOf(c);
            while (_cycles.charAt(i) != '(') {
                i--;
            }
            i++;
            char r = _cycles.charAt(i);
            return _alphabet.toInt(r);
        } else {
            char r = _cycles.charAt(_cycles.indexOf(c) + 1);
            return _alphabet.toInt(r);
        }
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char p = _alphabet.toChar(wrap(c));
        if (_cycles.charAt(_cycles.indexOf(p) - 1) == '(') {
            int i = _cycles.indexOf(p);
            while (_cycles.charAt(i) != ')') {
                i++;
            }
            i--;
            char r = _cycles.charAt(i);
            return _alphabet.toInt(r);
        } else {
            char r = _cycles.charAt(_cycles.indexOf(p) - 1);
            return _alphabet.toInt(r);
        }
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        return alphabet().toChar(permute(_alphabet.toInt(p)));
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        return alphabet().toChar(invert(_alphabet.toInt(c)));
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {

        for (int i = 0; i < _alphabet.size(); i++) {
            char t = _alphabet.toChar(i);
            int index = _cycles.indexOf(t);
            if (_cycles.charAt(index + 1) != ')'
                    && _cycles.charAt(index - 1) != '(') {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private final Alphabet _alphabet;
    /** cycles of this permutation. */
    private String _cycles;
}
