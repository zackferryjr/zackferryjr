package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Zack Ferry
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            CommandArgs options =
                new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                            + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Open the necessary files for non-option arguments ARGS (see comment
      *  on main). */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        if (!_input.hasNext() || !_config.hasNext()) {
            throw new EnigmaException("No input or config");
        }

        Machine m = readConfig();
        String s = _input.nextLine();
        if (!s.contains("*")) {
            throw new EnigmaException("Not a setting");
        }
        setUp(m, s);

        while (_input.hasNext()) {
            String l = _input.nextLine().strip();
            if (l == "") {
                _output.println();
            } else if (l.charAt(0) == '*') {
                setUp(m, l);
            } else {
                printMessageLine(m.convert(l.replaceAll(" ", "")));
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            _alphabet = new Alphabet(_config.nextLine());
            if (_alphabet.contains('(') || _alphabet.contains(')')
                    || _alphabet.contains('*')) {
                throw new EnigmaException("Not actually an alphabet");
            }
            if (!_config.hasNextInt()) {
                throw new EnigmaException("no number");
            }
            int numRotors = _config.nextInt();
            if (!_config.hasNextInt()) {
                throw new EnigmaException("no number");
            }
            int pawls = _config.nextInt();
            if (pawls > numRotors || numRotors < 0) {
                throw new EnigmaException("bad pawls and numrotors");
            }
            ArrayList<Rotor> allRotors = new ArrayList<Rotor>();
            while (_config.hasNextLine()) {
                Rotor r = readRotor();
                allRotors.add(r);
            }
            return new Machine(_alphabet, numRotors, pawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String s = _config.nextLine();
            while (s.equals("") || s.matches("\\s*")
                    || _config.hasNext("\\(+.*")) {
                s += _config.nextLine();
            }

            String [] specs = s.trim().split("\\s");


            String name = specs[0];
            String type = specs[1];
            String perm = "";

            for (int i = 2; i < specs.length; i++) {
                perm += specs[i];
            }

            Permutation daPerm = new Permutation(perm, _alphabet);

            if (type.charAt(0) == 'M') {
                return new MovingRotor(name, daPerm, type.substring(1).trim());
            } else if (type.charAt(0) == 'N') {
                return new FixedRotor(name, daPerm);
            } else if (type.charAt(0) == 'R') {
                return new Reflector(name, daPerm);
            } else {
                throw new EnigmaException("Unknown type");
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        if (settings == "") {
            throw new EnigmaException("no settings :(");
        }
        String [] rotorSettings = settings.trim().split("\\s");
        String[] rotors = new String[M.numRotors()];

        for (int i = 0; i < M.numRotors(); i++) {
            String roddy = rotorSettings[i + 1];
            rotors[i] = roddy;
        }
        M.insertRotors(rotors);

        String set = rotorSettings[M.numRotors() + 1];
        M.setRotors(set);
        String permSettings = "";
        for (int f = rotorSettings.length - 1; f < M.numRotors() + 1; f++) {
            permSettings += rotorSettings[f] + " ";
        }
        Permutation p = new Permutation(permSettings, _alphabet);
        M.setPlugboard(p);
    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        while (msg != "") {
            if (msg.length() > 5) {
                _output.print(msg.substring(0, 5) + " ");
                msg = msg.substring(5);
            } else {
                _output.print(msg);
                msg = "";
            }
        }
        _output.print("\n");
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** True if --verbose specified. */
    private static boolean _verbose;
}
