package com.github.frapontillo.pulse.crowd.sentiment.sentiwordnet;

import com.github.frapontillo.pulse.util.StringUtil;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * MultiWordNet implementation based on text files.
 *
 * @author Francesco Pontillo
 * @see <a href="https://github.com/frapontillo/multiwordnet-simple">A simpler MultiWordNet
 * format</a>
 * @see <a href="http://multiwordnet.fbk.eu/english/home.php">MultiWordNet homepage</a>
 */
public class MultiWordNet {
    private final static Pattern mainDivider = Pattern.compile("\t");
    private final static Pattern subDivider = Pattern.compile(" ");

    private HashMap<String, HashMap<String, String[]>> wordNets;

    public MultiWordNet() {
        wordNets = new HashMap<>();
    }

    /**
     * Get a {@link HashMap}&lt;String, String[]&gt; representing a dictionary for a given
     * language.
     * A dictionary is composed of a lemma as the key and an array of synsets as the value.
     *
     * @param language The language to get the dictionary for.
     *
     * @return a {@link HashMap}&lt;String, String[]&gt; as a dictionary.
     */
    public HashMap<String, String[]> getDictionary(String language) {
        HashMap<String, String[]> dict;
        if ((dict = wordNets.get(language)) == null) {
            dict = loadDictionary(language);
            wordNets.put(language, dict);
        }
        return dict;
    }

    /**
     * Get all the synsets for the specified lemma in the given language.
     * If the simple POS tag is specified, the synsets will be filtered to match it (e.g., if the
     * lemma has been tagged as a noun - the "n" tag - only synsets starting with "n#" will be
     * returned).
     *
     * @param lemma     The lemma to retrieve.
     * @param language  The language of the lemma.
     * @param simplePos The (optional) simple POS tag for the lemma ("n", "v", "a", "r").
     *
     * @return a {@link String} array containing all the synsets of the lemma.
     */
    public String[] getSynsets(String lemma, String language, String simplePos) {
        HashMap<String, String[]> dict = getDictionary(language);
        if (dict != null) {
            String[] synsets = dict.get(lemma);
            if (!StringUtil.isNullOrEmpty(simplePos) && synsets != null) {
                List<String> synsetList = Arrays.asList(synsets);
                synsetList = synsetList.stream().filter(s -> s.startsWith(simplePos))
                        .collect(Collectors.toList());
                synsets = synsetList.toArray(new String[synsetList.size()]);
            }
            return synsets;
        }
        return null;
    }

    /**
     * Loads a dictionary for a given language.
     *
     * @param language A {@link String} representing the language.
     *
     * @return a {@link HashMap}&lt;String, String[]&gt; where keys are lemmas and values are synsets.
     */
    private HashMap<String, String[]> loadDictionary(String language) {
        HashMap<String, String[]> dict = new HashMap<>();
        InputStream model = getClass().getClassLoader().getResourceAsStream(language + "_index");
        if (model != null) {
            try {
                List<String> lines = IOUtils.readLines(model, Charset.forName("UTF-8"));
                lines.forEach(s -> {
                    String[] components = mainDivider.split(s);
                    if (components.length == 2) {
                        String lemma = components[0];
                        String[] synsets = subDivider.split(components[1]);
                        dict.put(lemma, synsets);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return dict;
    }
}
